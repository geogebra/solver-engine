package methods.integerarithmetic

import methods.plans.testMethod
import methods.rules.RuleTest
import methods.rules.RuleTestCase
import org.junit.jupiter.api.Test
import java.util.stream.Stream

object IntegerArithmeticRulesTest : RuleTest {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(

        RuleTestCase("5 - 4", evaluateSignedIntegerAddition, "1"),
        RuleTestCase("4 - 5", evaluateSignedIntegerAddition, "-1"),
        RuleTestCase("1 + x + 2", evaluateSignedIntegerAddition, "3 + x"),
        RuleTestCase("1 + x + (-2)", evaluateSignedIntegerAddition, "-1 + x"),
        RuleTestCase("(-2) + 3", evaluateSignedIntegerAddition, "1"),
        RuleTestCase("(-2) + (-3) + x", evaluateSignedIntegerAddition, "-5 + x"),

        RuleTestCase("z*2*x*3*y", evaluateIntegerProductAndDivision, "z*6*x*y"),
        RuleTestCase("2*3", evaluateIntegerProductAndDivision, "6"),

        RuleTestCase("1 * (-2)", evaluateIntegerProductAndDivision, "-2"),
        RuleTestCase("(-2) * x * 5", evaluateIntegerProductAndDivision, "(-10) * x"),
        RuleTestCase("10:2", evaluateIntegerProductAndDivision, "5"),
        RuleTestCase("10:(-2)", evaluateIntegerProductAndDivision, "-5"),

        RuleTestCase("[5^3]", evaluateIntegerPowerDirectly, "125"),
        RuleTestCase("[(-5) ^ 3]", evaluateIntegerPowerDirectly, "-125"),

        RuleTestCase("[4^2]", rewriteIntegerPowerAsProduct, "4 * 4"),

        RuleTestCase("[(-2)^4]", simplifyEvenPowerOfNegative, "[2^4]"),
        RuleTestCase("[(-x)^6]", simplifyEvenPowerOfNegative, "[x^6]"),
        RuleTestCase("[(-2)^5]", simplifyOddPowerOfNegative, "-[2^5]"),
        RuleTestCase("[(-x)^7]", simplifyOddPowerOfNegative, "-[x^7]"),
        RuleTestCase("[(-[1 / 2]) ^ 3]", simplifyOddPowerOfNegative, "-[([1 / 2]) ^ 3]"),
    )
}

class SeparateIntegerArithmeticRulesTest {

    @Test
    fun testEvaluateSignedIntegerAddition() {
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
}
