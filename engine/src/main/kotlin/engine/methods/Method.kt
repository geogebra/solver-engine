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

package engine.methods

import engine.context.Context
import engine.expressions.Expression
import engine.methods.stepsproducers.StepsProducer
import engine.steps.Transformation
import java.util.logging.Level
import kotlin.time.measureTimedValue

fun interface Method : StepsProducer {
    fun tryExecute(ctx: Context, sub: Expression): Transformation?

    override fun produceSteps(ctx: Context, sub: Expression): List<Transformation>? =
        tryExecute(ctx, sub)?.let { listOf(it) }
}

fun interface Runner {
    fun run(ctx: Context, sub: Expression): Transformation?

    val minDepth get() = 0
}

interface RunnerMethod : Method {
    val name: String
    val runner: Runner

    override fun tryExecute(ctx: Context, sub: Expression): Transformation? {
        ctx.requireActive()

        if (timeRuns) {
            // ctx.log(Level.FINE) { "$name MINDEPTH $minDepth" }
            ctx.log(Level.FINER) { "-> $name: $sub" }
            val (result, duration) = try {
                ctx.nest()
                measureTimedValue { runner.run(ctx, sub) }
            } finally {
                ctx.unnest()
            }
            if (result == null) {
                ctx.log(Level.FINER) { "<- ${duration.inWholeNanoseconds} $name: FAIL" }
            } else {
                ctx.log(Level.FINE) { "<- ${duration.inWholeNanoseconds} $name: ${result.toExpr}" }
            }
            return result
        } else {
            return runner.run(ctx, sub)
        }
    }

    override val minDepth get() = runner.minDepth

    companion object {
        // This is not a very sound way to express the intent that we should not time runs in productions and we should
        // only time runs in development when set the log level to at least DEBUG.
        val timeRuns = System.getenv("LOG_LEVEL")?.let { it == "TRACE" || it == "DEBUG" } ?: false
    }
}

@Target(AnnotationTarget.FIELD)
annotation class PublicMethod(val hiddenFromList: Boolean = false)
