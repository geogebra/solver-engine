package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Expression
import engine.methods.Method
import engine.steps.Transformation
import engine.steps.metadata.Metadata
import engine.steps.metadata.MetadataKey

data class InStepItem(
    val method: Method,
    val explanation: MetadataKey,
    val optional: Boolean,
)

interface InStep : StepsProducer {

    val inStepItems: List<InStepItem>
    fun getSubexpressions(sub: Expression): List<Expression>

    override fun produceSteps(ctx: Context, sub: Expression): List<Transformation>? {
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
                val substitution = lastSub.substitute(tr.fromExpr.updateOrigin(lastSub), tr.toExpr)
                lastSub = substitution.withOrigin(lastSub.origin)
            }

            steps.add(
                Transformation(
                    explanation = Metadata(explanation, listOf()),
                    fromExpr = prevSub,
                    toExpr = lastSub,
                    steps = nonNullTransformations
                )
            )

            for ((i, tr) in stepTransformations.withIndex()) {
                if (tr != null) {
                    val substitution = tr.fromExpr.substitute(tr.fromExpr, tr.toExpr)
                    stepSubs[i] = substitution.withOrigin(tr.fromExpr.origin)
                }
            }
        }
        return if (steps.isEmpty()) null else steps
    }
}

data class ApplyToChildrenInStep(
    override val inStepItems: List<InStepItem>
) : InStep {
    override fun getSubexpressions(sub: Expression): List<Expression> {
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
    override fun produceSteps(ctx: Context, sub: Expression): List<Transformation>? {
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
                explanation = Metadata(stepBuilder.explanationKey, emptyList()),
                fromExpr = builder.lastSub,
                toExpr = newSub,
                steps = nonNullTransformations
            )
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

internal class ApplyToChildrenInStepDataBuilder : InStepBuilder {
    private var steps = mutableListOf<InStepItem>()

    override fun step(init: InStepStepBuilder.() -> Unit) {
        val builder = InStepStepBuilder()
        builder.init()
        steps.add(InStepItem(builder.method, builder.explanationKey, false))
    }

    override fun optionalStep(init: InStepStepBuilder.() -> Unit) {
        val builder = InStepStepBuilder()
        builder.init()
        steps.add(InStepItem(builder.method, builder.explanationKey, true))
    }

    fun buildStepsProducer(): InStep {
        return ApplyToChildrenInStep(steps)
    }
}
