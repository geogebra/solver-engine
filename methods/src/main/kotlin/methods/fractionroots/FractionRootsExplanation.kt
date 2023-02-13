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

    /**
     * Rationalize a fraction with a root of order > 2
     *
     * E.g. [1 / [root[3, 3]]] -> [1 / [root[3, 3]]] * [root[[3 ^ 3 - 1], 3] / root[[3 ^ 3 - 1], 3]]
     */
    RationalizeHigherOrderRoot,

    /**
     * Simplify the rationalizing term
     *
     * E.g. [1 / [root[3, 3]]] * [root[[3 ^ 3 - 1], 3] / root[[3 ^ 3 - 1], 3]]
     *      -> [1 / [root[3, 3]]] * [root[[3 ^ 2], 3] / root[[3 ^ 2], 3]]
     */
    SimplifyRationalizingTerm,

    CollectRationalizingRadicals,

    /**
     * Simplify the numerator of a fraction after higher order rationalization
     */
    SimplifyNumeratorAfterRationalization,

    /**
     * Simplify the denominator of a fraction after higher order rationalization
     */
    SimplifyDenominatorAfterRationalization,

    /**
     * E.g. [sqrt[3] / sqrt[5]] -> sqrt[[3 / 5]]
     *
     * Can apply to root of any order, not just square roots
     */
    TurnFractionOfRootsIntoRootOfFractions,

    /**
     * Simplify a fraction of roots
     *
     * E.g. [root[6, 3] / root[2, 3]] -> [root[3, 3]]
     */
    SimplifyFractionOfRoots,
    HigherOrderRationalizingTerm,
    SimplifyNthRootOfNthPower,
    FactorizeHigherOrderRadicand,
    ;

    override val category = "FractionRoots"
}

typealias Explanation = FractionRootsExplanation
