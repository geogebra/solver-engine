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
