package server

import methods.fallback.FallbackPlans
import methods.polynomials.PolynomialsPlans
import org.junit.jupiter.api.Test

class TestSelectPlansForPolynomial {
    @Test
    fun testExpandSimpleExpression() {
        testSelectPlanApiInX(
            "x(x + 1)",
            setOf(
                PolynomialsPlans.ExpandPolynomialExpression,
            ),
        )
    }

    @Test
    fun testSimplifiedLinearExpression() {
        testSelectPlanApi(
            "2x + 1",
            setOf(
                FallbackPlans.ExpressionIsFullySimplified,
            ),
        )
    }

    @Test
    fun testSimplifiedMonomial() {
        testSelectPlanApi(
            "-5[x ^ 7]",
            setOf(
                FallbackPlans.ExpressionIsFullySimplified,
            ),
        )
    }

    @Test
    fun testIrreducibleQuadratic() {
        testSelectPlanApi(
            "[x^2] + 2x + 10",
            setOf(
                FallbackPlans.QuadraticIsIrreducible,
            ),
        )
    }
}
