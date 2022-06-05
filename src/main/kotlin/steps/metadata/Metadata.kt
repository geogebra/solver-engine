package steps.metadata

import expressionmakers.ExpressionMaker
import expressionmakers.OperatorExpressionMaker
import expressions.MappedExpression
import expressions.MetadataOperator

interface MetadataKey

data class Metadata(val key: MetadataKey, val mappedParams: List<MappedExpression>)

fun makeMetadata(key: MetadataKey, vararg expressionMakers: ExpressionMaker) =
    OperatorExpressionMaker(MetadataOperator(key), expressionMakers.toList())
