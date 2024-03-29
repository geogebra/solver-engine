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
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestBigIntegerExtensions {
    private fun i(n: Int) = n.toBigInteger()

    private fun p(p: Int, m: Int) = Pair(i(p), i(m))

    @Test
    fun testIsPowerOfDegree() {
        assertTrue(i(1000000).isPowerOfDegree(2))
        assertTrue(i(1000000).isPowerOfDegree(3))
        assertFalse(i(1000000).isPowerOfDegree(4))
        assertTrue(i(76543).isPowerOfDegree(1))
        assertTrue(i(1000).isPowerOfDegree(3))
        assertFalse(i(125).isPowerOfDegree(2))
        assertTrue(i(1).isPowerOfDegree(6))
        assertTrue(i(0).isPowerOfDegree(10))
        assertTrue(i(11).isPrime())
    }

    @Test
    fun testPrimeFactorDecomposition() {
        assertContentEquals(
            listOf(p(2, 1), p(3, 2)),
            i(18).primeFactorDecomposition(),
        )
        assertContentEquals(
            listOf(p(2, 8), p(5, 8), p(7, 1)),
            i(700000000).primeFactorDecomposition(),
        )
        assertContentEquals(listOf(p(23, 1)), i(23).primeFactorDecomposition())
    }

    @Test
    fun testIsFactorizableUnderRationalExponent() {
        // root[9, 4]
        assertTrue(i(9).isFactorizableUnderRationalExponent(i(1), i(4)))
        // root[10, 4]
        assertFalse(i(10).isFactorizableUnderRationalExponent(i(1), i(4)))
        // 8 ^ [3 / 5] --> I don't think we should be simplifying this
        // to 2 * 2^[1 / 5] -- thoughts?
        assertFalse(i(8).isFactorizableUnderRationalExponent(i(3), i(5)))
        // root[12, 2] --> 2 * sqrt[3]
        assertTrue(i(12).isFactorizableUnderRationalExponent(i(1), i(2)))
        assertFalse(i(1).isFactorizableUnderRationalExponent(i(1), i(2)))
    }

    @Test
    fun testGreatestSquareFactor() {
        assertEquals(i(100), i(100).greatestSquareFactor())
        assertEquals(i(100), i(1500).greatestSquareFactor())
        assertEquals(i(1), i(777).greatestSquareFactor())
        assertEquals(i(1024), i(2048).greatestSquareFactor())
    }

    private fun factorsOf(n: Int) = i(n).factors().map { it.toInt() }.toList().sorted()

    @Test
    fun testFactors() {
        assertContentEquals(listOf(1, 2, 4, 5, 10, 20), factorsOf(20))
        assertContentEquals(listOf(1), factorsOf(1))
        assertContentEquals(listOf(1, 19), factorsOf(19))
        assertContentEquals(listOf(1, 2, 4, 8, 16), factorsOf(16))
    }

    @Test
    fun testFactorsCount() {
        assertEquals(49, i(1000000).factors().count())
        assertEquals(10, i(1000000).factors(limit = 10).count())
    }

    @Test
    fun testAsKnownPower() {
        assertEquals(p(2, 2), i(4).asKnownPower())
        assertEquals(p(2, 3), i(8).asKnownPower())
        assertEquals(p(10, 3), i(1000).asKnownPower())
        assertEquals(p(30, 2), i(900).asKnownPower())
        assertEquals(p(200, 3), i(8000000).asKnownPower())
        assertEquals(null, i(5).asKnownPower())
        assertEquals(null, i(10).asKnownPower())
    }
}
