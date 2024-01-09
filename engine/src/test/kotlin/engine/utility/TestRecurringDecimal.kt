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
import kotlin.test.assertEquals

class TestRecurringDecimal {
    @Test
    fun testExpand() {
        assertEquals("0.66[6]", RecurringDecimal("0.6", 1).expand(3).toString())
        assertEquals("3.141515[15]", RecurringDecimal("3.1415", 2).expand(8).toString())
        assertEquals("0.010[10]", RecurringDecimal("0.010", 2).expand(5).toString())
    }

    @Test
    fun testMovePointRight() {
        assertEquals("33.[3]", RecurringDecimal("0.3", 1).movePointRight(2).toString())
        assertEquals("60272.7[27]", RecurringDecimal("6.027", 2).movePointRight(4).toString())
    }
}
