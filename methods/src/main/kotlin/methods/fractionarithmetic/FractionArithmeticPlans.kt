package methods.fractionarithmetic

import engine.expressions.Expression
import engine.expressions.base
import engine.expressions.denominator
import engine.expressions.exponent
import engine.methods.Plan
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.operators.BinaryExpressionOperator
import engine.operators.IntegerOperator
import engine.operators.UnaryExpressionOperator
import engine.patterns.AnyPattern
import engine.patterns.IntegerFractionPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.commutativeSumContaining
import engine.patterns.fractionOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.sumContaining
import engine.steps.metadata.Skill
import engine.utility.gcd
import methods.general.GeneralPlans
import methods.general.GeneralRules
import methods.general.normalizeNegativeSigns
import methods.general.removeRedundantBrackets
import methods.integerarithmetic.IntegerArithmeticPlans
import methods.integerarithmetic.IntegerArithmeticRules
import methods.integerarithmetic.simplifyIntegersInExpression
import java.math.BigInteger

enum class FractionArithmeticPlans(override val runner: Plan) : RunnerMethod {

    NormalizeSignsInFraction(
        plan {
            explanation = Explanation.NormalizeSignsInFraction

            steps {
                whilePossible {
                    firstOf {
                        option {
                            deeply {
                                applyTo(GeneralRules.FactorMinusFromSum) {
                                    if (it.parent?.operator == BinaryExpressionOperator.Fraction) it else null
                                }
                            }
                        }
                        option { deeply(FractionArithmeticRules.SimplifyNegativeNumeratorAndDenominator) }
                        option { deeply(FractionArithmeticRules.SimplifyNegativeInNumerator) }
                        option { deeply(FractionArithmeticRules.SimplifyNegativeInDenominator) }
                        option { deeply(normalizeNegativeSigns) }
                    }
                }
            }
        }
    ),
    NormalizeFractions(
        plan {
            // Normalize fractions within fractions
            explanation = Explanation.NormalizeFractionsAndDivisions

            steps {
                whilePossible {
                    deeply {
                        firstOf {
                            option(GeneralPlans.RewriteDivisionsAsFractions)
                            option(FractionArithmeticRules.SimplifyFractionWithFractionDenominator)
                            option(FractionArithmeticRules.SimplifyFractionWithFractionNumerator)
                        }
                    }
                }
            }
        }
    ),
    SimplifyFraction(
        plan {
            val f = fractionOf(AnyPattern(), AnyPattern())
            pattern = f

            explanation = Explanation.SimplifyFraction
            explanationParameters(f)

            skill(Skill.SimplifyNumericFraction, f)

            steps {
                whilePossible {
                    firstOf {
                        option(GeneralRules.SimplifyUnitFractionToOne)
                        option(GeneralRules.SimplifyFractionWithOneDenominator)
                        option(FractionArithmeticRules.SimplifyFractionToInteger)
                        option(GeneralRules.CancelCommonTerms)
                        option(GeneralRules.CancelDenominator)
                        option(FractionArithmeticRules.FindCommonFactorInFraction)
                    }
                }
            }
        }
    ),

    EvaluateFractionSum(evaluateFractionSum),
    EvaluateSumOfFractionAndInteger(evaluateSumOfFractionAndInteger),

    MultiplyAndSimplifyFractions(
        plan {
            explanation = Explanation.MultiplyAndSimplifyFractions

            steps {
                whilePossible(FractionArithmeticRules.TurnFactorIntoFractionInProduct)
                apply {
                    whilePossible(FractionArithmeticRules.MultiplyFractions)
                }
                optionally(SimplifyFraction)
                whilePossible {
                    deeply(IntegerArithmeticPlans.SimplifyIntegersInProduct)
                }
            }
        }
    ),

    /**
     * [2 ^ [11/3]] --> [2 ^ [3 2/3]] --> [2 ^ 3 + [2 / 3]]
     * --> [2 ^ 3] * [2 ^ [2 / 3]]
     */
    SplitRationalExponent(
        plan {
            pattern = powerOf(AnyPattern(), IntegerFractionPattern())
            explanation = Explanation.SplitRationalExponent

            steps {
                applyTo(FractionArithmeticRules.ConvertImproperFractionToSumOfIntegerAndFraction) { it.exponent() }
                apply(GeneralRules.DistributeSumOfPowers)
            }
        }
    )
}

private val evaluateFractionSum = plan {
    val f1 = optionalNegOf(fractionOf(AnyPattern(), AnyPattern()))
    val f2 = optionalNegOf(fractionOf(AnyPattern(), AnyPattern()))

    pattern = sumContaining(f1, f2)

    explanation = Explanation.EvaluateFractionSum
    explanationParameters(f1, f2)

    skill(Skill.AddFractions, f1, f2)

    partialSumSteps {
        optionally {
            plan {
                explanation = Explanation.EvaluateProductsInNumeratorAndDenominator
                steps {
                    whilePossible {
                        deeply(IntegerArithmeticRules.EvaluateIntegerProductAndDivision)
                    }
                }
            }
        }
        optionally(FractionArithmeticRules.BringToCommonDenominator)
        optionally {
            plan {
                explanation = Explanation.EvaluateProductsInNumeratorAndDenominator

                steps {
                    whilePossible { deeply(IntegerArithmeticRules.EvaluateIntegerProductAndDivision) }
                }
            }
            check {
                val f1 = it.firstChild.asFraction()
                val f2 = it.secondChild.asFraction()
                f1 != null && f2 != null && gcd(
                    f1.numerator,
                    f1.denominator,
                    f2.numerator,
                    f2.denominator
                ) == BigInteger.ONE
            }
        }
        apply(FractionArithmeticRules.AddLikeFractions)
        plan {
            explanation = Explanation.EvaluateSumInNumerator

            steps {
                deeply(IntegerArithmeticRules.EvaluateSignedIntegerAddition)
            }
        }
        optionally(FractionArithmeticPlans.NormalizeSignsInFraction)
        optionally(FractionArithmeticPlans.SimplifyFraction)
    }
}

private data class NumericFraction(val numerator: BigInteger, val denominator: BigInteger) {
    fun neg() = NumericFraction(-numerator, denominator)
}

private fun numericFraction(numerator: BigInteger?, denominator: BigInteger?) =
    numerator?.let { n -> denominator?.let { d -> NumericFraction(n, d) } }

private fun Expression.asFraction(): NumericFraction? = when (operator) {
    UnaryExpressionOperator.Minus -> firstChild.asPositiveIntegerFraction()?.neg()
    else -> asPositiveIntegerFraction()
}

private fun Expression.asPositiveIntegerFraction(): NumericFraction? = when (operator) {
    BinaryExpressionOperator.Fraction -> numericFraction(
        firstChild.asPositiveInteger(),
        secondChild.asPositiveInteger()
    )
    else -> null
}

private fun Expression.asPositiveInteger(): BigInteger? = when (val op = operator) {
    is IntegerOperator -> op.value
    else -> null
}

private val evaluateSumOfFractionAndInteger = plan {
    explanation = Explanation.EvaluateSumOfFractionAndInteger
    val f = optionalNegOf(IntegerFractionPattern())
    val n = SignedIntegerPattern()
    pattern = commutativeSumContaining(f, n)

    partialSumSteps {
        apply(FractionArithmeticRules.TurnSumOfFractionAndIntegerToFractionSum)
        deeply(IntegerArithmeticRules.EvaluateIntegerProductAndDivision)
        apply(FractionArithmeticPlans.EvaluateFractionSum)
    }
}

val simplifyIntegerToNegativePower = steps {
    firstOf {
        option(FractionArithmeticRules.TurnIntegerToMinusOneToFraction)

        option {
            plan {
                explanation = Explanation.EvaluateIntegerToNegativePower

                steps {
                    apply(FractionArithmeticRules.TurnNegativePowerOfIntegerToFraction)
                    applyTo(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) { it.denominator() }
                }
            }
        }

        option {
            plan {
                explanation = Explanation.EvaluateIntegerToNegativePower

                steps {
                    // [0 ^ -n] -> [[1 / 0] ^ n]
                    apply(FractionArithmeticRules.TurnNegativePowerOfZeroToPowerOfFraction)
                    applyTo(GeneralRules.SimplifyZeroDenominatorFractionToUndefined) { it.base() }
                }
            }
        }
    }
}

// Auxiliary steps used in several plans
val simplifyFractionsInExpression = steps {
    whilePossible {
        firstOf {
            option { deeply(simplifyIntegersInExpression) }
            option { deeply(FractionArithmeticPlans.EvaluateFractionSum) }
            option { deeply(FractionArithmeticPlans.EvaluateSumOfFractionAndInteger) }
        }
    }
}

val simplifyAfterCollectingLikeTerms = steps {
    apply(simplifyFractionsInExpression)
    optionally { deeply(GeneralRules.MoveSignOfNegativeFactorOutOfProduct) }
    optionally { deeply(removeRedundantBrackets) }
    optionally { deeply(FractionArithmeticPlans.MultiplyAndSimplifyFractions) }
}
