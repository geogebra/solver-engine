/*
 * Copyright (c) 2025 GeoGebra GmbH, office@geogebra.org
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

package methods.units

import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.TranslationKeys

@TranslationKeys
enum class UnitsExplanation : CategorisedMetadataKey {
    /**
     * Evaluate the subtraction of two integers with the same unit
     */
    EvaluateIntegerUnitSubtraction,

    /**
     * Evaluate the addition of two integers with the same unit
     */
    EvaluateIntegerUnitAddition,

    /**
     * Evaluate the product of two integers with one containing units.
     */
    EvaluateIntegerUnitProduct,

    /**
     * Evaluate the division of two integers with the dividend containing units.
     */
    EvaluateIntegerUnitDivision,

    /**
     * Cancel out units in a fraction with unit in the numerator and denominator.
     */
    CancelOutUnitInFractionOfUnits,

    /**
     * Simplifies a fraction of an expression with a unit and a denominator that is an exact divisor.
     * For example, 4° / 2  = 2°.
     */
    SimplifyFractionOfUnitAndConstantToInteger,

    /**
     * Find a common factor in a fraction with unit and constant. For example, (2 m) / 4 = (1/2) m.
     */
    FindCommonFactorInFractionWithUnitAndConstant,

    /**
     * Convert a terminating decimal with units to a fraction. For example 1.2° -> 12° / 10.
     */
    ConvertTerminatingDecimalWithUnitToFraction,

    ;

    override val category = "Units"
}

typealias Explanation = UnitsExplanation
