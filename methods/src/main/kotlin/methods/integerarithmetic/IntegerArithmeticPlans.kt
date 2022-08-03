package methods.integerarithmetic

import engine.expressionmakers.move
import engine.expressions.BinaryOperator
import engine.expressions.BracketOperator
import engine.expressions.Expression
import engine.expressions.IntegerOperator
import engine.expressions.NaryOperator
import engine.expressions.UnaryOperator
import engine.methods.plan
import engine.methods.steps
import engine.patterns.AnyPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.bracketOf
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
import methods.general.simplifyDoubleMinus

val evaluateProductOfIntegers = plan {
    pattern = productContaining()
    explanation(Explanation.EvaluateProductOfIntegers, move(pattern!!))
    whilePossible(evaluateIntegerProductAndDivision)
}

val evaluateSumOfIntegers = plan {
    pattern = sumContaining()
    explanation(Explanation.EvaluateSumOfIntegers, move(pattern!!))
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
                steps(rewriteIntegerPowerAsProduct)
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
    explanation(Explanation.SimplifyIntegersInSum, move(pattern!!))

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
    explanation(Explanation.SimplifyIntegersInSum, move(pattern!!))

    whilePossible {
        firstOf {
            option(evaluateSignedIntegerAddition)
            option(eliminateZeroInSum)
        }
    }
}

private val arithmeticOperators = listOf(
    UnaryOperator.InvisibleBracket,
    UnaryOperator.Minus,
    UnaryOperator.Plus,
    UnaryOperator.DivideBy,
    BinaryOperator.Power,
    NaryOperator.Sum,
    NaryOperator.Product,
)

private fun Expression.isArithmeticExpression(): Boolean {
    for (operand in operands) {
        if (!operand.isArithmeticExpression()) return false
    }

    return operator is IntegerOperator || operator is BracketOperator ||
        arithmeticOperators.contains(operator)
}

val evaluationSteps = steps {
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
    pattern = bracketOf(AnyPattern())
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
                plan {
                    whilePossible(evaluationSteps)
                }
            }
        }
    }
}
