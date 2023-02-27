package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Expression
import engine.methods.Method
import engine.steps.Transformation
import engine.steps.metadata.Metadata
import engine.steps.metadata.MetadataKey

private class FailedInStepStep : Exception() {
    override fun fillInStackTrace(): Throwable {
        return this
    }
}

@StepsProducerBuilderMarker
class InStepStepBuilder {
    lateinit var method: Method
    lateinit var explanationKey: MetadataKey
}

internal class ProceduralApplyToChildrenInStep(val init: InStepBuilder.() -> Unit) : StepsProducer {
    override fun produceSteps(ctx: Context, sub: Expression): List<Transformation>? {
        val builder = StepsBuilder(ctx, sub)
        val runner = InStepRunner(builder, ctx, sub.children)
        try {
            runner.init()
        } catch (_: FailedInStepStep) {
            return null
        }
        return builder.getFinalSteps()
    }
}

private class InStepRunner(val builder: StepsBuilder, val ctx: Context, stepSubs: List<Expression>) :
    InStepBuilder {

    private var lastStepSubs = stepSubs.toMutableList()

    private fun step(init: InStepStepBuilder.() -> Unit, optional: Boolean) {
        val stepBuilder = InStepStepBuilder()
        stepBuilder.init()

        val stepTransformations = lastStepSubs.map { stepBuilder.method.tryExecute(ctx, it) }
        if (!optional && stepTransformations.any { it == null }) {
            builder.abort()
            throw FailedInStepStep()
        }

        val nonNullTransformations = stepTransformations.filterNotNull()
        if (nonNullTransformations.isEmpty()) {
            return
        }

        val newSub = nonNullTransformations.fold(builder.lastSub) { acc, tr ->
            acc.substitute(tr.fromExpr.updateOrigin(acc), tr.toExpr).withOrigin(acc.origin)
        }

        builder.addStep(
            Transformation(
                type = Transformation.Type.Plan,
                explanation = Metadata(stepBuilder.explanationKey, emptyList()),
                fromExpr = builder.lastSub,
                toExpr = newSub,
                steps = nonNullTransformations,
            ),
        )

        for ((i, tr) in stepTransformations.withIndex()) {
            if (tr != null) {
                lastStepSubs[i] =
                    tr.fromExpr.substitute(tr.fromExpr, tr.toExpr).withOrigin(tr.fromExpr.origin)
            }
        }
    }

    override fun step(init: InStepStepBuilder.() -> Unit) {
        step(init, false)
    }

    override fun optionalStep(init: InStepStepBuilder.() -> Unit) {
        step(init, true)
    }
}
