package engine.methods.stepsproducers

import engine.context.Context
import engine.context.Setting
import engine.expressions.Expression
import engine.expressions.Extractor
import engine.expressions.Label
import engine.expressions.LabelSpace
import engine.methods.CompositeMethod
import engine.methods.InlinePartialExpressions
import engine.methods.PlanBuilder
import engine.methods.TaskSetBuilder
import engine.patterns.Pattern
import engine.steps.Transformation
import kotlin.concurrent.Volatile

/**
 * [Pipeline.whilePossible] will fail if exceeding this number of iterations.
 */
internal const val MAX_WHILE_POSSIBLE_ITERATIONS = 100

/**
 * Exception returned by [Pipeline.whilePossible] when it exceeds the maximum number of iterations. It probably means
 * that there is a buggy plan specification (or that the expression it is applied to is very large)
 */
class TooManyIterationsException(msg: String) : RuntimeException(msg)

/**
 * Implements the pipeline [StepsProducer] from [init].  If [optionalSteps] is true, then the pipeline succeeds (with an
 * empty step list) even if none of the steps applied or it was aborted.  It uses a [PipelineCompiler] to prepare the
 * steps and calculate its minDepth, then uses a [PipelineRunner] to execute the pipeline.  If [minDepth] is specified
 * and non-negative, its value will be used as [minDepth] instead of the one provided by the [PipelineCompiler]. Note
 * that this can sometimes be necessary to prevent circular calculations leading to stack overflows.
 */
private class Pipeline(
    val init: PipelineFunc,
    private val optionalSteps: Boolean = false,
    minDepth: Int = -1,
) : StepsProducer {
    // These vars may be accessed from different request threads, so we want reads and writes to be atomic.  It doesn't
    // matter if two threads call initialize() because they will calculate the same value.  That wastes a little time
    // but that is more than offset by avoiding the slowdown that putting a lock around initialize() and accessing
    // stepsProducers and minDepthValue would induce.

    @Volatile
    private lateinit var stepsProducers: List<StepsProducer>

    @Volatile
    private var minDepthValue = minDepth

    override val minDepth: Int get() {
        // We only want to compute minDepth if it wasn't provided explicitly.  The reason it was provided is probably to
        // break cycles in this very computations leading to stack overflows.
        if (minDepthValue < 0) {
            initialize()
        }
        return minDepthValue
    }

    private fun initialize() {
        val compiler = PipelineCompiler()
        compiler.init()
        stepsProducers = compiler.getStepsProducers()
        minDepthValue = maxOf(minDepthValue, compiler.getMinDepth())
    }

    override fun produceSteps(ctx: Context, sub: Expression): List<Transformation>? {
        if (!::stepsProducers.isInitialized) {
            initialize()
        }

        val builder = StepsBuilder(ctx, sub, optionalSteps = optionalSteps)
        val runner = PipelineRunner(stepsProducers, builder, ctx)
        runner.init()
        return builder.getFinalSteps()
    }

    override fun toString() =
        if (::stepsProducers.isInitialized) {
            "ProceduralPipeline(minDepth=$minDepthValue)"
        } else {
            "ProceduralPipeline(uninitialized)"
        }
}

/**
 * A [PipelineBuilder] whose purpose is to compile the pipeline, i.e. precalculate steps from their specification and
 * precalculate the minDepth of a pipeline.
 */
@Suppress("TooManyFunctions")
private class PipelineCompiler : PipelineBuilder {
    private val stepsProducers = mutableListOf<StepsProducer>()
    private val optionalSteps = mutableListOf<Pair<StepsProducer, Int>>()

    private var minDepth = 0
    private var compulsoryStepFound = false

    fun getStepsProducers(): List<StepsProducer> {
        return stepsProducers.toList()
    }

    fun getMinDepth(): Int {
        return if (compulsoryStepFound) {
            minDepth
        } else {
            // The pipeline is made only of optional steps. So we want the same minDepth as the shallowest optional
            // step.  It is only calculated at the end because many plans have optional steps that introduce cycles in
            // minDepth calculation.
            optionalSteps.minOfOrNull { (step, offset) -> step.minDepth + offset } ?: 0
        }
    }

    private fun registerStep(step: StepsProducer, optional: Boolean = false, depth: Int = 0) {
        when {
            compulsoryStepFound -> return
            optional -> optionalSteps.add(Pair(step, depth))
            else -> {
                compulsoryStepFound = true
                minDepth = maxOf(minDepth, step.minDepth + depth)
            }
        }
    }

    private fun registerStepsInit(init: PipelineFunc, optional: Boolean = false, depth: Int = 0) {
        val stepsProducer = steps(init)
        stepsProducers.add(stepsProducer)
        registerStep(stepsProducer, optional, depth)
    }

    override fun optionally(steps: StepsProducer) {
        registerStep(steps, optional = true)
    }

    override fun optionally(init: PipelineFunc) {
        registerStepsInit(init, optional = true)
    }

    override fun shortcut(steps: StepsProducer) {
        registerStep(steps, optional = true)
    }

    override fun shortcut(init: PipelineFunc) {
        registerStepsInit(init, optional = true)
    }

    override fun withNewLabels(init: PipelineFunc) {
        registerStepsInit(init)
    }

    override fun apply(steps: StepsProducer) {
        registerStep(steps)
    }

    override fun apply(init: PipelineFunc) {
        registerStepsInit(init)
    }

    override fun check(condition: Context.(Expression) -> Boolean) {
        // Here the condition could tell us something about the min depth of the expression
    }

    override fun applyTo(stepsProducer: StepsProducer, extractor: Extractor<Expression>) {
        // Here the extractor could tell us about its depth
        registerStep(stepsProducer)
    }

    override fun applyTo(stepsProducer: StepsProducer, label: Label) {
        registerStep(stepsProducer)
    }

    override fun applyTo(extractor: Extractor<Expression>, init: PipelineFunc) {
        // Here the extractor could tell us about its depth
        registerStepsInit(init)
    }

    override fun applyTo(label: Label, init: PipelineFunc) {
        registerStepsInit(init)
    }

    override fun <T : Expression> applyToKind(stepsProducer: StepsProducer, extractor: Extractor<T>) {
        // Here the extractor could tell us about its depth and also the expression type
        registerStep(stepsProducer)
    }

    override fun applyToChildren(stepsProducer: StepsProducer, all: Boolean, atLeastOne: Boolean) {
        registerStep(stepsProducer, optional = !atLeastOne, depth = 1)
    }

    override fun applyToChildren(all: Boolean, atLeastOne: Boolean, init: PipelineFunc) {
        registerStepsInit(init, optional = !atLeastOne, depth = 1)
    }

    override fun applyToConstraint(stepsProducer: StepsProducer) {
        // Add 1 because the constraint is a child of the expression
        registerStep(stepsProducer, depth = 1)
    }

    override fun firstOf(init: FirstOfFunc) {
        val stepsProducer = engine.methods.stepsproducers.firstOf(init)
        registerStep(stepsProducer)
        stepsProducers.add(stepsProducer)
    }

    override fun branchOn(setting: Setting, init: BranchOnFunc) {
        val stepsProducer = engine.methods.stepsproducers.branchOn(setting, init)
        registerStep(stepsProducer)
        stepsProducers.add(stepsProducer)
    }

    override fun whilePossible(stepsProducer: StepsProducer) {
        registerStep(stepsProducer, optional = true)
    }

    override fun whilePossible(init: PipelineFunc) {
        registerStepsInit(init, optional = true)
    }

    override fun deeply(stepsProducer: StepsProducer, deepFirst: Boolean) {
        registerStep(stepsProducer)
    }

    override fun deeply(deepFirst: Boolean, init: PipelineFunc) {
        registerStepsInit(init)
    }

    override fun plan(init: PlanBuilder.() -> CompositeMethod) {
        val stepsProducer = engine.methods.plan(init)
        stepsProducers.add(stepsProducer)
        registerStep(stepsProducer)
    }

    override fun taskSet(init: TaskSetBuilder.() -> CompositeMethod) {
        val stepsProducer = engine.methods.taskSet(init)
        stepsProducers.add(stepsProducer)
        registerStep(stepsProducer)
    }

    override fun checkForm(patternProvider: () -> Pattern) {
        val pattern = patternProvider()
        if (!compulsoryStepFound && pattern.minDepth > minDepth) {
            minDepth = pattern.minDepth
        }
    }

    override fun inContext(contextFactory: Context.(Expression) -> Context, init: PipelineFunc) {
        registerStepsInit(init)
    }
}

/**
 * A [PipelineBuilder] that executes the pipeline. [stepsProducers] is the list of [StepsProducer] instances
 * precalculated by the [PipelineCompiler].
 */
@Suppress("TooManyFunctions")
private class PipelineRunner(
    val stepsProducers: List<StepsProducer>,
    val builder: StepsBuilder,
    val ctx: Context,
) : PipelineBuilder {
    private val expression get() = builder.expression
    private var index = 0

    private fun nextStepsProducer(): StepsProducer {
        val next = stepsProducers[index]
        index++
        return next
    }

    override fun inContext(contextFactory: Context.(Expression) -> Context, init: PipelineFunc) {
        if (!builder.inProgress) return
        addSteps(nextStepsProducer().produceSteps(ctx.contextFactory(expression), expression))
    }

    private fun tidyUp() {
        repeat(MAX_WHILE_POSSIBLE_ITERATIONS) {
            val iterationStep = ctx.constraintMerger?.tryExecute(ctx, builder.expression)
                ?: InlinePartialExpressions.tryExecute(ctx, builder.simpleExpression)
                ?: return
            builder.addStep(iterationStep)
        }
    }

    private fun addSteps(steps: List<Transformation>?) {
        if (steps != null) {
            builder.addSteps(steps)
            tidyUp()
        } else {
            builder.abort()
        }
    }

    override fun withNewLabels(init: PipelineFunc) {
        if (!builder.inProgress) return
        val labels = LabelSpace()

        inContext({ copy(labelSpace = labels) }, init)
        builder.clearLabels(labels)
    }

    override fun optionally(steps: StepsProducer) {
        if (!builder.inProgress) return

        steps.produceSteps(ctx, expression)
            ?.let {
                builder.addSteps(it)
                tidyUp()
            }
    }

    override fun optionally(init: PipelineFunc) {
        if (!builder.inProgress) return

        optionally(nextStepsProducer())
    }

    override fun shortcut(steps: StepsProducer) {
        if (!builder.inProgress) return

        steps.produceSteps(ctx, expression)
            ?.let {
                builder.addSteps(it)
                builder.succeed()
            }
    }

    override fun shortcut(init: PipelineFunc) {
        if (!builder.inProgress) return

        shortcut(nextStepsProducer())
    }

    override fun apply(steps: StepsProducer) {
        if (!builder.inProgress) return

        addSteps(steps.produceSteps(ctx, expression))
    }

    override fun apply(init: PipelineFunc) {
        if (!builder.inProgress) return

        apply(nextStepsProducer())
    }

    override fun check(condition: Context.(Expression) -> Boolean) {
        if (!builder.inProgress) return

        if (!ctx.condition(expression)) {
            builder.abort()
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Expression> applyToKind(stepsProducer: StepsProducer, extractor: Extractor<T>) {
        if (!builder.inProgress) return

        addSteps(
            extractor.extract(builder.simpleExpression as T)?.let {
                stepsProducer.produceSteps(ctx, it)
            },
        )
    }

    override fun applyTo(stepsProducer: StepsProducer, extractor: Extractor<Expression>) {
        if (!builder.inProgress) return

        addSteps(
            extractor.extract(builder.simpleExpression)?.let {
                stepsProducer.produceSteps(ctx, it)
            },
        )
    }

    override fun applyTo(stepsProducer: StepsProducer, label: Label) {
        if (!builder.inProgress) return

        val extractor = ctx.labelSpace?.getLabelInstance(label)
        if (extractor == null) {
            builder.abort()
        } else {
            applyTo(stepsProducer, extractor)
        }
    }

    override fun applyTo(extractor: Extractor<Expression>, init: PipelineFunc) {
        if (!builder.inProgress) return

        applyTo(nextStepsProducer(), extractor)
    }

    override fun applyTo(label: Label, init: PipelineFunc) {
        if (!builder.inProgress) return

        applyTo(nextStepsProducer(), label)
    }

    override fun applyToChildren(all: Boolean, atLeastOne: Boolean, init: PipelineFunc) {
        if (!builder.inProgress) return

        applyToChildren(nextStepsProducer(), all, atLeastOne)
    }

    override fun applyToChildren(stepsProducer: StepsProducer, all: Boolean, atLeastOne: Boolean) {
        if (!builder.inProgress) return

        val childCount = builder.simpleExpression.childCount
        var appliedOnce = false
        for (i in 0 until childCount) {
            val steps = stepsProducer.produceSteps(ctx, builder.simpleExpression.nthChild(i))
            when {
                steps != null -> {
                    appliedOnce = true
                    builder.addSteps(steps)
                }
                all -> {
                    builder.abort()
                    return
                }
            }
        }
        tidyUp()
        if (atLeastOne && !appliedOnce) {
            builder.abort()
        }
    }

    override fun applyToConstraint(stepsProducer: StepsProducer) {
        val constraint = builder.constraint
        if (constraint != null) {
            addSteps(stepsProducer.produceSteps(ctx, constraint))
        }
    }

    override fun firstOf(init: FirstOfFunc) {
        if (!builder.inProgress) return

        addSteps(nextStepsProducer().produceSteps(ctx, expression))
    }

    override fun branchOn(setting: Setting, init: BranchOnFunc) {
        if (!builder.inProgress) return

        addSteps(nextStepsProducer().produceSteps(ctx, expression))
    }

    override fun whilePossible(stepsProducer: StepsProducer) {
        if (!builder.inProgress) return

        repeat(MAX_WHILE_POSSIBLE_ITERATIONS) {
            val iterationSteps = stepsProducer.produceSteps(ctx, expression) ?: return
            builder.addSteps(iterationSteps)
            tidyUp()
            if (builder.undefined()) {
                return
            }
        }

        throw TooManyIterationsException(
            "WhilePossible max iteration number ($MAX_WHILE_POSSIBLE_ITERATIONS) " +
                "exceeded for expression $expression",
        )
    }

    override fun whilePossible(init: PipelineFunc) {
        if (!builder.inProgress) return

        whilePossible(nextStepsProducer())
    }

    private data class DeeplyCacheKey(val stepsProducer: StepsProducer)

    override fun deeply(stepsProducer: StepsProducer, deepFirst: Boolean) {
        if (!builder.inProgress) return

        val cacheKey = DeeplyCacheKey(stepsProducer)

        // visitPrefix and visitPostfix are only defined in the branches so that the closures only get created when
        // necessary.
        val steps = if (!deepFirst) {
            fun visitPrefix(sub: Expression): List<Transformation>? =
                builder.context.unlessPreviouslyFailed(cacheKey, sub) {
                    if (sub.depth < stepsProducer.minDepth) {
                        null
                    } else {
                        stepsProducer.produceSteps(ctx, sub)
                            ?: sub.childrenInVisitingOrder().firstNotNullOfOrNull { visitPrefix(it) }
                    }
                }
            visitPrefix(builder.simpleExpression)
        } else {
            fun visitPostfix(sub: Expression): List<Transformation>? =
                builder.context.unlessPreviouslyFailed(cacheKey, sub) {
                    if (sub.depth < stepsProducer.minDepth) {
                        null
                    } else {
                        sub.childrenInVisitingOrder().firstNotNullOfOrNull { visitPostfix(it) }
                            ?: stepsProducer.produceSteps(ctx, sub)
                    }
                }
            visitPostfix(builder.simpleExpression)
        }

        addSteps(steps)
    }

    override fun deeply(deepFirst: Boolean, init: PipelineFunc) {
        if (!builder.inProgress) return

        deeply(nextStepsProducer(), deepFirst)
    }

    override fun plan(init: PlanBuilder.() -> CompositeMethod) {
        if (!builder.inProgress) return

        addSteps(nextStepsProducer().produceSteps(ctx, expression))
    }

    override fun taskSet(init: TaskSetBuilder.() -> CompositeMethod) {
        if (!builder.inProgress) return

        addSteps(nextStepsProducer().produceSteps(ctx, expression))
    }

    override fun checkForm(patternProvider: () -> Pattern) {
        if (!builder.inProgress) return

        val pattern = patternProvider()
        if (!pattern.matches(ctx, builder.simpleExpression)) {
            builder.abort()
        }
    }
}

/**
 * Type-safe builder to create a [StepsProducer] using the [PipelineBuilder] DSL.
 */
fun steps(init: PipelineFunc): StepsProducer {
    return Pipeline(init)
}

fun stepsWithMinDepth(minDepth: Int, init: PipelineFunc): StepsProducer {
    return Pipeline(init = init, minDepth = minDepth)
}

fun optionalSteps(init: PipelineFunc): StepsProducer {
    return Pipeline(init, optionalSteps = true)
}
