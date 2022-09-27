package methods.fractionroots

import engine.expressions.denominator
import engine.expressions.numerator
import engine.methods.plan
import methods.fractionarithmetic.multiplyFractions
import methods.fractionarithmetic.simplifyFractionToInteger
import methods.general.eliminateLoneOneInExponent
import methods.general.removeBracketsProduct
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
    pipeline {
        steps(higherOrderRationalizingTerm)
        steps {
            plan {
                pipeline {
                    steps {
                        whilePossible {
                            deeply(evaluateSignedIntegerAddition)
                        }
                    }
                    optionalSteps {
                        whilePossible {
                            deeply(eliminateLoneOneInExponent)
                        }
                    }
                }
            }
        }
    }
}

val findRationalizingTerm = plan {
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
    pipeline {
        steps(multiplyNthRoots)
        optionalSteps {
            whilePossible {
                deeply(collectPowersOfExponentsWithSameBase)
            }
        }
    }
}

private val simplifyAfterRationalization = plan {
    whilePossible {
        deeply {
            firstOf {
                option(identityCubeSumDifference)
                option {
                    pipeline {
                        steps(collectRationalizingRadicals)
                        optionalSteps {
                            plan {
                                whilePossible {
                                    deeply(evaluateSignedIntegerAddition)
                                }
                            }
                        }
                        optionalSteps(combineProductOfSamePowerUnderHigherRoot)
                        steps {
                            plan {
                                explanation(Explanation.SimplifyNthRootOfNthPower)
                                pipeline {
                                    steps { deeply(simplifyNthRootOfNthPower) }
                                    optionalSteps(removeBracketsProduct)
                                }
                            }
                        }
                    }
                }
                option(simplifyNthRootToThePowerOfN)
                option(simplifyProductWithRoots)
                option(simplifyProductOfConjugates)
                option(simplifyIntegersInSum)
            }
        }
    }
}

val rationalizeDenominators = plan {
    explanation(Explanation.RationalizeDenominator)

    pipeline {
        optionalSteps(flipRootsInDenominator)
        optionalSteps { deeply(simplifyRootOfOne) }
        optionalSteps { deeply(simplifyIntegerRoot) }
        optionalSteps { deeply(factorizeHigherOrderRadicand) }
        steps(findRationalizingTerm)
        steps(multiplyFractions)
        optionalSteps {
            applyTo(simplifyAfterRationalization) { it.numerator() }
        }
        optionalSteps {
            applyTo(simplifyAfterRationalization) { it.denominator() }
        }
    }
}

val simplifyFractionOfRoots = plan {
    explanation(Explanation.SimplifyFractionOfRoots)

    pipeline {
        optionalSteps(bringRootsToSameIndexInFraction)
        optionalSteps {
            whilePossible {
                deeply(evaluateIntegerPowerDirectly)
            }
        }
        steps(simplifyFractionOfRootsWithSameOrder)
        steps {
            // apply to the fraction under the root
            applyTo(simplifyFractionToInteger) { it.nthChild(0) }
        }
    }
}
