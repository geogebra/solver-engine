package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Expression
import engine.expressions.Extractor

/**
 * A `StepsProducer` that can apply another `StepsProducer` to a `Subexpression` provided by the given `Extractor`.
 */
data class ApplyTo(
    val extractor: Extractor,
    val stepsProducer: StepsProducer,
) : StepsProducer {
    override fun produceSteps(ctx: Context, sub: Expression) = buildSteps(sub) {
        extractor.extract(sub)?.let { extracted ->
            addSteps(stepsProducer.produceSteps(ctx, extracted))
        }
    }
}
