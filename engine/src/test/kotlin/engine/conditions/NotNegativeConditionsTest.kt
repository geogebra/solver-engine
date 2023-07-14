
import engine.conditions.isDefinitelyNotNegative
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

class NotNegativeConditionsTest {

    private fun testNotNegative(exprString: String, notNegative: Boolean) {
        val expr = parseExpression(exprString)
        assertEquals(notNegative, expr.isDefinitelyNotNegative())
    }

    @Test
    fun testExpressionIsDefinitelyNotNegative() {
        testNotNegative("1", true)
        testNotNegative("[x^2]", true)
        testNotNegative("[x^3]", false)
        testNotNegative("2*(sqrt[3] - sqrt[2])", true)
        testNotNegative("abs[x]", true)
        testNotNegative("-3x", false)
        testNotNegative("2 - 3 + 5", true)
        testNotNegative("sqrt[2*sqrt[3] - 2]", true)
        testNotNegative("2 - sqrt[2]", true)
    }
}
