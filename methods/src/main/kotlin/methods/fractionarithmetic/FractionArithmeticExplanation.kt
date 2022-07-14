package methods.fractionarithmetic

import engine.steps.metadata.ExplanationBase

enum class FractionArithmeticExplanation : ExplanationBase {

    // Rules
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
    SimplifyDividingByAFraction,
    TurnDivisionToFraction,
    SimplifyFractionWithFractionDenominator,
    SimplifyFractionWithFractionNumerator,
    DistributeFractionPositivePower,
    SimplifyFractionNegativePower,
    SimplifyFractionToMinusOne,
    TurnIntegerToMinusOneToFraction,
    TurnNegativePowerOfIntegerToFraction,
    ConvertMultiplicationOfFractionsToFraction,

    // Plans
    SimplifyNumericFraction,
    AddFractions,
    EvaluatePositiveFractionProduct,
    EvaluatePowerOfFraction;

    override val category = "FractionArithmetic"
}

typealias Explanation = FractionArithmeticExplanation
