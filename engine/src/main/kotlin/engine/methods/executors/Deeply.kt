package engine.methods.executors

import engine.context.Context
import engine.expressions.Subexpression
import engine.methods.Method
import engine.steps.Transformation

data class Deeply(val plan: Method, val deepFirst: Boolean = false) : PlanExecutor {

    private fun visitPrefix(ctx: Context, sub: Subexpression): Transformation? {
        return plan.tryExecute(ctx, sub)
            ?: sub.children().firstNotNullOfOrNull { visitPrefix(ctx, it) }
    }

    private fun visitPostfix(ctx: Context, sub: Subexpression): Transformation? {
        return sub.children().firstNotNullOfOrNull { visitPostfix(ctx, it) }
            ?: plan.tryExecute(ctx, sub)
    }

    override fun produceSteps(ctx: Context, sub: Subexpression): List<Transformation> {
        val step = if (deepFirst) visitPostfix(ctx, sub) else visitPrefix(ctx, sub)
        if (step != null) {
            return listOf(step)
        }

        return emptyList()
    }
}
