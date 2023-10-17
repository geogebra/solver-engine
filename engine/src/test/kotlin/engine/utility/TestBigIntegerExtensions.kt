package engine.utility

import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
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
}
