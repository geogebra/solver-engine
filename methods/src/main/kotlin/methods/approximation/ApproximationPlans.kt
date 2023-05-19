package methods.approximation

import engine.context.Context
import engine.expressions.DecimalExpression
import engine.expressions.Expression
import engine.expressions.RecurringDecimalExpression
import engine.expressions.xp
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.patterns.AnyPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.condition
import engine.patterns.productContaining
import engine.steps.Transformation
import engine.steps.metadata.Metadata
import methods.constantexpressions.ConstantExpressionsPlans
import methods.decimals.DecimalPlans
import methods.general.GeneralRules
import methods.general.NormalizationPlans
import methods.general.inlineSumsAndProducts
import methods.integerarithmetic.IntegerArithmeticPlans
import methods.integerarithmetic.hasArithmeticOperation
import java.math.RoundingMode

private fun Expression.canBeApproximated(): Boolean {
    val validOperator = this is DecimalExpression ||
        this is RecurringDecimalExpression || hasArithmeticOperation()

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
                        option(NormalizationPlans.NormalizeExpression)

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

    @PublicMethod
    EvaluateExpressionNumerically(
        object : CompositeMethod(specificPlans = listOf(ConstantExpressionsPlans.SimplifyConstantExpression)) {
            private val numberPattern = SignedNumberPattern()

            override fun run(ctx: Context, sub: Expression): Transformation? {
                if (numberPattern.matches(ctx, sub)) return null

                val numericValue = sub.doubleValue
                if (!numericValue.isFinite()) return null

                return Transformation(
                    type = Transformation.Type.Rule,
                    fromExpr = sub,
                    toExpr = xp(numericValue.toBigDecimal().setScale(ctx.effectivePrecision, RoundingMode.HALF_UP)),
                    explanation = Metadata(Explanation.EvaluateExpressionNumerically, listOf(sub)),
                )
            }
        },
    ),
}

val approximationSteps = steps {
    firstOf {
        option { deeply(GeneralRules.EvaluateProductDividedByZeroAsUndefined, deepFirst = true) }
        option { deeply(inlineSumsAndProducts, deepFirst = true) }
        option { deeply(GeneralRules.SimplifyDoubleMinus, deepFirst = true) }
        option { deeply(GeneralRules.EvaluateZeroToThePowerOfZero, deepFirst = true) }
        option { deeply(ApproximationRules.ApproximateDecimalPower, deepFirst = true) }
        option { deeply(ApproximationPlans.ApproximateProductAndDivisionOfDecimals, deepFirst = true) }
        option { deeply(DecimalPlans.EvaluateSumOfDecimals, deepFirst = true) }
    }
}
