package methods.general

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class GeneralExplanation : CategorisedMetadataKey {
    /**
     * Multiply the exponents using power rule, but don't
     * evaluate the product of exponents i.e. [( [a^b] ) ^ c] = [ a ^ b*c]
     * for e.g. [ ([2^6]) ^ 2] --> [ 2^6*2 ]
     */
    MultiplyExponentsUsingPowerRule,

    /**
     * distribute sum of powers to a base as a product of exponents with same base,
     * i.e. [base ^ exp1 + ... + expN] --> [base ^ exp1] * ... * [base ^ expN]
     */
    DistributeSumOfPowers,
    ReplaceInvisibleBrackets,
    RemoveBracketSumInSum,
    RemoveBracketProductInProduct,
    RemoveBracketAroundSignedIntegerInSum,
    RemoveRedundantBracket,
    RemoveRedundantPlusSign,
    EliminateOneInProduct,
    EliminateZeroInSum,
    EvaluateProductContainingZero,
    EvaluateZeroDividedByAnyValue,

    /**
     * Dividing by %1 is undefined
     *
     * %1 is the instance of 0 that the expression is divided by
     *
     * E.g. 3 * 5 : 0 -> UNDEFINED
     */
    EvaluateProductDividedByZeroAsUndefined,
    SimplifyDoubleMinus,
    SimplifyProductWithTwoNegativeFactors,
    MoveSignOfNegativeFactorOutOfProduct,
    CancelCommonTerms,
    SimplifyZeroNumeratorFractionToZero,
    SimplifyZeroDenominatorFractionToUndefined,
    SimplifyUnitFractionToOne,
    SimplifyFractionWithOneDenominator,
    CancelDenominator,
    AddClarifyingBrackets,
    FactorMinusFromSum,
    SimplifyProductOfConjugates,
    DistributePowerOfProduct,
    ExpandBinomialSquared,
    RewriteDivisionAsFraction,

    /**
     * Rewrite all divisions as fractions in an expression
     *
     * E.g. 3 : 4 + 4 * ( 2 - 1 : 2) -> [3 / 4] + 4 * (2 - [1 / 2])
     */
    RewriteDivisionsAsFractionInExpression,
    NormalizeExpression,
    DistributeMultiplicationOverSum,

    /**
     * Rewrite a power as a product
     *
     * E.g. [5 ^ 3] -> 5 * 5 * 5
     */
    RewritePowerAsProduct,

    /**
     * Raising to the power of 1 has no effect
     *
     * E.g. [(x + 1) ^ 1] -> x + 1
     */
    SimplifyExpressionToThePowerOfOne,

    /**
     * [0 ^ 0] is undefined
     */
    EvaluateZeroToThePowerOfZero,

    /**
     * Any (non-zero) value to the power of 0 is 1
     *
     * E.g. [(1 + 1) ^ 0] -> 1
     */
    EvaluateExpressionToThePowerOfZero,

    /**
     * cancels any two additive inverse elements in a sum, i.e. `a + X - a + Y = X + Y`
     * e.g. `sqrt[12] + 1 - sqrt[12] + 2` --> `1 + 2`
     * another e.g. `-root[3, 3] + root[3, 3] --> 0` (return `0`)
     */
    CancelAdditiveInverseElements,

    /**
     * 0 to any positive power is 0
     *
     * E.g. [0 ^ 3] -> 0
     */
    EvaluateZeroToAPositivePower,

    /**
     * 1 to any power is 1
     *
     * E.g. [1 ^ [2 / 3]] -> 1
     */
    EvaluateOneToAnyPower,

    /**
     * Convert [a ^ b] * [a ^ c] to [a ^ b + c]
     *
     * E.g. [3 ^ [1 / 2]] * [3 ^ [2 / 3]] -> [3 ^ [1 / 2] + [2 / 3]]
     */
    RewriteProductOfPowersWithSameBase,

    /**
     * Convert [a ^ c] * [b ^ c] to [(a * b) ^ c]
     *
     * E.g. [3 ^ [2 / 3]] * [2 ^ [2 / 3]] -> [(3 * 2) ^ [2 / 3]]
     */
    RewriteProductOfPowersWithSameExponent,

    /**
     * Flip a fraction under a negative power.
     *
     * E.g. [([2 / 3]) ^ -[4 / 5]] -> [([3 / 2]) ^ [4 / 5]]
     */
    FlipFractionUnderNegativePower,

    /**
     * If in a product there are two powers whose exponents are the negations
     * of each other, then inverts the base of the negated to get equal exponents.
     *
     * E.g. [2 ^ [1 / 2]] * [([4 / 3]) ^ -[1 / 2]] -> [2 ^ [1 / 2]] * [([3 / 4]) ^ [1 / 2]]
     * or [2 ^ [1 / 2]] * [3 ^ -[1 / 2]] -> [2 ^ [1 / 2]] * [([1 / 3]) ^ [1 / 2]]
     */
    RewriteProductOfPowersWithNegatedExponent,

    /**
     * If in a product there are two powers such that one of them is the reciprocal
     * of the other, then it inverts that to get equal bases.
     *
     * E.g. [2 ^ [1 / 2]] * [([1 / 2]) ^ [2 / 5]] -> [2 ^ [1 / 2]] * [2 ^ -[2 / 5]]
     */
    RewriteProductOfPowersWithInverseBase,

    /**
     * If in a product there are two powers of fractions, such that one of the
     * fractions is the inverse of the other, then it inverts one of them to get
     * equal bases.
     *
     * E.g. [([2 / 3]) ^ [1 / 2]] * [([3 / 2]) ^ [2 / 5]] -> [([2 / 3]) ^ [1 / 2]] * [([2 / 3]) ^ -[2 / 5]]
     */
    RewriteProductOfPowersWithInverseFractionBase;

    override val category = "General"
}

typealias Explanation = GeneralExplanation
