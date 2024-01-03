package engine.utility

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigInteger
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

private infix fun Int.over(n: Int) = Rational(this, n)

class TestRational {
    @Test
    fun testDenominatorCannotBeZero() {
        assertThrows<IllegalStateException> { 1 over 0 }
    }

    @Test
    fun testEquals() {
        assertEquals(1 over 2, 2 over 4)
        assertEquals(-3 over -5, 3 over 5)
        assertNotEquals(5 over 2, 5 over -2)
    }

    @Test
    fun testSameNumber() {
        assert((10 over 2).sameNumber(5))
        assert((10 over -1).sameNumber(-BigInteger.TEN))
        assert((7 over 2).sameNumber(14 over 4))
        assert(!(5 over 2).sameNumber((5)))
    }

    @Test
    fun testUnaryMinus() {
        assertEquals(-(1 over 2), -1 over 2)
        assertEquals(-(3 over -5), 3 over 5)
    }

    @Test
    fun testAdd() {
        assertEquals((1 over 2) + (1 over 3), 5 over 6)
        assertEquals((-2 over 3) + (4 over 4), 1 over 3)
    }

    @Test
    fun testMinus() {
        assertEquals((2 over 5) - (1 over 10), 3 over 10)
        assertEquals((5 over 2) - (1 over -3), 17 over 6)
    }

    @Test
    fun testTimes() {
        assertEquals((3 over 5) * (10 over 7), 6 over 7)
        assertEquals((-5 over 2) * (2 over -5), 1 over 1)
    }

    @Test
    fun testDiv() {
        assertEquals((1 over 2) / (1 over 4), 2 over 1)
        assertEquals((11 over -6) / (-1 over -3), (-11 over 2))
    }
}
