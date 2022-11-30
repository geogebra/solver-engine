package methods.integerarithmetic

import engine.methods.testMethod
import engine.methods.testRule
import methods.integerarithmetic.IntegerArithmeticRules.EvaluateIntegerPowerDirectly
import methods.integerarithmetic.IntegerArithmeticRules.EvaluateIntegerProductAndDivision
import methods.integerarithmetic.IntegerArithmeticRules.EvaluateSignedIntegerAddition
import methods.integerarithmetic.IntegerArithmeticRules.SimplifyEvenPowerOfNegative
import methods.integerarithmetic.IntegerArithmeticRules.SimplifyOddPowerOfNegative
import org.junit.jupiter.api.Test

class IntegerArithmeticRulesTest {

    @Test
    fun testEvaluateSignedIntegerAddition() {
        testRule("5 - 4", EvaluateSignedIntegerAddition, "1")
        testRule("4 - 5", EvaluateSignedIntegerAddition, "-1")
        testRule("1 + x + 2", EvaluateSignedIntegerAddition, "3 + x")
        testRule("1 + x + (-2)", EvaluateSignedIntegerAddition, "-1 + x")
        testRule("(-2) + 3", EvaluateSignedIntegerAddition, "1")
        testRule("(-2) + (-3) + x", EvaluateSignedIntegerAddition, "-5 + x")

        testMethod {
            method = EvaluateSignedIntegerAddition
            inputExpr = "5 - 3"
            check {
                toExpr = "2"
                explanation {
                    key = Explanation.EvaluateIntegerSubtraction
                }
            }
        }
        testMethod {
            method = EvaluateSignedIntegerAddition
            inputExpr = "-5 - 6"
            check {
                toExpr = "-11"
                explanation {
                    key = Explanation.EvaluateIntegerAddition
                }
            }
        }
        testMethod {
            method = EvaluateSignedIntegerAddition
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
        testRule("z*2*x*3*y", EvaluateIntegerProductAndDivision, "z*6xy")
        testRule("2*3", EvaluateIntegerProductAndDivision, "6")
        testRule("1 * (-2)", EvaluateIntegerProductAndDivision, "-2")
        testRule("(-2) * x * 5", EvaluateIntegerProductAndDivision, "(-10) x")
        testRule("10:2", EvaluateIntegerProductAndDivision, "5")
        testRule("10:(-2)", EvaluateIntegerProductAndDivision, "-5")
        testRule("5:0", EvaluateIntegerProductAndDivision, null)
    }

    @Test
    fun testEvaluateIntegerPowerDirectly() {
        testRule("[5^3]", EvaluateIntegerPowerDirectly, "125")
        testRule("[(-5) ^ 3]", EvaluateIntegerPowerDirectly, "-125")
    }

    @Test
    fun testSimplifyPowerOfNegative() {
        testRule("[(-2)^4]", SimplifyEvenPowerOfNegative, "[2^4]")
        testRule("[(-x)^6]", SimplifyEvenPowerOfNegative, "[x^6]")
        testRule("[(-2)^5]", SimplifyOddPowerOfNegative, "-[2^5]")
        testRule("[(-x)^7]", SimplifyOddPowerOfNegative, "-[x^7]")
        testRule("[(-[1 / 2]) ^ 3]", SimplifyOddPowerOfNegative, "-[([1 / 2]) ^ 3]")
    }
}
