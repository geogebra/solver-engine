package methods.mixednumbers

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class MixedNumbersExplanation : CategorisedMetadataKey {
    ConvertMixedNumbersToImproperFraction,
    ConvertMixedNumbersToSums,
    ConvertMixedNumberToSum,
    ConvertIntegersToFractions,
    AddFractions,
    ConvertFractionToMixedNumber;

    override val category = "MixedNumbers"
}

typealias Explanation = MixedNumbersExplanation
