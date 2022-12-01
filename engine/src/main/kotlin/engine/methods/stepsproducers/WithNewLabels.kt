package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Expression

/**
 * This executes [stepsProducer], making sure the expression is cleared of all labels before and after its execution.
 * This is a coarse safety measure, a better approach to the functionality provided by labels should be found.
 */
data class WithNewLabels(val stepsProducer: StepsProducer) : StepsProducer {

    override fun produceSteps(ctx: Context, sub: Expression) = buildSteps(sub) {
        clearLabels()
        addSteps(stepsProducer.produceSteps(ctx, lastSub))
        clearLabels()
    }
}
