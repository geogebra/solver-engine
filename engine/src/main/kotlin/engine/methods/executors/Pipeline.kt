package engine.methods.executors

import engine.context.Context
import engine.expressions.Subexpression
import engine.methods.Method
import engine.steps.Transformation

data class PipelineItem(val plan: Method, val optional: Boolean = false)

data class Pipeline(val items: List<PipelineItem>) : PlanExecutor {
    override fun produceSteps(ctx: Context, sub: Subexpression): List<Transformation> {
        val steps = mutableListOf<Transformation>()
        var lastSub = sub

        for (item in items) {
            val step = item.plan.tryExecute(ctx, lastSub)

            if (step != null) {
                val substitution = lastSub.substitute(step.fromExpr.path, step.toExpr)
                lastSub = Subexpression(substitution.expr, sub.parent, lastSub.path)
                steps.add(step)
            } else if (!item.optional) {
                return emptyList()
            }
        }

        return steps
    }
}
