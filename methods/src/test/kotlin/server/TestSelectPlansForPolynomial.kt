package server

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
}
