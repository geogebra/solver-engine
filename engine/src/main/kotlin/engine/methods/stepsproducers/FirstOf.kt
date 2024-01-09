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
import engine.expressions.Expression
import engine.steps.Transformation
import kotlin.concurrent.Volatile

/**
 * Implements the firstOf [StepsProducer] from [init].  It uses a [FirstOfCompiler] to prepare the
 * options and calculate its minDepth, then uses a [FirstOfRunner] to execute the firstOf.
 */
private class FirstOf(val init: FirstOfFunc) : StepsProducer {
    // These vars may be accessed from different request threads, so we want reads and writes to be atomic.  It doesn't
    // matter if two threads call initialize() because they will calculate the same value.  That wastes a little time
    // but that is more than offset by avoiding the slowdown that putting a lock around initialize() and accessing
    // stepsProducers and minDepthValue would induce.

    @Volatile
    private lateinit var stepsProducers: List<StepsProducer>

    @Volatile
    private var minDepthValue = -1

    private fun initialize() {
        val compiler = FirstOfCompiler()
        compiler.init()
        stepsProducers = compiler.getStepsProducers()
        minDepthValue = compiler.getMindDepth()
    }

    override val minDepth: Int get() {
        if (minDepthValue < 0) {
            initialize()
        }
        return minDepthValue
    }

    override fun produceSteps(ctx: Context, sub: Expression): List<Transformation>? {
        if (!::stepsProducers.isInitialized) {
            initialize()
        }
        val runner = FirstOfRunner(stepsProducers, sub, ctx)
        runner.init()
        return runner.steps
    }
}

/**
 * A [FirstOfBuilder] whose purpose is to compile the pipeline, i.e. precalculate options from their specification and
 * precalculate the minDepth of a firstOf.
 */
private class FirstOfCompiler : FirstOfBuilder {
    private val stepsProducers = mutableListOf<StepsProducer>()
    private var minDepth = Int.MAX_VALUE

    fun getStepsProducers() = stepsProducers.toList()

    fun getMindDepth() = minDepth

    private fun registerOption(opt: StepsProducer) {
        minDepth = minOf(minDepth, opt.minDepth)
    }

    private fun registerOption(init: PipelineFunc) {
        val stepsProducer = steps(init)
        stepsProducers.add(stepsProducer)
        registerOption(stepsProducer)
    }

    override fun option(opt: StepsProducer) {
        registerOption(opt)
    }

    override fun option(init: PipelineFunc) {
        registerOption(init)
    }

    override fun shortOption(opt: StepsProducer) {
        registerOption(opt)
    }

    override fun shortOption(init: PipelineFunc) {
        registerOption(init)
    }

    override fun <T> optionsFor(
        sequenceGenerator: (Expression) -> Iterable<T>,
        optionGenerator: PipelineBuilder.(T) -> Unit,
    ) {
        // It is not possible to compile this at the moment because [sequenceGenerator] depends on the input.
        // This is probably an antipattern and we should fix it but I don't know how yet.
        minDepth = 0
    }
}

/**
 * A [FirstOfBuilder] that executes the pipeline. [stepsProducers] is the list of [StepsProducer] instances
 * precalculated by the [FirstOfCompiler].
 */
private class FirstOfRunner(
    val stepsProducers: List<StepsProducer>,
    val sub: Expression,
    val ctx: Context,
) : FirstOfBuilder {
    var steps: List<Transformation>? = null
    private var index = 0

    private fun nextStepsProducer(): StepsProducer {
        val next = stepsProducers[index]
        index++
        return next
    }

    override fun option(opt: StepsProducer) {
        if (steps == null) {
            val currentSteps = opt.produceSteps(ctx, sub)
            if (currentSteps != null) {
                steps = currentSteps
            }
        }
    }

    override fun option(init: PipelineFunc) {
        if (steps == null) {
            option(nextStepsProducer())
        }
    }

    override fun shortOption(opt: StepsProducer) {
        val currentSteps = opt.produceSteps(ctx, sub)
        val steps = steps
        if (steps == null || currentSteps != null && steps.last().toExpr == currentSteps.last().toExpr) {
            this.steps = currentSteps
        }
    }

    override fun shortOption(init: PipelineFunc) {
        shortOption(nextStepsProducer())
    }

    override fun <T> optionsFor(
        sequenceGenerator: (Expression) -> Iterable<T>,
        optionGenerator: PipelineBuilder.(T) -> Unit,
    ) {
        if (steps != null) return

        val sequence = sequenceGenerator(sub)
        for (elem in sequence) {
            val currentSteps = steps { optionGenerator(elem) }.produceSteps(ctx, sub)
            if (currentSteps != null) {
                steps = currentSteps
                break
            }
        }
    }
}

/**
 * Type-safe builder to create a [StepsProducer] using the [PipelineBuilder] DSL.
 */
fun firstOf(init: FirstOfFunc): StepsProducer = FirstOf(init)
