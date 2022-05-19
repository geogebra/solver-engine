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

class RuleBuilder {
    var pattern: Pattern? = null
    var explanationMaker: ExplanationMaker? = null
    var skillMakers: List<SkillMaker> = emptyList()
    var resultMaker: ExpressionMaker? = null

    fun buildRule(): Rule {
        return Rule(
            pattern = pattern!!,
            explanationMaker = explanationMaker!!,
            skillMakers = skillMakers,
            resultMaker = resultMaker!!,
        )
    }
}

fun rule(init: RuleBuilder.() -> Unit): Rule {
    val ruleBuilder = RuleBuilder()
    ruleBuilder.init()
    return ruleBuilder.buildRule()
}