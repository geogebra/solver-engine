package methods.integerroots

import engine.patterns.UnsignedIntegerPattern
import engine.patterns.squareRootOf
import engine.plans.plan
import methods.integerarithmetic.evaluateIntegerProductAndDivision

val simplifyIntegerRoot = plan {
    pattern = squareRootOf(UnsignedIntegerPattern())

    pipeline {
        optionalStep(factorizeIntegerUnderSquareRoot)
        optionalStep(separateIntegerPowersUnderSquareRoot)
        optionalStep(separateSquaresUnderSquareRoot)
        step {
            firstOf {
                option {
                    deeply(simplifySquareRootOfSquare)
                }
                option {
                    deeply(simplifySquareRootOfPower)
                }
            }
        }
        optionalStep {
            deeply(evaluateIntegerProductAndDivision)
        }
    }
}
