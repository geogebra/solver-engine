package methods.integerarithmetic

import engine.methods.testMethod
import engine.methods.testRule
import methods.integerarithmetic.IntegerArithmeticRules.EvaluateIntegerPowerDirectly
import methods.integerarithmetic.IntegerArithmeticRules.EvaluateIntegerProductAndDivision
import methods.integerarithmetic.IntegerArithmeticRules.EvaluateSignedIntegerAddition
import org.junit.jupiter.api.Test
import engine.methods.SerializedGmAction as GmAction

class IntegerArithmeticRulesTest {

    @Test
    fun testEvaluateSignedIntegerAddition() {
        testRule("5 - 4", EvaluateSignedIntegerAddition, "1", GmAction("Drag", "./1:group", "./0:group"))
        testRule("4 - 5", EvaluateSignedIntegerAddition, "-1")
        testRule("1 + x + 2", EvaluateSignedIntegerAddition, "3 + x", GmAction("Drag", "./2:group", "./0:group"))
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
        testRule("z*2*x*3*y", EvaluateIntegerProductAndDivision, "z*6xy", GmAction("Drag", "./3:group", "./1:group"))
        testRule("2*3", EvaluateIntegerProductAndDivision, "6")
        testRule("1 * (-2)", EvaluateIntegerProductAndDivision, "-2", GmAction("Drag", "./1:group", "./0:group"))
        testRule("(-2) * x * 5", EvaluateIntegerProductAndDivision, "(-10) x")
        testRule("10:2", EvaluateIntegerProductAndDivision, "5", GmAction("NotSupported"))
        testRule("10:(-2)", EvaluateIntegerProductAndDivision, "-5")
        testRule("-2*0", EvaluateIntegerProductAndDivision, "0")
        testRule("-2*3", EvaluateIntegerProductAndDivision, "-6")
        testRule("5:0", EvaluateIntegerProductAndDivision, null)

        testMethod {
            method = EvaluateIntegerProductAndDivision
            inputExpr = "6 : 6"

            check {
                toExpr = "1"

                combine {
                    fromPaths("./0", "./1/0", "./1/0:outerOp")
                    toPaths(".")
                }
            }
        }
    }

    @Test
    fun testEvaluateIntegerPowerDirectly() {
        testRule("[5^3]", EvaluateIntegerPowerDirectly, "125", GmAction("Tap", "./1"))
        testRule("[(-5) ^ 3]", EvaluateIntegerPowerDirectly, "-125")
    }
}
