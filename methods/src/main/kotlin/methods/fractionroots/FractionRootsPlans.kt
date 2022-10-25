package methods.fractionroots

import engine.expressions.denominator
import engine.expressions.numerator
import engine.methods.plan
import engine.methods.stepsproducers.steps
import methods.fractionarithmetic.multiplyFractions
import methods.fractionarithmetic.simplifyFractionToInteger
import methods.general.removeBracketProductInProduct
import methods.general.simplifyExpressionToThePowerOfOne
import methods.general.simplifyProductOfConjugates
import methods.integerarithmetic.evaluateIntegerPowerDirectly
import methods.integerarithmetic.evaluateSignedIntegerAddition
import methods.integerarithmetic.simplifyIntegersInSum
import methods.integerroots.collectPowersOfExponentsWithSameBase
import methods.integerroots.combineProductOfSamePowerUnderHigherRoot
import methods.integerroots.multiplyNthRoots
import methods.integerroots.simplifyIntegerRoot
import methods.integerroots.simplifyNthRootOfNthPower
import methods.integerroots.simplifyNthRootToThePowerOfN
import methods.integerroots.simplifyProductWithRoots
import methods.integerroots.simplifyRootOfOne

private val rationalizeHigherOrderRoot = plan {
    explanation(Explanation.RationalizeHigherOrderRoot)

    steps {
        apply(higherOrderRationalizingTerm)
        plan {
            explanation(Explanation.SimplifyRationalizingTerm)

            steps {
                whilePossible { deeply(evaluateSignedIntegerAddition) }
                whilePossible { deeply(simplifyExpressionToThePowerOfOne) }
            }
        }
    }
}

val findRationalizingTerm = steps {
    firstOf {
        option(rationalizeSimpleDenominator)
        option(rationalizeHigherOrderRoot)
        option(rationalizeSumOfIntegerAndSquareRoot)
        option(rationalizeSumOfIntegerAndCubeRoot)
    }
}

/**
 * root[ 3 * [4^2] * [5^3], 4] * root[ [3^3] * [4^2] * 5, 4] -->
 * root[ [3^1 + 3] * [4^2 + 2] * [5^3 + 1], 4]
 */
val collectRationalizingRadicals = plan {
    explanation(Explanation.CollectRationalizingRadicals)

    steps {
        apply(multiplyNthRoots)
        whilePossible {
            deeply(collectPowersOfExponentsWithSameBase)
            deeply(evaluateSignedIntegerAddition)
        }
    }
}

private val simplifyAfterRationalization = steps {
    whilePossible {
        firstOf {
            option(simplifyProductOfConjugates)
            option(identifyCubeSumDifference)
            option {
                apply(collectRationalizingRadicals)
                optionally(combineProductOfSamePowerUnderHigherRoot)
                deeply(simplifyNthRootOfNthPower)
                optionally(removeBracketProductInProduct)
            }
            option { deeply(simplifyNthRootToThePowerOfN) }
            option { deeply(simplifyProductWithRoots) }
            option { deeply(simplifyIntegersInSum) }
        }
    }
}

val rationalizeDenominators = plan {
    explanation(Explanation.RationalizeDenominator)

    steps {
        optionally(flipRootsInDenominator)
        optionally { deeply(simplifyRootOfOne) }
        optionally { deeply(simplifyIntegerRoot) }
        optionally { deeply(factorizeHigherOrderRadicand) }
        apply(findRationalizingTerm)
        apply(multiplyFractions)
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
        optionally(bringRootsToSameIndexInFraction)
        whilePossible { deeply(evaluateIntegerPowerDirectly) }
        apply(turnFractionOfRootsIntoRootOfFractions)
        // apply to the fraction under the root
        applyTo(simplifyFractionToInteger) { it.nthChild(0) }
    }
}
