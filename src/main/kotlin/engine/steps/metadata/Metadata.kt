package engine.steps.metadata

import engine.expressionmakers.ExpressionMaker
import engine.expressions.MappedExpression
import engine.patterns.Maker
import engine.patterns.Match

interface MetadataKey

data class Metadata(val key: MetadataKey, val mappedParams: List<MappedExpression>)

typealias MetadataMaker = Maker<Metadata>

data class KeyExprsMetadataMaker(val key: MetadataKey, val expressionMakers: List<ExpressionMaker>) : MetadataMaker {
    override fun make(match: Match) = Metadata(key, expressionMakers.map { it.make(match) })
}

fun makeMetadata(key: MetadataKey, vararg expressionMakers: ExpressionMaker): MetadataMaker =
    KeyExprsMetadataMaker(key, expressionMakers.asList())
