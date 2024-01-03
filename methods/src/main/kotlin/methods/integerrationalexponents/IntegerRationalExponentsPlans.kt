package methods.integerrationalexponents

import engine.expressions.Fraction
import engine.expressions.Minus
import engine.expressions.Power
import engine.expressions.isSigned
import engine.methods.CompositeMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.steps
import engine.patterns.BuilderCondition
import engine.patterns.ConditionPattern
import engine.patterns.IntegerFractionPattern
import engine.patterns.RationalPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.optionalNegOf
import engine.patterns.optionalPowerOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import methods.fractionarithmetic.FractionArithmeticPlans
import methods.fractionarithmetic.addIntegerAndFraction
import methods.fractionarithmetic.addIntegerFractions
import methods.general.GeneralRules
import methods.general.NormalizationRules
import methods.integerarithmetic.IntegerArithmeticRules
import methods.integerarithmetic.simplifyIntegersInExpression
import java.math.BigInteger

enum class IntegerRationalExponentsPlans(override val runner: CompositeMethod) : RunnerMethod {
    /**
     * Transform [([x ^ a]) ^ b] to [x ^ a * b] and simplify the
     * product of exponents
     */
    ApplyPowerRuleOfExponents(
        plan {
            explanation = Explanation.PowerRuleOfExponents

            steps {
                apply(GeneralRules.MultiplyExponentsUsingPowerRule)
                optionally {
                    applyToKind<Power>(methods.general.GeneralPlans.NormalizeNegativeSignsInProduct) { it.exponent }
                }
                applyToKind<Power>(FractionArithmeticPlans.MultiplyAndSimplifyFractions) {
                    if (it.exponent is Minus) it.exponent.firstChild else it.exponent
                }
            }
        },
    ),

    SimplifyProductOfPowersWithSameBase(
        plan {
            explanation = Explanation.SimplifyProductOfPowersWithSameBase

            steps {
                apply(GeneralRules.RewriteProductOfPowersWithSameBase)
                firstOf {
                    option { deeply(addIntegerAndFraction) }
                    option { deeply(addIntegerFractions) }
                    option { deeply(IntegerArithmeticRules.EvaluateSignedIntegerAddition) }
                }
            }
        },
    ),

    SimplifyProductOfIntegerAndRationalExponentOfInteger(simplifyProductOfIntegerAndRationalExponentOfInteger),

    SimplifyProductOfPowersWithInverseFractionBase(
        plan {
            explanation = Explanation.SimplifyProductOfPowersWithInverseFractionBase

            steps {
                apply(GeneralRules.RewriteProductOfPowersWithInverseFractionBase)
                apply(SimplifyProductOfPowersWithSameBase)
            }
        },
    ),

    SimplifyProductOfPowersWithInverseBase(
        plan {
            explanation = Explanation.SimplifyProductOfPowersWithInverseBase

            steps {
                apply(GeneralRules.RewriteProductOfPowersWithInverseBase)
                apply(SimplifyProductOfPowersWithSameBase)
            }
        },
    ),

    SimplifyProductOfPowersWithSameExponent(
        plan {
            explanation = Explanation.SimplifyProductOfPowersWithSameExponent

            steps {
                apply(GeneralRules.RewriteProductOfPowersWithSameExponent)
                firstOf {
                    option { deeply(IntegerArithmeticRules.EvaluateIntegerProductAndDivision) }
                    option { deeply(FractionArithmeticPlans.MultiplyAndSimplifyFractions) }
                }
            }
        },
    ),

    SimplifyProductOfPowersWithNegatedExponent(
        plan {
            explanation = Explanation.SimplifyProductOfPowersWithNegatedExponent

            steps {
                apply(GeneralRules.RewriteProductOfPowersWithNegatedExponent)
                apply(SimplifyProductOfPowersWithSameExponent)
            }
        },
    ),

    SimplifyFractionOfPowersWithSameBase(
        plan {
            explanation = Explanation.SimplifyFractionOfPowersWithSameBase

            steps {
                apply(GeneralRules.RewriteFractionOfPowersWithSameBase)
                applyToKind<Power>(addIntegerFractions) { it.exponent }
            }
        },
    ),

    SimplifyFractionOfPowersWithSameExponent(
        plan {
            explanation = Explanation.SimplifyFractionOfPowersWithSameExponent

            steps {
                apply(GeneralRules.RewriteFractionOfPowersWithSameExponent)
                optionally { applyToKind<Power>(FractionArithmeticPlans.SimplifyFraction) { it.base } }
            }
        },
    ),

    SimplifyProductOfPowersWithRationalExponents(
        plan {
            explanation = Explanation.SimplifyProductOfPowersWithRationalExponents

            steps {
                plan {
                    explanation = Explanation.BringRationalExponentsToSameDenominator

                    steps {
                        apply(IntegerRationalExponentsRules.FindCommonDenominatorOfRationalExponents)
                        whilePossible { deeply(IntegerArithmeticRules.EvaluateIntegerProductAndDivision) }
                    }
                }
                apply(IntegerRationalExponentsRules.FactorDenominatorOfRationalExponents)
                whilePossible { deeply(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) }
                optionally { deeply(IntegerArithmeticRules.EvaluateIntegerProductAndDivision) }
                optionally { deeply(FractionArithmeticPlans.SimplifyFraction) }
            }
        },
    ),

    SimplifyRationalExponentOfInteger(simplifyRationalExponentOfInteger),
}

val simplifyRationalExponentsInProduct = steps {
    check { it.isConstant() }
    whilePossible {
        firstOf {
            option { deeply(IntegerRationalExponentsPlans.SimplifyFractionOfPowersWithSameBase) }
            option { deeply(IntegerRationalExponentsPlans.SimplifyFractionOfPowersWithSameExponent) }
            option { deeply(IntegerRationalExponentsRules.ApplyReciprocalPowerRule) }
            option { deeply(simplifyRationalExponentOfInteger) }
            option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithSameBase) }
            option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithInverseFractionBase) }
            option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithInverseBase) }
            option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfIntegerAndRationalExponentOfInteger) }
            option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithSameExponent) }
            option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithNegatedExponent) }
            option { deeply(IntegerRationalExponentsPlans.ApplyPowerRuleOfExponents) }
            option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithRationalExponents) }
        }
    }
}

private val simplifyRationalExponentOfIntegerAlways = simplifyRationalExponentOfInteger(
    IntegerRationalExponentsRules.FactorizeIntegerUnderRationalExponentAlways,
)

private val simplifyRationalExponentOfInteger = simplifyRationalExponentOfInteger(
    IntegerRationalExponentsRules.FactorizeIntegerUnderRationalExponent,
)

private fun simplifyRationalExponentOfInteger(factorizeIntegerUnderRationalExponent: StepsProducer) =
    plan {
        pattern = powerOf(UnsignedIntegerPattern(), optionalNegOf(IntegerFractionPattern()))

        explanation = Explanation.SimplifyRationalExponentOfInteger

        steps {
            // input: 1350 ^ [2 / 5]

            // [ ( 2 * 3^3 * 5^2 ) ^ [2 / 5] ]
            optionally(factorizeIntegerUnderRationalExponent)

            // [2 ^ [2 / 5]] * [ (3^3) ^ [2 / 5]] * [ (5^2) ^ [2 / 5]]
            optionally(GeneralRules.DistributePowerOfProduct)

            // [2 ^ [2 / 5] ] * [ 3 ^ [6 / 5] ] * [ 5 ^ [4 / 5] ]
            whilePossible { deeply(IntegerRationalExponentsPlans.ApplyPowerRuleOfExponents) }

            // [2 ^ [2 / 5] ] * [ 3 * 3 ^ [1 / 5] ] * [ 5 ^ [4 / 5] ]
            optionally {
                plan {
                    explanation = Explanation.SplitProductOfExponentsWithImproperFractionPowers

                    steps {
                        whilePossible { deeply(FractionArithmeticPlans.SplitRationalExponent) }
                        whilePossible { deeply(NormalizationRules.RemoveBracketProductInProduct) }
                    }
                }
            }

            optionally {
                plan {
                    explanation = Explanation.NormalizeRationalExponentsAndIntegers

                    steps {
                        optionally(IntegerRationalExponentsRules.NormaliseProductWithRationalExponents)
                        whilePossible { deeply(simplifyIntegersInExpression) }
                    }
                }
            }
        }
    }

private val simplifyProductOfIntegerAndRationalExponentOfInteger = plan {
    explanation = Explanation.SimplifyProductOfIntegerAndRationalExponentOfInteger

    val base1 = UnsignedIntegerPattern()
    val base2 = UnsignedIntegerPattern()

    val exponent1 = RationalPattern()
    val exponent2 = RationalPattern()

    val power1 = optionalPowerOf(base1, exponent1)
    val power2 = optionalPowerOf(base2, exponent2)

    val suitablePower2 = ConditionPattern(
        power2,
        BuilderCondition {
            if (!get(power1.exponent).isSigned<Fraction>() && !get(power2.exponent).isSigned<Fraction>()) {
                return@BuilderCondition false
            }

            if (get(power1.exponent) !is Minus && get(power2.exponent) !is Minus) {
                return@BuilderCondition false
            }

            getValue(base1).gcd(getValue(base2)) != BigInteger.ONE
        },
    )

    pattern = productContaining(power1, suitablePower2)

    partialExpressionSteps {
        applyToChildren {
            firstOf {
                option(GeneralRules.FactorizeInteger)
                option(simplifyRationalExponentOfIntegerAlways)
            }
        }
        whilePossible(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithSameBase)
    }
}
