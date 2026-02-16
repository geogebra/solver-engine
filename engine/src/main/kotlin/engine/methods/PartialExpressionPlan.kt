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
import engine.expressions.Child
import engine.expressions.Decorator
import engine.expressions.Expression
import engine.methods.stepsproducers.StepsBuilder
import engine.methods.stepsproducers.StepsProducer
import engine.operators.ProductOperator
import engine.operators.SumOperator
import engine.patterns.AnyPattern
import engine.patterns.Match
import engine.patterns.NaryPattern
import engine.patterns.RootMatch
import engine.steps.Transformation
import engine.steps.metadata.MetadataMaker
import engine.steps.metadata.metadata

class PartialExpressionPlan(
    val pattern: NaryPattern,
    val explanationMaker: MetadataMaker,
    val skillMakers: List<MetadataMaker>?,
    specificPlans: List<Method> = emptyList(),
    val stepsProducer: StepsProducer,
) : CompositeMethod(specificPlans) {
    private val regularPlan = Plan(pattern, AnyPattern(), explanationMaker, skillMakers, specificPlans, stepsProducer)

    override fun run(ctx: Context, sub: Expression): Transformation? {
        if (sub.childCount == pattern.childPatterns.size) {
            return regularPlan.run(ctx, sub)
        }

        for (match in pattern.findMatches(ctx, RootMatch, sub)) {
            val partialExpression = pattern.extract(match)
            val substitutedPartialExpression = pattern.substitute(
                match,
                arrayOf(partialExpression.decorate(Decorator.PartialBracket)),
            )

            val builder = StepsBuilder(ctx, sub)

            builder.addStep(
                if (matchedTermsAreNextToEachOther(pattern, match)) {
                    Transformation(
                        fromExpr = sub,
                        toExpr = substitutedPartialExpression,
                        explanation = metadata(SolverEngineExplanation.ExtractPartialExpression),
                        type = Transformation.Type.Rule,
                        tags = listOf(Transformation.Tag.InvisibleChange),
                    )
                } else {
                    Transformation(
                        fromExpr = sub,
                        toExpr = substitutedPartialExpression,
                        explanation = when (pattern.operator) {
                            is ProductOperator -> metadata(SolverEngineExplanation.RearrangeProduct, partialExpression)
                            is SumOperator -> metadata(SolverEngineExplanation.RearrangeSum, partialExpression)
                        },
                        type = Transformation.Type.Rule,
                        tags = listOf(Transformation.Tag.Rearrangement),
                    )
                },
            )

            // Sometimes we substitute the expression into the end of the expression, so just looking at the
            // original indices is not a good solution. It's better to just scan through the children and get the one
            // that is partial
            val expression = builder.simpleExpression
            val partialSubExpression = expression.children.first {
                it.decorators.contains(Decorator.PartialBracket)
            }

            val steps = stepsProducer.produceSteps(ctx, partialSubExpression)

            if (steps != null) {
                builder.addSteps(steps)

                return Transformation(
                    fromExpr = sub,
                    toExpr = builder.expression,
                    steps = builder.getFinalSteps(),
                    explanation = explanationMaker.make(ctx, sub, match),
                    skills = skillMakers?.map { it.make(ctx, sub, match) },
                    type = Transformation.Type.Plan,
                )
            }
        }

        return null
    }

    override val minDepth get() = maxOf(pattern.minDepth, stepsProducer.minDepth)
}

private fun matchedTermsAreNextToEachOther(pattern: NaryPattern, match: Match): Boolean {
    val indices = pattern.getMatchedOrigins(match).map { (it as Child).index }.sorted()
    return indices == (indices.first()..indices.last()).toList()
}
