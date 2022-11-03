package methods.fractionroots

import engine.expressions.denominator
import engine.expressions.numerator
import engine.methods.plan
import engine.methods.stepsproducers.steps
import methods.fractionarithmetic.FractionArithmeticRules
import methods.general.GeneralRules
import methods.general.NormalizationRules
import methods.integerarithmetic.IntegerArithmeticRules
import methods.integerarithmetic.simplifyIntegersInSum
import methods.integerroots.IntegerRootsRules
import methods.integerroots.simplifyIntegerRoot
import methods.integerroots.simplifyProductWithRoots

private val rationalizeHigherOrderRoot = plan {
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

val findRationalizingTerm = steps {
    firstOf {
        option(FractionRootsRules.RationalizeSimpleDenominator)
        option(rationalizeHigherOrderRoot)
        option(FractionRootsRules.RationalizeSumOfIntegerAndSquareRoot)
        option(FractionRootsRules.RationalizeSumOfIntegerAndCubeRoot)
    }
}

/**
 * root[ 3 * [4^2] * [5^3], 4] * root[ [3^3] * [4^2] * 5, 4] -->
 * root[ [3^1 + 3] * [4^2 + 2] * [5^3 + 1], 4]
 */
val collectRationalizingRadicals = plan {
    explanation(Explanation.CollectRationalizingRadicals)

    steps {
        apply(IntegerRootsRules.MultiplyNthRoots)
        whilePossible {
            deeply(IntegerRootsRules.CollectPowersOfExponentsWithSameBase)
            deeply(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
        }
    }
}

private val simplifyAfterRationalization = steps {
    whilePossible {
        firstOf {
            option(GeneralRules.SimplifyProductOfConjugates)
            option(FractionRootsRules.IdentifyCubeSumDifference)
            option {
                apply(collectRationalizingRadicals)
                optionally(IntegerRootsRules.CombineProductOfSamePowerUnderHigherRoot)
                deeply(IntegerRootsRules.SimplifyNthRootOfNthPower)
                optionally(NormalizationRules.RemoveBracketProductInProduct)
            }
            option { deeply(IntegerRootsRules.SimplifyNthRootToThePowerOfN) }
            option { deeply(simplifyProductWithRoots) }
            option { deeply(simplifyIntegersInSum) }
        }
    }
}

val rationalizeDenominators = plan {
    explanation(Explanation.RationalizeDenominator)

    steps {
        optionally(FractionRootsRules.FlipRootsInDenominator)
        optionally { deeply(IntegerRootsRules.SimplifyRootOfOne) }
        optionally { deeply(simplifyIntegerRoot) }
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

val simplifyFractionOfRoots = plan {
    explanation(Explanation.SimplifyFractionOfRoots)

    steps {
        optionally(FractionRootsRules.BringRootsToSameIndexInFraction)
        whilePossible { deeply(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) }
        apply(FractionRootsRules.TurnFractionOfRootsIntoRootOfFractions)
        // apply to the fraction under the root
        applyTo(FractionArithmeticRules.SimplifyFractionToInteger) { it.nthChild(0) }
    }
}
