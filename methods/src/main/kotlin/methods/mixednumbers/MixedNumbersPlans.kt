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
        steps(convertMixedNumberToImproperFraction)
        steps(evaluateFractionSum)
        // result might be integer or proper fraction after
        // simplification, so this step is optional
        optionalSteps(fractionToMixedNumber)
    }

    alternative {
        resourceData = ResourceData(curriculum = "US")

        pipeline {
            steps {
                plan {
                    explanation(Explanation.ConvertMixedNumbersToSums)
                    whilePossible {
                        deeply(splitMixedNumber)
                    }
                }
            }
            steps {
                plan {
                    whilePossible(removeBracketsSum)
                }
            }
            steps(evaluateSignedIntegerAddition)
            steps(evaluateFractionSum)
            steps(convertIntegerToFraction)
            steps(evaluateFractionSum)
            steps(fractionToMixedNumber)
        }
    }
}
