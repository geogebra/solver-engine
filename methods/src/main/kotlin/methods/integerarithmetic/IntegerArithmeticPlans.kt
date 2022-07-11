package methods.integerarithmetic

import engine.patterns.AnyPattern
import engine.plans.PlanId
import engine.plans.plan
import methods.general.removeBracketAroundSignedIntegerInSum
import methods.general.removeBracketAroundUnsignedInteger
import methods.general.simplifyDoubleMinus
import methods.integerroots.simplifyIntegerRoot
import methods.integerroots.simplifyRootOfOne
import methods.integerroots.simplifyRootOfZero

/*
evaluates: [2^4] as:
1. [2^4] --> 2 * 2 * 2 * 2
2. 2 * 2 * 2 * 2 --> 16

and
evaluates: [2^6] as:
1. [2^6] --> 64
 */
val evaluateSignedIntegerPower = plan {
    planId = PlanId.EvaluateSignedIntegerPower

    firstOf {
        option {
            pipeline {
                step(rewriteIntegerPowerAsProduct)
                step {
                    whilePossible(evaluateIntegerProductAndDivision)
                }
            }
        }
        option(evaluateIntegerPower)
    }
}

val simplifyArithmeticExpression = plan {
    planId = PlanId.SimplifyArithmeticExpression

    pattern = AnyPattern() /* TODO add condition that it is constant in all variables */
    explanation(Explanation.SimplifyArithmeticExpression)

    whilePossible {
        deeply(deepFirst = true) {
            firstOf {
                option(removeBracketAroundUnsignedInteger)
                option(removeBracketAroundSignedIntegerInSum)
                option(simplifyDoubleMinus)
                option(simplifyRootOfOne)
                option(simplifyRootOfZero)
                option(evaluateSignedIntegerPower)
                option {
                    explanation(Explanation.SimplifyIntegerProduct)
                    whilePossible(evaluateIntegerProductAndDivision)
                }
                option {
                    explanation(Explanation.SimplifyIntegerSum)
                    whilePossible(evaluateSignedIntegerAddition)
                }
                option(simplifyIntegerRoot)
            }
        }
    }
}
