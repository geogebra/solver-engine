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
import engine.expressions.Combine
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.ExpressionWithConstraint
import engine.methods.stepsproducers.PipelineFunc
import engine.methods.stepsproducers.StepsProducer
import engine.patterns.NaryPattern
import engine.patterns.Pattern
import engine.patterns.RootMatch
import engine.steps.Transformation
import engine.steps.metadata.MetadataMaker

/**
 * A `Plan` is a `Method` with a non-empty set of steps which are produced by a `StepsProducer`.
 */
class Plan(
    private val pattern: Pattern,
    private val resultPattern: Pattern,
    private val explanationMaker: MetadataMaker,
    private val skillMakers: List<MetadataMaker>? = null,
    specificPlans: List<Method> = emptyList(),
    private val stepsProducer: StepsProducer,
) : CompositeMethod(specificPlans) {
    override fun run(ctx: Context, sub: Expression) = ctx.unlessPreviouslyFailed(this, sub) { doRun(ctx, sub) }
    // To disable cache, do this instead
    //  doRun(ctx, sub)

    private fun doRun(ctx: Context, sub: Expression): Transformation? {
        val expression = if (sub is ExpressionWithConstraint) sub.expression else sub
        val match = pattern.findMatches(ctx, RootMatch, expression).firstOrNull() ?: return null

        return stepsProducer.produceSteps(ctx, sub)?.let { steps ->
            val toExpr = steps.last().toExpr.withOrigin(Combine(listOf(sub)))

            when {
                toExpr == Constants.Undefined || resultPattern.matches(ctx, toExpr) -> Transformation(
                    type = Transformation.Type.Plan,
                    fromExpr = sub,
                    toExpr = toExpr,
                    steps = steps,
                    explanation = explanationMaker.make(ctx, sub, match),
                    skills = skillMakers?.map { it.make(ctx, sub, match) },
                )

                else -> null
            }
        }
    }

    override val minDepth get() = maxOf(pattern.minDepth, stepsProducer.minDepth)
}

class PlanBuilder : CompositeMethodBuilder() {
    private lateinit var defaultSteps: StepsProducer

    private fun checkNotInitialized() {
        check(!::defaultSteps.isInitialized)
    }

    fun partialExpressionSteps(init: PipelineFunc): PartialExpressionPlan {
        checkNotInitialized()
        require(pattern is NaryPattern)
        return PartialExpressionPlan(
            pattern = pattern as NaryPattern,
            stepsProducer = engine.methods.stepsproducers.steps(init),
            explanationMaker = explanationMaker,
            skillMakers = skillMakers,
            specificPlans = specificPlans,
        )
    }

    fun steps(init: PipelineFunc): Plan {
        checkNotInitialized()
        return Plan(
            pattern = pattern,
            resultPattern = resultPattern,
            stepsProducer = engine.methods.stepsproducers.steps(init),
            explanationMaker = explanationMaker,
            skillMakers = skillMakers,
            specificPlans = specificPlans,
        )
    }
}

/**
 * Type-safe builder to create [CompositeMethod] instance using the [PlanBuilder] DSL.
 */
fun plan(init: PlanBuilder.() -> CompositeMethod): CompositeMethod {
    val planBuilder = PlanBuilder()
    return planBuilder.init()
}
