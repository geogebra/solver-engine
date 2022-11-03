package methods.integerrationalexponents

import engine.expressions.base
import engine.expressions.exponent
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.patterns.IntegerFractionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.powerOf
import method.integerrationalexponents.Explanation
import methods.constantexpressions.simplifyAfterCollectingLikeTerms
import methods.fractionarithmetic.FractionArithmeticRules
import methods.fractionarithmetic.evaluateFractionSum
import methods.fractionarithmetic.multiplyAndSimplifyFractions
import methods.fractionarithmetic.simplifyFraction
import methods.general.GeneralRules
import methods.general.NormalizationRules
import methods.general.simplifyProductOfPowersWithSameBase
import methods.integerarithmetic.IntegerArithmeticRules
import methods.integerarithmetic.simplifyIntegersInExpression

/**
 * Transform [([x ^ a]) ^ b] to [x ^ a * b] and simplify the
 * product of exponents
 */
val applyPowerRuleOfExponents = plan {
    explanation(Explanation.PowerRuleOfExponents)

    steps {
        apply(GeneralRules.MultiplyExponentsUsingPowerRule)
        applyTo(multiplyAndSimplifyFractions) { it.exponent() }
    }
}

/**
 * [2 ^ [11/3]] --> [2 ^ [3 2/3]] --> [2 ^ 3 + [2 / 3]]
 * --> [2 ^ 3] * [2 ^ [2 / 3]]
 */
val splitRationalExponent = plan {
    pattern = powerOf(UnsignedIntegerPattern(), IntegerFractionPattern())
    explanation(Explanation.SplitRationalExponent)

    steps {
        applyTo(FractionArithmeticRules.ConvertImproperFractionToSumOfIntegerAndFraction) { it.exponent() }
        apply(GeneralRules.DistributeSumOfPowers)
    }
}

val simplifyRationalExponentOfInteger = plan {
    pattern = powerOf(UnsignedIntegerPattern(), IntegerFractionPattern())

    explanation(Explanation.SimplifyRationalExponentOfInteger)

    steps {
        // input: 1350 ^ [2 / 5]

        // [ ( 2 * 3^3 * 5^2 ) ^ [2 / 5] ]
        optionally(IntegerRationalExponentsRules.FactorizeIntegerUnderRationalExponent)
        // [2 ^ [2 / 5]] * [ (3^3) ^ [2 / 5]] * [ (5^2) ^ [2 / 5]]
        optionally(GeneralRules.DistributePowerOfProduct)

        // [2 ^ [2 / 5] ] * [ 3 ^ [6 / 5] ] * [ 5 ^ [4 / 5] ]
        whilePossible { deeply(applyPowerRuleOfExponents) }

        // [2 ^ [2 / 5] ] * [ 3 * 3 ^ [1 / 5] ] * [ 5 ^ [4 / 5] ]
        optionally {
            plan {
                explanation(Explanation.SplitProductOfExponentsWithImproperFractionPowers)

                steps {
                    whilePossible { deeply(splitRationalExponent) }
                    whilePossible { deeply(NormalizationRules.RemoveBracketProductInProduct) }
                }
            }
        }

        optionally {
            plan {
                explanation(Explanation.NormalizeRationalExponentsAndIntegers)

                steps {
                    optionally(IntegerRationalExponentsRules.NormaliseProductWithRationalExponents)
                    whilePossible { deeply(simplifyIntegersInExpression) }
                }
            }
        }
    }
}

val simplifyProductOfPowersWithInverseFractionBase = plan {
    explanation(Explanation.SimplifyProductOfPowersWithInverseFractionBase)

    steps {
        apply(GeneralRules.RewriteProductOfPowersWithInverseFractionBase)
        apply(simplifyProductOfPowersWithSameBase)
    }
}

val simplifyProductOfPowersWithInverseBase = plan {
    explanation(Explanation.SimplifyProductOfPowersWithInverseBase)

    steps {
        apply(GeneralRules.RewriteProductOfPowersWithInverseBase)
        apply(simplifyProductOfPowersWithSameBase)
    }
}

val simplifyProductOfPowersWithSameExponent = plan {
    explanation(Explanation.SimplifyProductOfPowersWithSameExponent)

    steps {
        apply(GeneralRules.RewriteProductOfPowersWithSameExponent)
        firstOf {
            option { deeply(IntegerArithmeticRules.EvaluateIntegerProductAndDivision) }
            option { deeply(multiplyAndSimplifyFractions) }
        }
    }
}

val simplifyProductOfPowersWithNegatedExponent = plan {
    explanation(Explanation.SimplifyProductOfPowersWithNegatedExponent)

    steps {
        apply(GeneralRules.RewriteProductOfPowersWithNegatedExponent)
        apply(simplifyProductOfPowersWithSameExponent)
    }
}

val simplifyFractionOfPowersWithSameBase = plan {
    explanation(Explanation.SimplifyFractionOfPowersWithSameBase)

    steps {
        apply(GeneralRules.RewriteFractionOfPowersWithSameBase)
        applyTo(evaluateFractionSum) { it.exponent() }
    }
}

val simplifyFractionOfPowersWithSameExponent = plan {
    explanation(Explanation.SimplifyFractionOfPowersWithSameExponent)

    steps {
        apply(GeneralRules.RewriteFractionOfPowersWithSameExponent)
        optionally { applyTo(simplifyFraction) { it.base() } }
    }
}

val simplifyProductOfPowersWithRationalExponents = plan {
    explanation(Explanation.SimplifyProductOfPowersWithRationalExponents)

    steps {
        plan {
            explanation(Explanation.BringRationalExponentsToSameDenominator)

            steps {
                apply(IntegerRationalExponentsRules.FindCommonDenominatorOfRationalExponents)
                whilePossible { deeply(IntegerArithmeticRules.EvaluateIntegerProductAndDivision) }
            }
        }
        apply(IntegerRationalExponentsRules.FactorDenominatorOfRationalExponents)
        whilePossible { deeply(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) }
        optionally { deeply(IntegerArithmeticRules.EvaluateIntegerProductAndDivision) }
        optionally { deeply(simplifyFraction) }
    }
}

val simplifyRationalExponentsInProduct = steps {
    whilePossible {
        firstOf {
            option { deeply(simplifyProductOfPowersWithSameBase) }
            option { deeply(simplifyRationalExponentOfInteger) }
            option { deeply(simplifyProductOfPowersWithInverseFractionBase) }
            option { deeply(simplifyProductOfPowersWithInverseBase) }
            option { deeply(simplifyProductOfPowersWithSameExponent) }
            option { deeply(simplifyProductOfPowersWithNegatedExponent) }
            option { deeply(simplifyFractionOfPowersWithSameBase) }
            option { deeply(simplifyFractionOfPowersWithSameExponent) }
            option { deeply(GeneralRules.FlipFractionUnderNegativePower) }
            option { deeply(applyPowerRuleOfExponents) }
            option { deeply(simplifyProductOfPowersWithRationalExponents) }
        }
    }
}

/**
 * Collect and simplify all terms containing a rational exponent of an
 * integer (with a rational coefficient)
 */
val collectLikeRationalPowersAndSimplify = plan {
    explanation(Explanation.CollectLikeRationalPowersAndSimplify)

    steps {
        apply(IntegerRationalExponentsRules.CollectLikeRationalPowers)
        apply(simplifyAfterCollectingLikeTerms)
    }
}
