package processor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File

class CategoryProcessor(val categoryFile: File, val outputDir: File) {

    fun processCategory() {
        val category = CategoryReader.parseCategoryFile(categoryFile)

        outputDir.mkdirs()
        createMethodIdEnum(category)
    }

    fun createMethodIdEnum(category: Category) {
        val className = "${category.metadata.name}MethodId"
        val outFile = File("$outputDir/$className.kt")

        val enumFileContent = buildString {
            appendLine("package ${getPackageName(outputDir)}")
            appendLine("import engine.context.ResourceData")
            appendLine("import engine.methods.ContextSensitiveMethod")
            appendLine("import engine.methods.MethodId")
            appendLine("import engine.methods.MethodRegistry")

            appendLine("enum class $className : MethodId {")
            category.methods.keys.joinTo(buffer = this, separator = ",\n", postfix = ";\n")
            appendLine("override val category = \"${category.metadata.name}\"")
            appendLine("}")

            appendLine("fun $registerMethodsFunctionName(registry: MethodRegistry) {")
            for ((methodId, methodData) in category.methods.entries) {
                appendLine("registry.registerEntry(")
                appendLine("MethodRegistry.EntryData(")
                appendLine("methodId = $className.$methodId,")
                appendLine("isPublic = ${methodData.visibility == "public"},")
                appendLine("description = \"${methodData.description}\",")
                appendLine("implementation = ${methodData.implementationName},")
                appendLine(")")
                appendLine(")")
            }
            appendLine("}")
        }

        writeFormattedString(outFile, enumFileContent)
    }
}
