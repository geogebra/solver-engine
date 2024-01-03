package methods.fractionarithmetic

import engine.context.BooleanSetting
import engine.context.Preset
import engine.context.Setting
import engine.methods.testMethodInX
import methods.simplify.SimplifyPlans
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

// An example of how the tests can be written, to be moved somewhere better.
// The tag can be on the class as here, in which case all tests in the class will be tagged, or
// on individual tests.
@Tag("GmAction")
class GmFractionsTest {
    @Test
    fun `frac{-a}{-b}`() =
        testMethodInX(Preset.GMFriendly) {
            // method = ConstantExpressionsPlans.SimplifyConstantExpression
            method = SimplifyPlans.SimplifyAlgebraicExpression

            inputExpr = "[-3/-2]"
            check {
                toExpr = "[3 / 2]"
            }
        }

    @Test
    fun `-frac{a}{-b}`() =
        testMethodInX(Preset.GMFriendly) {
            // method = ConstantExpressionsPlans.SimplifyConstantExpression
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "-[3/-2]"
            check {
                toExpr = "[3 / 2]"
            }
        }

    @Test
    fun `-frac{-a}{b}+x`() =
        testMethodInX(Preset.GMFriendly) {
            // method = ConstantExpressionsPlans.SimplifyConstantExpression
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "-[-3/2]+x"
            check {
                toExpr = "x + [3 / 2]"
            }
        }

    @Test
    fun `multiply fractions`() =
        testMethodInX(Preset.GMFriendly) {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "[2/3]*[3/5]"
            check { toExpr = "[2/5]" }
        }

    @Test
    fun `add fraction and non-fraction`() =
        testMethodInX(Preset.GMFriendly) {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "2+[1/3]"
            check { toExpr = "[7/3]" }
        }

    @Test
    fun `simplify polynomial with fractional coefficients`() =
        testMethodInX(Preset.GMFriendly) {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            context =
                context.addSettings(
                    mapOf(Setting.RestrictAddingFractionsWithConstantDenominator setTo BooleanSetting.True),
                )
            inputExpr = "1 + [2 x / 3] + [[x ^ 2] / 2] - 3 * [[x ^ 4] / 2]"
            check { toExpr = "-[3 / 2] [x ^ 4] + [1 / 2] [x ^ 2] + [2 / 3] x + 1" }
        }
}
