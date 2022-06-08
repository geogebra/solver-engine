package engine.steps.metadata

import engine.expressionmakers.ExpressionMaker
import engine.expressionmakers.OperatorExpressionMaker
import engine.expressions.MappedExpression
import engine.expressions.MetadataOperator

interface MetadataKey

data class Metadata(val key: MetadataKey, val mappedParams: List<MappedExpression>)

fun makeMetadata(key: MetadataKey, vararg expressionMakers: ExpressionMaker) =
    OperatorExpressionMaker(MetadataOperator(key), expressionMakers.toList())
