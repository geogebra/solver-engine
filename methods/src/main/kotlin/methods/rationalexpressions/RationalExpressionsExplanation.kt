package methods.rationalexpressions

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class RationalExpressionsExplanation : CategorisedMetadataKey {
    /**
     * Factor the numerator of a fraction.
     * E.g. (x^2 + 3x + 2)/(x + 2) -> (x + 2)(x + 1)/(x + 2)
     */
    FactorNumeratorOfFraction,

    /**
     * Factor the denominator of a fraction.
     * E.g. (x + 2)/(x^2 + 3x + 2) -> (x + 2)/(x + 2)(x + 1)
     */
    FactorDenominatorOfFraction,

    /**
     * Add two rational expressions.
     * E.g. 1/(x + 1) + 1/(x + 2) -> (2x + 3)/(x + 1)(x + 2)
     */
    AddRationalExpressions,

    /**
     * Compute the least common denominator of two fractions.
     * E.g. for 1/((x + 1)^2 (x + 2)) and 1/((x + 1) (x + 2) (x + 3)) the result is (x + 1)^2 (x + 2) (x + 3)
     */
    ComputeLeastCommonDenominatorOfFractions,

    /**
     * Bring a fraction to the previously determined least common denominator.
     * E.g. if the LCD is (x + 1)^3 (x + 2) (x + 3)
     *      and the fraction is 1/((x + 1)^2 (x + 2))
     *      then the result is ((x + 1) (x + 3))/((x + 1)^3 (x + 2) (x + 3))
     */
    BringFractionToLeastCommonDenominator,

    /**
     * Add two like (having the same denominator) rational expressions and simplify the result.
     * E.g. x^2 / (x + 1) + x / (x + 1)
     *      -> (x^2 + x) / (x + 1)
     *      -> x
     */
    AddLikeRationalExpressions,

    /**
     * Add a non-fractional term and a rational expression and simplify the result.
     * E.g. 2x + (1 - x) / (1 + x)
     *      -> 2x (1 + x) / (1 + x) + (1 - x) / (1 + x)
     *      -> ...
     *      -> (2x^2 + x + 1) / (1 + x)
     */
    AddTermAndRationalExpression,

    /**
     * Simplify a rational expression by factoring the numerator and the denominator and cancelling
     * the common factors.
     * E.g. (x^3 + 3x^2 + 3x + 1) / (x^3 + 1)
     *      -> (x + 1)^3 / (x^3 + 1)
     *      -> (x + 1)^3 / (x + 1) (x^2 - x + 1)
     *      -> (x + 1)^2 / (x^2 - x + 1)
     */
    SimplifyRationalExpression,

    /**
     * Multiply two rational expressions and simplify the result.
     * E.g. x / (x + 1) * (3x + 2) / x
     *      -> x (3x + 2) / (x + 1) x
     *      -> (3x + 2) / (x + 1)
     */
    MultiplyRationalExpressions,

    /**
     * Simplify the power of a rational expression.
     * E.g. (2x / (1 + x))^2 -> 4x^2 / (1 + x)^2
     */
    SimplifyPowerOfRationalExpression,

    /**
     * Distribute a division over a sum.
     * E.g. (3x^3 + 2x^2) : 5x -> 3x^3 : 5x + 2x^2 : 5x
     */
    DistributeDivisionOverSum,

    /**
     * Distribute a division over a sum then simplify the terms
     * E.g. (3x^3 + 2x^2) : 5x
     *      -> 3x^3 : 5x + 2x^2 : 5x
     *      -> 3/5 x^2 + 2/5 x
     */
    SimplifyDivisionOfPolynomial,
    ;

    override val category = "RationalExpressions"
}

typealias Explanation = RationalExpressionsExplanation
