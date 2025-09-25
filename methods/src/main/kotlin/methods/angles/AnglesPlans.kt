/*
 * Copyright (c) 2025 GeoGebra GmbH, office@geogebra.org
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

package methods.angles

import engine.expressions.Constants.Pi
import engine.expressions.Expression
import engine.expressions.Minus
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.patterns.AnyPattern
import engine.patterns.ConstantPattern
import engine.patterns.FixedPattern
import engine.patterns.TrigonometricExpressionPattern
import engine.patterns.degreeOf
import engine.patterns.withOptionalRationalCoefficient
import methods.constantexpressions.ConstantExpressionsPlans
import methods.simplify.SimplifyPlans

enum class AnglesPlans(override val runner: CompositeMethod) : RunnerMethod {
    /**
     * Try to convert an angle from degrees to radians.
     * - Use degree conversion formula
     * - Simplify the resulting expression
     */
    @PublicMethod
    ConvertDegreesToRadians(
        plan {
            pattern = degreeOf(ConstantPattern())

            explanation = Explanation.ConvertDegreesToRadians

            steps {
                apply(AnglesRules.UseDegreeConversionFormula)
                apply(ConstantExpressionsPlans.SimplifyConstantExpression)
            }
        },
    ),

    /**
     * Try to convert an angle from radians to degrees.
     * - Use radian conversion formula
     * - Simplify the resulting expression
     */
    @PublicMethod
    ConvertRadiansToDegrees(
        plan {
            pattern = withOptionalRationalCoefficient(FixedPattern(Pi), false)

            explanation = Explanation.ConvertRadiansToDegrees

            steps {
                apply(AnglesRules.UseRadianConversionFormula)
                apply(ConstantExpressionsPlans.SimplifyConstantExpression)
            }
        },
    ),

    /**
     * Try to reduce the argument of a trigonometric expression to a main angle by:
     * - IF POSSIBLE: reducing it to the first quadrant
     * - IF POSSIBLE: simplifying the resulting argument
     * - evaluating the exact value of the resulting main angle
     */
    EvaluateExactValuesOfMainAngle(
        plan {
            pattern = TrigonometricExpressionPattern(AnyPattern())

            explanation = Explanation.EvaluateExactValueOfMainAngle

            val reductionSteps = engine.methods.stepsproducers.steps {
                firstOf {
                    option(AnglesRules.FindReferenceAngleInFirstQuadrantInDegree)
                    option(AnglesRules.FindReferenceAngleInFirstQuadrantInRadian)
                }
                optionally {
                    applyTo(SimplifyPlans.SimplifyAlgebraicExpression) {
                        getTrigonometricFunctionFromOptionalNegative(it).firstChild
                    }
                }
                optionally {
                    applyTo(ConstantExpressionsPlans.SimplifyConstantExpression) {
                        getTrigonometricFunctionFromOptionalNegative(it).firstChild
                    }
                }
            }

            steps {
                optionally {
                    apply(reductionSteps)
                }
                applyTo(AnglesRules.EvaluateExactValueOfMainAngle) { getTrigonometricFunctionFromOptionalNegative(it) }
            }
        },
    ),

    /**
     * Evaluate a trigonometric expression.
     * - we try to simplify the expression
     * ┌ IF it can be evaluated as a main angle, we evaluate it exactly.
     * └ ELSE we try to reduce it to a main angle and then evaluate it.
     */
    @PublicMethod
    EvaluateTrigonometricExpression(
        plan {
            pattern = TrigonometricExpressionPattern(AnyPattern())

            explanation = Explanation.EvaluateTrigonometricExpression

            steps {
                optionally { apply(ConstantExpressionsPlans.SimplifyConstantExpression) }

                shortcut(AnglesRules.EvaluateExactValueOfMainAngle)

                apply(EvaluateExactValuesOfMainAngle)
            }
        },
    ),
}

private fun getTrigonometricFunctionFromOptionalNegative(expression: Expression) =
    if (expression is Minus) expression.firstChild else expression
