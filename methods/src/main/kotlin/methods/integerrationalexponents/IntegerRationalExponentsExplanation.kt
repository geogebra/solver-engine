package method.integerrationalexponents

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class IntegerRationalExponentsExplanation : CategorisedMetadataKey {

    /**
     * factorize integer under rational exponent
     * e.g. [ 18^[2/3] ] --> [ ([2 * [3^2]]) ^ [2/3] ]
     */
    FactorizeIntegerUnderRationalExponent,

    /**
     * do two things one after the other to one exponent term:
     * 1. apply power rule of exponent, i.e. [ [a ^ b] ^ c] --> [ a ^ b*c ]
     * 2. simplify the power `b * c`
     */
    PowerRuleOfExponents,

    /**
     * brings the "integers" or exponents with integral powers to the front
     * e.g. [2 ^ [2 / 5]] * [3 ^ 2] * 5 --> [3 ^ 2] * 5 * [2 ^ [2 / 5]]
     */
    NormaliseProductWithRationalExponents,

    /**
     * splits product of exponents with rational powers,
     * e.g. [2 ^ [2 / 3]] * [3 ^ [10 / 3]] * [5 ^ [4 / 3]] -->
     * [2 ^ [2 / 3]] * [3 ^ 3] *  [3 ^ [1 / 3]] * 5 * [5 ^ [4 / 3]]
     */
    SplitProductOfExponentsWithImproperFractionPowers,

    SplitRationalExponent,

    /**
     * evaluates product of integers, integer exponents
     * and simplifies the product to bring integer to the beginning
     * of the expression
     * for e.g. 2 * [2 ^ [1/5]] * 5 * [5 ^ [2/3]] * [7^2] -->
     * 2 * 5 * [7^2] * [2 ^ [1/5]] * [5 ^ [2/3]] -->
     * 490 * [2^[1/5]] * [5^[2/3]]
     */
    NormalizeRationalExponentsAndIntegers,

    /**
     * simplifies rational exponent of an integer
     */
    SimplifyRationalExponentOfInteger,

    /**
     * Transforms two powers with rational exponents in a product
     * such that the exponents have the same denominator
     * E.g. [3 ^ [1 / 2]] * [5 ^ [2 / 3]] -> [3 ^ [3 * 1 / 3 * 2]] * [5 ^ [2 * 2 / 2 * 3]]
     */
    BringRationalExponentsToSameDenominator,

    /**
     * Transforms two powers with rational exponents with the
     * same denominator in a product by extracting the denominator
     * E.g. [3 ^ [3 / 6]] * [5 ^ [4 / 6]] -> [[3 ^ 3] * [5 ^ 4] ^ [1 / 6]]
     */
    FactorDenominatorOfRationalExponents,

    /**
     * Write the product of two powers with the same exponent
     * as the power of the products
     * E.g. [3 ^ [2 / 3]] * [5 ^ [2 / 3]] -> [(3 * 5) ^ [2 / 3]]
     */
    SimplifyProductOfPowersWithSameExponent,

    /**
     * Write the product of two powers with the same base
     * as the power to the sum of the exponents
     * E.g. [3 ^ [1 / 2]] * [3 ^ [2 / 3]] -> [3 ^ [1 / 2] + [2 / 3]]
     */
    SimplifyProductOfPowersWithSameBase,

    /**
     * Do a series of transformations to convert a product of
     * two powers with different bases and different rational
     * exponents to a single power.
     * E.g. [3 ^ [1 / 2]] * [5 ^ [2 / 3]] -> [16875 ^ [1 / 6]]
     */
    SimplifyProductOfPowersWithRationalExponents,

    /**
     * Collect like rational powers in a sum
     * E.g. 2 + [3 ^ [1 / 2]] - [2 / 3]*[3 ^ [1 / 2]] + [2 ^ [1 / 2]] + [[3 ^ [1 / 2]] / 2]
     *  -> 2 + (1 - [2 / 3] + [1 / 2]) * [3 ^ [1 / 2]] + [2 ^ [1 / 2]]
     */
    CollectLikeRationalPowers,

    /**
     * Collect like rational powers in a sum and simplify the resulting coefficient.
     * E.g. for the input 2 + [3 ^ [1 / 2]] - [2 / 3]*[3 ^ [1 / 2]] + [2 ^ [1 / 2]] + [[3 ^ [1 / 2]] / 2]
     * we get first 2 + (1 - [2 / 3] + [1 / 2]) * [3 ^ [1 / 2]] + [2 ^ [1 / 2]]
     * then 2 + [5 / 6] * [3 ^ [1 / 2]] + [2 ^ [1 / 2]]
     * and finally 2 + [5 * [3 ^ [1 / 2]] / 6] + [2 ^ [1 / 2]]
     */
    CollectLikeRationalPowersAndSimplify;

    override val category = "IntegerRationalExponents"
}

typealias Explanation = IntegerRationalExponentsExplanation
