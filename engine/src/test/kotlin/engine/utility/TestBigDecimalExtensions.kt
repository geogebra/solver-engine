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

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals

private fun d(s: String) = BigDecimal(s)

class TestBigDecimalExtensions {
    @Test
    fun tesWithMaxDP() {
        assertEquals(d("123"), d("123").withMaxDP(3))
        assertEquals(d("145.12"), d("145.12").withMaxDP(3))
        assertEquals(d("456.463"), d("456.46345").withMaxDP(3))
        assertEquals(d("0.13"), d("0.128564").withMaxDP(2))
        assertEquals(d("0.00123"), d("0.0012345").withMaxDP(3))
    }
}
