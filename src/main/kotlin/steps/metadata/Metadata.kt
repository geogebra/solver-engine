package steps.metadata

import expressionmakers.ExpressionMaker
import expressions.MappedExpression
import patterns.Match

interface MetadataKey

object EmptyMetadataKey : MetadataKey

data class Metadata(val key: MetadataKey, val expressionsWithMappings: List<MappedExpression>)

data class MetadataMaker(val key: MetadataKey, val expressionMakers: List<ExpressionMaker>) {
    fun makeMetadata(match: Match): Metadata {
        return Metadata(key, expressionMakers.map { it.makeMappedExpression(match) },
        )
    }
}

fun makeMetadata(key: MetadataKey, vararg expressionMakers: ExpressionMaker) = MetadataMaker(key, expressionMakers.toList())