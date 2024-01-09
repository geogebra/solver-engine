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

package methods.collecting

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.LegacyKeyName
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class CollectingExplanation : CategorisedMetadataKey {
    /**
     * Collect like roots in a sum
     * E.g. 2 + sqrt(3) - (2 / 3) * sqrt(3) + sqrt(2) + sqrt(3) / 2
     *  -> 2 + (1 - (2 / 3) + (1 / 2)) * sqrt(3) + sqrt(2)
     */
    @LegacyKeyName("IntegerRoots.CollectLikeRoots")
    CollectLikeRoots,

    /**
     * Collect like rational powers in a sum
     * E.g. 2 + 3 ^ (1 / 2) - (2 / 3) * 3 ^ (1 / 2) + 2 ^ (1 / 2) + (3 ^ (1 / 2) / 2)
     *  -> 2 + (1 - (2 / 3) + (1 / 2)) * 3 ^ (1 / 2) + 2 ^ (1 / 2)
     */
    @LegacyKeyName("IntegerRationalExponents.CollectLikeRationalPowers")
    CollectLikeRationalPowers,

    /**
     * Collect like terms of the form a(x^n) in a sum
     *
     * E.g. x^2 + 3 + x + 2 x^2 --> (1 + 2) x^2 + 3 + x
     */
    @LegacyKeyName("Polynomials.CollectLikeTerms")
    CollectLikeTerms,

    /**
     * Add the first two addends that are in the form of an integer times a root (that have the same root)
     *
     * E.g. 2 sqrt(2) + 3 + sqrt(2) + 2 sqrt(5) --> 3 sqrt(2) + 3 + 2 sqrt(5)
     */
    CombineTwoSimpleLikeRoots,

    /**
     * Add the first two addends that are in the form of an integer times a
     * rational power (that have the base and exponent)
     *
     * E.g. 2 * 2 ^ (5 / 2) + 3 + 2 ^ (5 / 2) + 2 * 3 ^ (4 / 3) --> 3 * 2 ^ (5 / 2) + 3 + 2 * 3 ^ (4 / 3)
     */
    CombineTwoSimpleLikeRationalPowers,

    /**
     * Add the first two addends that are in the form of an integer times a variable (that have the same variable)
     *
     * E.g. 2x + 3 + x + 2 x^2 --> 3x + 3 + 2 x^2
     */
    CombineTwoSimpleLikeTerms,

    /**
     * Simplify the coefficient of a monomial after collecting like terms
     *
     * E.g. ((1 / 2) + 2) x^2 -> (5 / 2) x^2
     */
    @LegacyKeyName("Polynomials.SimplifyCoefficient")
    SimplifyCoefficient,

    /**
     * Collect like roots in a sum and simplify the resulting coefficient.
     *
     * E.g. 2 + sqrt(2) - (2 / 3) * sqrt(2) + sqrt(5) + sqrt(2) / 2
     *  -> 2 + (1 - (2 / 3) + (1 / 2)) * sqrt(2) + sqrt(5)
     *  -> 2 + (5 / 6) * sqrt(2) + sqrt(5)
     *  -> 2 + (5 * sqrt(2) / 6) + sqrt(5)
     */
    @LegacyKeyName("IntegerRoots.CollectLikeRootsAndSimplify")
    CollectLikeRootsAndSimplify,

    /**
     * Collect like rational powers in a sum and simplify the resulting coefficient.
     *
     * E.g. 2 + 3 ^ (1 / 2) - (2 / 3) * 3 ^ (1 / 2) + 2 ^ (1 / 2) + (3 ^ (1 / 2) / 2)
     *  -> 2 + (1 - (2 / 3) + (1 / 2)) * 3 ^ (1 / 2) + 2 ^ (1 / 2)
     *  -> 2 + (5 / 6) * 3 ^ (1 / 2) + 2 ^ (1 / 2)
     *  -> 2 + (5 * 3 ^ (1 / 2) / 6) + 2 ^ (1 / 2)
     */
    @LegacyKeyName("IntegerRationalExponents.CollectLikeRationalPowersAndSimplify")
    CollectLikeRationalPowersAndSimplify,

    /**
     * Collect like terms in a sum and simplify the resulting coefficient.
     *
     * E.g. 2 + x^2 - (2 / 3) x^2 + x + (x^2 / 2)
     *  -> 2 + (1 - (2 / 3) + (1 / 2)) x^2 + x
     *  -> 2 + (5 / 6) x^2 + x
     *  -> 2 + (5 x^2 / 6) + x
     */
    @LegacyKeyName("Polynomials.CollectLikeTermsAndSimplify")
    CollectLikeTermsAndSimplify,

    ;

    override val category = "Collecting"
}

typealias Explanation = CollectingExplanation
