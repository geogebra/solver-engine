package methods.plans

import engine.patterns.AnyPattern
import engine.patterns.squareRootOf
import engine.plans.plan
import engine.steps.metadata.PlanExplanation
import methods.rules.evaluateSignedIntegerAddition
import methods.rules.evaluateSignedIntegerPower
import methods.rules.evaluateSignedIntegerProduct
import methods.rules.factorizeIntegerUnderSquareRoot
import methods.rules.removeBracketAroundSignedIntegerInSum
import methods.rules.removeBracketAroundUnsignedInteger
import methods.rules.rootOfOne
import methods.rules.rootOfZero
import methods.rules.separateIntegerPowersUnderSquareRoot
import methods.rules.separateSquaresUnderSquareRoot
import methods.rules.simplifyDoubleNeg
import methods.rules.simplifySquareRootOfPower

val simplifyArithmeticExpression = plan {
    pattern = AnyPattern() /* TODO add condition that it is constant in all variables */
    explanation(PlanExplanation.SimplifyArithmeticExpression)

    whilePossible {
        deeply(deepFirst = true) {
            firstOf {
                option(removeBracketAroundUnsignedInteger)
                option(removeBracketAroundSignedIntegerInSum)
                option(simplifyDoubleNeg)
                option(rootOfOne)
                option(rootOfZero)
                option(evaluateSignedIntegerPower)
                option {
                    explanation(PlanExplanation.SimplifyIntegerProduct)
                    whilePossible(evaluateSignedIntegerProduct)
                }
                option {
                    explanation(PlanExplanation.SimplifyIntegerSum)
                    whilePossible(evaluateSignedIntegerAddition)
                }
                option {
                    pattern = squareRootOf(AnyPattern())

                    pipeline {
                        optionalStep(factorizeIntegerUnderSquareRoot)
                        optionalStep(separateIntegerPowersUnderSquareRoot)
                        optionalStep(separateSquaresUnderSquareRoot)
                        step {
                            deeply(simplifySquareRootOfPower)
                        }
                    }
                }
            }
        }
    }
}
