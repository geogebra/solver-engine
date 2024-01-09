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

package methods.fallback

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class FallbackExplanation : CategorisedMetadataKey {
    /**
     * This explanation is given when the input is already simplified or factored as much as possible.
     * It means the solver thinks there is nothing useful to do, as opposed to not knowing what to do.
     */
    ExpressionIsFullySimplified,

    /**
     * This explanation is given when the input is a simplified quadratic with a negative discriminant
     */
    QuadraticIsIrreducible,

    /**
     * Form an inequality of the form b^2 - 4ac < 0 and solve it to find whether it is true
     */
    CheckDiscriminantIsNegative,

    /**
     * Conclude that the quadratic is negative because its discriminant was shown to be negative.
     */
    QuadraticIsIrreducibleBecauseDiscriminantIsNegative,

    ;

    override val category = "Fallback"
}

typealias Explanation = FallbackExplanation
