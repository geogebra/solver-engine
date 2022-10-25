package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Subexpression
import engine.methods.Method
import engine.patterns.RootMatch
import engine.steps.Transformation
import engine.steps.metadata.Metadata
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.MetadataMaker
import engine.steps.metadata.makeMetadata

data class InStepItem(
    val method: Method,
    val explanation: MetadataMaker,
    val optional: Boolean,
)

interface InStep : StepsProducer {

    val inStepItems: List<InStepItem>
    fun getSubexpressions(sub: Subexpression): List<Subexpression>

    override fun produceSteps(ctx: Context, sub: Subexpression): List<Transformation>? {
        val stepSubs = getSubexpressions(sub).toMutableList()

        val steps = mutableListOf<Transformation>()
        var lastSub = sub
        for ((stepPlan, explanation, optional) in inStepItems) {
            val stepTransformations = stepSubs.map { stepPlan.tryExecute(ctx, it) }
            if (!optional && stepTransformations.any { it == null }) {
                return null
            }

            val nonNullTransformations = stepTransformations.filterNotNull()
            if (nonNullTransformations.isEmpty()) {
                continue
            }

            val prevSub = lastSub
            for (tr in nonNullTransformations) {
                val (_, newSub) = lastSub.substitute(tr.fromExpr.path, tr.toExpr)
                lastSub = newSub
            }

            steps.add(
                Transformation(
                    explanation = explanation.make(RootMatch),
                    fromExpr = prevSub,
                    toExpr = lastSub.toMappedExpr(),
                    steps = nonNullTransformations
                )
            )

            for ((i, tr) in stepTransformations.withIndex()) {
                if (tr != null) {
                    val (_, newSub) = tr.fromExpr.substitute(tr.fromExpr.path, tr.toExpr)
                    stepSubs[i] = newSub
                }
            }
        }
        return if (steps.isEmpty()) null else steps
    }
}

data class ApplyToChildrenInStep(
    override val inStepItems: List<InStepItem>
) : InStep {
    override fun getSubexpressions(sub: Subexpression): List<Subexpression> {
        return sub.children()
    }
}

private class FailedInStepStep : Exception()

@StepsProducerBuilderMarker
class InStepStepBuilder {
    lateinit var method: Method
    lateinit var explanationKey: MetadataKey
}

internal class ProceduralApplyToChildrenInStep(val init: InStepBuilder.() -> Unit) : StepsProducer {
    override fun produceSteps(ctx: Context, sub: Subexpression): List<Transformation>? {
        val builder = StepsBuilder(sub)
        val runner = InStepRunner(builder, ctx, sub.children())
        try {
            runner.init()
        } catch (_: FailedInStepStep) {
            return null
        }
        return builder.getFinalSteps()
    }
}

private class InStepRunner(val builder: StepsBuilder, val ctx: Context, stepSubs: List<Subexpression>) :
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
            acc.substitute(tr.fromExpr.path, tr.toExpr).second
        }

        builder.addStep(
            Transformation(
                explanation = Metadata(stepBuilder.explanationKey, emptyList()),
                fromExpr = builder.lastSub,
                toExpr = newSub.toMappedExpr(),
                steps = nonNullTransformations
            )
        )

        for ((i, tr) in stepTransformations.withIndex()) {
            if (tr != null) {
                lastStepSubs[i] = tr.fromExpr.substitute(tr.fromExpr.path, tr.toExpr).second
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

internal class ApplyToChildrenInStepDataBuilder : InStepBuilder {
    private var steps = mutableListOf<InStepItem>()

    override fun step(init: InStepStepBuilder.() -> Unit) {
        val builder = InStepStepBuilder()
        builder.init()
        steps.add(InStepItem(builder.method, makeMetadata(builder.explanationKey), false))
    }

    override fun optionalStep(init: InStepStepBuilder.() -> Unit) {
        val builder = InStepStepBuilder()
        builder.init()
        steps.add(InStepItem(builder.method, makeMetadata(builder.explanationKey), true))
    }

    fun buildStepsProducer(): InStep {
        return ApplyToChildrenInStep(steps)
    }
}
