package methods.general

import engine.context.Context
import engine.methods.testMethod
import org.junit.jupiter.api.Test

class NormalizationPlansTests {

    @Test
    fun testAddImplicitMultiplicationGm() = testMethod {
        method = NormalizationPlans.NormaliseSimplifiedProduct
        context = Context(gmFriendly = true)
        inputExpr = "x sqrt[y]"

        check {
            fromExpr = "x sqrt[y]"
            toExpr = "x * sqrt[y]"
            explanation {
                key = GeneralExplanation.NormaliseSimplifiedProduct
            }
        }
    }

    @Test
    fun testRemoveImplicitMultiplicationGm() = testMethod {
        method = NormalizationPlans.NormaliseSimplifiedProduct
        context = Context(gmFriendly = true)
        inputExpr = "3 * sqrt[3]"

        check {
            fromExpr = "3 * sqrt[3]"
            toExpr = "3 sqrt[3]"
            explanation {
                key = GeneralExplanation.NormaliseSimplifiedProduct
            }
        }
    }

    @Test
    fun testRearrangeTermsInAProductGm() = testMethod {
        method = NormalizationPlans.NormaliseSimplifiedProduct
        context = Context(gmFriendly = true)
        inputExpr = "sqrt[3] * 5 * ([y ^ 2] + 1) * (1 + sqrt[3]) * sqrt[y] * y"
        check {
            step { toExpr = "5 sqrt[3] ([y ^ 2] + 1) (1 + sqrt[3]) sqrt[y] * y" }
            step { toExpr = "5 sqrt[3] (1 + sqrt[3]) ([y ^ 2] + 1) sqrt[y] * y" }
            step { toExpr = "5 sqrt[3] (1 + sqrt[3]) sqrt[y] ([y ^ 2] + 1) y" }
            step { toExpr = "5 sqrt[3] (1 + sqrt[3]) y * sqrt[y] ([y ^ 2] + 1)" }
        }
    }
}
