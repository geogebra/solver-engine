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

import engine.expressions.Constants.Two
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.operators.TrigonometricFunctionType
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.TrigonometricExpressionPattern
import engine.patterns.commutativeSumContaining
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumOf
import methods.factor.FactorRules

/**
 * Try to apply the pythagorean identity to an expressions, in this case we should try factoring as well in case
 * both terms are multiplied by the same coefficient.
 * - we try to apply the pythagorean identity
 * ┌ IF it can be applied, we return the result
 * │    - [sin^2]\[x\] + [cos^2]\[x\] --> 1
 * └ ELSE we try to factor the expression and apply the identity to the result
 *      - k [sin ^ 2]\[x\] + k [cos ^ 2]\[x\] --> k (sin^2)\[x\] + k (cos^2)\[x\] --> k (1) --> k
 */
fun createUsePythagoreanIdentityAndSimplifyPlan(simplificationSteps: StepsProducer): StepsProducer {
    return plan {
        val argument = AnyPattern()
        val exponent = FixedPattern(Two)
        val sine =
            powerOf(TrigonometricExpressionPattern(argument, listOf(TrigonometricFunctionType.Sin)), exponent)
        val cosine =
            powerOf(TrigonometricExpressionPattern(argument, listOf(TrigonometricFunctionType.Cos)), exponent)

        pattern = oneOf(
            commutativeSumContaining(sine, cosine),
            commutativeSumContaining(productContaining(sine), productContaining(cosine)),
        )

        explanation = Explanation.ApplyPythagoreanIdentityAndSimplify

        steps {
            shortcut(TrigonometricFunctionsRules.ApplyPythagoreanIdentity)

            applyTo(FactorRules.FactorCommonFactor) {
                it
            }
            applyTo(TrigonometricFunctionsRules.ApplyPythagoreanIdentity) {
                it.secondChild
            }
            whilePossible(simplificationSteps)
        }
    }
}

/**
 * - Apply trigonometric identity
 * - Simplify resulting expression using the provided simplification rules
 */
fun createUseTrigonometricIdentityAndSimplifyPlan(simplificationSteps: StepsProducer): StepsProducer {
    return plan {
        pattern = TrigonometricExpressionPattern(
            sumOf(AnyPattern(), AnyPattern()),
            listOf(
                TrigonometricFunctionType.Sin,
                TrigonometricFunctionType.Cos,
                TrigonometricFunctionType.Tan,
            ),
        )

        explanation = Explanation.ApplyTrigonometricIdentityAndSimplify

        steps {
            optionally(TrigonometricFunctionsRules.RearrangeAddendsInArgument)
            firstOf {
                option(TrigonometricFunctionsRules.ApplyCosineIdentity)
                option(TrigonometricFunctionsRules.ApplySineIdentity)
                option(TrigonometricFunctionsRules.ApplyTangentIdentity)
            }
            whilePossible(simplificationSteps)
        }
    }
}

/**
 * - Evaluate inverse trigonometric function exactly
 * - OPTIONALLY: Simplify the result (in case of negative values, the result may be in the form /pi/ - x)
 */
fun createEvaluateInverseTrigonometricFunctionExactlyPlan(simplificationSteps: StepsProducer): StepsProducer {
    return plan {
        pattern = TrigonometricExpressionPattern(
            AnyPattern(),
            listOf(
                TrigonometricFunctionType.Arcsin,
                TrigonometricFunctionType.Arccos,
                TrigonometricFunctionType.Arctan,
                TrigonometricFunctionType.Arccot,
            ),
        )

        explanation = Explanation.DetermineMainAnglePrincipalValueOfInverseFunction

        steps {
            apply(AnglesRules.EvaluateInverseFunctionOfMainAngle)
            optionally(simplificationSteps)
        }
    }
}
