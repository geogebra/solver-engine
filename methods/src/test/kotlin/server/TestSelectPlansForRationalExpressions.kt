package server

import methods.algebra.AlgebraPlans
import methods.polynomials.PolynomialsPlans
import org.junit.jupiter.api.Test

class TestSelectPlansForRationalExpressions {
    @Test
    fun `test addition of rational expressions`() {
        testSelectPlanApiInX(
            "[3 / (x - 1)(x - 2)] + [3 / (-x + 1)(x - 3)]",
            setOf(
                AlgebraPlans.ComputeDomainAndSimplifyAlgebraicExpression,
                PolynomialsPlans.ExpandPolynomialExpression,
            ),
        )
    }

    @Test
    fun `test simplifying of rational expression`() {
        testSelectPlanApiInX(
            "[3*2 / x]",
            setOf(
                AlgebraPlans.ComputeDomainAndSimplifyAlgebraicExpression,
            ),
        )
    }
}
