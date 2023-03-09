/**
 * this file contains tests for all SelectPlanApiService
 * for where the input is an equation
 */
package server

import methods.equations.EquationsPlans
import org.junit.jupiter.api.Test

class TestSelectPlansForEquation {
    @Test
    fun `test ax = b divides c form`() {
        testSelectPlanApiInX(
            "2x = [1/2]",
            setOf(
                EquationsPlans.SolveLinearEquation,
            ),
        )
    }
}
