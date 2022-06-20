package engine.steps.metadata

enum class Explanation : MetadataKey {
    /* Basic algebraic rules */
    SimplifyDoubleMinus,
    SimplifyTwoNegativeFactorsInProduct,
    MoveSignOfNegativeFactorOutOfProduct,
    SimplifyEvenPowerOfNegative,
    SimplifyOddPowerOfNegative,

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
    SimplifyNegativeInNumerator,
    SimplifyNegativeInNumeratorAndDenominator,
    ConvertIntegerToFraction,
    AddLikeFractions,
    SubtractLikeFractions,
    BringToCommonDenominator,
    CancelCommonTerms,
    MultiplyFractions,
    FindCommonFactorInFraction,
    SimplifyFractionToInteger,
    DivideByAFraction,
    TurnDivisionToFraction,
    SimplifyFractionWithFractionDenominator,
    SimplifyFractionWithFractionNumerator,
    DistributeFractionPositivePower,
    SimplifyFractionNegativePower,
    SimplifyFractionToMinusOne,
    TurnIntegerToMinusOneToFraction,
    TurnNegativePowerOfIntegerToFraction,

    /* Roots */
    RootOfOne,
    RootOfZero,
    FactorizeNumberUnderSquareRoot,
    SeparateIntegerPowersUnderSquareRoot,
    SeparateSquaresUnderSquareRoot,
    SimplifySquareRootOfPower,
}
