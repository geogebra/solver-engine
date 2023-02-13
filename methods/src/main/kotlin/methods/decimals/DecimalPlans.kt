package methods.decimals

import engine.context.Curriculum
import engine.context.ResourceData
import engine.expressions.Expression
import engine.expressions.denominator
import engine.expressions.numerator
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.operators.BinaryExpressionOperator
import engine.operators.DecimalOperator
import engine.operators.IntegerOperator
import engine.patterns.AnyPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import methods.decimals.DecimalRules.StripTrailingZerosAfterDecimal
import methods.fractionarithmetic.FractionArithmeticPlans
import methods.general.GeneralRules
import methods.general.NormalizationPlans
import methods.general.NormalizationRules
import methods.general.removeRedundantBrackets
import methods.integerarithmetic.IntegerArithmeticPlans
import methods.integerarithmetic.IntegerArithmeticRules
import methods.integerarithmetic.arithmeticOperators

enum class DecimalPlans(override val runner: CompositeMethod) : RunnerMethod {
    EvaluateSumOfDecimals(
        plan {
            pattern = sumContaining()
            explanation = Explanation.EvaluateSumOfDecimals
            explanationParameters(pattern)

            steps {
                whilePossible(DecimalRules.EvaluateSignedDecimalAddition)
            }
        },
    ),
    EvaluateProductOfDecimals(
        plan {
            pattern = productContaining()
            explanation = Explanation.EvaluateProductOfDecimals
            explanationParameters(pattern)

            steps {
                whilePossible(DecimalRules.EvaluateDecimalProductAndDivision)
            }
        },
    ),

    EvaluateDecimalPower(
        plan {
            val base = SignedNumberPattern()
            val exponent = UnsignedIntegerPattern()
            pattern = powerOf(base, exponent)
            explanation = Explanation.EvaluateDecimalPower
            explanationParameters(base, exponent)

            steps {
                firstOf {
                    option(GeneralRules.EvaluateZeroToThePowerOfZero)
                    // at this point the base is guaranteed to be nonzero
                    option(GeneralRules.EvaluateExpressionToThePowerOfZero)
                    option(GeneralRules.SimplifyExpressionToThePowerOfOne)
                    // at this point the exponent is guaranteed to be > 0
                    option(GeneralRules.EvaluateZeroToAPositivePower)
                    option {
                        apply(GeneralRules.RewritePowerAsProduct)
                        apply(DecimalPlans.EvaluateProductOfDecimals)
                    }
                    option(DecimalRules.EvaluateDecimalPowerDirectly)
                }
            }
        },
    ),

    StripTrailingZerosAfterDecimalOfAllDecimals(
        plan {
            explanation = Explanation.StripTrailingZerosAfterDecimalOfAllDecimals

            steps {
                whilePossible { deeply(StripTrailingZerosAfterDecimal) }
            }
        },
    ),

    ConvertTerminatingDecimalToFractionAndSimplify(
        plan {
            explanation = Explanation.ConvertTerminatingDecimalToFractionAndSimplify

            steps {
                apply(DecimalRules.ConvertTerminatingDecimalToFraction)
                optionally(FractionArithmeticPlans.SimplifyFraction)
            }
        },
    ),
    ConvertRecurringDecimalToFractionAndSimplify(
        plan {
            explanation = Explanation.ConvertRecurringDecimalToFractionAndSimplify

            steps(ResourceData(curriculum = Curriculum.EU)) {
                apply(DecimalRules.ConvertRecurringDecimalToFractionDirectly)
                deeply(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
                optionally(FractionArithmeticPlans.SimplifyFraction)
            }

            alternative(ResourceData(curriculum = Curriculum.US)) {
                apply(DecimalRules.ConvertRecurringDecimalToEquation)
                apply(DecimalRules.MakeEquationSystemForRecurringDecimal)
                apply(DecimalRules.SimplifyEquationSystemForRecurringDecimal)
                apply(DecimalRules.SolveLinearEquation)
                optionally(FractionArithmeticPlans.SimplifyFraction)
            }
        },
    ),
    SimplifyDecimalsInProduct(
        plan {
            pattern = productContaining()
            explanation = Explanation.SimplifyDecimalsInProduct
            explanationParameters(pattern)

            steps {
                whilePossible {
                    firstOf {
                        option(GeneralRules.EvaluateProductContainingZero)
                        option(DecimalRules.EvaluateDecimalProductAndDivision)
                        option(GeneralRules.EliminateOneInProduct)
                    }
                }
            }
        },
    ),
    NormalizeFractionOfDecimals(
        plan {
            explanation = Explanation.NormalizeFractionOfDecimals

            steps {
                apply(DecimalRules.MultiplyFractionOfDecimalsByPowerOfTen)
                whilePossible {
                    deeply(SimplifyDecimalsInProduct)
                }
            }
        },
    ),

    /**
     * Convert a "nice" fraction to a decimal if possible.  A fraction is called "nice" if its denominator divides a
     * power of 10.
     */
    ConvertNiceFractionToDecimal(
        plan {
            explanation = Explanation.ConvertNiceFractionToDecimal
            pattern = fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())

            steps {
                optionally(DecimalRules.ExpandFractionToPowerOfTenDenominator)
                optionally {
                    applyTo(DecimalRules.EvaluateDecimalProductAndDivision) { it.numerator() }
                }
                optionally {
                    applyTo(DecimalRules.EvaluateDecimalProductAndDivision) { it.denominator() }
                }
                apply(DecimalRules.ConvertFractionWithPowerOfTenDenominatorToDecimal)
            }
        },
    ),

    EvaluateSubexpressionAsDecimal(
        plan {
            explanation = Explanation.EvaluateExpressionInBracketsAsDecimal
            pattern = condition(AnyPattern()) { it.hasBracket() }

            steps {
                whilePossible(decimalEvaluationSteps)
            }
        },
    ),

    /**
     * Evaluate an expression as a terminating decimal
     */
    @PublicMethod
    EvaluateExpressionAsDecimal(
        plan {
            val expression = AnyPattern()
            pattern = condition(expression) { it.isDecimalExpression() }
            resultPattern = SignedNumberPattern()

            specificPlans(IntegerArithmeticPlans.EvaluateArithmeticExpression)

            explanation = Explanation.EvaluateExpressionAsDecimal

            steps {
                optionally(StripTrailingZerosAfterDecimalOfAllDecimals)
                whilePossible {
                    firstOf {
                        option(NormalizationPlans.AddClarifyingBrackets)
                        option(NormalizationRules.RemoveOuterBracket)

                        option {
                            deeply(DecimalPlans.EvaluateSubexpressionAsDecimal, deepFirst = true)
                        }

                        option {
                            whilePossible(decimalEvaluationSteps)
                        }
                    }
                }
            }
        },
    ),
}

val decimalEvaluationSteps = steps {
    whilePossible {
        firstOf {
            option { deeply(GeneralRules.EvaluateProductDividedByZeroAsUndefined, deepFirst = true) }
            option { deeply(GeneralRules.SimplifyZeroDenominatorFractionToUndefined, deepFirst = true) }
            option { deeply(removeRedundantBrackets, deepFirst = true) }

            option(FractionArithmeticPlans.NormalizeSignsInFraction)

            option { deeply(DecimalPlans.NormalizeFractionOfDecimals, deepFirst = true) }
            option { deeply(FractionArithmeticPlans.SimplifyFraction, deepFirst = true) }
            option { deeply(GeneralRules.SimplifyDoubleMinus, deepFirst = true) }
            option { deeply(DecimalPlans.ConvertNiceFractionToDecimal, deepFirst = true) }
            option { deeply(DecimalPlans.EvaluateDecimalPower, deepFirst = true) }
            option { deeply(DecimalPlans.EvaluateProductOfDecimals, deepFirst = true) }
            option { deeply(DecimalPlans.EvaluateSumOfDecimals, deepFirst = true) }
            option { deeply(FractionArithmeticPlans.MultiplyAndSimplifyFractions, deepFirst = true) }
            option { deeply(DecimalRules.TurnDivisionOfDecimalsIntoFraction, deepFirst = true) }
        }
    }
}

private fun Expression.isDecimalExpression(): Boolean {
    val validOperator = operator is IntegerOperator || operator is DecimalOperator ||
        arithmeticOperators.contains(operator) || operator == BinaryExpressionOperator.Fraction

    return validOperator && children().all { it.isDecimalExpression() }
}
