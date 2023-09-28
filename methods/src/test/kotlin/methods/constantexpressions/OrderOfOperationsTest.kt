package methods.constantexpressions

import engine.methods.testMethod
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test

class OrderOfOperationsTest {

    @Test
    fun testFactorOfOneIsEliminated() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "1 sqrt[2] * 1 sqrt[3]"

        check {
            fromExpr = "1 sqrt[2] * 1 sqrt[3]"
            toExpr = "sqrt[6]"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "1 sqrt[2] * 1 sqrt[3]"
                toExpr = "sqrt[2] * 1 sqrt[3]"
                explanation {
                    key = GeneralExplanation.RemoveUnitaryCoefficient
                }
            }

            step {
                fromExpr = "sqrt[2] * 1 sqrt[3]"
                toExpr = "sqrt[2] * sqrt[3]"
                explanation {
                    key = GeneralExplanation.RemoveUnitaryCoefficient
                }
            }

            step {}
        }
    }

    @Test
    fun testFractionOverOnePriority() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[sqrt[12] / 1]"

        check {
            step {
                toExpr = "sqrt[12]"
            }
            step {}
        }
    }
}
