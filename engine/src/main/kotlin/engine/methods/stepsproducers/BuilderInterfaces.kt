package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Expression
import engine.expressions.Extractor
import engine.expressions.Label
import engine.expressions.Minus
import engine.expressions.Product
import engine.methods.CompositeMethod
import engine.methods.PlanBuilder
import engine.methods.Strategy
import engine.methods.TaskSetBuilder
import engine.patterns.Pattern

@DslMarker
annotation class StepsProducerBuilderMarker

typealias PipelineFunc = PipelineBuilder.() -> Unit
typealias FirstOfFunc = FirstOfBuilder.() -> Unit

/**
 * This interface defines the actions that can be performed in a [FirstOf] StepsProducer.  Actions are tried in order
 * until one action succeeds.  If all actions fail then the whole StepsProducer fails.
 */
@StepsProducerBuilderMarker
interface FirstOfBuilder {

    /**
     * Try the option [opt] and stop if it is valid.
     */
    fun option(opt: StepsProducer)

    /**
     * Try the following option and stop here if it is valid.
     */
    fun option(init: PipelineFunc)

    /**
     * Overrides the previous option if it produces the same result.
     */
    fun shortOption(opt: StepsProducer)

    /**
     * Overrides the previous option if it produces the same result.
     */
    fun shortOption(init: PipelineFunc)

    /**
     * Generates options using the [optionGenerator] from the sequence produced by the [sequenceGenerator]
     */
    fun <T> optionsFor(sequenceGenerator: (Expression) -> Iterable<T>, optionGenerator: PipelineBuilder.(T) -> Unit)
}

/**
 * This interface describes the actions that can be performed in a [Pipeline] StepsProducer. The actions are performed
 * in order (as a pipeline), the output of the previous one being the input of the next one. Some actions are optional
 * and if they fail the pipeline continue, whereas non-optional actions must succeed otherwise the pipeline aborts
 */
@Suppress("TooManyFunctions")
@StepsProducerBuilderMarker
interface PipelineBuilder {

    /**
     * Optionally apply steps
     */
    fun optionally(steps: StepsProducer)

    /**
     * Optionally follow those steps
     */
    fun optionally(init: PipelineFunc)

    fun shortcut(steps: StepsProducer)

    fun shortcut(init: PipelineFunc)

    /**
     * Wrap a pipeline that uses labels in this.  It makes sures labels are cleared at the end.  This is not a long-term
     * solution but offers some safety while we look for a good solution.
     */
    fun withNewLabels(init: PipelineFunc)

    /**
     * Apply the [steps].  The pipeline will fail if the steps can't be applied.
     */
    fun apply(steps: StepsProducer)

    /**
     * Follow the given steps.  The pipeline will fail if the steps can't be followed to completion.
     */
    fun apply(init: PipelineFunc)

    fun check(condition: Context.(Expression) -> Boolean)

    /**
     * Apply the [stepsProducer] to a subexpression obtained by the [extractor]
     */
    fun applyTo(stepsProducer: StepsProducer, extractor: Extractor<Expression>)

    fun applyTo(stepsProducer: StepsProducer, label: Label)

    fun <T : Expression> applyToKind(stepsProducer: StepsProducer, extractor: Extractor<T>)

    fun applyTo(extractor: Extractor<Expression>, init: PipelineFunc)

    fun applyTo(label: Label, init: PipelineFunc)

    /**
     * Apply the [stepsProducer] to all children of the working expression in turn,
     * but not to the working expression itself. By default, it never fails (i.e. [stepsProducer] is optionally
     * applied to all children). The [atLeastOne] flags controls whether it must apply to at least one child, and the
     * [all] flag controls whether it must apply to all children.
     */
    fun applyToChildren(stepsProducer: StepsProducer, all: Boolean = false, atLeastOne: Boolean = false)

    /**
     * Apply the pipeline defined by [init] to all children of the working expression in turn,
     * but not to the working expression itself. By default, it never fails (i.e. the pipeline is optionally
     * applied to all children). The [atLeastOne] flags controls whether it must apply to at least one child, and the
     * [all] flag controls whether it must apply to all children.
     */
    fun applyToChildren(all: Boolean = false, atLeastOne: Boolean = false, init: PipelineFunc)

    fun applyToConstraint(stepsProducer: StepsProducer)

    /**
     * Apply the first valid option in the following.  The step fails if no option is valid.
     */
    fun firstOf(init: FirstOfFunc)

    /**
     * Apply the [stepsProducer] as many times as possible.  They are not required to be applied at least once.
     */
    fun whilePossible(stepsProducer: StepsProducer)

    /**
     * Apply the following steps as many times as possible. They are not required to be applied at least once.
     */
    fun whilePossible(init: PipelineFunc)

    /**
     * Apply [steps] deeply ([deepFirst] controls whether it is depth first or breadth first).
     */
    fun deeply(stepsProducer: StepsProducer, deepFirst: Boolean = false)

    /**
     * Apply the following steps deeply ([deepFirst] controls whether it is depth first or breadth first).
     */
    fun deeply(deepFirst: Boolean = false, init: PipelineFunc)

    /**
     * Apply the following plan.
     */
    fun plan(init: PlanBuilder.() -> Unit)

    /**
     * Apply the following task set
     */
    fun taskSet(init: TaskSetBuilder.() -> CompositeMethod)

    fun checkForm(patternProvider: () -> Pattern)
    fun contextSensitive(init: ContextSensitiveBuilder.() -> Unit)

    fun inContext(contextFactory: Context.(Expression) -> Context, init: PipelineFunc)
}

fun PipelineBuilder.applyAfterMaybeExtractingMinus(init: PipelineFunc) {
    applyTo(extractor = { if (it is Minus) it.argument else it }, init)
}

fun PipelineBuilder.applyToFactors(steps: StepsProducer) {
    firstOf {
        option {
            check { it is Product }
            applyToChildren(steps, all = false, atLeastOne = true)
        }
        option(steps)
    }
}

/**
 * This interface describes the actions that can be performed in a [WhileStrategiesAvailableFirstOf] StepsProducer.
 * It is made specially to apply strategies (see equation solving for an example).
 */
@StepsProducerBuilderMarker
interface WhileStrategiesAvailableFirstOfBuilder {
    fun option(strategy: Strategy)

    fun option(init: PipelineFunc)

    fun option(stepsProducer: StepsProducer)

    fun fallback(strategy: Strategy)
}
