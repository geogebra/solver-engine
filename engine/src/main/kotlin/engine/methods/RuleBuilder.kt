package engine.methods

import engine.context.Context
import engine.expressionmakers.MakerBuilder
import engine.expressions.Subexpression
import engine.patterns.Pattern
import engine.patterns.RootMatch

class Rule(
    val pattern: Pattern,
    val transformation: MakerBuilder.() -> TransformationResult?
) : Runner {

    override fun run(ctx: Context, sub: Subexpression): TransformationResult? {
        for (match in pattern.findMatches(ctx, RootMatch, sub)) {
            val builder = MakerBuilder(ctx, match)
            builder.transformation()?.let {
                return it
            }
        }
        return null
    }
}

class RuleBuilder {
    fun onPattern(pattern: Pattern, result: MakerBuilder.() -> TransformationResult?): Rule =
        Rule(pattern, result)
}

fun rule(init: RuleBuilder.() -> Rule): Rule {
    val builder = RuleBuilder()
    return builder.init()
}
