package methods.integerrationalexponents

import engine.expressions.exponent
import engine.methods.plan
import engine.methods.steps
import engine.patterns.IntegerFractionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.powerOf
import method.integerrationalexponents.Explanation
import methods.fractionarithmetic.convertImproperFractionToSumOfIntegerAndFraction
import methods.fractionarithmetic.evaluateFractionSum
import methods.fractionarithmetic.multiplyAndSimplifyFractions
import methods.general.collectLikeTermsAndSimplify
import methods.general.distributePowerOfProduct
import methods.general.distributeSumOfPowers
import methods.general.multiplyExponentsUsingPowerRule
import methods.general.removeBracketProductInProduct
import methods.general.rewriteProductOfPowersWithSameBase
import methods.general.rewriteProductOfPowersWithSameExponent
import methods.integerarithmetic.evaluateIntegerPowerDirectly
import methods.integerarithmetic.evaluateIntegerProductAndDivision
import methods.integerarithmetic.simplifyIntegersInExpression
import methods.integerarithmetic.simplifyIntegersInProduct

/**
 * [ ( [x^a] ) ^ b ] --> [x^ (ab)]
 * where `ab` is the simplified product of 'a' and 'b'
 */
val applyPowerRuleOfExponents = plan {
    explanation(Explanation.PowerRuleOfExponents)

    optionally(multiplyExponentsUsingPowerRule)
    optionally {
        deeply(multiplyAndSimplifyFractions, deepFirst = true)
    }
}

/**
 * [2 ^ [11/3]] --> [2 ^ [3 2/3]] --> [2 ^ 3 + [2 / 3]]
 * --> [2 ^ 3] * [2 ^ [2 / 3]]
 */
val splitRationalExponent = plan {
    pattern = powerOf(UnsignedIntegerPattern(), IntegerFractionPattern())
    explanation(Explanation.SplitRationalExponent)

    applyTo(convertImproperFractionToSumOfIntegerAndFraction) { it.exponent() }
    apply(distributeSumOfPowers)
}

val simplifyRationalExponentOfInteger = plan {
    pattern = powerOf(UnsignedIntegerPattern(), IntegerFractionPattern())

    explanation(Explanation.SimplifyRationalExponentOfInteger)

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

            whilePossible { deeply(splitRationalExponent) }
            whilePossible { deeply(removeBracketProductInProduct) }
        }
    }

    optionally {
        plan {
            explanation(Explanation.NormalizeRationalExponentsAndIntegers)

            optionally(normaliseProductWithRationalExponents)
            whilePossible { deeply(simplifyIntegersInExpression) }
        }
    }
}

val simplifyProductOfPowersWithSameBase = plan {
    explanation(Explanation.SimplifyProductOfPowersWithSameBase)

    apply(rewriteProductOfPowersWithSameBase)
    apply { deeply(evaluateFractionSum) }
}

val simplifyProductOfPowersWithSameExponent = plan {
    explanation(Explanation.SimplifyProductOfPowersWithSameExponent)

    apply(rewriteProductOfPowersWithSameExponent)
    whilePossible {
        deeply(evaluateIntegerProductAndDivision)
    }
}

val simplifyProductOfPowersWithRationalExponents = plan {
    explanation(Explanation.SimplifyProductOfPowersWithRationalExponents)

    apply(bringRationalExponentsToSameDenominator)
    whilePossible { deeply(evaluateIntegerProductAndDivision) }
    apply(factorDenominatorOfRationalExponents)
    whilePossible { deeply(evaluateIntegerPowerDirectly) }
    deeply(evaluateIntegerProductAndDivision)
}

val simplifyRationalExponentsInProduct = steps {
    whilePossible {
        firstOf {
            option { deeply(simplifyIntegersInProduct) }
            option { deeply(simplifyRationalExponentOfInteger) }
            option { deeply(simplifyProductOfPowersWithSameBase) }
            option { deeply(simplifyProductOfPowersWithSameExponent) }
            option { deeply(simplifyProductOfPowersWithRationalExponents) }
        }
    }
}

/**
 * Use the method factory [collectLikeTermsAndSimplify] to collect
 * and simplify all terms containing a rational exponent of an integer
 * (with a rational coefficient)
 */
val collectLikeRationalPowersAndSimplify = collectLikeTermsAndSimplify(
    powerOf(UnsignedIntegerPattern(), IntegerFractionPattern()),
    Explanation.CollectLikeRationalPowersAndSimplify,
    Explanation.CollectLikeRationalPowers,
)
