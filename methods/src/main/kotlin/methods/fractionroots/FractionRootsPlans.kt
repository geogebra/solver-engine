package methods.fractionroots

import engine.expressions.denominator
import engine.expressions.numerator
import engine.methods.plan
import engine.methods.steps
import methods.fractionarithmetic.multiplyFractions
import methods.fractionarithmetic.simplifyFractionToInteger
import methods.general.simplifyProductOfConjugates
import methods.integerarithmetic.evaluateIntegerPowerDirectly
import methods.integerarithmetic.simplifyIntegersInSum
import methods.integerroots.simplifyIntegerRoot
import methods.integerroots.simplifyNthRootToThePowerOfN
import methods.integerroots.simplifyProductWithRoots
import methods.integerroots.simplifyRootOfOne

val rationalizeDenominatorSubstep = plan {
    firstOf {
        option(rationalizeSimpleDenominator)
        option(rationalizeSimpleDenominatorWithCoefficient)
        option(rationalizeSumOfIntegerAndRadical)
        option(rationalizeCubeRootDenominator)
    }
}

private val simplifyAfterRationalization = steps {
    pipeline {
        optionalSteps { deeply(identityCubeSumDifference) }
        optionalSteps { deeply(simplifyProductWithRoots) }
        optionalSteps { deeply(simplifyProductOfConjugates) }
        optionalSteps {
            whilePossible {
                deeply(simplifyNthRootToThePowerOfN)
            }
        }
        optionalSteps(simplifyIntegersInSum)
    }
}

val rationalizeDenominators = plan {
    pipeline {
        optionalSteps(distributeRadicalOverFraction)
        optionalSteps(rewriteCubeRootDenominator)
        optionalSteps { deeply(simplifyRootOfOne) }
        optionalSteps { deeply(simplifyIntegerRoot) }
        steps(rationalizeDenominatorSubstep)
        optionalSteps(multiplyFractions)
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
