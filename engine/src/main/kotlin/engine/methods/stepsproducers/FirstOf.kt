package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Subexpression
import engine.steps.Transformation

/**
 * A `StepsProducer` implementation that applies the first successful `StepsProducer` in the given list.
 */
data class FirstOf(val options: List<StepsProducer>) : StepsProducer {

    override fun produceSteps(ctx: Context, sub: Subexpression): List<Transformation>? {
        return options.firstNotNullOfOrNull { it.produceSteps(ctx, sub) }
    }
}
