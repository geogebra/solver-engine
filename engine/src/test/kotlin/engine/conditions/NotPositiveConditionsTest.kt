
import engine.conditions.isDefinitelyNotPositive
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

class NotPositiveConditionsTest {

    private fun testNotPositive(exprString: String, notPositive: Boolean) {
        val expr = parseExpression(exprString)
        assertEquals(notPositive, expr.isDefinitelyNotPositive())
    }

    @Test
    fun testExpressionIsDefinitelyNotNegative() {
        testNotPositive("-1", true)
        testNotPositive("-[x^2]", true)
        testNotPositive("[x^3]", false)
        testNotPositive("2*(sqrt[2] - sqrt[3])", true)
        testNotPositive("-abs[x]", true)
        testNotPositive("-1 - abs[x]", true)
        testNotPositive("3x", false)
        testNotPositive("sqrt[3] - sqrt[4]", true)
        testNotPositive("-2 + sqrt[2]", true)
    }
}
