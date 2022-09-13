package methods.fractionroots

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class FractionRootsExplanation : CategorisedMetadataKey {
    RationalizeSimpleDenominator,
    DistributeRadicalOverFraction,
    RationalizeSumOfIntegerAndSquareRoot,
    BringRootsToSameIndexInFraction,
    RationalizeSumOfIntegerAndCubeRoot,
    IdentityCubeSumDifference,
    FlipRootsInDenominator,
    RationalizeDenominator,
    SimplifyFractionOfRoots,
    HigherOrderRationalizingTerm,
    FactorizeHigherOrderRadicand;

    override val category = "FractionRoots"
}

typealias Explanation = FractionRootsExplanation
