package methods.constantexpressions

import engine.methods.testMethod
import methods.decimals.DecimalsExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test

class DecimalsTest {

    @Test
    fun testSimplifyConstantExpressionWithDivisionOfDecimals() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "1 + 0.002 : 0.6"

        check {
            fromExpr = "1 + 0.002 : 0.6"
            toExpr = "[301 / 300]"

            step {
                fromExpr = "1 + 0.002 : 0.6"
                toExpr = "1 + [0.002 / 0.6]"
                explanation {
                    key = FractionArithmeticExplanation.RewriteDivisionAsFraction
                }
            }

            step {
                fromExpr = "1 + [0.002 / 0.6]"
                toExpr = "1 + [2 / 600]"
                explanation {
                    key = DecimalsExplanation.NormalizeFractionOfDecimals
                }
            }

            step {
                fromExpr = "1 + [2 / 600]"
                toExpr = "1 + [1 / 300]"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyFraction
                }
            }

            step {
                fromExpr = "1 + [1 / 300]"
                toExpr = "[301 / 300]"
            }
        }
    }

    @Test
    fun testDivisionOfDecimalsWithTrailingZeros() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[1.20000 / 1.2]"

        check {
            fromExpr = "[1.20000 / 1.2]"
            toExpr = "1"
            explanation {
                key = ConstantExpressionsExplanation.SimplifyConstantExpression
            }

            step {
                fromExpr = "[1.20000 / 1.2]"
                toExpr = "[1.2 / 1.2]"
                explanation {
                    key = DecimalsExplanation.StripTrailingZerosAfterDecimal
                }
            }

            step {
                fromExpr = "[1.2 / 1.2]"
                toExpr = "1"
                explanation {
                    key = GeneralExplanation.SimplifyUnitFractionToOne
                }
            }
        }
    }
}
