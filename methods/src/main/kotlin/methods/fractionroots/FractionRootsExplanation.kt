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
    FactorizeHigherOrderRadicand,
    ;

    override val category = "FractionRoots"
}

typealias Explanation = FractionRootsExplanation
