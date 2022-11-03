package methods.approximation

import engine.expressionmakers.move
import engine.expressions.Expression
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.operators.BracketOperator
import engine.operators.DecimalOperator
import engine.operators.IntegerOperator
import engine.operators.RecurringDecimalOperator
import engine.patterns.AnyPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.condition
import engine.patterns.productContaining
import methods.decimals.evaluateSumOfDecimals
import methods.general.GeneralRules
import methods.general.NormalizationRules
import methods.general.addClarifyingBrackets
import methods.general.removeRedundantBrackets
import methods.integerarithmetic.arithmeticOperators

private fun Expression.canBeApproximated(): Boolean {
    val validOperator = operator is IntegerOperator || operator is DecimalOperator ||
        operator is RecurringDecimalOperator || operator is BracketOperator ||
        arithmeticOperators.contains(operator)

    return validOperator && operands.all { it.canBeApproximated() }
}

val expandAndRoundRecurringDecimal = plan {
    explanation(Explanation.ExpandAndRoundRecurringDecimal)

    steps {
        optionally(ApproximationRules.ExpandRecurringDecimal)
        apply(ApproximationRules.RoundRecurringDecimal)
    }
}

val approximateProductAndDivisionOfDecimals = plan {
    pattern = productContaining()
    explanation(Explanation.ApproximateProductAndDivisionOfDecimals, move(pattern))

    steps {
        whilePossible(ApproximationRules.ApproximateDecimalProductAndDivision)
    }
}

val approximationSteps = steps {
    firstOf {
        option { deeply(GeneralRules.EvaluateProductDividedByZeroAsUndefined, deepFirst = true) }
        option { deeply(removeRedundantBrackets, deepFirst = true) }
        option { deeply(GeneralRules.SimplifyDoubleMinus, deepFirst = true) }
        option { deeply(GeneralRules.EvaluateZeroToThePowerOfZero, deepFirst = true) }
        option { deeply(ApproximationRules.ApproximateDecimalPower, deepFirst = true) }
        option { deeply(approximateProductAndDivisionOfDecimals, deepFirst = true) }
        option { deeply(evaluateSumOfDecimals, deepFirst = true) }
    }
}

val approximateSubexpression = plan {
    explanation(Explanation.ApproximateExpressionInBrackets)
    pattern = condition(AnyPattern()) { it.hasBracket() }

    steps {
        whilePossible(approximationSteps)
    }
}

val approximateExpression = plan {
    pattern = condition(AnyPattern()) { it.canBeApproximated() }
    resultPattern = SignedNumberPattern()

    explanation(Explanation.ApproximateExpression, move(pattern))

    steps {
        whilePossible {
            firstOf {
                option(addClarifyingBrackets)
                option(NormalizationRules.RemoveOuterBracket)

                option {
                    deeply {
                        firstOf {
                            option(ApproximationRules.RoundTerminatingDecimal)
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
}
