package methods.fractionroots

import engine.expressions.denominator
import engine.expressions.numerator
import engine.methods.plan
import methods.fractionarithmetic.multiplyFractions
import methods.fractionarithmetic.simplifyFractionToInteger
import methods.general.simplifyProductOfConjugates
import methods.integerarithmetic.evaluateIntegerPowerDirectly
import methods.integerarithmetic.simplifyIntegersInSum
import methods.integerroots.simplifyIntegerRoot
import methods.integerroots.simplifyNthRootToThePowerOfN
import methods.integerroots.simplifyProductWithRoots
import methods.integerroots.simplifyRootOfOne

val findRationalizingTerm = plan {
    firstOf {
        option(rationalizeSimpleDenominator)
        option(rationalizeSumOfIntegerAndRadical)
        option(rationalizeCubeRootDenominator)
    }
}

private val simplifyAfterRationalization = plan {
    whilePossible {
        deeply {
            firstOf {
                option(identityCubeSumDifference)
                option(simplifyProductWithRoots)
                option(simplifyProductOfConjugates)
                option(simplifyNthRootToThePowerOfN)
                option(simplifyIntegersInSum)
            }
        }
    }
}

val rationalizeDenominators = plan {
    explanation(Explanation.RationalizeDenominator)

    pipeline {
        optionalSteps(distributeRadicalOverFraction)
        optionalSteps(flipRootsInDenominator)
        optionalSteps { deeply(simplifyRootOfOne) }
        optionalSteps { deeply(simplifyIntegerRoot) }
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
