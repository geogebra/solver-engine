package methods.decimals

import engine.context.Curriculum
import engine.context.ResourceData
import engine.expressions.DecimalExpression
import engine.expressions.DivideBy
import engine.expressions.Expression
import engine.expressions.Fraction
import engine.expressions.ValueExpression
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.steps
import engine.patterns.SignedNumberPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import methods.decimals.DecimalRules.StripTrailingZerosAfterDecimal
import methods.fractionarithmetic.FractionArithmeticPlans
import methods.fractionarithmetic.normalizeNegativeSignsInFraction
import methods.general.GeneralRules
import methods.general.NormalizationPlans
import methods.general.createEvaluateAbsoluteValuePlan
import methods.general.inlineSumsAndProducts
import methods.integerarithmetic.IntegerArithmeticPlans
import methods.integerarithmetic.IntegerArithmeticRules

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
                        apply(EvaluateProductOfDecimals)
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
                        option(DecimalRules.EvaluateDecimalProductAndDivision)
                        option(GeneralRules.RemoveUnitaryCoefficient)
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
                    applyToKind<Fraction>(DecimalRules.EvaluateDecimalProductAndDivision) { it.numerator }
                }
                optionally {
                    applyToKind<Fraction>(DecimalRules.EvaluateDecimalProductAndDivision) { it.denominator }
                }
                apply(DecimalRules.ConvertFractionWithPowerOfTenDenominatorToDecimal)
            }
        },
    ),

    EvaluateSubexpressionAsDecimal(
        plan {
            explanation = Explanation.EvaluateExpressionInBracketsAsDecimal
            pattern = condition { it.hasBracket() }

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
            pattern = condition { it is ValueExpression && it.isConstant() && it.hasDecimalOrFractionOrDivideBy() }
            resultPattern = SignedNumberPattern()

            specificPlans(IntegerArithmeticPlans.EvaluateArithmeticExpression)

            explanation = Explanation.EvaluateExpressionAsDecimal

            steps {
                optionally(StripTrailingZerosAfterDecimalOfAllDecimals)
                optionally(NormalizationPlans.NormalizeExpression)
                whilePossible {
                    deeply(EvaluateSubexpressionAsDecimal, deepFirst = true)
                }
                whilePossible(decimalEvaluationSteps)
            }
        },
    ),
}

val decimalEvaluationSteps: StepsProducer = steps {
    firstOf {
        option { deeply(GeneralRules.EvaluateProductDividedByZeroAsUndefined, deepFirst = true) }
        option { deeply(GeneralRules.SimplifyZeroDenominatorFractionToUndefined, deepFirst = true) }

        option { deeply(GeneralRules.EvaluateProductContainingZero) }

        option { deeply(inlineSumsAndProducts, deepFirst = true) }

        option { deeply(evaluateDecimalAbsoluteValue) }

        option { deeply(normalizeNegativeSignsInFraction) }

        option { deeply(GeneralRules.SimplifyDoubleMinus, deepFirst = true) }
        option { deeply(DecimalPlans.EvaluateDecimalPower, deepFirst = true) }
        option {
            deeply(deepFirst = true) {
                firstOf {
                    option(DecimalPlans.NormalizeFractionOfDecimals)
                    option(FractionArithmeticPlans.SimplifyFraction)
                    option(DecimalPlans.ConvertNiceFractionToDecimal)
                    option(DecimalPlans.EvaluateProductOfDecimals)
                    option(FractionArithmeticPlans.MultiplyAndSimplifyFractions)
                    option(DecimalRules.TurnDivisionOfDecimalsIntoFraction)
                }
            }
        }
        option { deeply(DecimalPlans.EvaluateSumOfDecimals, deepFirst = true) }
    }
}

private val evaluateDecimalAbsoluteValue =
    createEvaluateAbsoluteValuePlan(decimalEvaluationSteps)

private fun Expression.hasDecimalOrFractionOrDivideBy(): Boolean {
    return this is DecimalExpression || this is Fraction || this is DivideBy ||
        this.children.any { it.hasDecimalOrFractionOrDivideBy() }
}
