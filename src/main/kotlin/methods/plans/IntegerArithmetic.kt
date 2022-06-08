package methods.plans

import engine.patterns.AnyPattern
import engine.plans.plan
import engine.steps.metadata.PlanExplanation
import methods.rules.*

val simplifyArithmeticExpression = plan {
    pattern = AnyPattern() /* TODO add condition that it is constant in all variables */
    explanation(PlanExplanation.SimplifyArithmeticExpression)

    whilePossible {
        deeply(deepFirst = true) {
            firstOf {
                option(removeBracketAroundUnsignedInteger)
                option(removeBracketAroundSignedIntegerInSum)
                option(simplifyDoubleNeg)
                option(evaluateSignedIntegerPower)
                option {
                    explanation(PlanExplanation.SimplifyIntegerProduct)
                    whilePossible(evaluateSignedIntegerProduct)
                }
                option {
                    explanation(PlanExplanation.SimplifyIntegerSum)
                    whilePossible(evaluateSignedIntegerAddition)
                }
            }
        }
    }
}
