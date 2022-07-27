package engine.methods

import engine.context.Context
import engine.expressions.Subexpression
import engine.methods.stepsproducers.StepsProducer
import engine.steps.Transformation

interface Method : StepsProducer {
    fun tryExecute(ctx: Context, sub: Subexpression): Transformation?

    override fun produceSteps(ctx: Context, sub: Subexpression): List<Transformation>? =
        tryExecute(ctx, sub)?.let { listOf(it) }
}
