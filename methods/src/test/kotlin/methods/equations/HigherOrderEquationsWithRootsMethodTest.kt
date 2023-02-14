package methods.equations

import engine.methods.testMethodInX
import org.junit.jupiter.api.Test

class HigherOrderEquationsWithRootsMethodTest {

    fun shortTest(inputExpr: String, toExpr: String?) = testMethodInX {
        method = EquationsPlans.SolveEquationUsingRootsMethod
        this.inputExpr = inputExpr

        check {
            if (toExpr != null) {
                this.toExpr = toExpr
                explanation {
                    key = EquationsExplanation.SolveEquationUsingRootsMethod
                }
            } else {
                noTransformation()
            }
        }
    }

    @Test
    fun testCubeEqualsNegative() = shortTest(
        inputExpr = "[x ^ 3] + 10 = 0",
        toExpr = "Solution[x, {-root[10, 3]}]",
    )

    @Test
    fun testCubeEqualsPositive() = shortTest(
        inputExpr = "8 - [x ^ 3] = 0",
        toExpr = "Solution[x, {2}]",
    )

    @Test
    fun testPowerOf5Equals0() = shortTest(
        inputExpr = "2 [x ^ 5] + 3 = 1 + 2",
        toExpr = "Solution[x, {0}]",
    )

    @Test
    fun testSquareOfSquareEqualsNegative() = shortTest(
        inputExpr = "[([x ^ 2]) ^ 2] + 1 = 0",
        toExpr = "Solution[x, {}]",
    )
}
