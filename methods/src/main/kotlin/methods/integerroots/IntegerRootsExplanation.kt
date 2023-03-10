package methods.integerroots

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class IntegerRootsExplanation : CategorisedMetadataKey {
    SimplifyRootOfOne,
    SimplifyRootOfZero,

    /**
     * Rewrite and cancel the common factor between rootIndex and exponent
     * of a power under root (does it in two steps)
     * for e.g. root[[7^6], 8] --> root[[7^3*2], 4*2] --> root[[7^3], 4]
     */
    RewriteAndCancelPowerUnderRoot,

    /**
     * Simplify the power of an integer under root
     * by either splitting or by factoring the base (or both)
     * for e.g. root[ [12^4], 3 ] --> 12 * root[12, 3]
     */
    SimplifyPowerOfIntegerUnderRoot,

    /**
     * Factorize the integer present as base of a power under root and then
     * apply the power rule to distribute the powers to its factors
     * for e.g. root[ 24^2, 3] --> root[ 2^6 * 3^2, 3]
     */
    FactorizeAndDistributePowerUnderRoot,

    /**
     * factorize the integer under the root term, if some special
     * factorization exists for the integer depending upon the order
     * of the root (for e.g. root[1000, 3] -> root[ [10^3], 3])
     * otherwise do the prime factor decomposition of the number
     */
    FactorizeIntegerUnderRoot,

    /**
     * Factorize the integer present as base of a power under a root
     * for e.g. root[ [24^2], 3 ] --> root[ [([2^3] *3)^2], 3]
     */
    FactorizeIntegerPowerUnderRoot,

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
    MultiplyNthRoots,
    SplitRootsAndCancelRootsOfPowers,

    /**
     * Write a root of an integer as a product of values whose root can easily be computed
     *
     * E.g. sqrt[400] -> sqrt[4 * 100]
     * E.g. root[8000, 3] -> root[8 * 1000, 3]
     * E.g. sqrt[160] -> sqrt[16 * 10]
     */
    WriteRootAsRootProduct,

    /**
     * Write the integer in a root so it can be cancelled easily
     *
     * E.g. sqrt[36] -> sqrt[6 ^ 2]
     * E.g. sqrt[10000] -> sqrt[100 ^ 2]
     * E.g. root[10000, 3] -> root[10 ^ 4, 3]
     */
    WriteRootAsRootPower,

    /**
     * Write each root in a product of roots so that it can be easily cancelled
     *
     * E.g. sqrt[36] * sqrt[100] -> sqrt[6 ^ 2] * sqrt[10 ^ 2]
     * E.g. root[8, 3] * root[10000, 3] -> root[2 ^ 3, 3] * root[10 ^ 4, 3]
     */
    WriteRootsAsRootPowers,

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
    SimplifyNthRootToThePowerOfN,
    PrepareCancellingPowerOfARoot,
    SimplifyNthRootOfNthPower,
    TurnPowerOfRootToRootOfPower,
    SimplifyRootOfRoot,
    PutRootCoefficientUnderRoot,
    BringRootsToSameIndexInProduct,
    CancelPowerOfARoot,
    PutRootCoefficientUnderRootAndSimplify,
    SimplifyRootOfRootWithCoefficient,
    CombineProductOfSamePowerUnderHigherRoot,
    ;

    override val category = "IntegerRoots"
}

typealias Explanation = IntegerRootsExplanation
