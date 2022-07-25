package engine.methods.executors

import engine.context.Context
import engine.expressions.Subexpression
import engine.methods.Method
import engine.steps.Transformation

data class FirstOf(val options: List<Method>) : PlanExecutor {

    override fun produceSteps(ctx: Context, sub: Subexpression): List<Transformation> {
        for (option in options) {
            val result = option.tryExecute(ctx, sub)
            if (result != null) {
                return listOf(result)
            }
        }
        return emptyList()
    }
}
