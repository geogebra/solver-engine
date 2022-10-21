package methods.approximation

import engine.expressionmakers.move
import engine.expressions.Expression
import engine.methods.plan
import engine.methods.steps
import engine.operators.BracketOperator
import engine.operators.DecimalOperator
import engine.operators.IntegerOperator
import engine.operators.RecurringDecimalOperator
import engine.patterns.AnyPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.condition
import engine.patterns.productContaining
import methods.decimals.evaluateSumOfDecimals
import methods.general.addClarifyingBrackets
import methods.general.evaluateProductDividedByZeroAsUndefined
import methods.general.evaluateZeroToThePowerOfZero
import methods.general.removeOuterBracket
import methods.general.removeRedundantBrackets
import methods.general.simplifyDoubleMinus
import methods.integerarithmetic.arithmeticOperators

private fun Expression.canBeApproximated(): Boolean {
    val validOperator = operator is IntegerOperator || operator is DecimalOperator ||
        operator is RecurringDecimalOperator || operator is BracketOperator ||
        arithmeticOperators.contains(operator)

    return validOperator && operands.all { it.canBeApproximated() }
}

val expandAndRoundRecurringDecimal = plan {
    explanation(Explanation.ExpandAndRoundRecurringDecimal)

    optionally(expandRecurringDecimal)
    apply(roundRecurringDecimal)
}

val approximateProductAndDivisionOfDecimals = plan {
    pattern = productContaining()
    explanation(Explanation.ApproximateProductAndDivisionOfDecimals, move(pattern))

    whilePossible(approximateDecimalProductAndDivision)
}

val approximationSteps = steps {
    firstOf {
        option { deeply(evaluateProductDividedByZeroAsUndefined, deepFirst = true) }
        option { deeply(removeRedundantBrackets, deepFirst = true) }
        option { deeply(simplifyDoubleMinus, deepFirst = true) }
        option { deeply(evaluateZeroToThePowerOfZero, deepFirst = true) }
        option { deeply(approximateDecimalPower, deepFirst = true) }
        option { deeply(approximateProductAndDivisionOfDecimals, deepFirst = true) }
        option { deeply(evaluateSumOfDecimals, deepFirst = true) }
    }
}

val approximateSubexpression = plan {
    explanation(Explanation.ApproximateExpressionInBrackets)
    pattern = condition(AnyPattern()) { it.hasBracket() }
    whilePossible(approximationSteps)
}

val approximateExpression = plan {
    pattern = condition(AnyPattern()) { it.canBeApproximated() }
    resultPattern = SignedNumberPattern()

    explanation(Explanation.ApproximateExpression, move(pattern))

    whilePossible {
        firstOf {
            option(addClarifyingBrackets)
            option(removeOuterBracket)

            option {
                deeply {
                    firstOf {
                        option(roundTerminatingDecimal)
                        option(expandAndRoundRecurringDecimal)
                    }
                }
            }

            option {
                deeply(approximateSubexpression, deepFirst = true)
            }

            option {
                whilePossible(approximationSteps)
            }
        }
    }
}
