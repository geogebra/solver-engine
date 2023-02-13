import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate
import com.google.devtools.ksp.visitor.KSDefaultVisitor
import java.io.OutputStream

/**
 * This scans the code for enums annotated with @TranslationKeys and produces a single JSON file containing a list of
 * translation keys definitions as the ggbtrans solver_import expects them.  So it is possible to make a POST request to
 * props/api/solver_import with the content of the generated file as request body.
 */
class TranslationKeysProcessor(
    private val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
) : SymbolProcessor {

    private lateinit var file: OutputStream
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }

        val symbols = resolver.getSymbolsWithAnnotation("engine.steps.metadata.TranslationKeys").toList()
        val ret = symbols.filter { !it.validate() }.toList()

        file = codeGenerator.createNewFile(
            Dependencies(true, *symbols.map { it.containingFile!! }.toTypedArray()),
            "",
            "TranslationKeys",
            "json",
        )
        val entries = symbols
            .filter { it.validate() }
            .flatMap { it.accept(TranslationKeysVisitor(), Unit) }
        val mapper = jacksonObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        mapper.writeValue(file, entries)

        invoked = true
        return ret
    }

    inner class TranslationKeysVisitor : KSDefaultVisitor<Unit, Sequence<Entry>>() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): Sequence<Entry> {
            if (classDeclaration.classKind != ClassKind.ENUM_CLASS) {
                logger.error("Cannot get translation keys from non-enum class", classDeclaration)
                return emptySequence()
            }
            val prefix = classDeclaration.simpleName.getShortName().removeSuffix("Explanation")
            return classDeclaration.declarations
                .filter { it is KSClassDeclaration && it.classKind == ClassKind.ENUM_ENTRY }
                .map {
                    Entry("$prefix.$it", it.docString?.trim())
                }
        }

        override fun defaultHandler(node: KSNode, data: Unit): Sequence<Entry> {
            logger.error("Translation keys can only be obtained from enum classes", node)
            return emptySequence()
        }
    }
}

class TranslationKeysProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment,
    ): SymbolProcessor {
        return TranslationKeysProcessor(environment.codeGenerator, environment.logger)
    }
}

/**
 * This represents a translation key definition as the ggbtrans solver_import API expects it.
 */
data class Entry(val key: String, val comment: String?)
