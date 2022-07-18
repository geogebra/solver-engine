package methods.mixednumbers

import engine.context.ResourceData
import engine.methods.plan
import engine.patterns.mixedNumberOf
import engine.patterns.sumOf
import methods.fractionarithmetic.convertIntegerToFraction
import methods.fractionarithmetic.evaluatePositiveFractionSum
import methods.general.removeBracketsSum
import methods.integerarithmetic.evaluateSignedIntegerAddition

val convertMixedNumberToImproperFraction = plan {
    pattern = mixedNumberOf()

    pipeline {
        step(splitMixedNumber)
        step(convertIntegerToFraction)
        step(evaluatePositiveFractionSum)
    }
}

val addMixedNumbers = plan {
    pattern = sumOf(mixedNumberOf(), mixedNumberOf())

    resourceData = ResourceData(curriculum = "EU")
    pipeline {
        step {
            applyToChildrenInStep(convertMixedNumberToImproperFraction)
        }

        step(evaluatePositiveFractionSum)
        step(fractionToMixedNumber)
    }

    alternative {
        resourceData = ResourceData(curriculum = "US")

        pipeline {
            step {
                whilePossible {
                    deeply(splitMixedNumber)
                }
            }
            step {
                whilePossible(removeBracketsSum)
            }
            step(evaluateSignedIntegerAddition)
            step(evaluatePositiveFractionSum)
            step(convertIntegerToFraction)
            step(evaluatePositiveFractionSum)
            step(fractionToMixedNumber)
        }
    }
}
