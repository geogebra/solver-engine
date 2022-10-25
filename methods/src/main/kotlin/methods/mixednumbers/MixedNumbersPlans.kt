package methods.mixednumbers

import engine.context.ResourceData
import engine.methods.plan
import engine.patterns.mixedNumberOf
import engine.patterns.sumOf
import methods.fractionarithmetic.convertIntegerToFraction
import methods.fractionarithmetic.evaluateFractionSum
import methods.fractionarithmetic.simplifyFraction
import methods.general.removeBracketSumInSum
import methods.general.simplifyZeroDenominatorFractionToUndefined
import methods.integerarithmetic.evaluateSignedIntegerAddition

val convertMixedNumberToImproperFraction = plan {
    explanation(Explanation.ConvertMixedNumbersToImproperFraction)

    steps {
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
}

val addMixedNumbers = plan {
    pattern = sumOf(mixedNumberOf(), mixedNumberOf())

    explanation(Explanation.AddMixedNumbers)

    steps(ResourceData(curriculum = "EU")) {
        apply(convertMixedNumberToImproperFraction)
        apply(evaluateFractionSum)
        // result might be integer or proper fraction after
        // simplification, so this step is optional
        optionally(fractionToMixedNumber)
    }

    alternative(ResourceData(curriculum = "US")) {

        plan {
            explanation(Explanation.ConvertMixedNumbersToSums)

            steps {
                whilePossible { deeply(splitMixedNumber) }
            }
        }

        whilePossible {
            deeply(simplifyZeroDenominatorFractionToUndefined)
        }

        plan {
            explanation(Explanation.RemoveAllBracketsInSum)

            steps {
                whilePossible(removeBracketSumInSum)
            }
        }

        whilePossible {
            deeply(simplifyFraction)
        }

        apply(evaluateSignedIntegerAddition)
        apply(evaluateFractionSum)

        firstOf {
            option(evaluateSignedIntegerAddition)
            option(convertSumOfIntegerAndProperFractionToMixedNumber)
            option {
                apply(convertIntegerToFraction)
                apply(evaluateFractionSum)
                apply(fractionToMixedNumber)
            }
        }
    }
}
