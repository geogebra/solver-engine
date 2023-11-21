package methods.general

import engine.context.BooleanSetting
import engine.context.Setting
import engine.methods.CompositeMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.branchOn
import engine.methods.stepsproducers.firstOf
import engine.methods.stepsproducers.steps

enum class NormalizationPlans(override val runner: CompositeMethod) : RunnerMethod {
    NormalizeExpression(
        plan {
            explanation = Explanation.NormalizeExpression

            steps {
                // Temporary workaround. Changing the order of the `deeply` and the
                //  `firstOf` does not currently work because of the removal of the outer
                //  brackets in StepsBuilder
                whilePossible {
                    firstOf {
                        option { deeply(NormalizationRules.NormalizeNegativeSignOfIntegerInSum) }
                        option {
                            check { !isSet(Setting.DontAddClarifyingBrackets) }
                            deeply(NormalizationRules.AddClarifyingBracket)
                        }
                        option { deeply(NormalizationRules.RemoveRedundantBracket) }
                        option { deeply(NormalizationRules.RemoveRedundantPlusSign) }
                    }
                }
            }
        },
    ),

    ReorderProductInSteps(
        plan {
            explanation = Explanation.ReorderProduct

            steps {
                whilePossible(NormalizationRules.ReorderProductSingleStep)
            }
        },
    ),

    RemoveAllBracketSumInSum(
        plan {
            explanation = Explanation.RemoveAllBracketSumInSum

            steps {
                whilePossible(NormalizationRules.RemoveBracketSumInSum)
            }
        },
    ),

    RemoveAllBracketProductInProduct(
        plan {
            explanation = Explanation.RemoveAllBracketProductInProduct

            steps {
                whilePossible(NormalizationRules.RemoveBracketProductInProduct)
            }
        },
    ),
}

val reorderProductSteps = branchOn(Setting.ReorderProductsInSteps) {
    case(BooleanSetting.True, NormalizationPlans.ReorderProductInSteps)
    case(BooleanSetting.False, NormalizationRules.ReorderProduct)
}

val inlineSumsAndProducts = steps {
    firstOf {
        option(NormalizationPlans.RemoveAllBracketSumInSum)
        option(NormalizationPlans.RemoveAllBracketProductInProduct)
    }
}
