package methods.mixednumbers

import engine.context.ResourceData
import engine.methods.plan
import engine.patterns.mixedNumberOf
import engine.patterns.sumOf
import methods.fractionarithmetic.FractionArithmeticRules
import methods.fractionarithmetic.evaluateFractionSum
import methods.fractionarithmetic.simplifyFraction
import methods.general.GeneralRules
import methods.general.NormalizationRules
import methods.integerarithmetic.IntegerArithmeticRules

val convertMixedNumberToImproperFraction = plan {
    explanation(Explanation.ConvertMixedNumbersToImproperFraction)

    steps {
        applyToChildrenInStep {
            step {
                explanationKey = Explanation.ConvertMixedNumbersToSums
                method = MixedNumbersRules.SplitMixedNumber
            }
            step {
                explanationKey = Explanation.ConvertIntegersToFractions
                method = FractionArithmeticRules.ConvertIntegerToFraction
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
        optionally(MixedNumbersRules.FractionToMixedNumber)
    }

    alternative(ResourceData(curriculum = "US")) {

        plan {
            explanation(Explanation.ConvertMixedNumbersToSums)

            steps {
                whilePossible { deeply(MixedNumbersRules.SplitMixedNumber) }
            }
        }

        whilePossible {
            deeply(GeneralRules.SimplifyZeroDenominatorFractionToUndefined)
        }

        plan {
            explanation(Explanation.RemoveAllBracketsInSum)

            steps {
                whilePossible(NormalizationRules.RemoveBracketSumInSum)
            }
        }

        whilePossible {
            deeply(simplifyFraction)
        }

        apply(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
        apply(evaluateFractionSum)

        firstOf {
            option(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
            option(MixedNumbersRules.ConvertSumOfIntegerAndProperFractionToMixedNumber)
            option {
                apply(FractionArithmeticRules.ConvertIntegerToFraction)
                apply(evaluateFractionSum)
                apply(MixedNumbersRules.FractionToMixedNumber)
            }
        }
    }
}
