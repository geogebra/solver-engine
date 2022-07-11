package methods.mixednumbers

import engine.steps.metadata.ExplanationBase

enum class MixedNumbersExplanation : ExplanationBase {
    // Rules
    ConvertMixedNumberToSum,
    ConvertFractionToMixedNumber;

    override val category = "MixedNumbers"
}

typealias Explanation = MixedNumbersExplanation
