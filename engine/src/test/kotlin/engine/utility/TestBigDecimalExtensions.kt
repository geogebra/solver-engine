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
