package methods.general

import methods.plans.testMethod
import org.junit.jupiter.api.Test

class GeneralPlansTest {

    @Test
    fun testEvaluateOperationContainingZero() = testMethod {
        method = evaluateOperationContainingZero
        inputExpr = "0:1"
    }
}
