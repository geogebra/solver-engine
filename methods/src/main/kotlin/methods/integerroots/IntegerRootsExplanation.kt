package methods.integerroots

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class IntegerRootsExplanation : CategorisedMetadataKey {
    SimplifyRootOfOne,
    SimplifyRootOfZero,
    FactorizeNumberUnderSquareRoot,
    SimplifyMultiplicationOfSquareRoots,
    SeparateSquaresUnderSquareRoot,

    /**
     * Simplify a product containing integers and / or roots.  Note that the product may not
     * contain roots or may not contain integers so the explanation should not be e.g.
     *     'Simplify product with roots'
     *
     * E.g. 2 * sqrt[2] * root[2, 3] * 3 -> 6 * root[32, 6]
     */
    SimplifyProductWithRoots,
    SimplifyIntegerRoot,
    SimplifyIntegerRootToInteger,
    CollectLikeRootsAndSimplify,
    MultiplyNthRoots,

    /**
     * Split roots that can be in a product of roots so that they can be simplified
     *
     * E.g. sqrt[[2^5]] * sqrt[[3^3]] -> sqrt[[2^4]] * sqrt[2] * sqrt[[3^2]] * sqrt[3]
     *
     * Note: this can apply to roots of any order.
     */
    SplitRootsInProduct,

    /**
     * Cancel all roots of powers that can be cancelled, in a product
     *
     * Eg.g. sqrt[[2 ^ 4]] * sqrt[2] -> [2 ^ 2] * sqrt[2]
     */
    CancelAllRootsOfPowers,
    SplitRootOfProduct,
    NormaliseProductWithRoots,
    SimplifyNthRootToThePowerOfN,
    PrepareCancellingPowerOfARoot,
    SimplifyNthRootOfNthPower,
    PrepareCancellingRootOfAPower,
    TurnPowerOfRootToRootOfPower,
    SimplifyRootOfRoot,
    PutRootCoefficientUnderRoot,
    BringRootsToSameIndexInProduct,
    CancelPowerOfARoot,
    CancelRootOfAPower,
    PutRootCoefficientUnderRootAndSimplify,
    SimplifyRootOfRootWithCoefficient,
    CollectLikeRoots,
    CombineProductOfSamePowerUnderHigherRoot,
    CollectPowersOfExponentsWithSameBase;

    override val category = "IntegerRoots"
}

typealias Explanation = IntegerRootsExplanation
