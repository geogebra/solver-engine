package steps

import expressionmakers.ExpressionMaker
import expressions.Expression
import patterns.Match

data class MetadataMaker<T>(val key: T, val expressionMakers: List<ExpressionMaker>) {
    fun makeMetadata(match: Match): Metadata {
        return Metadata(
            key.toString(),
            expressionMakers.map { it.makeExpression(match) },
        )
    }
}

data class Metadata(val key: String, val expressionsWithMappings: List<Pair<Expression, List<PathMapping>>>)

typealias ExplanationMaker = MetadataMaker<String>

typealias SkillMaker = MetadataMaker<SkillType>

fun <T> makeMetadata(key: T, vararg expressionMakers: ExpressionMaker) = MetadataMaker(key, expressionMakers.toList())