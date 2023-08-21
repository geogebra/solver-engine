package methods.constantexpressions

import engine.methods.testMethod
import org.junit.jupiter.api.Test

class OrderOfOperationsTest {

    @Test
    fun testFractionOverOnePriority() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[sqrt[12] / 1]"

        check {
            step {
                toExpr = "sqrt[12]"
            }
            step {}
        }
    }
}
