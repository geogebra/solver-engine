package methods.constantexpressions

import engine.methods.testMethod
import methods.expand.ExpandExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import org.junit.jupiter.api.Test

class ExpandExpressionTest {
    @Test
    fun `test simplify by expanding constant expression`() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "3(sqrt[2] + 1) + root[5, 3] + 1"

        check {
            fromExpr = "3 (sqrt[2] + 1) + root[5, 3] + 1"
            toExpr = "3 sqrt[2] + 4 + root[5, 3]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "3 (sqrt[2] + 1) + root[5, 3] + 1"
                toExpr = "<.3 sqrt[2] + 3.> + root[5, 3] + 1"
                explanation {
                    key = ExpandExplanation.ExpandSingleBracketAndSimplify
                }

                step {
                    fromExpr = "3 (sqrt[2] + 1)"
                    toExpr = "<.3 * sqrt[2] + 3 * 1.>"
                    explanation {
                        key = ExpandExplanation.DistributeMultiplicationOverSum
                    }
                }

                step {
                    fromExpr = "<.3 * sqrt[2] + 3 * 1.>"
                    toExpr = "<.3 sqrt[2] + 3.>"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyConstantExpression
                    }
                }
            }

            step {
                fromExpr = "3 sqrt[2] + 3 + root[5, 3] + 1"
                toExpr = "3 sqrt[2] + 4 + root[5, 3]"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                }
            }
        }
    }
}
