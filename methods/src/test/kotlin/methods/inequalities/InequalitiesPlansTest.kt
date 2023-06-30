package methods.inequalities

import engine.methods.testMethodInX
import org.junit.jupiter.api.Test

class InequalitiesPlansTest {
    @Test
    fun test() = testMethodInX {
        method = InequalitiesPlans.DivideByCoefficientOfVariableAndSimplify
        inputExpr = "-3x >= 1"

        check {
            toExpr = "x <= -[1/3]"
        }
    }
}
