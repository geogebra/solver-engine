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
     * applies the "PowerRuleOfExponents" to all the exponent terms,
     * this plan encompasses the "PowerRuleOfExponents"
     * for e.g. ([2^3]) ^ [2/5] * ([5^4]) ^ [2/5] * ([7^5]) ^ [2/5] -->
     * [2 ^ [6/5]] * [5 ^ [8/5] ] * [7 ^ 2]
     */
    PowerRule,

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
    SimplifyRationalExponentOfInteger;

    override val category = "IntegerRationalExponents"
}

typealias Explanation = IntegerRationalExponentsExplanation
