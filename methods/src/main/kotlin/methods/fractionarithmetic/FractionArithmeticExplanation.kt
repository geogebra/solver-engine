package methods.fractionarithmetic

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class FractionArithmeticExplanation : CategorisedMetadataKey {
    SimplifyNegativeInDenominator,
    SimplifyNegativeInNumerator,
    SimplifyNegativeInNumeratorAndDenominator,
    ConvertIntegerToFraction,
    AddLikeFractions,
    SubtractLikeFractions,
    BringToCommonDenominator,
    MultiplyFractions,

    /**
     * Split improper fraction power to any base by converting the exponent to
     * sum of an integer and proper fraction and then distribute sum of powers
     * to its base
     */
    SplitRationalExponent,

    /**
     * Add an integer to a fraction
     *
     * E.g. 3 + [2 / 3] -> [11 / 3]
     */
    EvaluateSumOfFractionAndInteger,

    /**
     * Turn a non-fractional factor in a product containing fractions into a fraction
     *
     * E.g. 3 * [1 / 2] -> [3 / 1] * [1 / 2]
     *
     * This is vith a view to multiplying out the fractions.
     */
    TurnFactorIntoFractionInProduct,

    /**
     * Multiply fractions together in a product and also multiply fractions by integers,
     * simplifying the resulting fraction as much as possible
     *
     * E.g. [2 / 3] * [sqrt[2]/ 2] * 5 -> [5 * sqrt[2] / 3]
     */
    MultiplyAndSimplifyFractions,

    /**
     * Evaluate an integer to a negative power
     *
     * E.g. [5 ^ -2] -> [1 / 25]
     */
    EvaluateIntegerToNegativePower,

    FindCommonFactorInFraction,
    SimplifyFractionToInteger,
    SimplifyFractionWithFractionDenominator,
    SimplifyFractionWithFractionNumerator,
    DistributeFractionPositivePower,
    SimplifyFractionNegativePower,
    SimplifyFractionToMinusOne,
    TurnIntegerToMinusOneToFraction,
    TurnNegativePowerOfIntegerToFraction,
    SimplifyFraction,
    EvaluateFractionSum,
    EvaluateProductsInNumeratorAndDenominator,
    EvaluateSumInNumerator,
    NormalizeSignsInFraction,

    /**
     * Normalize fractions and divisions in an expression by turning divisions into fractions
     * and simplifying nested fractions
     *
     * E.g. 5 : [2 / 3] -> 5 * [3 / 2]
     */
    NormalizeFractionsAndDivisions,

    /**
     * Convert an improper fraction (one with a greater numerator than denominator) to the sum
     * of an integer and a proper fraction.
     *
     * E.g. [13 / 5] -> 2 + [3 / 5]
     */
    ConvertImproperFractionToSumOfIntegerAndFraction,

    /**
     * Turns any negative power of zero into a fraction 1 / 0 to the negation of that power.
     * E.g. [0 ^ -[2 / 3]] -> [(1 / 0) ^ [2 / 3]]
     */
    TurnNegativePowerOfZeroToPowerOfFraction,

    ;

    override val category = "FractionArithmetic"
}

typealias Explanation = FractionArithmeticExplanation
