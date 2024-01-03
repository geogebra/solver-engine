package server

import methods.inequalities.InequalitiesPlans
import org.junit.jupiter.api.Test

class TestSelectPlansForInequality {
    @Test
    fun `test constant inequality`() {
        testSelectPlanApi(
            "3 + 4 <= 7",
            setOf(
                InequalitiesPlans.SolveConstantInequality,
            ),
        )
    }

    @Test
    fun `test linear inequality`() {
        testSelectPlanApiInX(
            "2x < 1",
            setOf(
                InequalitiesPlans.SolveLinearInequality,
            ),
        )
    }

    @Test
    fun `test linear inequality with simplification`() {
        testSelectPlanApiInX(
            "2x + x < 1 - 4",
            setOf(
                InequalitiesPlans.SolveLinearInequality,
            ),
        )
    }
}
