package engine.steps.metadata

import engine.expressionmakers.ExpressionMaker
import engine.expressions.MappedExpression
import engine.patterns.Match

interface MetadataKey

data class Metadata(val key: MetadataKey, val mappedParams: List<MappedExpression>)

data class MetadataMaker(val key: MetadataKey, val expressionMakers: List<ExpressionMaker>) {
    fun makeMetadata(match: Match) = Metadata(key, expressionMakers.map { it.makeMappedExpression(match) })
}

fun makeMetadata(key: MetadataKey, vararg expressionMakers: ExpressionMaker) =
    MetadataMaker(key, expressionMakers.asList())
