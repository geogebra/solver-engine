package engine.steps.metadata

import engine.expressionmakers.ExpressionMaker
import engine.expressions.MappedExpression
import engine.patterns.Maker
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

data class Metadata(val key: MetadataKey, val mappedParams: List<MappedExpression>)

typealias MetadataMaker = Maker<Metadata>

data class KeyExprsMetadataMaker(val key: MetadataKey, val expressionMakers: List<ExpressionMaker>) : MetadataMaker {
    override fun make(match: Match) = Metadata(key, expressionMakers.map { it.make(match) })
}

fun makeMetadata(key: MetadataKey, vararg expressionMakers: ExpressionMaker): MetadataMaker =
    KeyExprsMetadataMaker(key, expressionMakers.asList())
