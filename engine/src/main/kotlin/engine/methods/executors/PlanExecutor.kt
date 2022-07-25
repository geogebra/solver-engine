package engine.methods.executors

import engine.context.Context
import engine.expressions.Subexpression
import engine.steps.Transformation

interface PlanExecutor {
    fun produceSteps(ctx: Context, sub: Subexpression): List<Transformation>
}
