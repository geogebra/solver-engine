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
import kotlin.test.Test
import kotlin.test.assertEquals

class TestHelperFunctions {
    private fun assertPickValueInInterval(x1: Double, x2: Double, v: String) {
        assertEquals(BigDecimal(v), pickValueInInterval(x1, x2))
    }

    @Test
    fun testPickValueInInterval() {
        assertPickValueInInterval(-3.0, -2.0, "-2.5")
        assertPickValueInInterval(-4.0, -1.0, "-2")
        assertPickValueInInterval(-3.0, -1.6, "-2")
        assertPickValueInInterval(-4.2, -3.9, "-4")
        assertPickValueInInterval(-1.0, -0.6, "-0.7")
        assertPickValueInInterval(Double.NEGATIVE_INFINITY, -7.2, "-8")
        assertPickValueInInterval(-10.0, 0.0, "-1")

        assertPickValueInInterval(-1.0, 5.0, "0")
        assertPickValueInInterval(-0.1, 0.2, "0")

        assertPickValueInInterval(0.0, Double.POSITIVE_INFINITY, "1")
        assertPickValueInInterval(1.0, 7.0, "2")
        assertPickValueInInterval(1.0, 2.0, "1.5")
        assertPickValueInInterval(0.6, 1.0, "0.7")
        assertPickValueInInterval(1.67, 1.78, "1.7")
    }
}
