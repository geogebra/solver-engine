package methods.general

import engine.context.ResourceData
import engine.methods.CompositeMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.contextSensitiveSteps
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
                        option { deeply(NormalizationRules.AddClarifyingBracket) }
                        option { deeply(NormalizationRules.RemoveRedundantBracket) }
                        option { deeply(NormalizationRules.RemoveRedundantPlusSign) }
                        option { deeply(NormalizationRules.NormalizeProductSigns) }
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
}

val reorderProductSteps = contextSensitiveSteps {
    default(ResourceData(), NormalizationRules.ReorderProduct)
    alternative(ResourceData(gmFriendly = true), NormalizationPlans.ReorderProductInSteps)
}

val inlineSumsAndProducts = steps {
    firstOf {
        option(NormalizationPlans.RemoveAllBracketSumInSum)
        option(NormalizationRules.RemoveBracketProductInProduct)
    }
}
