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

package methods.integerroots

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

class BigIntegerExtensionsTest {
    private fun i(n: Int) = n.toBigInteger()

    @Test
    fun testAsProductForRoot() {
        assertContentEquals(
            listOf(i(36), i(100)),
            i(3600).asProductForRoot(i(2)),
        )
        assertContentEquals(
            listOf(i(8), i(10)),
            i(80).asProductForRoot(i(3)),
        )
        assertContentEquals(
            listOf(i(49), i(1000)),
            i(49000).asProductForRoot(i(2)),
        )
        assertNull(i(700).asProductForRoot(i(2)))
        assertNull(i(100).asProductForRoot(i(3)))
        assertNull(i(16).asProductForRoot(i(4)))
    }

    @Test
    fun testAsPowerForRoot() {
        assertEquals(Pair(i(2), i(2)), i(4).asPowerForRoot(i(2)))
        assertEquals(Pair(i(5), i(3)), i(125).asPowerForRoot(i(3)))
        assertEquals(Pair(i(100), i(3)), i(1000000).asPowerForRoot(i(3)))
        assertEquals(Pair(i(10), i(5)), i(100000).asPowerForRoot(i(3)))
        assertNull(i(27).asPowerForRoot(i(2)))
        assertNull(i(16).asPowerForRoot(i(4)))
        assertNull(i(10).asPowerForRoot(i(2)))
        assertNull(i(1000).asPowerForRoot(i(4)))
    }
}
