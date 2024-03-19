/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package engine.methods.stepsproducers

import engine.context.Context
import engine.context.Setting
import engine.context.SettingValue
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
typealias BranchOnFunc = BranchOnBuilder.() -> Unit

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
 * This interface defines the actions that can be performed in a [BranchOn] StepsProducer.
 */
@StepsProducerBuilderMarker
interface BranchOnBuilder {
    fun case(value: SettingValue, opt: StepsProducer)

    fun case(value: SettingValue, init: PipelineFunc)
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
     * Optionally apply [steps] defined as a StepsProducer  If the step producer does not apply to the input, then it is
     * simply skipped and the next action in the pipeline is tried on the input.
     */
    fun optionally(steps: StepsProducer)

    /**
     * Optionally the steps defined by [init].  If the steps do not apply to the input, then they ar simply skipped and
     * the next action in the pipeline is tried on the input.
     */
    fun optionally(init: PipelineFunc)

    /**
     * Try applying [steps] defined as a StepsProducer.  If it is a success, then the whole pipeline ends
     * with success (further steps are not attempted).
     */
    fun shortcut(steps: StepsProducer)

    /**
     * Try applying the steps defined by [init].  If it is a success, then the whole pipeline ends
     * with success (further steps are not attempted).
     */
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

    /**
     * Check [condition].  If not fulfilled, then the pipeline fails.
     */
    fun check(condition: Context.(Expression) -> Boolean)

    /**
     * Apply the [stepsProducer] to a subexpression obtained by the [extractor].  The whole pipeline fails if the
     * extractor returns null.
     */
    fun applyTo(stepsProducer: StepsProducer, extractor: Extractor<Expression>)

    /**
     * Apply the [stepsProducer] to the subexpression with the given [label].  Fail if there is no such label.
     */
    fun applyTo(stepsProducer: StepsProducer, label: Label)

    /**
     * Apply the [stepsProducer] to a subexpression obtained by the [extractor], only if the subexpression has type [T].
     */
    fun <T : Expression> applyToKind(stepsProducer: StepsProducer, extractor: Extractor<T>)

    /**
     * Apply the pipeline defined by [init] to the subexpression obtained by the [extractor].
     */
    fun applyTo(extractor: Extractor<Expression>, init: PipelineFunc)

    /**
     * Apply the pipeline defined by [init] to the subexpression with the given [label].  Fail if there is no such
     * label.
     */
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

    /**
     * Apply [stepsProducer] to the current constraint, not expression.  Fail if there is no constraint.
     */
    fun applyToConstraint(stepsProducer: StepsProducer)

    /**
     * Apply the first valid option in the following.  The step fails if no option is valid.
     */
    fun firstOf(init: FirstOfFunc)

    /**
     * Try different steps depending on the value of [setting] (see [BranchOnBuilder] for details)
     */
    fun branchOn(setting: Setting, init: BranchOnFunc)

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
     * Apply the pipeline defined by [init] deeply ([deepFirst] controls whether it is depth first or breadth first).
     * This means that the steps are attempted recursively to child expressions.  Return successfully once one
     * subexpression has been transformed.
     */
    fun deeply(deepFirst: Boolean = false, init: PipelineFunc)

    /**
     * Apply the following plan.  The whole pipeline fails if the plan fails.
     */
    fun plan(init: PlanBuilder.() -> CompositeMethod)

    /**
     * Apply the following task set.  The whole pipeline fails if the task set fails.
     */
    fun taskSet(init: TaskSetBuilder.() -> CompositeMethod)

    /**
     * Check that the pattern returned by [patternProvider] is matched by the current expression.  The whole pipeline
     * fails if this is not the case.
     */
    fun checkForm(patternProvider: () -> Pattern)

    /**
     * Apply the pipeline defined by [init] in a new context defined by [contextFactory] (which is called on the
     * current context with the current expression as its sole argument.
     */
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
