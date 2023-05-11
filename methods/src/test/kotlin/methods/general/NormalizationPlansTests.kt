package methods.general

import engine.context.Context
import engine.methods.testMethod
import org.junit.jupiter.api.Test

class NormalizationPlansTests {

    @Test
    fun testReplaceAllInvisibleBracketsSimple() = testMethod {
        method = NormalizationPlans.NormalizeExpression
        inputExpr = "3*-4"
        check {
            toExpr = "3*(-4)"

            explanation {
                key = Explanation.AddClarifyingBracket
            }
        }
    }

    @Test
    fun testReplaceAllInvisibleBracketsSimpleGm() = testMethod {
        method = NormalizationPlans.NormalizeExpression
        inputExpr = "3*-4"
        context = Context(gmFriendly = true)
        check {
            noTransformation()
        }
    }

    @Test
    fun testReplaceAllInvisibleBracketsNoTransformation() = testMethod {
        method = NormalizationPlans.NormalizeExpression
        inputExpr = "[1/3 - 4x]"

        check {
            noTransformation()
        }
    }

    @Test
    fun testReplaceAllInvisibleBracketsNested() = testMethod {
        method = NormalizationPlans.NormalizeExpression
        inputExpr = "[3 * -4/1 --+-2]"

        check {
            fromExpr = "[3 * -4 / 1 - -+-2]"
            toExpr = "[3 * (-4) / 1 - (-(-2))]"
            explanation {
                key = GeneralExplanation.NormalizeExpression
            }

            step {
                fromExpr = "[3 * -4 / 1 - -+-2]"
                toExpr = "[3 * (-4) / 1 - -+-2]"
                explanation {
                    key = GeneralExplanation.AddClarifyingBracket
                }
            }

            step {
                fromExpr = "[3 * (-4) / 1 - -+-2]"
                toExpr = "[3 * (-4) / 1 - (-+-2)]"
                explanation {
                    key = GeneralExplanation.AddClarifyingBracket
                }
            }

            step {
                fromExpr = "[3 * (-4) / 1 - (-+-2)]"
                toExpr = "[3 * (-4) / 1 - (-(+-2))]"
                explanation {
                    key = GeneralExplanation.AddClarifyingBracket
                }
            }

            step {
                fromExpr = "[3 * (-4) / 1 - (-(+-2))]"
                toExpr = "[3 * (-4) / 1 - (-(+(-2)))]"
                explanation {
                    key = GeneralExplanation.AddClarifyingBracket
                }
            }

            step {
                fromExpr = "[3 * (-4) / 1 - (-(+(-2)))]"
                toExpr = "[3 * (-4) / 1 - (-(-2))]"
                explanation {
                    key = GeneralExplanation.RemoveRedundantPlusSign
                }
            }
        }
    }

    @Test
    fun testRearrangeTermsInAProductGm() = testMethod {
        method = NormalizationPlans.ReorderProductInSteps
        inputExpr = "sqrt[3] * 5 * ([y ^ 2] + 1) * (1 + sqrt[3]) * sqrt[y] * y"

        check {
            step { toExpr = "5 sqrt[3] ([y ^ 2] + 1) (1 + sqrt[3]) sqrt[y] * y" }
            step { toExpr = "5 sqrt[3] (1 + sqrt[3]) ([y ^ 2] + 1) sqrt[y] * y" }
            step { toExpr = "5 sqrt[3] (1 + sqrt[3]) sqrt[y] ([y ^ 2] + 1) y" }
            step { toExpr = "5 sqrt[3] (1 + sqrt[3]) y sqrt[y] ([y ^ 2] + 1)" }
        }
    }

    @Test
    fun testRemoveAllBracketProductInProduct() = testMethod {
        method = NormalizationPlans.RemoveAllBracketProductInProduct
        inputExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"

        check {
            toExpr = "sqrt[2] * 2 sqrt[2] * 3 sqrt[2]"

            step { toExpr = "sqrt[2] * 2 sqrt[2] (3 sqrt[2])" }
            step { toExpr = "sqrt[2] * 2 sqrt[2] * 3 sqrt[2]" }
        }
    }
}
