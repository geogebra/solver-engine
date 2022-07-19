package methods.integerarithmetic

import engine.methods.plan
import engine.patterns.AnyPattern
import methods.general.normalizeBrackets
import methods.general.normalizeNegativeSigns

/**
 * evaluates: [2^4] as:
 *  1. [2^4] --> 2 * 2 * 2 * 2
 *  2. 2 * 2 * 2 * 2 --> 16
 * and evaluates: [2^6] as:
 *  1. [2^6] --> 64
 */
val evaluateSignedIntegerPower = plan {
    firstOf {
        option {
            pipeline {
                step(rewriteIntegerPowerAsProduct)
                step {
                    whilePossible(evaluateIntegerProductAndDivision)
                }
            }
        }
        option(simplifyEvenPowerOfNegative)
        option(simplifyOddPowerOfNegative)
        option(evaluateIntegerPower)
    }
}

val simplifyIntegersInProduct = plan {
    explanation(Explanation.SimplifyIntegersInProduct)
    whilePossible(evaluateIntegerProductAndDivision)
}

val simplifyIntegersInSum = plan {
    explanation(Explanation.SimplifyIntegersInSum)
    whilePossible(evaluateSignedIntegerAddition)
}

val simplifyArithmeticExpression = plan {
    pattern = AnyPattern() /* TODO add condition that it is constant in all variables */
    explanation(Explanation.SimplifyArithmeticExpression)

    pipeline {
        optionalStep(normalizeBrackets)
        step {
            whilePossible {
                deeply(deepFirst = true) {
                    firstOf {
                        option(normalizeBrackets)
                        option(normalizeNegativeSigns)
                        option(evaluateSignedIntegerPower)
                        option(simplifyIntegersInProduct)
                        option(simplifyIntegersInSum)
                    }
                }
            }
        }
    }
}
