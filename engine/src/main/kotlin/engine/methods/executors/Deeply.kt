package engine.methods.executors

import engine.context.Context
import engine.expressions.Subexpression
import engine.methods.Method
import engine.patterns.FindPattern
import engine.patterns.Match
import engine.steps.Transformation

data class Deeply(val plan: Method, val deepFirst: Boolean = false) : PlanExecutor {

    override val pattern = FindPattern(plan.pattern, deepFirst)

    override fun produceSteps(ctx: Context, match: Match, sub: Subexpression): List<Transformation> {
        val step = plan.execute(ctx, match, match.getLastBinding(plan.pattern)!!)
        return if (step == null) emptyList() else listOf(step)
    }
}
