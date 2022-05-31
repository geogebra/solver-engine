package rules

import context.Context
import expressionmakers.ExpressionMaker
import expressions.Subexpression
import patterns.Match
import patterns.Pattern
import plans.Plan
import steps.Transformation

data class Rule(
    override val pattern: Pattern,
    override val explanationMaker: ExpressionMaker,
    override val skillMakers: List<ExpressionMaker> = emptyList(),
    val resultMaker: ExpressionMaker,
) : Plan {

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
