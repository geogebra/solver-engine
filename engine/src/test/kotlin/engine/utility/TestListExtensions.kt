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
import java.math.BigInteger
import kotlin.test.assertEquals

class TestListExtensions {
    private fun i(n: Int) = n.toBigInteger()

    @Test
    fun testGcd() {
        assertEquals(listOf(i(2), i(4), i(8)).gcd(), i(2))
        assertEquals(listOf(i(4), i(6), i(11)).gcd(), i(1))
        assertEquals(listOf(i(2), i(1)).gcd(), i(1))
        assertEquals(listOf<BigInteger>().gcd(), i(0))
    }

    @Test
    fun testLcm() {
        assertEquals(listOf(i(1), i(2)).lcm(), i(2))
        assertEquals(listOf(i(2), i(4), i(6)).lcm(), i(12))
        assertEquals(listOf(i(12), i(10), i(22)).lcm(), i(660))
    }
}

class TestProduct {
    private fun <T> p(vararg lists: List<T>) = product(lists.toList()).toList()

    @Test
    fun testEmptyProduct() {
        assertEquals(listOf(emptyList()), p<Int>())
    }

    @Test
    fun testProductOfOneList() {
        assertEquals(emptyList(), p<Int>(emptyList()))
        assertEquals(listOf(listOf(1)), p(listOf(1)))
        assertEquals(listOf(listOf(1), listOf(2)), p(listOf(1, 2)))
        assertEquals(listOf(listOf(1), listOf(2), listOf(3)), p(listOf(1, 2, 3)))
    }

    @Test
    fun testProductOfTwoLists() {
        assertEquals(listOf(listOf(1, 3), listOf(2, 3)), p(listOf(1, 2), listOf(3)))
        assertEquals(emptyList(), p(listOf(1, 2, 3), emptyList()))
        assertEquals(listOf(listOf(1, 3), listOf(1, 4), listOf(2, 3), listOf(2, 4)), p(listOf(1, 2), listOf(3, 4)))
    }

    @Test
    fun testProductOfThreeLists() {
        assertEquals(listOf(listOf(1, 2, 3)), p(listOf(1), listOf(2), listOf(3)))
        assertEquals(emptyList(), p(listOf(1), listOf(3), emptyList()))
        assertEquals(listOf(listOf(1, 2, 4), listOf(1, 3, 4)), p(listOf(1), listOf(2, 3), listOf(4)))
    }
}
