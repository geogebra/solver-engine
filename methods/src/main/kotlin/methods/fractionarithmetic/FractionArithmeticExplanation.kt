package methods.fractionarithmetic

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.LegacyKeyName
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class FractionArithmeticExplanation : CategorisedMetadataKey {

    /**
     * Rewrite
     *  a : (b / c) -> a * (c / b)
     *  (a / b) : c -> (a / b) * (1 / c)
     */
    @LegacyKeyName("FractionArithmetic.RewriteDivisionByFractionAsProduct")
    RewriteDivisionAsMultiplicationByReciprocal,

    /**
     * Rewrite a * b : c as a * (b / c)
     */
    @LegacyKeyName("General.RewriteDivisionAsFraction")
    RewriteDivisionAsFraction,

    /**
     * Rewrite all divisions as fractions in an expression
     *
     * E.g. 3 : 4 + 4 * (2 - 1 : 2) -> 3 / 4 + 4 * (2 - 1 / 2)
     */
    @LegacyKeyName("General.RewriteDivisionsAsFractionInExpression")
    RewriteDivisionsAsFractionInExpression,

    SimplifyNegativeInDenominator,
    SimplifyNegativeInNumerator,
    SimplifyNegativeInNumeratorAndDenominator,
    ConvertIntegerToFraction,
    AddLikeFractions,
    SubtractLikeFractions,
    BringToCommonDenominator,
    BringToCommonDenominatorWithNonFractionalTerm,
    MultiplyFractions,

    /**
     * Multiply a fraction by a non-fraction value
     *
     * Parameters:
     *   %1: the fraction
     *   %2: the value which is not a fraction
     *
     *   E.g. 3 * [2 / 5] --> [3 * 2 / 5]
     */
    MultiplyFractionAndValue,

    /**
     * Split improper fraction power to any base by converting the exponent to
     * sum of an integer and proper fraction and then distribute sum of powers
     * to its base
     */
    SplitRationalExponent,

    /**
     * Turn a non-fraction factor in a product containing fractions into a fraction
     *
     * E.g. 3 * [1 / 2] -> [3 / 1] * [1 / 2]
     */
    TurnFactorIntoFractionInProduct,

    /**
     * Turn product of rational fraction term and a polynomial term into fraction
     *
     * E.g. [12 / [x^2] - 9] 4(x-3) --> [12 * 4(x-3) / [x^2] - 9]
     */
    TurnProductOfFractionAndNonFractionFactorIntoFraction,

    /**
     * Multiply fractions together in a product and also multiply fractions by integers,
     * simplifying the resulting fraction as much as possible
     *
     * E.g. [2 / 3] * [sqrt[2]/ 2] * 5 -> [5 * sqrt[2] / 3]
     */
    MultiplyAndSimplifyFractions,

    /**
     * Find a common factor in a numeric fraction and rewrite it so the factor is explicit.
     *
     * E.g. [4 / 6] --> [2 * 2 / 2 * 3]
     */
    FindCommonFactorInFraction,

    /**
     * Find a common integer factor in a fraction and rewrite it so the factor is explicit.
     *
     * E.g. [12 + 6sqrt[2]] / 15] --> [3(4 + 2sqrt[2]) / 3 * 5]
     */
    FactorCommonIntegerFactorInFraction,

    @LegacyKeyName("General.CancelCommonTerms")
    CancelCommonFactorInFraction,

    ReorganizeCommonSumFactorInFraction,

    SimplifyFractionToInteger,
    SimplifyFractionWithFractionDenominator,
    SimplifyFractionWithFractionNumerator,
    DistributeFractionPositivePower,
    SimplifyFractionToMinusOne,
    TurnIntegerToMinusOneToFraction,
    TurnNegativePowerOfIntegerToFraction,
    SimplifyFraction,

    @LegacyKeyName("FractionArithmetic.EvaluateFractionSum")
    AddFractions,
    EvaluateProductsInNumeratorAndDenominator,

    /**
     * After the addition of two fractions simplify the numerator of the resulting fraction.
     *
     * E.g. ((1 + 4 sqrt(2)) + (2 + sqrt(2))) / 4 -> (3 + 5 sqrt(2)) / 4
     */
    SimplifyNumerator,

    /**
     * Add an integer to a fraction
     *
     * E.g. 3 + 2 / 3 -> 11 / 3
     */
    @LegacyKeyName("FractionArithmetic.EvaluateSumOfFractionAndInteger")
    AddIntegerAndFraction,

    /**
     * Add a root to a fraction
     *
     * E.g. sqrt(2) + (3 + 2 sqrt(2)) / 5 -> (3 + 7 sqrt(2)) / 5
     */
    AddRootAndFraction,

    /**
     * Add a term to a fraction
     *
     * E.g. x + (x + x^2) / 5 -> (6x + x^2) / 5
     */
    AddTermAndFraction,

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
