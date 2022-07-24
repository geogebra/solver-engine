package processor

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.ruleset.standard.StandardRuleSetProvider
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.util.PatternSet
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import java.io.File

abstract class ProcessTranslationsTask : DefaultTask() {

    @get:Incremental
    @get:InputDirectory
    abstract val categoriesRoot: DirectoryProperty

    @get:OutputDirectory
    abstract val outputRoot: DirectoryProperty

    @TaskAction
    fun generateAll(inputChanges: InputChanges) {
        if (inputChanges.getFileChanges(categoriesRoot)
                .any { it.file.path.endsWith(".category.yaml") }) {
            val categories = categoriesRoot.asFileTree.matching(PatternSet().include("**/*.category.yaml"))
            TranslationProcessor(categories.asSequence(), outputRoot.asFile.get()).processTranslations()
        }
    }
}

internal fun writeFormattedJSONstring(outFile: File, jsonString: String) {
    outFile.printWriter().use { out ->
        out.println(
            jsonString
        )
    }
}
