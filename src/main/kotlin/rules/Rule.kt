package rules

import context.Context
import expressionmakers.ExpressionMaker
import expressions.Subexpression
import patterns.Match
import patterns.Pattern
import plans.Plan
import steps.ExplanationMaker
import steps.SkillMaker
import steps.Transformation

data class Rule(
    override val pattern: Pattern,
    override val explanationMaker: ExplanationMaker,
    override val skillMakers: List<SkillMaker> = emptyList(),
    val resultMaker: ExpressionMaker,
) : Plan {

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        val result = resultMaker.makeMappedExpression(match)
        return Transformation(
            sub,
            result,
            null,
            explanationMaker.makeMetadata(match),
            skillMakers.map { it.makeMetadata(match) },
        )
    }
}
