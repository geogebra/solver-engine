package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Expression
import engine.expressions.Extractor
import engine.methods.PlanBuilder
import engine.steps.Transformation

/**
 * Item for a `Pipeline`, specifying which `StepsProducer` to apply and whether it is optional.
 */
data class PipelineItem(val stepsProducer: StepsProducer, val optional: Boolean = false)

/**
 * A `StepsProducer` implementation that chains together a set of `StepsProducer` instances.  Each pipeline item can be
 * optional, in which case they can be skipped if unsuccessfull.  Non-optional items make `produceSteps()` return null.
 */
data class Pipeline(val items: List<PipelineItem>) : StepsProducer {

    override fun produceSteps(ctx: Context, sub: Expression) = buildSteps(sub) {
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

private class FailedStep : Exception() {
    override fun fillInStackTrace(): Throwable {
        return this
    }
}

internal class ProceduralPipeline(val init: PipelineBuilder.() -> Unit) : StepsProducer {
    override fun produceSteps(ctx: Context, sub: Expression): List<Transformation>? {
        val builder = StepsBuilder(sub)
        val runner = PipelineRunner(builder, ctx)
        try {
            runner.init()
        } catch (_: FailedStep) {
            return null
        }
        return builder.getFinalSteps()
    }
}

private class PipelineRunner(val builder: StepsBuilder, val ctx: Context) : PipelineBuilder {

    private fun runProducer(prod: StepsProducer, optional: Boolean = false) {
        val steps = prod.produceSteps(ctx, builder.lastSub)
        if (steps != null) {
            builder.addSteps(steps)
        } else if (!optional) {
            builder.abort()
            throw FailedStep()
        }
    }

    override fun withNewLabels(init: PipelineBuilder.() -> Unit) {
        builder.clearLabels()
        apply(init)
        builder.clearLabels()
    }

    override fun optionally(steps: StepsProducer) {
        runProducer(steps, optional = true)
    }

    override fun optionally(init: PipelineBuilder.() -> Unit) {
        optionally(ProceduralPipeline(init))
    }

    override fun apply(steps: StepsProducer) {
        runProducer(steps)
    }

    override fun apply(init: PipelineBuilder.() -> Unit) {
        apply(ProceduralPipeline(init))
    }

    override fun applyTo(steps: StepsProducer, extractor: Extractor) {
        runProducer(ApplyTo(extractor, steps))
    }

    override fun applyTo(extractor: Extractor, init: PipelineBuilder.() -> Unit) {
        applyTo(ProceduralPipeline(init), extractor)
    }

    override fun applyToChildrenInStep(init: InStepBuilder.() -> Unit) {
        runProducer(ProceduralApplyToChildrenInStep(init))
    }

    override fun firstOf(init: FirstOfBuilder.() -> Unit) {
        runProducer(ProceduralFirstOf(init))
    }

    override fun whilePossible(steps: StepsProducer) {
        runProducer(WhilePossible(steps), optional = true)
    }

    override fun whilePossible(init: PipelineBuilder.() -> Unit) {
        whilePossible(ProceduralPipeline(init))
    }

    override fun deeply(steps: StepsProducer, deepFirst: Boolean) {
        runProducer(Deeply(steps, deepFirst))
    }

    override fun deeply(deepFirst: Boolean, init: PipelineBuilder.() -> Unit) {
        deeply(ProceduralPipeline(init), deepFirst)
    }

    override fun plan(init: PlanBuilder.() -> Unit) {
        runProducer(engine.methods.plan(::proceduralSteps, init))
    }
}

private class PipelineDataBuilder : PipelineBuilder {
    private var pipelineItems = mutableListOf<PipelineItem>()

    private fun addItem(stepsProducer: StepsProducer, optional: Boolean = false) {
        pipelineItems.add(PipelineItem(stepsProducer, optional))
    }

    fun buildStepsProducer(): StepsProducer {
        return when (pipelineItems.size) {
            0 -> throw IllegalStateException("steps producer produces no steps")
            1 -> pipelineItems[0].stepsProducer
            else -> Pipeline(pipelineItems)
        }
    }

    override fun withNewLabels(init: PipelineBuilder.() -> Unit) {
        addItem(WithNewLabels(dataSteps(init)))
    }

    override fun optionally(steps: StepsProducer) {
        addItem(steps, true)
    }

    override fun optionally(init: PipelineBuilder.() -> Unit) {
        optionally(dataSteps(init))
    }

    override fun apply(steps: StepsProducer) {
        addItem(steps)
    }

    override fun apply(init: PipelineBuilder.() -> Unit) {
        apply(dataSteps(init))
    }

    override fun applyTo(steps: StepsProducer, extractor: Extractor) {
        addItem(ApplyTo(extractor, steps))
    }

    override fun applyTo(extractor: Extractor, init: PipelineBuilder.() -> Unit) {
        applyTo(dataSteps(init), extractor)
    }

    override fun applyToChildrenInStep(init: InStepBuilder.() -> Unit) {
        val builder = ApplyToChildrenInStepDataBuilder()
        builder.init()
        addItem(builder.buildStepsProducer())
    }

    override fun firstOf(init: FirstOfBuilder.() -> Unit) {
        val builder = FirstOfDataBuilder()
        builder.init()
        addItem(builder.buildStepsProducer())
    }

    override fun whilePossible(steps: StepsProducer) {
        addItem(WhilePossible(steps), true)
    }

    override fun whilePossible(init: PipelineBuilder.() -> Unit) {
        whilePossible(dataSteps(init))
    }

    override fun deeply(steps: StepsProducer, deepFirst: Boolean) {
        addItem(Deeply(steps, deepFirst))
    }

    override fun deeply(deepFirst: Boolean, init: PipelineBuilder.() -> Unit) {
        deeply(dataSteps(init), deepFirst)
    }

    override fun plan(init: PlanBuilder.() -> Unit) {
        addItem(engine.methods.plan(::dataSteps, init))
    }
}

/**
 * Type-safe builder to create a data [StepsProducer] using the [PipelineBuilder] DSL.
 */
internal fun dataSteps(init: PipelineBuilder.() -> Unit): StepsProducer {
    val builder = PipelineDataBuilder()
    builder.init()
    return builder.buildStepsProducer()
}

/**
 * Type-safe builder to create a procedural [StepsProducer] using the [PipelineBuilder] DSL.
 */
internal fun proceduralSteps(init: PipelineBuilder.() -> Unit): StepsProducer = ProceduralPipeline(init)

/**
 * The step type can be configured
 */
private val useDataSteps = System.getenv("SOLVER_STEPS_TYPE") == "data"

/**
 * Type-safe builder to create a [StepsProducer] using the [PipelineBuilder] DSL.
 */
fun steps(init: PipelineBuilder.() -> Unit): StepsProducer =
    if (useDataSteps) dataSteps(init) else proceduralSteps(init)
