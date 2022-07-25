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
    explanation(Explanation.ConvertMixedNumbersToImproperFraction)

    applyToChildrenInStep {
        step {
            explanationKey = Explanation.ConvertMixedNumbersToSums
            method = splitMixedNumber
        }
        step {
            explanationKey = Explanation.ConvertIntegersToFractions
            method = convertIntegerToFraction
        }
        step {
            explanationKey = Explanation.AddFractions
            method = evaluateFractionSum
        }
    }
}

val addMixedNumbers = plan {
    pattern = sumOf(mixedNumberOf(), mixedNumberOf())

    resourceData = ResourceData(curriculum = "EU")
    pipeline {
        step(convertMixedNumberToImproperFraction)
        step(evaluateFractionSum)
        // result might be integer or proper fraction after
        // simplification, so this step is optional
        optionalStep(fractionToMixedNumber)
    }

    alternative {
        resourceData = ResourceData(curriculum = "US")

        pipeline {
            step {
                explanation(Explanation.ConvertMixedNumbersToSums)
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
