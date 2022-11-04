package methods.mixednumbers

import engine.context.ResourceData
import engine.methods.Plan
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.patterns.mixedNumberOf
import engine.patterns.sumOf
import methods.fractionarithmetic.FractionArithmeticPlans
import methods.fractionarithmetic.FractionArithmeticRules
import methods.general.GeneralRules
import methods.general.NormalizationRules
import methods.integerarithmetic.IntegerArithmeticRules

enum class MixedNumbersPlans(override val runner: Plan) : RunnerMethod {

    ConvertMixedNumberToImproperFraction(
        plan {
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
                        method = FractionArithmeticPlans.EvaluateFractionSum
                    }
                }
            }
        }
    ),

    @PublicMethod
    AddMixedNumbers(
        plan {
            pattern = sumOf(mixedNumberOf(), mixedNumberOf())

            explanation(Explanation.AddMixedNumbers)

            steps(ResourceData(curriculum = "EU")) {
                apply(ConvertMixedNumberToImproperFraction)
                apply(FractionArithmeticPlans.EvaluateFractionSum)
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
                    deeply(FractionArithmeticPlans.SimplifyFraction)
                }

                apply(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
                apply(FractionArithmeticPlans.EvaluateFractionSum)

                firstOf {
                    option(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
                    option(MixedNumbersRules.ConvertSumOfIntegerAndProperFractionToMixedNumber)
                    option {
                        apply(FractionArithmeticRules.ConvertIntegerToFraction)
                        apply(FractionArithmeticPlans.EvaluateFractionSum)
                        apply(MixedNumbersRules.FractionToMixedNumber)
                    }
                }
            }
        }
    )
}
