package methods.general

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.LegacyKeyName
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
     * Rewrite power under root to enable it to cancel the common
     * factor between root order and exponent,
     * for e.g. root[ [7^6], 8] --> root[ [7^3*2], 4*2]
     * as gcd(8, 6) = 2, its factored out from both rootIndex and exponent
     */
    RewritePowerUnderRoot,

    /**
     * Cancels the common factor between rootIndex and exponent
     * of a power under root
     * for e.g. root[ [7^3*2], 5*2 ] --> root[ [7^3], 5 ]
     */
    CancelRootIndexAndExponent,

    /**
     * distribute sum of powers to a base as a product of exponents with same base,
     * i.e. [base ^ exp1 + ... + expN] --> [base ^ exp1] * ... * [base ^ expN]
     */
    DistributeSumOfPowers,

    /**
     * Add a bracket to make the expression clearer
     * E.g. sqrt(2)^2 -> (sqrt(2))^2
     */
    @LegacyKeyName("General.ReplaceInvisibleBrackets")
    AddClarifyingBracket,
    RemoveBracketSumInSum,
    RemoveBracketProductInProduct,

    /**
     * E.g. 1 + (-2) -> 1 - 2
     *  1 + -2 -> 1 - 2
     */
    @LegacyKeyName("General.RemoveBracketAroundSignedIntegerInSum")
    NormalizeNegativeSignOfIntegerInSum,

    RemoveRedundantBracket,
    RemoveRedundantPlusSign,

    RemoveUnitaryCoefficient,

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
    FactorMinusFromSum,
    SimplifyProductOfConjugates,
    DistributePowerOfProduct,

    NormalizeExpression,

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
     * Convert [[a ^ b] / [a ^ c]] to [a ^ b - c]
     *
     * E.g. [[3 ^ [1 / 2]] / [3 ^ [2 / 3]]] -> [3 ^ [1 / 2] - [2 / 3]]
     */
    RewriteFractionOfPowersWithSameBase,

    /**
     * Convert [[a ^ c] / [b ^ c]] to [([a / b]) ^ c]
     *
     * E.g. [[3 ^ [2 / 3]] / [2 ^ [2 / 3]]] -> [([3 / 2]) ^ [2 / 3]]
     */
    RewriteFractionOfPowersWithSameExponent,

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
    RewriteProductOfPowersWithInverseFractionBase,

    /**
     * Move sign out of odd root of negative
     *
     * E.g. root[-6, 3] -> -root[6, 3]
     */
    RewriteOddRootOfNegative,

    /**
     * Rewrite an integer order root as a power.
     *
     * E.g. root[x + 2, 3] -> [(x + 2) ^ [1/3]]
     */
    RewriteIntegerOrderRootAsPower,

    /**
     * Reorder a product in a canonical form:
     * 1. numeric constants
     * 2. constant roots
     * 3. constant sums
     * 4. variables or variable powers
     * 5. variable roots
     * 6. polynomials (i.e. non-constant sums)
     *
     * E.g. sqrt(3) * (1 + sqrt(3)) * (y^2 + 1) * y * 5 ->
     *  5 sqrt(3) (1 + sqrt(3)) y (y^2 + 1)
     */
    ReorderProduct,

    /**
     * Normalize product signs: remove redundant ones and add
     * clarifying signs.
     *
     * E.g. 3 * sqrt(2) -> 3 sqrt(2)
     *  3 x * y -> 3 x y
     *  sqrt(2) y -> sqrt(2) * y
     */
    @LegacyKeyName("General.NormaliseSimplifiedProduct")
    NormalizeProductSigns,

    /**
     * Normalize all the negative signs in a product (i.e. cancel pairs of
     * negative signs and potentially bring the remaining sign to the front)
     *
     * E.g. (-x)*(-y)*(-z) -> x*y*(-z) -> -x*y*z
     */
    NormalizeNegativeSignsInProduct,
    ;

    override val category = "General"
}

typealias Explanation = GeneralExplanation
