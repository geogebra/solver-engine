package methods.equations

import engine.context.BooleanSetting
import engine.context.Setting
import engine.methods.testMethodInX
import org.junit.jupiter.api.Test

/**
 * For EquationsPlans.SimplifyEquation
 */
class SimplifyTest {
    @Test
    fun `test simplify constant factor when rhs = 0`() =
        testMethodInX {
            method = EquationsPlans.SimplifyEquation
            inputExpr = "3 (x + 1) = 0"

            check {
                fromExpr = "3 (x + 1) = 0"
                toExpr = "x + 1 = 0"
                explanation {
                    key = EquationsExplanation.EliminateConstantFactorOfLhsWithZeroRhs
                }
            }
        }

    @Test
    fun `test simplify constant factor when rhs = 0 by dividing`() =
        testMethodInX {
            method = EquationsPlans.SimplifyEquation
            context = context.addSettings(mapOf(Setting.EliminateNonZeroFactorByDividing.setTo(BooleanSetting.True)))
            inputExpr = "(x + 1) (sqrt[3] + 2) = 0"

            check {
                fromExpr = "(x + 1) (sqrt[3] + 2) = 0"
                toExpr = "x + 1 = 0"
                explanation {
                    key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariableAndSimplify
                }

                step {
                    fromExpr = "(x + 1) (sqrt[3] + 2) = 0"
                    toExpr = "[(x + 1) (sqrt[3] + 2) / sqrt[3] + 2] = [0 / sqrt[3] + 2]"
                    explanation {
                        key = methods.solvable.EquationsExplanation.DivideByCoefficientOfVariable
                    }
                }

                step {
                    fromExpr = "[(x + 1) (sqrt[3] + 2) / sqrt[3] + 2] = [0 / sqrt[3] + 2]"
                    toExpr = "x + 1 = 0"
                    explanation {
                        key = EquationsExplanation.SimplifyEquation
                    }
                }
            }
        }
}
