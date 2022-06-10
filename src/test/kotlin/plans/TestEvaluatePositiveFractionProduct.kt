package plans

import engine.steps.metadata.PlanExplanation
import methods.plans.evaluatePositiveFractionProduct
import org.junit.jupiter.api.Test

class TestEvaluatePositiveFractionProduct {

    @Test
    fun simpleTest() = testPlan {
        plan = evaluatePositiveFractionProduct

        inputExpr = "[3 / 4] * [5 / 6]"

        check {
            toExpr = "[5 / 8]"

            explanation {
                key = PlanExplanation.MultiplyFractions

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