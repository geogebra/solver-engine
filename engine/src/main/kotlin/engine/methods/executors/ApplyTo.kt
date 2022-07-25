package engine.methods.executors

import engine.context.Context
import engine.expressions.Subexpression
import engine.methods.Method
import engine.steps.Transformation

fun interface Extractor {
    fun extract(sub: Subexpression): Subexpression?
}

data class ApplyTo(
    val extractor: Extractor,
    val method: Method,
) : PlanExecutor {
    override fun produceSteps(ctx: Context, sub: Subexpression): List<Transformation> {
        val transformation = extractor.extract(sub)?.let { extracted ->
            method.tryExecute(ctx, extracted)
        }
        return transformation?.let { listOf(it) } ?: emptyList()
    }
}
