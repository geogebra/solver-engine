package methods.integerarithmetic

import methods.plans.testMethod
import methods.rules.testRule
import org.junit.jupiter.api.Test

class IntegerArithmeticRulesTest {

    @Test
    fun testEvaluateSignedIntegerAddition() {
        testRule("5 - 4", evaluateSignedIntegerAddition, "1")
        testRule("4 - 5", evaluateSignedIntegerAddition, "-1")
        testRule("1 + x + 2", evaluateSignedIntegerAddition, "3 + x")
        testRule("1 + x + (-2)", evaluateSignedIntegerAddition, "-1 + x")
        testRule("(-2) + 3", evaluateSignedIntegerAddition, "1")
        testRule("(-2) + (-3) + x", evaluateSignedIntegerAddition, "-5 + x")

        testMethod {
            method = evaluateSignedIntegerAddition
            inputExpr = "5 - 3"
            check {
                toExpr = "2"
                explanation {
                    key = Explanation.EvaluateIntegerSubtraction
                }
            }
        }
        testMethod {
            method = evaluateSignedIntegerAddition
            inputExpr = "-5 - 6"
            check {
                toExpr = "-11"
                explanation {
                    key = Explanation.EvaluateIntegerAddition
                }
            }
        }
        testMethod {
            method = evaluateSignedIntegerAddition
            inputExpr = "5 + 10"
            check {
                toExpr = "15"
                explanation {
                    key = Explanation.EvaluateIntegerAddition
                }
            }
        }
    }

    @Test
    fun testEvaluateIntegerProductAndDivision() {
        testRule("z*2*x*3*y", evaluateIntegerProductAndDivision, "z*6*x*y")
        testRule("2*3", evaluateIntegerProductAndDivision, "6")
        testRule("1 * (-2)", evaluateIntegerProductAndDivision, "-2")
        testRule("(-2) * x * 5", evaluateIntegerProductAndDivision, "(-10) * x")
        testRule("10:2", evaluateIntegerProductAndDivision, "5")
        testRule("10:(-2)", evaluateIntegerProductAndDivision, "-5")
        testRule("5:0", evaluateIntegerProductAndDivision, null)
    }

    @Test
    fun testEvaluateIntegerPowerDirectly() {
        testRule("[5^3]", evaluateIntegerPowerDirectly, "125")
        testRule("[(-5) ^ 3]", evaluateIntegerPowerDirectly, "-125")
    }

    @Test
    fun testSimplifyPowerOfNegative() {
        testRule("[(-2)^4]", simplifyEvenPowerOfNegative, "[2^4]")
        testRule("[(-x)^6]", simplifyEvenPowerOfNegative, "[x^6]")
        testRule("[(-2)^5]", simplifyOddPowerOfNegative, "-[2^5]")
        testRule("[(-x)^7]", simplifyOddPowerOfNegative, "-[x^7]")
        testRule("[(-[1 / 2]) ^ 3]", simplifyOddPowerOfNegative, "-[([1 / 2]) ^ 3]")
    }
}
