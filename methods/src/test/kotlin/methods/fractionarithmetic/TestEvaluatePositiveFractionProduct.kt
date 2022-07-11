package methods.fractionarithmetic

import methods.plans.testPlan
import org.junit.jupiter.api.Test

class TestEvaluatePositiveFractionProduct {

    @Test
    fun simpleTest() = testPlan {
        plan = evaluatePositiveFractionProduct

        inputExpr = "[3 / 4] * [5 / 6]"

        check {
            toExpr = "[5 / 8]"

            explanation {
                key = Explanation.EvaluatePositiveFractionProduct

                param {
                    expr = "[3 / 4]"
                }
                param {
                    expr = "[5 / 6]"
                }
            }
        }
    }
}
