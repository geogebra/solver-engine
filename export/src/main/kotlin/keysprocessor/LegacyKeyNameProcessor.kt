package keysprocessor

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import java.io.OutputStream

/**
 * This scans the code for enum entries annotated with @LegacyKeyName and creates a [KeyNameRegistry]
 * object in the [methods] package which registers all translation keys with a temporary legacy name.
 */
class LegacyKeyNameProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {
    private lateinit var file: OutputStream
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }

        val symbols = resolver.getSymbolsWithAnnotation("engine.steps.metadata.LegacyKeyName").toList()
        val ret = symbols.filter { !it.validate() }.toList()

        val entries = symbols.filter { it.validate() }.map { it.accept(PublicMethodVisitor(), Unit) }
        if (entries.isEmpty()) return ret

        val duplicateLegacyNames = entries.mapIndexedNotNull { index, entry ->
            val firstIndex = entries.indexOfFirst { it.legacyName == entry.legacyName }
            if (firstIndex < index) {
                Pair(entries[firstIndex], entry)
            } else {
                null
            }
        }
        require(duplicateLegacyNames.isEmpty()) {
            val duplicatesString = duplicateLegacyNames.joinToString(separator = "\n") { (first, second) ->
                "For legacy name ${first.legacyName}:\n\t${first.entryName}\n\t${second.entryName}"
            }
            "Duplicated legacy names found:\n$duplicatesString"
        }

        file = codeGenerator.createNewFile(
            Dependencies(true, *symbols.map { it.containingFile!! }.toTypedArray()),
            "methods",
            "KeyNameRegistry",
            "kt",
        )

        val writer = file.writer()
        with(writer) {
            appendLine("package methods\n")
            appendLine("import engine.steps.metadata.MetadataKey\n")

            appendLine("object KeyNameRegistry {")

            appendLine("    private val legacyKeyNames = mapOf<MetadataKey, String>(")
            for (entry in entries) {
                appendLine("        ${entry.entryName} to \"${entry.legacyName}\",")
            }
            appendLine("    )\n")

            appendLine("    fun getKeyName(key: MetadataKey): String {")
            appendLine("        return legacyKeyNames[key] ?: key.keyName")
            appendLine("    }")
            appendLine("}")
        }
        writer.close()

        invoked = true
        return ret
    }

    private inner class PublicMethodVisitor : KSDefaultVisitor<Unit, Entry>() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): Entry {
            val parentDeclaration = classDeclaration.parentDeclaration
            val qualifiedName = classDeclaration.qualifiedName
            if (qualifiedName == null || parentDeclaration == null || parentDeclaration.qualifiedName == null) {
                throw invalidNodeError(classDeclaration)
            }

            val entryName = qualifiedName.asString()
            val nameAnnotation = classDeclaration.annotations.first { it.shortName.getShortName() == "LegacyKeyName" }
            val legacyName = nameAnnotation.arguments.first().value.toString()

            return Entry(entryName, legacyName)
        }

        override fun defaultHandler(node: KSNode, data: Unit): Entry {
            throw invalidNodeError(node)
        }

        private fun invalidNodeError(node: KSNode) =
            InvalidPublicMethodException(
                "The object at ${node.location} is not a valid target for @PublicMethod. " +
                    "Annotated object must be an enum entry.",
            )
    }
}

private data class Entry(
    val entryName: String,
    val legacyName: String,
)

class InvalidPublicMethodException(msg: String) : Exception(msg)

class LegacyKeyNameProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return LegacyKeyNameProcessor(environment.codeGenerator)
    }
}
