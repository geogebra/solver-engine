package methods.equationsystems

import engine.methods.testMethod
import org.junit.jupiter.api.Test

class SolveEquationSystemInOneVariablesTest {

    private fun shortTest(equations: String, solution: String) {
        testMethod {
            method = EquationSystemsPlans.SolveEquationSystemInOneVariable
            inputExpr = equations

            check {
                toExpr = solution
            }
        }
    }

    @Test
    fun testSameSolution() = shortTest(
        "2x + 1 = 3, 4x - 1 = 3",
        "SetSolution[x : {1}]",
    )

    @Test
    fun testDifferentSolutions() = shortTest(
        "2x + 1 = 3, 3x + 2 = 1",
        "Contradiction[x : 2x + 1 = 3, 3x + 2 = 1]",
    )

    @Test
    fun testOneCommonSolution() = shortTest(
        "[x ^ 2] = 1, x(x - 1) = 0",
        "SetSolution[x : {1}]",
    )

    @Test
    fun testFirstEquationIsIdentity() = shortTest(
        "x = x, 2x =  6",
        "SetSolution[x : {3}]",
    )

    @Test
    fun testSecondEquationIsIdentity() = shortTest(
        "[x^2] = 4, 2x = x + x",
        "SetSolution[x : {-2, 2}]",
    )

    @Test
    fun testFirstEquationIsContradiction() = shortTest(
        "x + 1 = x, 2x = 3",
        "Contradiction[x : 1 = 0]",
    )

    @Test
    fun testSecondEquationIsContradiction() = shortTest(
        "x = x, x + 1 = x + 2",
        "Contradiction[x : 1 = 2]",
    )
}
