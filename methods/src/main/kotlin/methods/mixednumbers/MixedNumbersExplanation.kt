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
     * Convert two or more mixed numbers to sums in parallel
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
