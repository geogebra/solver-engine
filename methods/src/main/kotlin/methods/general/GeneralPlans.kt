package methods.general

import engine.methods.plan
import engine.methods.stepsproducers.steps
import methods.fractionarithmetic.evaluateFractionSum
import methods.integerarithmetic.evaluateSumOfIntegers

val normalizeNegativeSigns = steps {
    firstOf {
        option(GeneralRules.SimplifyDoubleMinus)
        option(GeneralRules.SimplifyProductWithTwoNegativeFactors)
        option(GeneralRules.MoveSignOfNegativeFactorOutOfProduct)
    }
}

val rewriteDivisionsAsFractions = plan {
    explanation(Explanation.RewriteDivisionsAsFractionInExpression)

    steps {
        whilePossible { deeply(GeneralRules.RewriteDivisionAsFraction) }
    }
}

val evaluateOperationContainingZero = steps {
    firstOf {
        option(GeneralRules.EvaluateZeroDividedByAnyValue)
        option(GeneralRules.EvaluateProductContainingZero)
    }
}

val simplifyProductOfPowersWithSameBase = plan {
    explanation(Explanation.SimplifyProductOfPowersWithSameBase)

    steps {
        apply(GeneralRules.RewriteProductOfPowersWithSameBase)
        firstOf {
            option { deeply(evaluateFractionSum) }
            option { deeply(evaluateSumOfIntegers) }
        }
    }
}
