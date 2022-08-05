package methods.integerroots

import methods.plans.testPlan
import org.junit.jupiter.api.Test

class IntegerRootsPlansTest {
    @Test
    fun testSimplifyIntegerRootsTest() = testPlan {
        plan = simplifyIntegerRoot
        inputExpr = "sqrt[113400]"

        check {
            // toExpr = "90 * sqrt[14]"

            step {
                fromExpr = "sqrt[113400]"
                toExpr = "sqrt[[2^3] * [3^4] * [5^2] * 7]"
            }

            step {
                fromExpr = "sqrt[[2^3] * [3^4] * [5^2] * 7]"
                toExpr = "2 * sqrt[2] * [3^2] * 5 * sqrt[7]"
            }

            step {
                fromExpr = "2 * sqrt[2] * [3^2] * 5 * sqrt[7]"
                toExpr = "(2 * [3^2] * 5) * (sqrt[2] * sqrt[7])"
            }

            step {
                fromExpr = "(2 * [3^2] * 5) * (sqrt[2] * sqrt[7])"
                toExpr = "90 * sqrt[14]"
            }
        }
    }
}
