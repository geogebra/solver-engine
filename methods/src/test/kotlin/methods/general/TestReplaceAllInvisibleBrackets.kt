package methods.general

import methods.plans.testPlan
import org.junit.jupiter.api.Test

class TestReplaceAllInvisibleBrackets {

    // @Test
    fun testReplaceAllInvisibleBracketsSimple() = testPlan {
        plan = normalizeBrackets
        inputExpr = "3*-4"
        check {
            toExpr = "3*(-4)"

            explanation {
                key = Explanation.ReplaceAllInvisibleBrackets
            }
        }
    }

    @Test
    fun testReplaceAllInvisibleBracketsNoTransformation() = testPlan {
        plan = normalizeBrackets
        inputExpr = "[1/3 -4x]"

        check {
            noTransformation()
        }
    }

    // @Test
    fun testReplaceAllInvisibleBracketsNested() = testPlan {
        plan = normalizeBrackets
        inputExpr = "[3 * -4/1 --+-2]"

        check {
            toExpr = "[3 * (-4) / 1 - (-(+(-2)))]"

            explanation {
                key = Explanation.ReplaceAllInvisibleBrackets
            }
        }
    }
}
