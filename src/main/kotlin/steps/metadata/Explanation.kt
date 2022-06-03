package steps.metadata

enum class Explanation : MetadataKey {
    /* Basic algebraic rules */
    SimplifyDoubleMinus,

    /* Handling brackets */
    ReplaceInvisibleBrackets,
    RemoveBracketSumInSum,
    RemoveBracketSignedIntegerInSum,
    RemoveBracketUnsignedInteger,

    /* Integer arithmetic */
    EliminateZeroInSum,
    EliminateOneInProduct,
    ProductContainingZero,
    EvaluateIntegerSubtraction,
    EvaluateIntegerAddition,
    EvaluateIntegerProduct,
    EvaluateIntegerPower,

    /* Mixed numbers */
    ConvertMixedNumberToSum,
    ConvertFractionToMixedNumber,

    /* Fractions */
    SimplifyNegativeInDenominator,
    ConvertIntegerToFraction,
    AddLikeFractions,
    SubtractLikeFractions,
    BringToCommonDenominator,
    CancelCommonTerms,
}