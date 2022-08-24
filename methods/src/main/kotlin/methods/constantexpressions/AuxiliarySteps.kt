package methods.constantexpressions

import engine.methods.steps
import methods.fractionarithmetic.evaluateFractionSum
import methods.fractionarithmetic.evaluateSumOfFractionAndInteger
import methods.integerarithmetic.evaluateIntegerPowerDirectly
import methods.integerarithmetic.simplifyIntegersInProduct
import methods.integerarithmetic.simplifyIntegersInSum

// to avoid circular dependency between files some commonly used
// steps producers are collected into this file

val simplifyIntegersInExpression = steps {
    whilePossible {
        firstOf {
            option { deeply(evaluateIntegerPowerDirectly) }
            option { deeply(simplifyIntegersInProduct) }
            option { deeply(simplifyIntegersInSum) }
        }
    }
}

val simplifyFractionsInExpression = steps {
    whilePossible {
        firstOf {
            option { deeply(simplifyIntegersInExpression) }
            option { deeply(evaluateFractionSum) }
            option { deeply(evaluateSumOfFractionAndInteger) }
        }
    }
}
