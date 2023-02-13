package engine.expressions

import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

fun e(s: String) = parseExpression(s)

class TestExpression {

    @Test
    fun testSplitPlusMinus() {
        assertEquals(listOf(e("1 + 1")), e("1 + 1").splitPlusMinus())
        assertEquals(listOf(e("4 - 1"), e("4 + 1")), e("4 +/- 1").splitPlusMinus())
        assertEquals(listOf(e("[2 - 5/6]"), e("[2 + 5/6]")), e("[2 +/- 5/6]").splitPlusMinus())
        assertEquals(
            listOf(e("1 - 2 - 3"), e("1 - 2 + 3"), e("1 + 2 - 3"), e("1 + 2 + 3")),
            e("1 +/- 2 +/- 3").splitPlusMinus(),
        )
    }
}
