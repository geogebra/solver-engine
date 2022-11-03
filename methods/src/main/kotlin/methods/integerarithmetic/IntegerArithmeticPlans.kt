package methods.integerarithmetic

import engine.expressionmakers.move
import engine.expressions.Expression
import engine.methods.plan
import engine.methods.stepsproducers.steps
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
import methods.general.GeneralRules
import methods.general.NormalizationRules
import methods.general.addClarifyingBrackets
import methods.general.removeRedundantBrackets

val evaluateProductOfIntegers = plan {
    pattern = productContaining()
    explanation(Explanation.EvaluateProductOfIntegers, move(pattern))

    steps {
        whilePossible {
            firstOf {
                option(GeneralRules.EvaluateProductDividedByZeroAsUndefined)
                option(IntegerArithmeticRules.EvaluateIntegerProductAndDivision)
            }
        }
    }
}

val evaluateSumOfIntegers = plan {
    pattern = sumContaining()
    explanation(Explanation.EvaluateSumOfIntegers, move(pattern))

    steps {
        whilePossible(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
    }
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

    steps {
        firstOf {
            option(GeneralRules.EvaluateZeroToThePowerOfZero)
            option {
                apply(GeneralRules.RewritePowerAsProduct)
                apply(evaluateProductOfIntegers)
            }
            option {
                optionally(IntegerArithmeticRules.SimplifyEvenPowerOfNegative)
                optionally(IntegerArithmeticRules.SimplifyOddPowerOfNegative)
                apply(IntegerArithmeticRules.EvaluateIntegerPowerDirectly)
            }
        }
    }
}

val simplifyIntegersInProduct = plan {
    pattern = productContaining()
    explanation(Explanation.SimplifyIntegersInProduct, move(pattern))

    steps {
        whilePossible {
            firstOf {
                option(GeneralRules.EvaluateProductDividedByZeroAsUndefined)
                option(GeneralRules.EvaluateProductContainingZero)
                option(IntegerArithmeticRules.EvaluateIntegerProductAndDivision)
                option(GeneralRules.EliminateOneInProduct)
            }
        }
    }
}

val simplifyIntegersInSum = plan {
    pattern = sumContaining()
    explanation(Explanation.SimplifyIntegersInSum, move(pattern))

    steps {
        whilePossible {
            firstOf {
                option(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
                option(GeneralRules.EliminateZeroInSum)
            }
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
        option { deeply(GeneralRules.SimplifyDoubleMinus, deepFirst = true) }
        option { deeply(evaluateSignedIntegerPower, deepFirst = true) }
        option { deeply(evaluateProductOfIntegers, deepFirst = true) }
        option { deeply(evaluateSumOfIntegers, deepFirst = true) }
    }
}

val evaluateArithmeticSubexpression = plan {
    explanation(Explanation.SimplifyExpressionInBrackets)
    pattern = condition(AnyPattern()) { it.hasBracket() }

    steps {
        whilePossible(evaluationSteps)
    }
}

val evaluateArithmeticExpression = plan {
    val expression = AnyPattern()
    pattern = condition(expression) { it.isArithmeticExpression() }
    explanation(Explanation.EvaluateArithmeticExpression, move(expression))

    steps {
        whilePossible {
            firstOf {
                option(addClarifyingBrackets)
                option(NormalizationRules.RemoveOuterBracket)

                option {
                    deeply(evaluateArithmeticSubexpression, deepFirst = true)
                }

                option {
                    whilePossible(evaluationSteps)
                }
            }
        }
    }
}

// Auxiliary steps used in several plans
val simplifyIntegersInExpression = steps {
    whilePossible {
        firstOf {
            option { deeply(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) }
            option { deeply(simplifyIntegersInProduct) }
            option { deeply(simplifyIntegersInSum) }
        }
    }
}
