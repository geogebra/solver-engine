package methods.constantexpressions

import engine.methods.steps
import methods.integerarithmetic.evaluateIntegerPowerDirectly
import methods.integerarithmetic.evaluateProductOfIntegers
import methods.integerarithmetic.evaluateSumOfIntegers

// to avoid circular dependency between files some commonly used
// steps producers are collected into this file

val evaluateSimpleIntegerExpression = steps {
    whilePossible {
        firstOf {
            option { deeply(evaluateIntegerPowerDirectly) }
            option { deeply(evaluateProductOfIntegers) }
            option { deeply(evaluateSumOfIntegers) }
        }
    }
}
