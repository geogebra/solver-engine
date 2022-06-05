package rules

import context.Context
import expressionmakers.ExpressionMaker
import expressions.Subexpression
import patterns.Match
import patterns.Pattern
import plans.TransformationProducer
import steps.Transformation

data class Rule(
    override val pattern: Pattern,
    val resultMaker: ExpressionMaker,
    val explanationMaker: ExpressionMaker,
    val skillMakers: List<ExpressionMaker> = emptyList(),
) : TransformationProducer {

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        val result = resultMaker.makeMappedExpression(match)
        return Transformation(
            sub,
            result,
            null,
            explanationMaker.makeMappedExpression(match),
            skillMakers.map { it.makeMappedExpression(match) },
        )
    }
}
