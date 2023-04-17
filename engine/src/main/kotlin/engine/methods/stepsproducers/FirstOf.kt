package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Expression
import engine.steps.Transformation

internal class FirstOf(val init: FirstOfBuilder.() -> Unit) : StepsProducer {
    override fun produceSteps(ctx: Context, sub: Expression): List<Transformation>? {
        val runner = FirstOfRunner(sub, ctx)
        runner.init()
        return runner.steps
    }
}

private class FirstOfRunner(val sub: Expression, val ctx: Context) : FirstOfBuilder {

    var steps: List<Transformation>? = null

    override fun option(opt: StepsProducer) {
        if (steps == null) {
            val currentSteps = opt.produceSteps(ctx, sub)
            if (currentSteps != null) {
                steps = currentSteps
            }
        }
    }

    override fun option(init: PipelineBuilder.() -> Unit) {
        if (steps == null) {
            option(ProceduralPipeline(init))
        }
    }

    override fun shortOption(opt: StepsProducer) {
        val currentSteps = opt.produceSteps(ctx, sub)
        val steps = steps
        if (steps == null || currentSteps != null && steps.last().toExpr == currentSteps.last().toExpr) {
            this.steps = currentSteps
        }
    }

    override fun shortOption(init: PipelineBuilder.() -> Unit) {
        shortOption(ProceduralPipeline(init))
    }
}
