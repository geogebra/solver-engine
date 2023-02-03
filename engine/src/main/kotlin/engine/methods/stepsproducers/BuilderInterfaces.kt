package engine.methods.stepsproducers

import engine.context.Context
import engine.expressions.Expression
import engine.expressions.Extractor
import engine.methods.PlanBuilder
import engine.methods.TaskSetBuilder
import engine.patterns.Pattern

@DslMarker
annotation class StepsProducerBuilderMarker

@StepsProducerBuilderMarker
interface InStepBuilder {

    /**
     * A single mandatory step.
     */
    fun step(init: InStepStepBuilder.() -> Unit)

    /**
     * A single optional step.
     */
    fun optionalStep(init: InStepStepBuilder.() -> Unit)
}

@StepsProducerBuilderMarker
interface FirstOfBuilder {

    /**
     * Try the option [opt] and stop if it is valid.
     */
    fun option(opt: StepsProducer)

    /**
     * Try the following option and stop here if it is valid.
     */
    fun option(init: PipelineBuilder.() -> Unit)
}

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
    fun optionally(init: PipelineBuilder.() -> Unit)

    /**
     * Wrap a pipeline that uses labels in this.  It makes sures labels are cleared at the end.  This is not a long-term
     * solution but offers some safety while we look for a good solution.
     */
    fun withNewLabels(init: PipelineBuilder.() -> Unit)

    /**
     * Apply the [steps].  The pipeline will fail if the steps can't be applied.
     */
    fun apply(steps: StepsProducer)

    /**
     * Follow the given steps.  The pipeline will fail if the steps can't be followed to completion.
     */
    fun apply(init: PipelineBuilder.() -> Unit)

    fun check(condition: Context.(Expression) -> Boolean)

    /**
     * Apply the [steps] to a subexpression obtained by the [extractor]
     */
    fun applyTo(steps: StepsProducer, extractor: Extractor)

    fun applyTo(extractor: Extractor, init: PipelineBuilder.() -> Unit)

    /**
     * Apply the recipe to the children of the expression in step.
     */
    fun applyToChildrenInStep(init: InStepBuilder.() -> Unit)

    /**
     * Apply the first valid option in the following.  The step fails if no option is valid.
     */
    fun firstOf(init: FirstOfBuilder.() -> Unit)

    /**
     * Apply the [steps] as many times as possible.  They are not required to be applied at least once.
     */
    fun whilePossible(steps: StepsProducer)

    /**
     * Apply the following steps as many times as possible. They are not required to be applied at least once.
     */
    fun whilePossible(init: PipelineBuilder.() -> Unit)

    /**
     * Apply [steps] deeply ([deepFirst] controls whether it is depth first or breadth first).
     */
    fun deeply(steps: StepsProducer, deepFirst: Boolean = false)

    /**
     * Apply the following steps deeply ([deepFirst] controls whether it is depth first or breadth first).
     */
    fun deeply(deepFirst: Boolean = false, init: PipelineBuilder.() -> Unit)

    /**
     * Apply the following plan.
     */
    fun plan(init: PlanBuilder.() -> Unit)

    /**
     * Apply the following task set
     */
    fun taskSet(init: TaskSetBuilder.() -> Unit)

    fun checkForm(patternProvider: () -> Pattern)
    fun contextSensitive(init: ContextSensitiveBuilder.() -> Unit)
}
