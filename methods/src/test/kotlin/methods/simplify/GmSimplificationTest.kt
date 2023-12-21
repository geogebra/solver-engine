package methods.simplify

import engine.context.Preset
import engine.methods.testMethod
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("GmAction")
class GmSimplificationTest {
    // @erik you might want to make this a gmaction test
    @Test
    fun `test fraction and value multiplication`() = testMethod(Preset.GMFriendly) {
        method = SimplifyPlans.SimplifyAlgebraicExpression
        inputExpr = "2 * [x / 2]"

        check {
            fromExpr = "2 * [x / 2]"
            toExpr = "x"
            explanation {
                key = FractionArithmeticExplanation.MultiplyAndSimplifyFractions
            }

            step {
                fromExpr = "2 * [x / 2]"
                toExpr = "[2 x / 2]"
                explanation {
                    key = FractionArithmeticExplanation.MultiplyFractionAndValue
                }
            }

            step {
                fromExpr = "[2 x / 2]"
                toExpr = "x"
                explanation {
                    key = GeneralExplanation.CancelDenominator
                }
            }
        }
    }
}
