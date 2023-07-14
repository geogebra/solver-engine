package methods.constantexpressions

import engine.methods.testMethod
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import org.junit.jupiter.api.Test

class ZeroNumeratorTest {

    @Test
    fun testSimpleRootDenominator() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[0 / sqrt[12]]"

        check {
            fromExpr = "[0 / sqrt[12]]"
            toExpr = "0"
            explanation {
                key = GeneralExplanation.SimplifyZeroNumeratorFractionToZero
            }
        }
    }

    @Test
    fun testPositiveDenominator() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[0 / root[3, 3] + root[4, 3]]"
        check {
            fromExpr = "[0 / root[3, 3] + root[4, 3]]"
            toExpr = "0"

            explanation {
                key = GeneralExplanation.SimplifyZeroNumeratorFractionToZero
            }
        }
    }

    @Test
    fun testDenominatorNotZeroBecauseOfIncommensurableTerms() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[0 / [sqrt[3]/3] - [sqrt[2]/2]]"
        check {
            toExpr = "0"

            explanation {
                key = GeneralExplanation.SimplifyZeroNumeratorFractionToZero
            }
        }
    }

    @Test
    fun testDenominatorEventuallyNotZero() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "[0 / 3 - 5]"

        check {
            fromExpr = "[0 / 3 - 5]"
            toExpr = "0"

            step {
                fromExpr = "[0 / 3 - 5]"
                toExpr = "[0 / -2]"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                }
            }
            step {
                fromExpr = "[0 / -2]"
                toExpr = "0"
                explanation {
                    key = GeneralExplanation.SimplifyZeroNumeratorFractionToZero
                }
            }
        }
    }
}
