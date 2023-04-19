package server

import methods.approximation.ApproximationPlans
import methods.constantexpressions.ConstantExpressionsPlans
import methods.decimals.DecimalPlans
import methods.integerarithmetic.IntegerArithmeticPlans
import org.junit.jupiter.api.Test

class TestSelectPlansForConstantExpressions {
    @Test
    fun testAdditionOfTwoIntegers() {
        testSelectPlanApi(
            "1 + 2",
            setOf(
                IntegerArithmeticPlans.EvaluateArithmeticExpression,
            ),
        )
    }

    @Test
    fun testAdditionOfFractions() {
        testSelectPlanApi(
            "[1/2] + [1/5]",
            setOf(
                ConstantExpressionsPlans.SimplifyConstantExpression,
                DecimalPlans.EvaluateExpressionAsDecimal,
            ),
        )
    }

    @Test
    fun testDivisionResultNonInteger() {
        testSelectPlanApi(
            "5 + 6*3:5",
            setOf(
                ApproximationPlans.ApproximateExpression,
                DecimalPlans.EvaluateExpressionAsDecimal,
                ConstantExpressionsPlans.SimplifyConstantExpression,
            ),
        )
    }
}
