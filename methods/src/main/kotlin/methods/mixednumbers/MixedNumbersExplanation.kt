package methods.mixednumbers

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class MixedNumbersExplanation : CategorisedMetadataKey {

    /**
     * Convert two or more mixed numbers to improper fractions in parallel
     */
    ConvertMixedNumbersToImproperFraction,

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
     * Convert two or more integers to fractions in parallel
     * E.g. (1 + [2 / 3]) + (3 + [1 / 2]) -> ([1 / 1] + [2 / 3]) + ([3 / 1] + [1 / 2])
     */
    ConvertIntegersToFractions,

    /**
     * Add two or more (unlike) fractions in parallel
     * E.g. ([1 / 1] + [2 / 3]) + ([3 / 1] + [1 / 2]) -> [5 / 3] + [7 / 2]
     */
    AddFractions,

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
