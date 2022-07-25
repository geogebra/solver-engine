package methods.integerroots

import engine.methods.plan
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.squareRootOf
import methods.integerarithmetic.evaluateIntegerProductAndDivision

val simplifyIntegerRoot = plan {
    pattern = squareRootOf(UnsignedIntegerPattern())

    pipeline {
        optionalStep(factorizeIntegerUnderSquareRoot)
        optionalStep(separateOddPowersUnderSquareRoot)
        optionalStep(splitEvenPowersUnderSeparateRoot)
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
