package plans

import org.junit.jupiter.api.Test
import steps.metadata.PlanExplanation

class TestReplaceAllInvisibleBrackets {

    @Test
    fun simpleTest() = testPlan {
        plan = replaceAllInvisibleBrackets
        inputExpr = "3*-4"
        check {
            toExpr = "3*(-4)"

            explanation {
                key = PlanExplanation.ReplaceAllInvisibleBrackets
            }
        }
    }

    @Test
    fun testNoTransformation() = testPlan {
        plan = replaceAllInvisibleBrackets
        inputExpr = "[1/3 -4x]"

        check {
            noTransformation()
        }
    }

    @Test
    fun harderTest() = testPlan {
        plan = replaceAllInvisibleBrackets
        inputExpr = "[3 * -4/1 --+-2]"

        check {
            toExpr = "[3 * (-4) / 1 - (-(+(-2)))]"

            explanation {
                key = PlanExplanation.ReplaceAllInvisibleBrackets
            }
        }
    }
}