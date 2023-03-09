package server

import methods.inequalities.InequalitiesPlans
import org.junit.jupiter.api.Test

class TestSelectPlansForInequality {
    @Test
    fun testSimpleLinearInequality() {
        testSelectPlanApiInX(
            "2x < 1",
            setOf(
                InequalitiesPlans.SolveLinearInequality,
            ),
        )
    }
}
