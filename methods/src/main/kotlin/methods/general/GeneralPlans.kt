package methods.general

import engine.methods.plan
import engine.methods.steps

val normalizeNegativeSigns = steps {
    firstOf {
        option(simplifyDoubleMinus)
        option(simplifyProductWithTwoNegativeFactors)
        option(moveSignOfNegativeFactorOutOfProduct)
    }
}

val rewriteDivisionsAsFractions = plan {
    explanation(Explanation.RewriteDivisionsAsFractionInExpression)

    whilePossible {
        deeply(rewriteDivisionAsFraction)
    }
}

val evaluateOperationContainingZero = steps {
    firstOf {
        option(evaluateZeroDividedByAnyValue)
        option(evaluateProductContainingZero)
    }
}
