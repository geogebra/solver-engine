package engine.steps.metadata

enum class Skill : MetadataKey {
    NumericLCM,
    DivisionWithRemainder,
    FactorInteger,
    AddFractions,
    MultiplyFractions,
    SimplifyNumericFraction;

    override val keyName = name
}
