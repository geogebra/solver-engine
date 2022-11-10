package methods.integerrationalexponents

import engine.expressions.base
import engine.expressions.exponent
import engine.methods.Plan
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.patterns.IntegerFractionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.powerOf
import method.integerrationalexponents.Explanation
import methods.fractionarithmetic.FractionArithmeticPlans
import methods.fractionarithmetic.FractionArithmeticRules
import methods.fractionarithmetic.simplifyAfterCollectingLikeTerms
import methods.general.GeneralRules
import methods.general.NormalizationRules
import methods.integerarithmetic.IntegerArithmeticPlans
import methods.integerarithmetic.IntegerArithmeticRules
import methods.integerarithmetic.simplifyIntegersInExpression

enum class IntegerRationalExponentsPlans(override val runner: Plan) : RunnerMethod {

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
        }
    ),

    /**
     * [2 ^ [11/3]] --> [2 ^ [3 2/3]] --> [2 ^ 3 + [2 / 3]]
     * --> [2 ^ 3] * [2 ^ [2 / 3]]
     */
    SplitRationalExponent(
        plan {
            pattern = powerOf(UnsignedIntegerPattern(), IntegerFractionPattern())
            explanation = Explanation.SplitRationalExponent

            steps {
                applyTo(FractionArithmeticRules.ConvertImproperFractionToSumOfIntegerAndFraction) { it.exponent() }
                apply(GeneralRules.DistributeSumOfPowers)
            }
        }
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
                            whilePossible { deeply(SplitRationalExponent) }
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
    ),

    SimplifyProductOfPowersWithSameBase(
        plan {
            explanation = Explanation.SimplifyProductOfPowersWithSameBase

            steps {
                apply(GeneralRules.RewriteProductOfPowersWithSameBase)
                firstOf {
                    option { deeply(FractionArithmeticPlans.EvaluateFractionSum) }
                    option { deeply(IntegerArithmeticPlans.EvaluateSumOfIntegers) }
                }
            }
        }
    ),

    SimplifyProductOfPowersWithInverseFractionBase(
        plan {
            explanation = Explanation.SimplifyProductOfPowersWithInverseFractionBase

            steps {
                apply(GeneralRules.RewriteProductOfPowersWithInverseFractionBase)
                apply(SimplifyProductOfPowersWithSameBase)
            }
        }
    ),

    SimplifyProductOfPowersWithInverseBase(
        plan {
            explanation = Explanation.SimplifyProductOfPowersWithInverseBase

            steps {
                apply(GeneralRules.RewriteProductOfPowersWithInverseBase)
                apply(SimplifyProductOfPowersWithSameBase)
            }
        }
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
        }
    ),

    SimplifyProductOfPowersWithNegatedExponent(
        plan {
            explanation = Explanation.SimplifyProductOfPowersWithNegatedExponent

            steps {
                apply(GeneralRules.RewriteProductOfPowersWithNegatedExponent)
                apply(SimplifyProductOfPowersWithSameExponent)
            }
        }
    ),

    SimplifyFractionOfPowersWithSameBase(
        plan {
            explanation = Explanation.SimplifyFractionOfPowersWithSameBase

            steps {
                apply(GeneralRules.RewriteFractionOfPowersWithSameBase)
                applyTo(FractionArithmeticPlans.EvaluateFractionSum) { it.exponent() }
            }
        }
    ),

    SimplifyFractionOfPowersWithSameExponent(
        plan {
            explanation = Explanation.SimplifyFractionOfPowersWithSameExponent

            steps {
                apply(GeneralRules.RewriteFractionOfPowersWithSameExponent)
                optionally { applyTo(FractionArithmeticPlans.SimplifyFraction) { it.base() } }
            }
        }
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
        }
    ),

    /**
     * Collect and simplify all terms containing a rational exponent of an
     * integer (with a rational coefficient)
     */
    CollectLikeRationalPowersAndSimplify(
        plan {
            explanation = Explanation.CollectLikeRationalPowersAndSimplify

            steps {
                apply(IntegerRationalExponentsRules.CollectLikeRationalPowers)
                apply(simplifyAfterCollectingLikeTerms)
            }
        }
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

val simplifyProductOfPowersWithInverseFractionBase = engine.methods.plan {
    explanation = Explanation.SimplifyProductOfPowersWithInverseFractionBase

    steps {
        apply(GeneralRules.RewriteProductOfPowersWithInverseFractionBase)
        apply(IntegerRationalExponentsPlans.SimplifyProductOfPowersWithSameBase)
    }
}
