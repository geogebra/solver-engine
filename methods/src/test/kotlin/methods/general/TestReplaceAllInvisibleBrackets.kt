package methods.general

import methods.plans.testMethod
import org.junit.jupiter.api.Test

class TestReplaceAllInvisibleBrackets {

    // @Test
    fun testReplaceAllInvisibleBracketsSimple() = testMethod {
        method = addClarifyingBrackets
        inputExpr = "3*-4"
        check {
            toExpr = "3*(-4)"

            explanation {
                key = Explanation.ReplaceAllInvisibleBrackets
            }
        }
    }

    @Test
    fun testReplaceAllInvisibleBracketsNoTransformation() = testMethod {
        method = addClarifyingBrackets
        inputExpr = "[1/3 -4x]"

        check {
            noTransformation()
        }
    }

    // @Test
    fun testReplaceAllInvisibleBracketsNested() = testMethod {
        method = addClarifyingBrackets
        inputExpr = "[3 * -4/1 --+-2]"

        check {
            toExpr = "[3 * (-4) / 1 - (-(+(-2)))]"

            explanation {
                key = Explanation.ReplaceAllInvisibleBrackets
            }
        }
    }
}
