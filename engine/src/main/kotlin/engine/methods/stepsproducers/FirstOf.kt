package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Expression
import engine.steps.Transformation

/**
 * A `StepsProducer` implementation that applies the first successful `StepsProducer` in the given list.
 */
data class FirstOf(val options: List<StepsProducer>) : StepsProducer {

    override fun produceSteps(ctx: Context, sub: Expression): List<Transformation>? {
        return options.firstNotNullOfOrNull { it.produceSteps(ctx, sub) }
    }
}

private class FoundOption : Exception() {
    override fun fillInStackTrace(): Throwable {
        return this
    }
}

internal class ProceduralFirstOf(val init: FirstOfBuilder.() -> Unit) : StepsProducer {
    override fun produceSteps(ctx: Context, sub: Expression): List<Transformation>? {
        val builder = StepsBuilder(sub)
        val runner = FirstOfRunner(builder, ctx)
        try {
            runner.init()
        } catch (_: FoundOption) {
            return builder.getFinalSteps()
        }
        return null
    }
}

private class FirstOfRunner(val builder: StepsBuilder, val ctx: Context) : FirstOfBuilder {
    override fun option(opt: StepsProducer) {
        val steps = opt.produceSteps(ctx, builder.lastSub)
        if (steps != null) {
            builder.addSteps(steps)
            throw FoundOption()
        }
    }

    override fun option(init: PipelineBuilder.() -> Unit) {
        option(ProceduralPipeline(init))
    }
}

internal class FirstOfDataBuilder : FirstOfBuilder {
    private var options: MutableList<StepsProducer> = mutableListOf()

    override fun option(opt: StepsProducer) {
        options.add(opt)
    }

    override fun option(init: PipelineBuilder.() -> Unit) {
        option(dataSteps(init))
    }

    fun buildStepsProducer(): FirstOf {
        require(options.isNotEmpty())
        return FirstOf(options)
    }
}
