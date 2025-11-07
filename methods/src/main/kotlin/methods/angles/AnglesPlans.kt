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
import engine.expressions.Product
import engine.expressions.containsExpression
import engine.expressions.containsTrigExpression
import engine.expressions.containsUnits
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.steps
import engine.operators.UnitType
import engine.patterns.AnyPattern
import engine.patterns.ConstantPattern
import engine.patterns.FixedPattern
import engine.patterns.TrigonometricExpressionPattern
import engine.patterns.UnsignedNumberPattern
import engine.patterns.condition
import engine.patterns.degreeOf
import engine.patterns.oneOf
import engine.patterns.withOptionalRationalCoefficient
import methods.fractionarithmetic.FractionArithmeticPlans
import methods.units.UnitsRules

private fun getTrigonometricFunctionFromOptionalNegative(expression: Expression) =
    if (expression is Minus) expression.firstChild else expression

enum class AnglesPlans(override val runner: CompositeMethod) : RunnerMethod {
    /**
     * Normalize angles to be within the unit circle.
     *  Apply one of:
     * ┌ 1) Extract multiples of 360° from angle in degrees.
     * └ 2) Extract multiples of 2π from angle in radians
     * - Cancel out the extracted circles from the expression
     */
    ReduceAngleToUnitCircle(
        plan {
            val degreePattern = degreeOf(UnsignedNumberPattern())
            val radianPattern = withOptionalRationalCoefficient(FixedPattern(Pi))

            val anglePattern = oneOf(degreePattern, radianPattern)

            pattern = anglePattern

            explanation = Explanation.NormalizeAngles

            steps {
                firstOf {
                    option(AnglesRules.RewriteAngleInDegreesByExtractingMultiplesOf360)
                    option(AnglesRules.RewriteAngleInRadiansByExtractingMultiplesOfTwoPi)
                }
                apply(AnglesRules.SubstituteAngleWithCoterminalAngleFromUnitCircle)
            }
        },
    ),

    /**
     * Try to convert an expression containing angles in degrees and radians to a single unit.
     * - Try to convert each subexpression deeply
     */
    ConvertExpressionWithMixedUnitsToRadians(
        plan {
            pattern = condition { it.containsUnits(UnitType.Degree) && it.containsExpression(Pi) }

            explanation = Explanation.ConvertExpressionWithMixedUnitsToRadians

            steps {
                applyToChildren(atLeastOne = true) {
                    deeply { apply(ConvertDegreesToRadians) }
                }
            }
        },
    ),

    /**
     * Try to convert an angle from degrees to radians.
     * - Use degree conversion formula
     * - Simplify the resulting expression
     */
    @PublicMethod
    ConvertDegreesToRadians(
        plan {
            pattern = withOptionalRationalCoefficient(degreeOf(ConstantPattern()))

            explanation = Explanation.ConvertDegreesToRadians

            steps {
                optionally { apply(ReduceAngleToUnitCircle) }
                apply(AnglesRules.UseDegreeConversionFormula)
                // In case the angle is decimal, simplify it as fraction
                optionally {
                    applyTo(UnitsRules.ConvertTerminatingDecimalWithUnitToFraction) {
                        it.firstChild
                    }
                    applyTo(FractionArithmeticPlans.SimplifyFraction) {
                        it.firstChild
                    }
                }
                apply(FractionArithmeticPlans.MultiplyAndSimplifyFractions)
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
                optionally { apply(ReduceAngleToUnitCircle) }
                apply(AnglesRules.UseRadianConversionFormula)
                apply(FractionArithmeticPlans.MultiplyAndSimplifyFractions)
                optionally(UnitsRules.EvaluateUnitProductAndDivision)
            }
        },
    ),
}

/**
 * Try to reduce the argument of a trigonometric expression to a main angle by:
 * - IF POSSIBLE: reducing it to the first quadrant
 * - IF POSSIBLE: simplifying the resulting argument
 * - evaluating the exact value of the resulting main angle
 */
fun createEvaluateExactValuesOfMainAnglePlan(simplificationSteps: StepsProducer): StepsProducer {
    return plan {
        pattern = TrigonometricExpressionPattern(AnyPattern())

        explanation = Explanation.EvaluateExactValueOfMainAngle

        val reductionSteps = engine.methods.stepsproducers.steps {
            firstOf {
                option(AnglesRules.FindReferenceAngleInFirstQuadrantInDegree)
                option(AnglesRules.FindReferenceAngleInFirstQuadrantInRadian)
            }
            applyTo(simplificationSteps) {
                getTrigonometricFunctionFromOptionalNegative(it).firstChild
            }
        }

        steps {
            optionally {
                apply(reductionSteps)
            }
            applyTo(AnglesRules.EvaluateExactValueOfMainAngle) { getTrigonometricFunctionFromOptionalNegative(it) }
        }
    }
}

/**
 * Evaluate a trigonometric expression.
 * - we try to simplify the expression
 * ┌ IF it can be evaluated as a main angle, we evaluate it exactly.
 * └ ELSE we try to reduce it to a main angle and then evaluate it.
 */
fun createEvaluateTrigonometricExpressionPlan(simplificationSteps: StepsProducer): StepsProducer {
    val exactAngleEvaluationPlan = createEvaluateExactValuesOfMainAnglePlan(simplificationSteps)

    return plan {
        pattern = TrigonometricExpressionPattern(AnyPattern())

        explanation = Explanation.EvaluateTrigonometricExpression

        steps {
            shortcut(AnglesRules.EvaluateExactValueOfMainAngle)

            firstOf {
                option(exactAngleEvaluationPlan)
                option {
                    apply(AnglesRules.DeriveTrigonometricFunctionFromPrimitiveFunctions)
                    applyToChildren(exactAngleEvaluationPlan, atLeastOne = true)
                }
            }
        }
    }
}

/**
 * - Check if any of the functions contained in the product are undefined
 * - Check if the product contains zero
 */
val simplifyProductContainingTrigonometricExpressions = steps {
    check { it is Product && it.containsTrigExpression() }
    firstOf {
        option { deeply(AnglesRules.CheckDomainOfFunction) }
        option(AnglesRules.EvaluateProductContainingTrigonometricExpressionsAsZero)
    }
}
