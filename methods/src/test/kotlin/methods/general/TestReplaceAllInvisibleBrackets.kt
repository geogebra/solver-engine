package methods.general

import engine.methods.testMethod
import org.junit.jupiter.api.Test

class TestReplaceAllInvisibleBrackets {

    @Test
    fun testReplaceAllInvisibleBracketsSimple() = testMethod {
        method = NormalizationPlans.AddClarifyingBrackets
        inputExpr = "3*-4"
        check {
            toExpr = "3*(-4)"

            explanation {
                key = Explanation.AddClarifyingBrackets
            }
        }
    }

    @Test
    fun testReplaceAllInvisibleBracketsNoTransformation() = testMethod {
        method = NormalizationPlans.AddClarifyingBrackets
        inputExpr = "[1/3 -4x]"

        check {
            noTransformation()
        }
    }

    @Test
    fun testReplaceAllInvisibleBracketsNested() = testMethod {
        method = NormalizationPlans.AddClarifyingBrackets
        inputExpr = "[3 * -4/1 --+-2]"

        check {
            toExpr = "[3 * (-4) / 1 - (-(+(-2)))]"

            explanation {
                key = Explanation.AddClarifyingBrackets
            }
        }
    }
}
