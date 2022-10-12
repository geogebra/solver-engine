package methods.mixednumbers

import engine.context.ResourceData
import engine.methods.plan
import engine.patterns.mixedNumberOf
import engine.patterns.sumOf
import methods.fractionarithmetic.convertIntegerToFraction
import methods.fractionarithmetic.evaluateFractionSum
import methods.fractionarithmetic.simplifyFraction
import methods.general.removeBracketsInSum
import methods.general.simplifyZeroDenominatorFractionToUndefined
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

    explanation(Explanation.AddMixedNumbers)

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
            optionalSteps {
                whilePossible {
                    deeply(simplifyZeroDenominatorFractionToUndefined)
                }
            }

            steps {
                plan {
                    explanation(Explanation.RemoveAllBracketsInSum)
                    whilePossible(removeBracketsInSum)
                }
            }

            optionalSteps {
                whilePossible {
                    deeply(simplifyFraction)
                }
            }

            steps(evaluateSignedIntegerAddition)
            steps(evaluateFractionSum)

            steps {
                firstOf {
                    option(evaluateSignedIntegerAddition)
                    option(convertSumOfIntegerAndProperFractionToMixedNumber)
                    option {
                        pipeline {
                            steps(convertIntegerToFraction)
                            steps(evaluateFractionSum)
                            steps(fractionToMixedNumber)
                        }
                    }
                }
            }
        }
    }
}
