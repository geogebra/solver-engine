package methods.expand

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class ExpandExplanation : CategorisedMetadataKey {
    /**
     * Distribute a product over a bracket to the terms,
     * i.e. a * (b1 + b2 + ... + bn) -> a * b1 + ... + a * bn
     */
    DistributeMultiplicationOverSum,

    /**
     * Distribute the numerator of a fraction so the fraction becomes a sum
     *
     * e.g. (x + 3)/5 --> x/5 + 3/5
     */
    DistributeConstantNumerator,

    /**
     * Distribute negative sign over a bracket to the terms,
     * i.e. -(x + y) -> -x - y
     */
    DistributeNegativeOverBracket,

    /**
     * Apply the identity
     * (a +- b) * (a -+ b) -> [a^2] - [b^2]
     */
    ExpandProductOfSumAndDifference,

    /**
     * Apply the FOIL identity
     * (a + b) * (c + d) -> a * c + a * d + b * c + b * d
     */
    ApplyFoilMethod,

    /**
     * Expand the product of two sums, i.e.
     * (a1 + a2 + ... + an) * (b1 + b2 + ... + bm)
     *      -> a1 * b1 + a1 * b2 + ... + a2 * b1 + a2 * b2 + ... + an * bm
     */
    ExpandDoubleBrackets,

    /**
     * Apply the identity
     * (a + b)^2 -> a^2 + 2 * a * b + b^2
     */
    ExpandBinomialSquaredUsingIdentity,

    /**
     * Apply the identity
     * (a + b)^3 -> a^3 + 3 * a^2 * b + 3 * a * b^2 + b^3
     */
    ExpandBinomialCubedUsingIdentity,

    /**
     * Apply the identity
     * (a + b + c)^2 -> a^2 + b^2 + c^2 + 2 * a * b + 2 * b * c + 2 * c * a
     */
    ExpandTrinomialSquaredUsingIdentity,

    /**
     * Expand a * (b1 + b2 + ... + bn) or -(b1 + b2 + ... + bn) and simplify the result
     */
    ExpandSingleBracketAndSimplify,

    /**
     * Expand a fraction and simplify the result
     *
     * E.g. (3x + 2)/6 --> 3x/6 + 2/6 --> x/2 + 1/3
     */
    ExpandFractionAndSimplify,

    /**
     * Expand the product of two brackets and simplify the result
     */
    ExpandDoubleBracketsAndSimplify,

    /**
     * Expand the square of a binomial and simplify the result
     */
    ExpandBinomialSquaredAndSimplify,

    /**
     * Expand the cube of a binomial and simplify the result
     */
    ExpandBinomialCubedAndSimplify,

    /**
     * Expand a trinomial squared, i.e. (a + b + c)^2 and simplify the result
     */
    ExpandTrinomialSquaredAndSimplify,

    ;

    override val category = "Expand"
}

typealias Explanation = ExpandExplanation
