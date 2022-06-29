package methods.plans

import engine.steps.metadata.PlanExplanation
import org.junit.jupiter.api.Test

class TestReplaceAllInvisibleBrackets {

    @Test
    fun testReplaceAllInvisibleBracketsSimple() = testPlan {
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
    fun testReplaceAllInvisibleBracketsNoTransformation() = testPlan {
        plan = replaceAllInvisibleBrackets
        inputExpr = "[1/3 -4x]"

        check {
            noTransformation()
        }
    }

    @Test
    fun testReplaceAllInvisibleBracketsNested() = testPlan {
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
