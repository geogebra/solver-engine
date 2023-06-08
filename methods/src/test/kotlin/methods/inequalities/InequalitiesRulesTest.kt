package methods.inequalities

import engine.methods.testMethod
import engine.methods.testMethodInX
import engine.methods.testRuleInX
import methods.inequalities.InequalitiesRules.MultiplyByInverseCoefficientOfVariable
import org.junit.jupiter.api.Test

class InequalitiesRulesTest {

    @Test
    fun testMultiplyByInverseCoefficientOfVariable() {
        testRuleInX(
            "3x > 1",
            MultiplyByInverseCoefficientOfVariable,
            null,
        )
        testRuleInX(
            "[x / 5] > 1",
            MultiplyByInverseCoefficientOfVariable,
            "[x / 5] * 5 > 1 * 5",
        )
        testRuleInX(
            "[3x / 2] > 1",
            MultiplyByInverseCoefficientOfVariable,
            "[3x / 2] * [2 / 3] > 1 * [2 / 3]",
        )
        testRuleInX(
            "-[x / 5] > 1",
            MultiplyByInverseCoefficientOfVariable,
            "(-[x / 5]) * (-5) < 1 * (-5)",
        )
        testRuleInX(
            "-[3x / 2] > 1",
            MultiplyByInverseCoefficientOfVariable,
            "(-[3x / 2]) (-[2 / 3]) < 1 (-[2 / 3])",
        )
    }

    @Test
    fun testExtractSolutionFromConstantInequalityBasedOnSign() {
        testMethodInX {
            inputExpr = "1 >= 0"
            method = InequalitiesRules.ExtractSolutionFromConstantInequalityBasedOnSign
            check {
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromTrueInequality
                    // Can't check the value of the parameter because the parser does not know how to make a
                    // variable list
                    param {}
                }
            }
        }
        testMethodInX {
            inputExpr = "1 < 0"
            method = InequalitiesRules.ExtractSolutionFromConstantInequalityBasedOnSign
            check {
                explanation {
                    key = InequalitiesExplanation.ExtractSolutionFromFalseInequality
                    // Can't check the value of the parameter because the parser does not know how to make a
                    // variable list
                    param {}
                }
            }
        }
        testMethod {
            inputExpr = "1 >= 0"
            method = InequalitiesRules.ExtractSolutionFromConstantInequalityBasedOnSign
            check {
                explanation {
                    key = InequalitiesExplanation.ExtractTruthFromTrueInequality
                }
            }
        }
        testMethod {
            inputExpr = "1 < 0"
            method = InequalitiesRules.ExtractSolutionFromConstantInequalityBasedOnSign
            check {
                explanation {
                    key = InequalitiesExplanation.ExtractFalsehoodFromFalseInequality
                }
            }
        }
    }
}
