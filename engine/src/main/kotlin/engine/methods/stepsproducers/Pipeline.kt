package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Subexpression

/**
 * Item for a `Pipeline`, specifying which `StepsProducer` to apply and whether it is optional.
 */
data class PipelineItem(val stepsProducer: StepsProducer, val optional: Boolean = false)

/**
 * A `StepsProducer` implementation that chains together a set of `StepsProducer` instances.  Each pipeline item can be
 * optional, in which case they can be skipped if unsuccessfull.  Non-optional items make `produceSteps()` return null.
 */
data class Pipeline(val items: List<PipelineItem>) : StepsProducer {

    override fun produceSteps(ctx: Context, sub: Subexpression) = buildSteps(sub) {
        for (item in items) {
            val itemSteps = item.stepsProducer.produceSteps(ctx, lastSub)

            if (itemSteps != null) {
                addSteps(itemSteps)
            } else if (!item.optional) {
                abort()
                break
            }
        }
    }
}
