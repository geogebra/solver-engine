package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Expression
import engine.expressions.Extractor
import engine.methods.PlanBuilder
import engine.methods.TaskSetBuilder
import engine.patterns.Pattern
import engine.steps.Transformation

/**
 * [ProceduralPipeline.whilePossible] will fail if exceeding this number of iterations.
 */
internal const val MAX_WHILE_POSSIBLE_ITERATIONS = 100

/**
 * Exception returned by [ProceduralPipeline.whilePossible] when it exceeds the maximum number of iterations.
 * It probably means that there is a buggy plan specification (or that the expression it is applied to is very large)
 */
class TooManyIterationsException(msg: String) : RuntimeException(msg)

internal class ProceduralPipeline(val init: PipelineBuilder.() -> Unit) : StepsProducer {
    override fun produceSteps(ctx: Context, sub: Expression): List<Transformation>? {
        val builder = StepsBuilder(ctx, sub)
        val runner = PipelineRunner(builder, ctx)
        runner.init()
        return builder.getFinalSteps()
    }
}

@Suppress("TooManyFunctions")
private class PipelineRunner(val builder: StepsBuilder, val ctx: Context) : PipelineBuilder {

    private val expression get() = builder.lastSub

    override fun inContext(contextFactory: Context.(Expression) -> Context, init: PipelineBuilder.() -> Unit) {
        PipelineRunner(builder, ctx.contextFactory(expression)).init()
    }

    private fun addSteps(steps: List<Transformation>?) {
        if (steps != null) {
            builder.addSteps(steps)
        } else {
            builder.abort()
        }
    }

    override fun withNewLabels(init: PipelineBuilder.() -> Unit) {
        if (builder.aborted) return

        builder.clearLabels()
        apply(init)
        builder.clearLabels()
    }

    override fun optionally(steps: StepsProducer) {
        if (builder.aborted) return

        steps.produceSteps(ctx, builder.lastSub)
            ?.let { builder.addSteps(it) }
    }

    override fun optionally(init: PipelineBuilder.() -> Unit) {
        if (builder.aborted) return

        optionally(ProceduralPipeline(init))
    }

    override fun apply(steps: StepsProducer) {
        if (builder.aborted) return

        addSteps(steps.produceSteps(ctx, builder.lastSub))
    }

    override fun apply(init: PipelineBuilder.() -> Unit) {
        if (builder.aborted) return

        apply(ProceduralPipeline(init))
    }

    override fun check(condition: Context.(Expression) -> Boolean) {
        if (builder.aborted) return

        if (!ctx.condition(builder.lastSub)) {
            builder.abort()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Expression>applyToKind(stepsProducer: StepsProducer, extractor: Extractor<T>) {
        if (builder.aborted) return

        addSteps(
            extractor.extract(builder.lastSub as T)?.let {
                stepsProducer.produceSteps(ctx, it)
            },
        )
    }

    override fun applyTo(stepsProducer: StepsProducer, extractor: Extractor<Expression>) {
        if (builder.aborted) return

        addSteps(
            extractor.extract(builder.lastSub)?.let {
                stepsProducer.produceSteps(ctx, it)
            },
        )
    }

    override fun applyTo(extractor: Extractor<Expression>, init: PipelineBuilder.() -> Unit) {
        if (builder.aborted) return

        applyTo(ProceduralPipeline(init), extractor)
    }

    override fun applyToChildrenInStep(init: InStepBuilder.() -> Unit) {
        if (builder.aborted) return

        addSteps(ProceduralApplyToChildrenInStep(init).produceSteps(ctx, builder.lastSub))
    }

    override fun firstOf(init: FirstOfBuilder.() -> Unit) {
        if (builder.aborted) return

        addSteps(FirstOf(init).produceSteps(ctx, builder.lastSub))
    }

    override fun whilePossible(stepsProducer: StepsProducer) {
        if (builder.aborted) return

        repeat(MAX_WHILE_POSSIBLE_ITERATIONS) {
            val iterationSteps = stepsProducer.produceSteps(ctx, builder.lastSub) ?: return
            builder.addSteps(iterationSteps)
            if (builder.undefined()) {
                return
            }
        }

        throw TooManyIterationsException(
            "WhilePossible max iteration number ($MAX_WHILE_POSSIBLE_ITERATIONS) " +
                "exceeded for expression ${builder.lastSub}",
        )
    }

    override fun whilePossible(init: PipelineBuilder.() -> Unit) {
        if (builder.aborted) return

        whilePossible(ProceduralPipeline(init))
    }

    override fun deeply(stepsProducer: StepsProducer, deepFirst: Boolean) {
        if (builder.aborted) return

        fun visitPrefix(sub: Expression): List<Transformation>? =
            stepsProducer.produceSteps(ctx, sub)
                ?: sub.children.firstNotNullOfOrNull { visitPrefix(it) }

        fun visitPostfix(sub: Expression): List<Transformation>? =
            sub.children.firstNotNullOfOrNull { visitPostfix(it) }
                ?: stepsProducer.produceSteps(ctx, sub)

        addSteps(
            when {
                deepFirst -> visitPostfix(builder.lastSub)
                else -> visitPrefix(builder.lastSub)
            },
        )
    }

    override fun deeply(deepFirst: Boolean, init: PipelineBuilder.() -> Unit) {
        if (builder.aborted) return

        deeply(ProceduralPipeline(init), deepFirst)
    }

    override fun plan(init: PlanBuilder.() -> Unit) {
        if (builder.aborted) return

        addSteps(engine.methods.plan(init).produceSteps(ctx, builder.lastSub))
    }

    override fun taskSet(init: TaskSetBuilder.() -> Unit) {
        if (builder.aborted) return

        addSteps(engine.methods.taskSet(init).produceSteps(ctx, builder.lastSub))
    }

    override fun checkForm(patternProvider: () -> Pattern) {
        if (builder.aborted) return

        val pattern = patternProvider()
        if (!pattern.matches(ctx, builder.lastSub)) {
            builder.abort()
        }
    }

    override fun contextSensitive(init: ContextSensitiveBuilder.() -> Unit) {
        if (builder.aborted) return

        addSteps(contextSensitiveSteps(init).produceSteps(ctx, builder.lastSub))
    }
}

/**
 * Type-safe builder to create a [StepsProducer] using the [PipelineBuilder] DSL.
 */
fun steps(init: PipelineBuilder.() -> Unit): StepsProducer = ProceduralPipeline(init)
