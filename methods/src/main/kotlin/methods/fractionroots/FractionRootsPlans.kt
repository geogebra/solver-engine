package methods.fractionroots

import engine.expressions.denominator
import engine.expressions.numerator
import engine.methods.Plan
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import methods.fractionarithmetic.FractionArithmeticRules
import methods.general.GeneralRules
import methods.general.NormalizationRules
import methods.integerarithmetic.IntegerArithmeticPlans
import methods.integerarithmetic.IntegerArithmeticRules
import methods.integerroots.IntegerRootsPlans
import methods.integerroots.IntegerRootsRules

enum class FractionRootsPlans(override val runner: Plan) : RunnerMethod {

    RationalizeHigherOrderRoot(
        plan {
            explanation(Explanation.RationalizeHigherOrderRoot)

            steps {
                apply(FractionRootsRules.HigherOrderRationalizingTerm)
                plan {
                    explanation(Explanation.SimplifyRationalizingTerm)

                    steps {
                        whilePossible { deeply(IntegerArithmeticRules.EvaluateSignedIntegerAddition) }
                        whilePossible { deeply(GeneralRules.SimplifyExpressionToThePowerOfOne) }
                    }
                }
            }
        }
    ),

    /**
     * root[ 3 * [4^2] * [5^3], 4] * root[ [3^3] * [4^2] * 5, 4] -->
     * root[ [3^1 + 3] * [4^2 + 2] * [5^3 + 1], 4]
     */
    CollectRationalizingRadicals(
        plan {
            explanation(Explanation.CollectRationalizingRadicals)

            steps {
                apply(IntegerRootsRules.MultiplyNthRoots)
                whilePossible {
                    deeply(IntegerRootsRules.CollectPowersOfExponentsWithSameBase)
                    deeply(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
                }
            }
        }
    ),

    RationalizeDenominators(
        plan {
            explanation(Explanation.RationalizeDenominator)

            steps {
                optionally(FractionRootsRules.FlipRootsInDenominator)
                optionally { deeply(IntegerRootsRules.SimplifyRootOfOne) }
                optionally { deeply(IntegerRootsPlans.SimplifyIntegerRoot) }
                optionally { deeply(FractionRootsRules.FactorizeHigherOrderRadicand) }
                apply(findRationalizingTerm)
                apply(FractionArithmeticRules.MultiplyFractions)
                optionally {
                    plan {
                        explanation(Explanation.SimplifyNumeratorAfterRationalization)

                        steps {
                            applyTo(simplifyAfterRationalization) { it.numerator() }
                        }
                    }
                }
                optionally {
                    plan {
                        explanation(Explanation.SimplifyDenominatorAfterRationalization)

                        steps {
                            applyTo(simplifyAfterRationalization) { it.denominator() }
                        }
                    }
                }
            }
        }
    ),
    SimplifyFractionOfRoots(
        plan {
            explanation(Explanation.SimplifyFractionOfRoots)

            steps {
                optionally(FractionRootsRules.BringRootsToSameIndexInFraction)
                whilePossible { deeply(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) }
                apply(FractionRootsRules.TurnFractionOfRootsIntoRootOfFractions)
                // apply to the fraction under the root
                applyTo(FractionArithmeticRules.SimplifyFractionToInteger) { it.nthChild(0) }
            }
        }
    )
}

val findRationalizingTerm = steps {
    firstOf {
        option(FractionRootsRules.RationalizeSimpleDenominator)
        option(FractionRootsPlans.RationalizeHigherOrderRoot)
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
                deeply(IntegerRootsRules.SimplifyNthRootOfNthPower)
                optionally(NormalizationRules.RemoveBracketProductInProduct)
            }
            option { deeply(IntegerRootsRules.SimplifyNthRootToThePowerOfN) }
            option { deeply(IntegerRootsPlans.SimplifyProductWithRoots) }
            option { deeply(IntegerArithmeticPlans.SimplifyIntegersInSum) }
        }
    }
}
