package engine.conditions

import engine.operators.SumOperator
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

class NotZeroConditionsTest

class SumTermsAreIncommensurableTest {

    private fun testSum(exprString: String, incommensurable: Boolean) {
        val expr = parseExpression(exprString)
        assertEquals(SumOperator, expr.operator)
        assertEquals(incommensurable, sumTermsAreIncommensurable(expr.operands))
    }

    @Test
    fun testSumTermsAreIncommensurable() {
        testSum("1 + 2", false)
        testSum("1 + sqrt[2]", true)
        testSum("-2*sqrt[3] + [1 / 3] - [sqrt[3]/5]", false)
        testSum("-2*sqrt[3] + [1 / 3] - [3*root[3, 3]/5]", true)
        testSum("[1 / 3] - 2", false)
        testSum("sqrt[18] + 2", false)
        testSum("root[5,3] + root[6, 3] - [1/3]", true)
    }
}

class ExpressionIsDefinitelyNotZeroTest {

    private fun testNotZero(exprString: String, notZero: Boolean) {
        val expr = parseExpression(exprString)
        assertEquals(notZero, expr.isDefinitelyNotZero())
    }

    @Test
    fun testExpressionIsDefinitelyNotZero() {
        testNotZero("1", true)
        testNotZero("-[1/2]", true)
        testNotZero("2*(sqrt[3] - sqrt[2])", true)
    }
}
