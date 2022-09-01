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
    FindCommonFactorInFraction,
    SimplifyFractionToInteger,
    SimplifyFractionWithFractionDenominator,
    SimplifyFractionWithFractionNumerator,
    DistributeFractionPositivePower,
    SimplifyFractionNegativePower,
    SimplifyFractionToMinusOne,
    TurnIntegerToMinusOneToFraction,
    TurnNegativePowerOfIntegerToFraction,
    ConvertMultiplicationOfFractionsToFraction,
    SimplifyFraction,
    EvaluateFractionSum,
    EvaluateProductsInNumeratorAndDenominator,
    EvaluateSumInNumerator,
    NormalizeSignsInFraction,
    NormalizeFractionsAndDivisions;

    override val category = "FractionArithmetic"
}

typealias Explanation = FractionArithmeticExplanation
