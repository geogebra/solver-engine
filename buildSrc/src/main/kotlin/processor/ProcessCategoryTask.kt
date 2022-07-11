package processor

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.ruleset.standard.StandardRuleSetProvider
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternSet
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import java.io.File

internal const val registerMethodsFunctionName = "registerMethods"

abstract class ProcessCategoriesTask : DefaultTask() {

    @get:Incremental
    @get:InputDirectory
    abstract val categoriesRoot: DirectoryProperty

    @get:OutputDirectory
    abstract val outputRoot: DirectoryProperty

    @TaskAction
    fun generateAll(inputChanges: InputChanges) {
        if (!inputChanges.isIncremental) {
            outputRoot.asFile.get().delete()
        }

        for (change in inputChanges.getFileChanges(categoriesRoot)) {
            if (change.file.path.endsWith(".category.yaml")) {
                val relativePath = change.file.relativeTo(categoriesRoot.asFile.get()).parentFile.path
                val outputDirectory = File(outputRoot.asFile.get(), relativePath)

                when (change.changeType) {
                    ChangeType.REMOVED -> outputDirectory.delete()
                    else -> CategoryProcessor(change.file, outputDirectory).processCategory()
                }
            }
        }

        generateRegistry()
    }

    /**
     * Find all category files in the root folder and create a
     * global register method
     */
    fun generateRegistry() {
        val categories = categoriesRoot.asFileTree.matching(PatternSet().include("**/*.category.yaml"))
        val outFile = File(outputRoot.asFile.get(), "main/kotlin/methods/RegisterAll.kt")

        if (categories.isEmpty) {
            outFile.delete()
            return
        }

        val fileContent = buildString {
            appendLine("package methods\n")
            appendLine("import engine.methods.MethodRegistry\n")
            appendLine("val methodRegistry = run {")
            appendLine("val registry = MethodRegistry()")
            for (categoryFile in categories) {
                val packageName = getPackageName(categoryFile.parentFile)
                appendLine("  $packageName.$registerMethodsFunctionName(registry)")
            }
            appendLine("registry")
            appendLine("}")
        }

        outFile.parentFile.mkdirs()
        writeFormattedString(outFile, fileContent)
    }
}

internal fun writeFormattedString(outFile: File, rawString: String) {
    outFile.printWriter().use { out ->
        out.println(
            KtLint.format(
                KtLint.ExperimentalParams(
                    text = rawString,
                    ruleSets = listOf(StandardRuleSetProvider().get()),
                    cb = { _, _ -> },
                )
            )
        )
    }
}

internal fun getPackageName(dir: File): String {
    return dir.path.substringAfter("kotlin${File.separator}").replace(File.separator, ".")
}
