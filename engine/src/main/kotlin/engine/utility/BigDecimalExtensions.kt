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

package engine.utility

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Rounds off a decimal if necessary to a maximum of [dp] decimal places without making the number of significant
 * figures go below [dp].
 */
fun BigDecimal.withMaxDP(dp: Int): BigDecimal {
    return when {
        scale() <= dp -> this
        scale() <= precision() -> setScale(dp, RoundingMode.HALF_UP)
        else -> round(MathContext(dp, RoundingMode.HALF_UP))
    }
}
