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

package methods.fractionroots

import engine.expressions.Fraction
import engine.expressions.containsRoots
import engine.methods.CompositeMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.patterns.condition
import methods.fractionarithmetic.FractionArithmeticRules
import methods.general.GeneralRules
import methods.integerarithmetic.IntegerArithmeticPlans
import methods.integerarithmetic.IntegerArithmeticRules
import methods.integerroots.IntegerRootsPlans
import methods.integerroots.IntegerRootsRules
import methods.integerroots.cancelRootOfPower

enum class FractionRootsPlans(override val runner: CompositeMethod) : RunnerMethod {
    RationalizeHigherOrderRoot(
        plan {
            explanation = Explanation.RationalizeHigherOrderRoot

            steps {
                apply(FractionRootsRules.HigherOrderRationalizingTerm)
                plan {
                    explanation = Explanation.SimplifyRationalizingTerm

                    steps {
                        whilePossible { deeply(IntegerArithmeticRules.EvaluateSignedIntegerAddition) }
                        whilePossible { deeply(GeneralRules.SimplifyExpressionToThePowerOfOne) }
                    }
                }
            }
        },
    ),

    /**
     * root[ 3 * [4^2] * [5^3], 4] * root[ [3^3] * [4^2] * 5, 4] -->
     * root[ [3^1 + 3] * [4^2 + 2] * [5^3 + 1], 4]
     */
    CollectRationalizingRadicals(
        plan {
            explanation = Explanation.CollectRationalizingRadicals

            steps {
                apply(IntegerRootsRules.MultiplyNthRoots)
                whilePossible {
                    deeply(GeneralRules.RewriteProductOfPowersWithSameBase)
                    deeply(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
                }
            }
        },
    ),

    RationalizeDenominators(
        plan {
            explanation = Explanation.RationalizeDenominator
            pattern = condition { it is Fraction && it.denominator.containsRoots() }

            steps {
                optionally(FractionRootsRules.FlipRootsInDenominator)
                apply(findRationalizingTerm)
                apply(FractionArithmeticRules.MultiplyFractionAndFractionable)
                optionally {
                    plan {
                        explanation = Explanation.SimplifyNumeratorAfterRationalization

                        steps {
                            applyToKind<Fraction>(simplifyAfterRationalization) { it.numerator }
                        }
                    }
                }
                optionally {
                    plan {
                        explanation = Explanation.SimplifyDenominatorAfterRationalization

                        steps {
                            applyToKind<Fraction>(simplifyAfterRationalization) { it.denominator }
                        }
                    }
                }
            }
        },
    ),
    SimplifyFractionOfRoots(
        plan {
            explanation = Explanation.SimplifyFractionOfRoots

            steps {
                optionally(FractionRootsRules.BringRootsToSameIndexInFraction)
                whilePossible { deeply(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) }
                apply(FractionRootsRules.TurnFractionOfRootsIntoRootOfFractions)
                // apply to the fraction under the root
                applyTo(FractionArithmeticRules.SimplifyFractionToInteger) { it.firstChild }
            }
        },
    ),
}

val findRationalizingTerm = steps {
    firstOf {
        option(FractionRootsRules.RationalizeSimpleDenominator)
        option {
            optionally(FractionRootsRules.FactorizeHigherOrderRadicand)
            apply(FractionRootsPlans.RationalizeHigherOrderRoot)
        }
        option(FractionRootsRules.RationalizeSumOfIntegerAndSquareRoot)
        option(FractionRootsRules.RationalizeSumOfIntegerAndCubeRoot)
    }
}

private val simplifyAfterRationalization = steps {
    whilePossible {
        firstOf {
            option(GeneralRules.SimplifyProductOfConjugates)
            option(FractionRootsRules.IdentifyCubeSumDifference)
            option {
                apply(FractionRootsPlans.CollectRationalizingRadicals)
                optionally(IntegerRootsRules.CombineProductOfSamePowerUnderHigherRoot)
                deeply(cancelRootOfPower)
            }
            option { deeply(IntegerRootsRules.SimplifyNthRootToThePowerOfN) }
            option { deeply(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) }
            option { deeply(IntegerRootsPlans.SimplifyProductWithRoots) }
            option { deeply(IntegerArithmeticPlans.SimplifyIntegersInProduct) }
            option { deeply(IntegerArithmeticPlans.SimplifyIntegersInSum) }
        }
    }
}
