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

interface Rule : Plan {
    val resultMaker: ExpressionMaker

    override fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation? {
        val (result, pathMappings) = resultMaker.makeExpression(match, sub.path)
        return Transformation(
            sub.path,
            sub.expr,
            result,
            pathMappings,
            null,
            explanationMaker.makeMetadata(match),
            skillMakers.map { it.makeMetadata(match) },
        )
    }
}

data class RuleData(
    override val pattern: Pattern,
    override val explanationMaker: ExplanationMaker,
    override val skillMakers: List<SkillMaker> = emptyList(),
    override val resultMaker: ExpressionMaker,
) : Rule
