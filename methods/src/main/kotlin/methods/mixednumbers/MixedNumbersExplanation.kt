package methods.mixednumbers

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class MixedNumbersExplanation : CategorisedMetadataKey {
    /**
     * Convert a mixed numbers to an improper fraction
     */
    ConvertMixedNumberToImproperFraction,

    /**
     * Convert two or more mixed numbers to sums in parellel
     * E.g. [1 2/3] + [3 1/2] -> (1 + [2 / 3]) + (3 + [1 / 2])
     */
    ConvertMixedNumbersToSums,

    /**
     * Convert a mixed number to a sum
     * E.g. [1 2/3] -> 1 + [2 / 3]
     */
    ConvertMixedNumberToSum,

    /**
     * Convert the sum of an integer and a proper fraction to a mixed number
     * E.g. 1 + [2 / 3] -> [1 2/3]
     */
    ConvertSumOfIntegerAndProperFractionToMixedNumber,

    /**
     * Convert an improper fraction to a mixed number
     * E.g. [13 / 5] -> [2 3/5]
     */
    ConvertFractionToMixedNumber,

    /**
     * Add mixed numbers
     */
    AddMixedNumbers,

    ;

    override val category = "MixedNumbers"
}

typealias Explanation = MixedNumbersExplanation
