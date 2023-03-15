package methods.general

import engine.methods.CompositeMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.patterns.productContaining
import engine.patterns.stickyOptionalNegOf

enum class GeneralPlans(override val runner: CompositeMethod) : RunnerMethod {
    NormalizeNegativeSignsInProduct(
        plan {
            pattern = stickyOptionalNegOf(productContaining())
            explanation = Explanation.NormalizeNegativeSignsInProduct

            steps {
                whilePossible {
                    firstOf {
                        option(GeneralRules.SimplifyProductWithTwoNegativeFactors)
                        option(GeneralRules.MoveSignOfNegativeFactorOutOfProduct)
                    }
                }
            }
        },
    ),
}
