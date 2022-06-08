package engine.steps.metadata

enum class PlanExplanation : MetadataKey {
    ReplaceAllInvisibleBrackets,

    SimplifyArithmeticExpression,

    SimplifyIntegerSum,
    SimplifyIntegerProduct,

    AddFractions,
}