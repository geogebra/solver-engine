package methods.plans

import engine.expressions.xp
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.powerOf
import engine.patterns.squareRootOf
import engine.plans.PlanId
import engine.plans.plan
import engine.steps.metadata.PlanExplanation
import methods.rules.evaluateSignedIntegerAddition
import methods.rules.evaluateSignedIntegerPowerWithoutProductStep
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
import methods.rules.writeIntegerPowerAsProduct
import methods.rules.writeIntegerSquareAsMulWithOneAtStart
import methods.rules.writeIntegerSquareAsMulWithoutOneAtStart

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
                step(writeIntegerPowerAsProduct)
                step {
                    whilePossible(evaluateSignedIntegerProduct)
                }
            }
        }
        option(evaluateSignedIntegerPowerWithoutProductStep)
    }
}

val simplifyArithmeticExpression = plan {
    planId = PlanId.SimplifyArithmeticExpression

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

val evaluateSquareOfIntegerWithOneAtStart = plan {
    pattern = powerOf(UnsignedIntegerPattern(), FixedPattern(xp(2)))

    pipeline {
        step(writeIntegerSquareAsMulWithOneAtStart)
        step(simplifyArithmeticExpression)
    }
}

val evaluateSquareOfIntegerWithoutOneAtStart = plan {
    pattern = powerOf(UnsignedIntegerPattern(), FixedPattern(xp(2)))

    pipeline {
        step(writeIntegerSquareAsMulWithoutOneAtStart)
        step(simplifyArithmeticExpression)
    }
}

val evaluateSquareOfInteger = plan {
    pattern = powerOf(SignedIntegerPattern(), FixedPattern(xp(2)))

    selectFromContext {
        case(curriculum = "EU", evaluateSquareOfIntegerWithoutOneAtStart)
        case(curriculum = "US", evaluateSquareOfIntegerWithOneAtStart)
        default(evaluateSquareOfIntegerWithoutOneAtStart)
    }
}
