package methods.algebra

import engine.methods.testMethod
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.rationalexpressions.RationalExpressionsExplanation
import org.junit.jupiter.api.Test

class AlgebraPlansTest {

    @Test
    fun `test dividing rational expressions`() = testMethod {
        method = AlgebraPlans.SimplifyAlgebraicExpressionInOneVariable
        inputExpr = "[[x ^ 3] + 3 [x ^ 2] + 3 x + 1 / [x ^ 3] + 1] : [x + 1 / [x ^ 2] - x + 1]"

        check {
            fromExpr = "[[x ^ 3] + 3 [x ^ 2] + 3 x + 1 / [x ^ 3] + 1] : [x + 1 / [x ^ 2] - x + 1]"
            toExpr = "x + 1"
            explanation {
                key = AlgebraExplanation.SimplifyAlgebraicExpression
            }

            step {
                fromExpr = "[[x ^ 3] + 3 [x ^ 2] + 3 x + 1 / [x ^ 3] + 1] : [x + 1 / [x ^ 2] - x + 1]"
                toExpr = "[[x ^ 3] + 3 [x ^ 2] + 3 x + 1 / [x ^ 3] + 1] * [[x ^ 2] - x + 1 / x + 1]"
                explanation {
                    key = FractionArithmeticExplanation.RewriteDivisionAsMultiplicationByReciprocal
                }
            }

            step {
                fromExpr = "[[x ^ 3] + 3 [x ^ 2] + 3 x + 1 / [x ^ 3] + 1] * [[x ^ 2] - x + 1 / x + 1]"
                toExpr = "[[(x + 1) ^ 2] / [x ^ 2] - x + 1] * [[x ^ 2] - x + 1 / x + 1]"
                explanation {
                    key = RationalExpressionsExplanation.SimplifyRationalExpression
                }
            }

            step {
                fromExpr = "[[(x + 1) ^ 2] / [x ^ 2] - x + 1] * [[x ^ 2] - x + 1 / x + 1]"
                toExpr = "x + 1"
                explanation {
                    key = RationalExpressionsExplanation.MultiplyRationalExpressions
                }
            }
        }
    }

    @Test
    fun `test simplifying before multiplying of rational expressions`() = testMethod {
        method = AlgebraPlans.SimplifyAlgebraicExpressionInOneVariable
        inputExpr = "[[x ^ 2] + 5 x + 6 / 4 [x ^ 2] + 12 x] * [[x ^ 3] / [x ^ 2] - 4]"

        check {
            fromExpr = "[[x ^ 2] + 5 x + 6 / 4 [x ^ 2] + 12 x] * [[x ^ 3] / [x ^ 2] - 4]"
            toExpr = "[[x ^ 2] / 4 (x - 2)]"
            explanation {
                key = AlgebraExplanation.SimplifyAlgebraicExpression
            }

            step {
                fromExpr = "[[x ^ 2] + 5 x + 6 / 4 [x ^ 2] + 12 x] * [[x ^ 3] / [x ^ 2] - 4]"
                toExpr = "[x + 2 / 4 x] * [[x ^ 3] / [x ^ 2] - 4]"
                explanation {
                    key = RationalExpressionsExplanation.SimplifyRationalExpression
                }
            }

            step {
                fromExpr = "[x + 2 / 4 x] * [[x ^ 3] / [x ^ 2] - 4]"
                toExpr = "[[x ^ 2] / 4 (x - 2)]"
                explanation {
                    key = RationalExpressionsExplanation.MultiplyRationalExpressions
                }
            }
        }
    }
}
