package engine.methods

import engine.context.Context
import engine.expressionbuilder.MappedExpressionBuilder
import engine.expressions.Expression
import engine.patterns.Match
import engine.patterns.Pattern
import engine.patterns.RootMatch
import engine.patterns.equationOf
import engine.steps.Task
import engine.steps.Transformation
import engine.steps.metadata.GmAction
import engine.steps.metadata.Metadata

class Rule(
    val pattern: Pattern,
    val transformation: RuleResultBuilder.() -> Transformation?,
) : Runner {

    override fun run(ctx: Context, sub: Expression): Transformation? {
        for (match in pattern.findMatches(ctx, RootMatch, sub)) {
            val builder = RuleResultBuilder(sub, ctx, match)
            builder.transformation()?.let {
                return it
            }
        }
        return null
    }
}

class RuleBuilder {
    fun onPattern(pattern: Pattern, result: RuleResultBuilder.() -> Transformation?): Rule =
        Rule(pattern, result)

    fun onEquation(lhs: Pattern, rhs: Pattern, result: RuleResultBuilder.() -> Transformation?): Rule =
        Rule(equationOf(lhs, rhs), result)
}

class RuleResultBuilder(val sub: Expression, ctx: Context, match: Match) : MappedExpressionBuilder(ctx, match) {

    @Suppress("LongParameterList")
    fun ruleResult(
        toExpr: Expression,
        steps: List<Transformation>? = null,
        tasks: List<Task>? = null,
        explanation: Metadata? = null,
        skills: List<Metadata> = emptyList(),
        gmAction: GmAction? = null,
        type: Transformation.Type = Transformation.Type.Rule,
    ) = Transformation(type, sub, toExpr, steps, tasks, explanation, skills, gmAction)
}

fun rule(init: RuleBuilder.() -> Rule): Rule {
    val builder = RuleBuilder()
    return builder.init()
}
