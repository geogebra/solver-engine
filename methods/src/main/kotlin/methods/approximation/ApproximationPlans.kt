package methods.approximation

import engine.expressions.Expression
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.operators.DecimalOperator
import engine.operators.IntegerOperator
import engine.operators.RecurringDecimalOperator
import engine.patterns.AnyPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.condition
import engine.patterns.productContaining
import methods.decimals.DecimalPlans
import methods.general.GeneralRules
import methods.general.NormalizationPlans
import methods.general.NormalizationRules
import methods.general.removeRedundantBrackets
import methods.integerarithmetic.IntegerArithmeticPlans
import methods.integerarithmetic.arithmeticOperators

private fun Expression.canBeApproximated(): Boolean {
    val validOperator = operator is IntegerOperator || operator is DecimalOperator ||
        operator is RecurringDecimalOperator || arithmeticOperators.contains(operator)

    return validOperator && children.all { it.canBeApproximated() }
}

enum class ApproximationPlans(override val runner: CompositeMethod) : RunnerMethod {
    ExpandAndRoundRecurringDecimal(
        plan {
            explanation = Explanation.ExpandAndRoundRecurringDecimal

            steps {
                optionally(ApproximationRules.ExpandRecurringDecimal)
                apply(ApproximationRules.RoundRecurringDecimal)
            }
        },
    ),
    ApproximateProductAndDivisionOfDecimals(
        plan {
            pattern = productContaining()

            explanation = Explanation.ApproximateProductAndDivisionOfDecimals
            explanationParameters(pattern)

            steps {
                whilePossible(ApproximationRules.ApproximateDecimalProductAndDivision)
            }
        },
    ),
    ApproximateSubexpression(
        plan {
            explanation = Explanation.ApproximateExpressionInBrackets
            pattern = condition(AnyPattern()) { it.hasBracket() }

            steps {
                whilePossible(approximationSteps)
            }
        },
    ),

    /**
     * Approximate an expression to a given number of decimal places.
     */
    @PublicMethod
    ApproximateExpression(
        plan {
            pattern = condition(AnyPattern()) { it.canBeApproximated() }
            resultPattern = SignedNumberPattern()

            specificPlans(IntegerArithmeticPlans.EvaluateArithmeticExpression)

            explanation = Explanation.ApproximateExpression
            explanationParameters(pattern)

            steps {
                whilePossible {
                    firstOf {
                        option(NormalizationPlans.AddClarifyingBrackets)
                        option(NormalizationRules.RemoveOuterBracket)

                        option {
                            deeply {
                                firstOf {
                                    option(ApproximationRules.RoundTerminatingDecimal)
                                    option(ApproximationPlans.ExpandAndRoundRecurringDecimal)
                                }
                            }
                        }

                        option {
                            deeply(ApproximationPlans.ApproximateSubexpression, deepFirst = true)
                        }

                        option {
                            whilePossible(approximationSteps)
                        }
                    }
                }
            }
        },
    ),
}

val approximationSteps = steps {
    firstOf {
        option { deeply(GeneralRules.EvaluateProductDividedByZeroAsUndefined, deepFirst = true) }
        option { deeply(removeRedundantBrackets, deepFirst = true) }
        option { deeply(GeneralRules.SimplifyDoubleMinus, deepFirst = true) }
        option { deeply(GeneralRules.EvaluateZeroToThePowerOfZero, deepFirst = true) }
        option { deeply(ApproximationRules.ApproximateDecimalPower, deepFirst = true) }
        option { deeply(ApproximationPlans.ApproximateProductAndDivisionOfDecimals, deepFirst = true) }
        option { deeply(DecimalPlans.EvaluateSumOfDecimals, deepFirst = true) }
    }
}
