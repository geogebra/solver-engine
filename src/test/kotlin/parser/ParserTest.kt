package parser

import expressions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ParserTest {

    private fun testCase(text: String, expr: Expression) {
        assertEquals(expr, parseExpression(text), text)
    }

    @Test
    fun testParser() {
        testCase("1+2", sumOf(xp(1), xp(2)))
        testCase("3-2*5", sumOf(xp(3), negOf(productOf(xp(2), xp(5)))))
        testCase(
            "(-5+2)*7",
            productOf(
                bracketOf(sumOf(negOf(xp(5)), xp(2))),
                xp(7)
            )
        )
        testCase(
            "2x^3y^5",
            implicitProductOf(
                xp(2),
                powerOf(xp("x"), xp(3)),
                powerOf(xp("y"), xp(5))
            )
        )
    }
}
