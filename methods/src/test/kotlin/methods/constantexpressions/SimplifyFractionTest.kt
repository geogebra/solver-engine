package methods.constantexpressions

import engine.methods.testMethod
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test

class SimplifyFractionTest {
    @Test
    fun `test simplify fraction with sum numerator & denominator additive inverse`() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[1 - 2 sqrt[2] / 2 sqrt[2] - 1]"

        check {
            fromExpr = "[1 - 2 sqrt[2] / 2 sqrt[2] - 1]"
            toExpr = "-1"
            explanation {
                key = FractionArithmeticExplanation.SimplifyFraction
            }

            step {
                fromExpr = "[1 - 2 sqrt[2] / 2 sqrt[2] - 1]"
                toExpr = "[-(-1 + 2 sqrt[2]) / 2 sqrt[2] - 1]"
                explanation {
                    key = GeneralExplanation.FactorMinusFromSum
                }
            }

            step {
                fromExpr = "[-(-1 + 2 sqrt[2]) / 2 sqrt[2] - 1]"
                toExpr = "[-(-1 + 2 sqrt[2]) / -1 + 2 sqrt[2]]"
                explanation {
                    key = FractionArithmeticExplanation.ReorganizeCommonSumFactorInFraction
                }
            }

            step {
                fromExpr = "[-(-1 + 2 sqrt[2]) / -1 + 2 sqrt[2]]"
                toExpr = "[-1 / 1]"
                explanation {
                    key = FractionArithmeticExplanation.CancelCommonFactorInFraction
                }
            }

            step {
                fromExpr = "[-1 / 1]"
                toExpr = "-1"
                explanation {
                    key = GeneralExplanation.SimplifyFractionWithOneDenominator
                }
            }
        }
    }

    @Test
    fun `test simplify fraction with power numerator & denominator additive inverse base even power`() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[[(sqrt[2] - 1)^2] / [(1 - sqrt[2])^2]]"

        check {
            fromExpr = "[[(sqrt[2] - 1) ^ 2] / [(1 - sqrt[2]) ^ 2]]"
            toExpr = "1"
            explanation {
                key = FractionArithmeticExplanation.SimplifyFraction
            }

            step {
                fromExpr = "[[(sqrt[2] - 1) ^ 2] / [(1 - sqrt[2]) ^ 2]]"
                toExpr = "[[(sqrt[2] - 1) ^ 2] / [(-(-1 + sqrt[2])) ^ 2]]"
                explanation {
                    key = GeneralExplanation.FactorMinusFromSum
                }
            }

            step {
                fromExpr = "[[(sqrt[2] - 1) ^ 2] / [(-(-1 + sqrt[2])) ^ 2]]"
                toExpr = "[[(sqrt[2] - 1) ^ 2] / [(-1 + sqrt[2]) ^ 2]]"
                explanation {
                    key = GeneralExplanation.SimplifyEvenPowerOfNegative
                }
            }

            step {
                fromExpr = "[[(sqrt[2] - 1) ^ 2] / [(-1 + sqrt[2]) ^ 2]]"
                toExpr = "[[(sqrt[2] - 1) ^ 2] / [(sqrt[2] - 1) ^ 2]]"
                explanation {
                    key = FractionArithmeticExplanation.ReorganizeCommonSumFactorInFraction
                }
            }

            step {
                fromExpr = "[[(sqrt[2] - 1) ^ 2] / [(sqrt[2] - 1) ^ 2]]"
                toExpr = "[1 / 1]"
                explanation {
                    key = FractionArithmeticExplanation.CancelCommonFactorInFraction
                }
            }

            step {
                fromExpr = "[1 / 1]"
                toExpr = "1"
                explanation {
                    key = GeneralExplanation.SimplifyUnitFractionToOne
                }
            }
        }
    }
}
