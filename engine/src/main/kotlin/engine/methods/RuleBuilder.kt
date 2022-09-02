package engine.methods

import engine.context.Context
import engine.expressionmakers.MakerBuilder
import engine.expressions.MappedExpression
import engine.expressions.Subexpression
import engine.patterns.Pattern
import engine.patterns.RootMatch
import engine.steps.Transformation
import engine.steps.metadata.Metadata

private class Rule(
    val pattern: Pattern,
    val transformation: MakerBuilder.() -> TransformationResult?,
) : Method {

    override fun tryExecute(ctx: Context, sub: Subexpression): Transformation? {
        val match = pattern.findMatches(sub, RootMatch).firstOrNull() ?: return null
        val builder = MakerBuilder(match)
        return builder.transformation()?.let {
            Transformation(
                fromExpr = sub,
                toExpr = it.toExpr,
                steps = it.steps,
                explanation = it.explanation,
                skills = it.skills,
            )
        }
    }
}

data class TransformationResult(
    val toExpr: MappedExpression,
    val steps: List<Transformation>? = null,
    val explanation: Metadata? = null,
    val skills: List<Metadata> = emptyList(),
)

class RuleBuilder {
    fun onPattern(pattern: Pattern, result: MakerBuilder.() -> TransformationResult?): Method =
        Rule(pattern, result)
}

fun rule(init: RuleBuilder.() -> Method): Method {
    val builder = RuleBuilder()
    return builder.init()
}
