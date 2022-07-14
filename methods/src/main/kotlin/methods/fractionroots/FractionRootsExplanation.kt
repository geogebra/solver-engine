package methods.fractionroots

import engine.steps.metadata.ExplanationBase

enum class FractionRootsExplanation : ExplanationBase {

    // Rules
    DistributeRadicalRuleOverFractionsToNumeratorAndDenominator,
    WriteAsMultiplicationWithUnitaryRadicalFraction,

    // Plans
    EvaluateSquareRootFractions;

    override val category = "FractionRoots"
}

typealias Explanation = FractionRootsExplanation
