package processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getAnnotationsByType
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
import engine.methods.PublicMethod
import engine.methods.PublicStrategy
import translationkeys.TranslationKey
import translationkeys.writeTranslationKeys
import java.io.OutputStream

/**
 * This scans the code for enum entries annotated with @PublicMethod and creates a [methodRegistry] object in the
 * [mehods] package which registers all annotated entries as publicly available methods.
 */
class MethodsProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {

    private lateinit var file: OutputStream
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }

        processPublicMethods(resolver)
        processPublicStrategies(resolver)

        invoked = true
        return emptyList()
    }

    private fun processPublicMethods(resolver: Resolver) {
        val symbols = resolver.getSymbolsWithAnnotation(PublicMethod::class.qualifiedName!!).toList()

        // skip directory if no symbols annotations (no PublicMethod).
        if (symbols.isEmpty()) return

        file = codeGenerator.createNewFile(
            Dependencies(true, *symbols.map { it.containingFile!! }.toTypedArray()),
            "methods",
            "PublicMethods",
            "kt",
        )

        val entries = symbols.filter { it.validate() }.map { it.accept(PublicMethodVisitor(), Unit) }
        val writer = file.writer()
        with(writer) {
            appendLine("package methods\n")
            appendLine("import engine.methods.MethodRegistry")
            appendLine("import engine.methods.MethodRegistryBuilder")
            appendLine("import engine.methods.MethodId\n")
            appendLine("val methodRegistry = run {")
            appendLine("    val builder = MethodRegistryBuilder()")
            for (entry in entries) {
                appendLine("    builder.registerEntry(")
                appendLine("        MethodRegistry.EntryData(")
                appendLine("            methodId = MethodId(\"${entry.category}\", \"${entry.name}\"),")
                appendLine("            hiddenFromList = ${(entry.hiddenFromList)},")
                appendLine("            description = \"\"\"${entry.description}\"\"\",")
                appendLine("            implementation = ${entry.implementationName}")
                appendLine("        )")
                appendLine("    )")
            }
            appendLine("    builder.buildRegistry()")
            appendLine("}")
        }
        writer.close()

        val keysFile = codeGenerator.createNewFile(
            Dependencies(true, *symbols.map { it.containingFile!! }.toTypedArray()),
            "",
            "Method.TranslationKeys",
            "json",
        )
        writeTranslationKeys(keysFile, entries.map { it.getTranslationKey() })
    }

    private fun processPublicStrategies(resolver: Resolver) {
        val symbols = resolver.getSymbolsWithAnnotation(PublicStrategy::class.qualifiedName!!).toList()
        // skip directory if no symbols annotations (no PublicMethod).
        if (symbols.isEmpty()) return

        file = codeGenerator.createNewFile(
            Dependencies(true, *symbols.map { it.containingFile!! }.toTypedArray()),
            "methods",
            "PublicStrategies",
            "kt",
        )

        val entries = symbols.filter { it.validate() }.map { it.accept(PublicMethodVisitor(), Unit) }
        val writer = file.writer()
        with(writer) {
            appendLine("package methods\n")
            appendLine("import engine.methods.StrategyRegistry")
            appendLine("val strategyRegistry = run {")
            appendLine("    val registry = StrategyRegistry()")
            for (entry in entries) {
                appendLine("    registry.addEntry(")
                appendLine("        \"${entry.category}\",")
                appendLine("        StrategyRegistry.EntryData(")
                appendLine("            category = ${entry.categoryImplementationName}::class,")
                appendLine("            strategy = ${entry.implementationName},")
                appendLine("            description = \"\"\"${entry.description}\"\"\",")
                appendLine("        ),")
                appendLine("    )")
            }
            appendLine("    registry")
            appendLine("}")
        }
        writer.close()
    }

    private inner class PublicMethodVisitor : KSDefaultVisitor<Unit, Entry>() {

        @OptIn(KspExperimental::class)
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): Entry {
            val parentDeclaration = classDeclaration.parentDeclaration
            if (classDeclaration.qualifiedName == null || parentDeclaration == null || parentDeclaration.qualifiedName == null) {
                throw invalidNodeError(classDeclaration)
            }
            val category = classDeclaration.parentDeclaration!!.qualifiedName!!
            val qname = classDeclaration.qualifiedName!!

            // This visitor function is used both for @PublicMethod and @PublicStrategy annotated enum values,
            // so the @PublicMethod annotation might not be there.  It's probably not the right way to organise
            // the code...
            val publicMethodAnnotation = classDeclaration.getAnnotationsByType(PublicMethod::class).firstOrNull()
            val hiddenFromList = when {
                publicMethodAnnotation != null -> publicMethodAnnotation.hiddenFromList
                else -> false
            }

            return Entry(
                category = category.getShortName().removeSuffix("Plans"),
                categoryImplementationName = category.asString(),
                name = qname.getShortName(),
                implementationName = qname.asString(),
                description = classDeclaration.docString?.trim() ?: "",
                hiddenFromList = hiddenFromList,
            )
        }

        override fun defaultHandler(node: KSNode, data: Unit): Entry {
            throw invalidNodeError(node)
        }

        private fun invalidNodeError(node: KSNode) = InvalidPublicMethodException(
            "The object at ${node.location} is not a valid target for @PublicMethod. " +
                "Annotated object must be an enum entry.",
        )
    }
}

private data class Entry(
    val category: String,
    val categoryImplementationName: String,
    val name: String,
    val implementationName: String,
    val description: String,
    val hiddenFromList: Boolean,
) {
    fun getTranslationKey() = TranslationKey(
        key = "Method.$category.$name",
        comment = if (description == "") null else description,
    )
}

class InvalidPublicMethodException(msg: String) : Exception(msg)

class MethodsProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment,
    ): SymbolProcessor {
        return MethodsProcessor(environment.codeGenerator)
    }
}
