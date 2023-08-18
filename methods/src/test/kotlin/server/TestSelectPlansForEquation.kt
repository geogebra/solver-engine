/**
 * this file contains tests for all SelectPlanApiService
 * for where the input is an equation
 */
package server

import methods.equations.EquationsPlans
import org.junit.jupiter.api.Test

class TestSelectPlansForEquation {

    @Test
    fun `test constant equation`() {
        testSelectPlanApi(
            "3 + 4 = 7",
            setOf(
                EquationsPlans.SolveConstantEquation,
            ),
        )
    }

    @Test
    fun `test linear equation with fractions`() {
        testSelectPlanApiInX(
            "2x = [1/2]",
            setOf(
                EquationsPlans.SolveEquationInOneVariable,
            ),
        )
    }

    @Test
    fun `test linear equation with decimal coefficients but with no exact decimal solution`() {
        testSelectPlanApiInX(
            "3.1 x + 2.2 = 1.21",
            setOf(
                EquationsPlans.SolveEquationInOneVariable,
            ),
        )
    }

    @Test
    fun `test trivial linear equation containing recurring decimal`() {
        testSelectPlanApiInX(
            "2x + 2.2[3] = x",
            setOf(
                EquationsPlans.SolveEquationInOneVariable,
                EquationsPlans.SolveDecimalLinearEquation,
            ),
        )
    }

    @Test
    fun `test non-trivial linear equation containing recurring decimal`() {
        testSelectPlanApiInX(
            "3.1x + 2.2[3] = 1.21",
            setOf(
                EquationsPlans.SolveEquationInOneVariable,
            ),
        )
    }

    @Test
    fun `test linear equations with decimal coefficients with solution expressible as an exact decimal`() {
        testSelectPlanApiInX(
            "3.2x + 2.2 = 1.2",
            setOf(
                EquationsPlans.SolveEquationInOneVariable,
                EquationsPlans.SolveDecimalLinearEquation,
            ),
        )
    }
}
