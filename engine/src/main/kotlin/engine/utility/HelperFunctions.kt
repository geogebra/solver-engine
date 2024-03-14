/*
 * Copyright (c) 2024 GeoGebra GmbH, office@geogebra.org
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

package engine.utility

import java.math.BigDecimal
import kotlin.math.ceil

/**
 * Pick a value in the interval ([x1], [x2]) that is as simple as possible (not too many decimal places, integer if
 * possible)
 */
fun pickValueInInterval(x1: Double, x2: Double): BigDecimal {
    return when {
        x2 <= 0 -> -pickValueInInterval(-x2, -x1)
        x1 >= 0 -> {
            // there are integers between x1, x2 -> pick one
            // else
            //  x1 and x2 are integers next to each other -> mean
            //  x1 and x2 are doubles with no integer in-between -> multiply by 10

            val p = ceil(x1).let { if (it == x1) it + 1 else it }
            @Suppress("MagicNumber")
            when {
                p < x2 -> BigDecimal(p)
                x1 == p - 1 && x2 == p -> BigDecimal((x1 + x2) / 2)
                else -> pickValueInInterval(x1 * 10, x2 * 10).scaleByPowerOfTen(-1)
            }
        }
        // Now x1 < 0 and x2 > 0 because neither case above applied
        else -> BigDecimal.ZERO
    }
}
