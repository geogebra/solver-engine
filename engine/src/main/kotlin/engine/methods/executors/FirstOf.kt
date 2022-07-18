package engine.methods.executors

import engine.context.Context
import engine.expressions.Subexpression
import engine.methods.Method
import engine.patterns.Match
import engine.patterns.OneOfPattern
import engine.steps.Transformation

data class FirstOf(val options: List<Method>) : PlanExecutor {

    override val pattern = OneOfPattern(options.map { it.pattern })

    override fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation> {
        for (option in options) {
            if (match.getLastBinding(option.pattern) != null) {
                val result = option.execute(ctx, match, sub)
                if (result != null) {
                    return listOf(result)
                }
            }
        }
        return emptyList()
    }
}
