package methods.mixednumbers

import engine.context.ResourceData
import engine.methods.plan
import engine.patterns.mixedNumberOf
import engine.patterns.sumOf
import methods.fractionarithmetic.convertIntegerToFraction
import methods.fractionarithmetic.evaluateFractionSum
import methods.general.removeBracketsSum
import methods.integerarithmetic.evaluateSignedIntegerAddition

val convertMixedNumberToImproperFraction = plan {
    pattern = mixedNumberOf()

    pipeline {
        step(splitMixedNumber)
        step(convertIntegerToFraction)
        step(evaluateFractionSum)
    }
}

val addMixedNumbers = plan {
    pattern = sumOf(mixedNumberOf(), mixedNumberOf())

    resourceData = ResourceData(curriculum = "EU")
    pipeline {
        step {
            explanation(Explanation.ConvertMixedNumbersToImproperFraction)
            applyToChildrenInStep(convertMixedNumberToImproperFraction)
        }

        step(evaluateFractionSum)
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
            step(evaluateFractionSum)
            step(convertIntegerToFraction)
            step(evaluateFractionSum)
            step(fractionToMixedNumber)
        }
    }
}
