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

package methods.general

import engine.methods.CompositeMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.steps
import engine.patterns.AnyPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.absoluteValueOf
import engine.patterns.productContaining
import engine.patterns.stickyOptionalNegOf

enum class GeneralPlans(override val runner: CompositeMethod) : RunnerMethod {
    NormalizeNegativeSignsInProduct(
        plan {
            pattern = stickyOptionalNegOf(productContaining())
            explanation = Explanation.NormalizeNegativeSignsInProduct

            steps {
                whilePossible(GeneralRules.SimplifyProductWithTwoNegativeFactors)
                optionally(GeneralRules.MoveSignOfNegativeFactorOutOfProduct)
            }
        },
    ),
}

fun createEvaluateAbsoluteValuePlan(simplificationSteps: StepsProducer): StepsProducer {
    val evaluateAbsoluteValuePlan = plan {
        explanation = Explanation.EvaluateAbsoluteValue
        pattern = absoluteValueOf(AnyPattern())

        steps {
            whilePossible {
                applyTo(simplificationSteps) { it.firstChild }
            }
            apply(resolveAbsoluteValuesSteps)
        }
    }

    // wrapped in a firstOf, so that it resolves the absolute value of simple numbers
    // without a wrapping step
    return steps {
        firstOf {
            option {
                checkForm { absoluteValueOf(SignedNumberPattern()) }
                apply(resolveAbsoluteValuesSteps)
            }
            option(evaluateAbsoluteValuePlan)
        }
    }
}

private val resolveAbsoluteValuesSteps = steps {
    firstOf {
        option(GeneralRules.ResolveAbsoluteValueOfZero)
        option(GeneralRules.ResolveAbsoluteValueOfNonNegativeValue)
        option {
            optionally {
                applyTo(GeneralRules.FactorMinusFromSumWithAllNegativeTerms) { it.firstChild }
            }
            firstOf {
                option(GeneralRules.ResolveAbsoluteValueOfNonPositiveValue)
                option(GeneralRules.SimplifyAbsoluteValueOfNegatedExpression)
            }
        }
    }
}
