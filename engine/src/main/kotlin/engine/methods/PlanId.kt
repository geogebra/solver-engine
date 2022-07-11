package engine.methods

// Added back to make it compile
enum class PlanId : MethodId {
    EvaluateSignedIntegerPower,
    SimplifyArithmeticExpression,
    SimplifyNumericFraction,
    EvaluatePositiveFractionSum,
    EvaluatePositiveFractionProduct,
    EvaluatePositiveFractionPower,
    CombineFractionsInExpression,
    EvaluatePowerOfFraction,
    AddMixedNumbers;

    override val category = "legacy"
}
