package engine.steps.metadata

import engine.context.Context
import engine.expressionbuilder.MappedExpressionBuilder
import engine.expressions.Expression
import engine.patterns.Match

/**
 * Enums containing translation keys should be annotated with this so that the keys will be imported into ggbtrans.
 */
@Target(AnnotationTarget.CLASS)
annotation class TranslationKeys

interface MetadataKey {
    val keyName: String
}

interface CategorisedMetadataKey : MetadataKey {
    val category: String
    override val keyName get() = "$category.$this"
}

data class Metadata(val key: MetadataKey, val mappedParams: List<Expression>)

data class MetadataMaker(val key: MetadataKey, val parameters: MappedExpressionBuilder.() -> List<Expression>) {
    fun make(context: Context, match: Match) =
        Metadata(
            key = key,
            mappedParams = with(MappedExpressionBuilder(context, match)) { parameters() },
        )
}

fun metadata(key: MetadataKey, vararg parameters: Expression) =
    Metadata(key, parameters.asList())
