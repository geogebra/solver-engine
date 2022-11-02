package methods.general

import engine.methods.plan
import engine.methods.stepsproducers.steps
import methods.fractionarithmetic.evaluateFractionSum
import methods.integerarithmetic.evaluateSumOfIntegers

val normalizeNegativeSigns = steps {
    firstOf {
        option(simplifyDoubleMinus)
        option(simplifyProductWithTwoNegativeFactors)
        option(moveSignOfNegativeFactorOutOfProduct)
    }
}

val rewriteDivisionsAsFractions = plan {
    explanation(Explanation.RewriteDivisionsAsFractionInExpression)

    steps {
        whilePossible { deeply(rewriteDivisionAsFraction) }
    }
}

val evaluateOperationContainingZero = steps {
    firstOf {
        option(evaluateZeroDividedByAnyValue)
        option(evaluateProductContainingZero)
    }
}

val simplifyProductOfPowersWithSameBase = plan {
    explanation(Explanation.SimplifyProductOfPowersWithSameBase)

    steps {
        apply(rewriteProductOfPowersWithSameBase)
        firstOf {
            option { deeply(evaluateFractionSum) }
            option { deeply(evaluateSumOfIntegers) }
        }
    }
}
