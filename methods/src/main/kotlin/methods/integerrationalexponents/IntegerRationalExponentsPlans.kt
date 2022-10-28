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
import methods.fractionarithmetic.convertImproperFractionToSumOfIntegerAndFraction
import methods.fractionarithmetic.evaluateFractionSum
import methods.fractionarithmetic.multiplyAndSimplifyFractions
import methods.fractionarithmetic.simplifyFraction
import methods.general.distributePowerOfProduct
import methods.general.distributeSumOfPowers
import methods.general.flipFractionUnderNegativePower
import methods.general.multiplyExponentsUsingPowerRule
import methods.general.removeBracketProductInProduct
import methods.general.rewriteFractionOfPowersWithSameBase
import methods.general.rewriteFractionOfPowersWithSameExponent
import methods.general.rewriteProductOfPowersWithInverseBase
import methods.general.rewriteProductOfPowersWithInverseFractionBase
import methods.general.rewriteProductOfPowersWithNegatedExponent
import methods.general.rewriteProductOfPowersWithSameBase
import methods.general.rewriteProductOfPowersWithSameExponent
import methods.integerarithmetic.evaluateIntegerPowerDirectly
import methods.integerarithmetic.evaluateIntegerProductAndDivision
import methods.integerarithmetic.simplifyIntegersInExpression

/**
 * Transform [([x ^ a]) ^ b] to [x ^ a * b] and simplify the
 * product of exponents
 */
val applyPowerRuleOfExponents = plan {
    explanation(Explanation.PowerRuleOfExponents)

    steps {
        apply(multiplyExponentsUsingPowerRule)
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
        applyTo(convertImproperFractionToSumOfIntegerAndFraction) { it.exponent() }
        apply(distributeSumOfPowers)
    }
}

val simplifyRationalExponentOfInteger = plan {
    pattern = powerOf(UnsignedIntegerPattern(), IntegerFractionPattern())

    explanation(Explanation.SimplifyRationalExponentOfInteger)

    steps {
        // input: 1350 ^ [2 / 5]

        // [ ( 2 * 3^3 * 5^2 ) ^ [2 / 5] ]
        optionally(factorizeIntegerUnderRationalExponent)
        // [2 ^ [2 / 5]] * [ (3^3) ^ [2 / 5]] * [ (5^2) ^ [2 / 5]]
        optionally(distributePowerOfProduct)

        // [2 ^ [2 / 5] ] * [ 3 ^ [6 / 5] ] * [ 5 ^ [4 / 5] ]
        whilePossible { deeply(applyPowerRuleOfExponents) }

        // [2 ^ [2 / 5] ] * [ 3 * 3 ^ [1 / 5] ] * [ 5 ^ [4 / 5] ]
        optionally {
            plan {
                explanation(Explanation.SplitProductOfExponentsWithImproperFractionPowers)

                steps {
                    whilePossible { deeply(splitRationalExponent) }
                    whilePossible { deeply(removeBracketProductInProduct) }
                }
            }
        }

        optionally {
            plan {
                explanation(Explanation.NormalizeRationalExponentsAndIntegers)

                steps {
                    optionally(normaliseProductWithRationalExponents)
                    whilePossible { deeply(simplifyIntegersInExpression) }
                }
            }
        }
    }
}

val simplifyProductOfPowersWithSameBase = plan {
    explanation(Explanation.SimplifyProductOfPowersWithSameBase)

    steps {
        apply(rewriteProductOfPowersWithSameBase)
        apply { deeply(evaluateFractionSum) }
    }
}

val simplifyProductOfPowersWithInverseFractionBase = plan {
    explanation(Explanation.SimplifyProductOfPowersWithInverseFractionBase)

    steps {
        apply(rewriteProductOfPowersWithInverseFractionBase)
        apply(simplifyProductOfPowersWithSameBase)
    }
}

val simplifyProductOfPowersWithInverseBase = plan {
    explanation(Explanation.SimplifyProductOfPowersWithInverseBase)

    steps {
        apply(rewriteProductOfPowersWithInverseBase)
        apply(simplifyProductOfPowersWithSameBase)
    }
}

val simplifyProductOfPowersWithSameExponent = plan {
    explanation(Explanation.SimplifyProductOfPowersWithSameExponent)

    steps {
        apply(rewriteProductOfPowersWithSameExponent)
        firstOf {
            option { deeply(evaluateIntegerProductAndDivision) }
            option { deeply(multiplyAndSimplifyFractions) }
        }
    }
}

val simplifyProductOfPowersWithNegatedExponent = plan {
    explanation(Explanation.SimplifyProductOfPowersWithNegatedExponent)

    steps {
        apply(rewriteProductOfPowersWithNegatedExponent)
        apply(simplifyProductOfPowersWithSameExponent)
    }
}

val simplifyFractionOfPowersWithSameBase = plan {
    explanation(Explanation.SimplifyFractionOfPowersWithSameBase)

    steps {
        apply(rewriteFractionOfPowersWithSameBase)
        applyTo(evaluateFractionSum) { it.exponent() }
    }
}

val simplifyFractionOfPowersWithSameExponent = plan {
    explanation(Explanation.SimplifyFractionOfPowersWithSameExponent)

    steps {
        apply(rewriteFractionOfPowersWithSameExponent)
        optionally { applyTo(simplifyFraction) { it.base() } }
    }
}

val simplifyProductOfPowersWithRationalExponents = plan {
    explanation(Explanation.SimplifyProductOfPowersWithRationalExponents)

    steps {
        plan {
            explanation(Explanation.BringRationalExponentsToSameDenominator)

            steps {
                apply(findCommonDenominatorOfRationalExponents)
                whilePossible { deeply(evaluateIntegerProductAndDivision) }
            }
        }
        apply(factorDenominatorOfRationalExponents)
        whilePossible { deeply(evaluateIntegerPowerDirectly) }
        optionally { deeply(evaluateIntegerProductAndDivision) }
        optionally { deeply(simplifyFraction) }
    }
}

val simplifyRationalExponentsInProduct = steps {
    whilePossible {
        firstOf {
            option { deeply(simplifyRationalExponentOfInteger) }
            option { deeply(simplifyProductOfPowersWithSameBase) }
            option { deeply(simplifyProductOfPowersWithInverseFractionBase) }
            option { deeply(simplifyProductOfPowersWithInverseBase) }
            option { deeply(simplifyProductOfPowersWithSameExponent) }
            option { deeply(simplifyProductOfPowersWithNegatedExponent) }
            option { deeply(simplifyFractionOfPowersWithSameBase) }
            option { deeply(simplifyFractionOfPowersWithSameExponent) }
            option { deeply(flipFractionUnderNegativePower) }
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
        apply(collectLikeRationalPowers)
        apply(simplifyAfterCollectingLikeTerms)
    }
}
