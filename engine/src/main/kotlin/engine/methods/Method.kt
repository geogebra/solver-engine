package engine.methods

import engine.context.Context
import engine.expressions.Subexpression
import engine.patterns.Match
import engine.patterns.Pattern
import engine.patterns.RootMatch
import engine.steps.Transformation

interface Method {
    val pattern: Pattern

    fun execute(ctx: Context, match: Match, sub: Subexpression): Transformation?

    fun tryExecute(ctx: Context, sub: Subexpression): Transformation? {
        for (match in pattern.findMatches(sub, RootMatch)) {
            return execute(ctx, match, sub)
        }
        return null
    }
}
