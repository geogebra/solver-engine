package methods.general

import methods.plans.testMethod
import org.junit.jupiter.api.Test

class GeneralPlansTest {
    @Test
    fun testSimplifyProductOfPowersWithSameBase() = testMethod {
        method = GeneralPlans.SimplifyProductOfPowersWithSameBase
        inputExpr = "[20 ^ 2] * [20 ^ -3]"

        check {
            toExpr = "[20 ^ -1]"

            step { toExpr = "[20 ^ 2 - 3]" }
            step { toExpr = "[20 ^ -1]" }
        }
    }
}
