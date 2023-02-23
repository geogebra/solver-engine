package methods.general

import engine.methods.CompositeMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps

enum class GeneralPlans(override val runner: CompositeMethod) : RunnerMethod {
    RewriteDivisionsAsFractions(
        plan {
            explanation = Explanation.RewriteDivisionsAsFractionInExpression

            steps {
                whilePossible { deeply(GeneralRules.RewriteDivisionAsFraction) }
            }
        },
    ),
}

val normalizeNegativeSigns = steps {
    firstOf {
        option(GeneralRules.SimplifyDoubleMinus)
        option(GeneralRules.SimplifyProductWithTwoNegativeFactors)
        option(GeneralRules.MoveSignOfNegativeFactorOutOfProduct)
    }
}
