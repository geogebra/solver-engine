/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package methods.integerroots

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.LegacyKeyName
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

    /**
     * Split root(a^b, c) to root(a^(q*c) * a^r), c) then simplify it to a^q * root(a^r, c)
     *
     * E.g. root(10^7, 3) -> root(10^6 * 10, 3) -> root(10^6, 3) * root(10, 3) -> 10^2 * root(10, 3)
     */
    @LegacyKeyName("IntegerRoots.SplitRootsAndCancelRootsOfPowers")
    SplitAndCancelRootOfPower,

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

    /**
     * Find the greatest common square factor in a product or sum and use it to factor it
     *
     * E.g. 8x - 12 --> 4(2x - 3)
     *      18xy    --> 9(2xy)
     *
     * This is used specifically to simplify a square root containing a square factor
     */
    FactorGreatestCommonSquareIntegerFactor,

    /**
     * Simplify a square root of that has common factor which is a square
     *
     * E.g. sqrt[9x - 18] --> sqrt[9(x - 2)]
     *                    --> sqrt[9]sqrt[x - 2]
     *                    --> 3sqrt[x - 2]
     *
     * E.g. sqrt[8a] --> sqrt[4*2a]
     */
    SimplifySquareRootWithASquareFactorRadicand,

    /**
     * Simplify an expression of the form sqrt[a +/- b sqrt[n]]
     * when a +/- b sqrt[n] can be written as a square.
     *
     * E.g. sqrt[11 - 6sqrt[2]] --> sqrt[(3 - sqrt[2]) ^ 2]
     *                          --> 3 - sqrt[2]
     */
    SimplifySquareRootOfIntegerPlusSurd,

    /**
     * Write an expression of the form a +/- b sqrt[n] as (x +/- y sqrt[n]) ^ 2 by writing
     * a system of equations for x and y and finding a solution to it.
     *
     * E.g. 11 - 6 sqrt[2] --> (3 - sqrt[2]) ^ 2
     */
    WriteIntegerPlusSquareRootAsSquare,

    /**
     * In the context of factoring a + b sqrt[n] as a square, write an equation in x and y,
     * deduce a system of two equations that x and y my satisfy and find integer solutions
     * for x and y.
     *
     * E.g. in the context of factoring 11 - 6sqrt[2]
     *     (x + y sqrt[2])^2 = 11 - 6sqrt[2]
     * --> x^2 + 2xy sqrt[2] + 2y^2 = 11 - 6 sqrt[2]
     * --> x^2 + 2y^2 = 11 AND 2xy sqrt[2] = -6 sqrt[2]
     * --> x^2 + 2y^2 = 11 AND xy = -3
     * --> x = 3 AND y = -1
     */
    WriteEquationInXAndYAndSolveItForFactoringIntegerPlusSurd,

    /**
     * In the context of factoring  a + b sqrt[n] = (x + y sqrt[2])^2, given that integer solutions
     * have been found for x and y, substituted them back in order to find the square form for
     * a + b sqrt[n].
     *
     * E.g. in the context of factoring 11 - 6sqrt[2] in the form (x + y sqrt[2])^2,
     * we found x= 3 AND y = -1.  So the form for 11 - 6sqrt[2] as a square will be:
     *
     *     3 - sqrt[2]
     */
    SubstituteXAndYorFactoringIntegerPlusSurd,
    ;

    override val category = "IntegerRoots"
}

typealias Explanation = IntegerRootsExplanation
