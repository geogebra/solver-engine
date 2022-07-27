package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Subexpression

/**
 * Interface for extracting subexpressions, used by `ApplyTo`.
 */
fun interface Extractor {
    /**
     * Extracts a `Subexpression` of a `Subexpression` and returns it, or return null if that extraction was
     * unsuccessful.
     */
    fun extract(sub: Subexpression): Subexpression?
}

/**
 * A `StepsProducer` that can apply another `StepsProducer` to a `Subexpression` provided by the given `Extractor`.
 */
data class ApplyTo(
    val extractor: Extractor,
    val stepsProducer: StepsProducer,
) : StepsProducer {
    override fun produceSteps(ctx: Context, sub: Subexpression) = buildSteps(sub) {
        extractor.extract(sub)?.let { extracted ->
            addSteps(stepsProducer.produceSteps(ctx, extracted))
        }
    }
}
