package methods.integerarithmetic

import engine.expressionmakers.move
import engine.expressions.Expression
import engine.methods.plan
import engine.methods.steps
import engine.operators.BinaryExpressionOperator
import engine.operators.BracketOperator
import engine.operators.IntegerOperator
import engine.operators.NaryOperator
import engine.operators.UnaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.condition
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import methods.general.addClarifyingBrackets
import methods.general.eliminateOneInProduct
import methods.general.eliminateZeroInSum
import methods.general.evaluateProductContainingZero
import methods.general.removeOuterBracket
import methods.general.removeRedundantBrackets
import methods.general.rewritePowerAsProduct
import methods.general.simplifyDoubleMinus

val evaluateProductOfIntegers = plan {
    pattern = productContaining()
    explanation(Explanation.EvaluateProductOfIntegers, move(pattern))
    whilePossible(evaluateIntegerProductAndDivision)
}

val evaluateSumOfIntegers = plan {
    pattern = sumContaining()
    explanation(Explanation.EvaluateSumOfIntegers, move(pattern))
    whilePossible(evaluateSignedIntegerAddition)
}

/**
 * evaluates: [2^4] as:
 *  1. [2^4] --> 2 * 2 * 2 * 2
 *  2. 2 * 2 * 2 * 2 --> 16
 * and evaluates: [2^6] as:
 *  1. [2^6] --> 64
 */
val evaluateSignedIntegerPower = plan {
    val base = SignedIntegerPattern()
    val exponent = SignedIntegerPattern()
    pattern = powerOf(base, exponent)
    explanation(Explanation.EvaluateIntegerPower, move(base), move(exponent))

    firstOf {
        option {
            pipeline {
                steps(rewritePowerAsProduct)
                steps(evaluateProductOfIntegers)
            }
        }
        option {
            pipeline {
                optionalSteps(simplifyEvenPowerOfNegative)
                optionalSteps(simplifyOddPowerOfNegative)
                steps(evaluateIntegerPowerDirectly)
            }
        }
    }
}

val simplifyIntegersInProduct = plan {
    pattern = productContaining()
    explanation(Explanation.SimplifyIntegersInProduct, move(pattern))

    whilePossible {
        firstOf {
            option(evaluateProductContainingZero)
            option(evaluateIntegerProductAndDivision)
            option(eliminateOneInProduct)
        }
    }
}

val simplifyIntegersInSum = plan {
    pattern = sumContaining()
    explanation(Explanation.SimplifyIntegersInSum, move(pattern))

    whilePossible {
        firstOf {
            option(evaluateSignedIntegerAddition)
            option(eliminateZeroInSum)
        }
    }
}

val arithmeticOperators = listOf(
    UnaryExpressionOperator.InvisibleBracket,
    UnaryExpressionOperator.Minus,
    UnaryExpressionOperator.Plus,
    UnaryExpressionOperator.DivideBy,
    BinaryExpressionOperator.Power,
    NaryOperator.Sum,
    NaryOperator.Product,
)

private fun Expression.isArithmeticExpression(): Boolean {
    val validOperator = operator is IntegerOperator || operator is BracketOperator ||
        arithmeticOperators.contains(operator)

    return validOperator && operands.all { it.isArithmeticExpression() }
}

private val evaluationSteps = steps {
    firstOf {
        option { deeply(removeRedundantBrackets, deepFirst = true) }
        option { deeply(simplifyDoubleMinus, deepFirst = true) }
        option { deeply(evaluateSignedIntegerPower, deepFirst = true) }
        option { deeply(evaluateProductOfIntegers, deepFirst = true) }
        option { deeply(evaluateSumOfIntegers, deepFirst = true) }
    }
}

val evaluateArithmeticSubexpression = plan {
    explanation(Explanation.SimplifyExpressionInBrackets)
    pattern = condition(AnyPattern()) { it.hasBracket() }
    whilePossible(evaluationSteps)
}

val evaluateArithmeticExpression = plan {
    val expression = AnyPattern()
    pattern = condition(expression) { it.isArithmeticExpression() }
    explanation(Explanation.EvaluateArithmeticExpression, move(expression))

    whilePossible {
        firstOf {
            option(addClarifyingBrackets)
            option(removeOuterBracket)

            option {
                deeply(evaluateArithmeticSubexpression, deepFirst = true)
            }

            option {
                whilePossible(evaluationSteps)
            }
        }
    }
}

// Auxiliary steps used in several plans
val simplifyIntegersInExpression = steps {
    whilePossible {
        firstOf {
            option { deeply(evaluateIntegerPowerDirectly) }
            option { deeply(simplifyIntegersInProduct) }
            option { deeply(simplifyIntegersInSum) }
        }
    }
}
