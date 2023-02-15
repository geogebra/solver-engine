package methods.polynomials

import engine.expressions.Expression
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

class MonomialGCFTest {

    private fun exprs(vararg exprsStrings: String): List<Expression> = exprsStrings.map { parseExpression(it) }

    @Test
    fun testSplitIntegersInMonomials() {
        assertEquals(
            null,
            MonomialGCF.splitIntegersInMonomials(exprs("9x * 4", "3 [x^2]")),
        )
        assertEquals(
            null,
            MonomialGCF.splitIntegersInMonomials(exprs("5x", "3 [x^2]")),
        )
        assertEquals(
            exprs("3x", "3 * 2"),
            MonomialGCF.splitIntegersInMonomials(exprs("3x", "6")),
        )
        assertEquals(
            exprs("4x", "4 * 2[x^2]", "4 * 3[x^3]"),
            MonomialGCF.splitIntegersInMonomials(exprs("4x", "8[x^2]", "12[x^3]")),
        )
        assertEquals(
            exprs("4 * 7x", "-4[x^2]", "4 * 3[x^3]"),
            MonomialGCF.splitIntegersInMonomials(exprs("28x", "-4[x^2]", "12[x^3]")),
        )
    }

    @Test
    fun testSplitVariablePowersInMonomials() {
        assertEquals(
            null,
            MonomialGCF.splitVariablePowersInMonomials(exprs("3x", "6"), "x"),
        )
        assertEquals(
            null,
            MonomialGCF.splitVariablePowersInMonomials(exprs("3x", "x * 6[x^2]"), "x"),
        )
        assertEquals(
            exprs("3x", "2 x * [x^2]"),
            MonomialGCF.splitVariablePowersInMonomials(exprs("3x", "2[x^3]"), "x"),
        )
        assertEquals(
            exprs("sqrt[3]*[x^2]", "2 [x^2] * x", "-[x^2] * [x^3]"),
            MonomialGCF.splitVariablePowersInMonomials(exprs("sqrt[3]*[x^2]", "2[x^3]", "-[x^5]"), "x"),
        )
    }
}
