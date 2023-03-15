package methods.integerrationalexponents

import engine.expressions.base
import engine.expressions.exponent
import engine.methods.CompositeMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.patterns.IntegerFractionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.powerOf
import methods.fractionarithmetic.FractionArithmeticPlans
import methods.fractionarithmetic.addIntegerFractions
import methods.general.GeneralRules
import methods.general.NormalizationRules
import methods.integerarithmetic.IntegerArithmeticRules
import methods.integerarithmetic.simplifyIntegersInExpression

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
                applyTo(FractionArithmeticPlans.MultiplyAndSimplifyFractions) { it.exponent() }
            }
        },
    ),

    SimplifyRationalExponentOfInteger(
        plan {
            pattern = powerOf(UnsignedIntegerPattern(), IntegerFractionPattern())

            explanation = Explanation.SimplifyRationalExponentOfInteger

            steps {
                // input: 1350 ^ [2 / 5]

                // [ ( 2 * 3^3 * 5^2 ) ^ [2 / 5] ]
                optionally(IntegerRationalExponentsRules.FactorizeIntegerUnderRationalExponent)
                // [2 ^ [2 / 5]] * [ (3^3) ^ [2 / 5]] * [ (5^2) ^ [2 / 5]]
                optionally(GeneralRules.DistributePowerOfProduct)

                // [2 ^ [2 / 5] ] * [ 3 ^ [6 / 5] ] * [ 5 ^ [4 / 5] ]
                whilePossible { deeply(ApplyPowerRuleOfExponents) }

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
        },
    ),

    SimplifyProductOfPowersWithSameBase(
        plan {
            explanation = Explanation.SimplifyProductOfPowersWithSameBase

            steps {
                apply(GeneralRules.RewriteProductOfPowersWithSameBase)
                firstOf {
                    option { deeply(addIntegerFractions) }
                    option { deeply(IntegerArithmeticRules.EvaluateSignedIntegerAddition) }
                }
            }
        },
    ),

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
                applyTo(addIntegerFractions) { it.exponent() }
            }
        },
    ),

    SimplifyFractionOfPowersWithSameExponent(
        plan {
            explanation = Explanation.SimplifyFractionOfPowersWithSameExponent

            steps {
                apply(GeneralRules.RewriteFractionOfPowersWithSameExponent)
                optionally { applyTo(FractionArithmeticPlans.SimplifyFraction) { it.base() } }
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
}

val simplifyRationalExponentsInProduct = steps {
    whilePossible {
        firstOf {
            option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithSameBase) }
            option { deeply(IntegerRationalExponentsPlans.SimplifyRationalExponentOfInteger) }
            option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithInverseFractionBase) }
            option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithInverseBase) }
            option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithSameExponent) }
            option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithNegatedExponent) }
            option { deeply(IntegerRationalExponentsPlans.SimplifyFractionOfPowersWithSameBase) }
            option { deeply(IntegerRationalExponentsPlans.SimplifyFractionOfPowersWithSameExponent) }
            option { deeply(GeneralRules.FlipFractionUnderNegativePower) }
            option { deeply(IntegerRationalExponentsPlans.ApplyPowerRuleOfExponents) }
            option { deeply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithRationalExponents) }
        }
    }
}
