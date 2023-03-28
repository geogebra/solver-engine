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

@Target(AnnotationTarget.FIELD)
annotation class LegacyKeyName(val name: String)

interface MetadataKey {
    val keyName: String
}

interface CategorisedMetadataKey : MetadataKey {
    val category: String
    override val keyName get() = "$category.$this"
}

data class Metadata(val key: MetadataKey, val mappedParams: List<Expression>)

interface MetadataMaker {
    fun make(context: Context, expression: Expression, match: Match): Metadata
}

data class FixedKeyMetadataMaker(
    val key: MetadataKey,
    val parameters: MappedExpressionBuilder.() -> List<Expression>,
) : MetadataMaker {
    override fun make(context: Context, expression: Expression, match: Match) =
        Metadata(
            key = key,
            mappedParams = with(MappedExpressionBuilder(context, expression, match)) { parameters() },
        )
}

data class GeneralMetadataMaker(val init: MappedExpressionBuilder.() -> Metadata) : MetadataMaker {
    override fun make(context: Context, expression: Expression, match: Match): Metadata {
        val builder = MappedExpressionBuilder(context, expression, match)
        return builder.init()
    }
}

fun metadata(key: MetadataKey, vararg parameters: Expression) =
    Metadata(key, parameters.asList())

fun metadata(key: MetadataKey, parameters: List<Expression>) = Metadata(key, parameters)
