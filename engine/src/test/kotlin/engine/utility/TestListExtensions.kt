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
}
