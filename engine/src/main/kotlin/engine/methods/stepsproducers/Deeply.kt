package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Expression
import engine.steps.Transformation

/**
 * A `StepsProducer` implementation that can apply a given `StepsProducer` to a descendant, `deepFirst` specifying
 * whether to try children first (when true) or parents first (when false).
 */
data class Deeply(val stepsProducer: StepsProducer, val deepFirst: Boolean = false) : StepsProducer {

    private fun visitPrefix(ctx: Context, sub: Expression): List<Transformation>? {
        return stepsProducer.produceSteps(ctx, sub)
            ?: sub.children().firstNotNullOfOrNull { visitPrefix(ctx, it) }
    }

    private fun visitPostfix(ctx: Context, sub: Expression): List<Transformation>? {
        return sub.children().firstNotNullOfOrNull { visitPostfix(ctx, it) }
            ?: stepsProducer.produceSteps(ctx, sub)
    }

    override fun produceSteps(ctx: Context, sub: Expression) = buildSteps(ctx, sub) {
        addSteps(if (deepFirst) visitPostfix(ctx, sub) else visitPrefix(ctx, sub))
    }
}
