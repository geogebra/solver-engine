package methods.integerroots

import engine.methods.plan
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.squareRootOf
import methods.integerarithmetic.evaluateIntegerProductAndDivision

val simplifyIntegerRoot = plan {
    pattern = squareRootOf(UnsignedIntegerPattern())

    pipeline {
        optionalSteps(factorizeIntegerUnderSquareRoot)
        optionalSteps(separateOddPowersUnderSquareRoot)
        optionalSteps(splitEvenPowersUnderSeparateRoot)
        steps {
            firstOf {
                option {
                    deeply(simplifySquareRootOfSquare)
                }
                option {
                    deeply(simplifySquareRootOfPower)
                }
            }
        }
        optionalSteps {
            deeply(evaluateIntegerProductAndDivision)
        }
    }
}
