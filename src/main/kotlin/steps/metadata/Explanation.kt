package steps.metadata

enum class Explanation : MetadataKey {
    /* Basic algebraic rules */
    SimplifyDoubleMinus,

    /* Handling brackets */
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
    ConvertIntegerToFraction,
    AddLikeFractions,
    BringToCommonDenominator,
    CancelCommonTerms,
}