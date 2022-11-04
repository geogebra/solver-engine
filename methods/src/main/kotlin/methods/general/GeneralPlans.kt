package methods.general

import engine.methods.Plan
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import methods.fractionarithmetic.FractionArithmeticPlans
import methods.integerarithmetic.IntegerArithmeticPlans

enum class GeneralPlans(override val runner: Plan) : RunnerMethod {
    RewriteDivisionsAsFractions(
        plan {
            explanation(Explanation.RewriteDivisionsAsFractionInExpression)

            steps {
                whilePossible { deeply(GeneralRules.RewriteDivisionAsFraction) }
            }
        }
    ),

    /**
     * This plan should not be here as it introduces circular dependencies
     */
    SimplifyProductOfPowersWithSameBase(
        plan {
            explanation(Explanation.SimplifyProductOfPowersWithSameBase)

            steps {
                apply(GeneralRules.RewriteProductOfPowersWithSameBase)
                firstOf {
                    option { deeply(FractionArithmeticPlans.EvaluateFractionSum) }
                    option { deeply(IntegerArithmeticPlans.EvaluateSumOfIntegers) }
                }
            }
        }
    )
}

val normalizeNegativeSigns = steps {
    firstOf {
        option(GeneralRules.SimplifyDoubleMinus)
        option(GeneralRules.SimplifyProductWithTwoNegativeFactors)
        option(GeneralRules.MoveSignOfNegativeFactorOutOfProduct)
    }
}

val evaluateOperationContainingZero = steps {
    firstOf {
        option(GeneralRules.EvaluateZeroDividedByAnyValue)
        option(GeneralRules.EvaluateProductContainingZero)
    }
}
