package methods.equations

import engine.methods.testRuleInX
import org.junit.jupiter.api.Test

class EquationRulesTest {

    @Test
    fun testMultiplyByInverseCoefficientOfSquaredMonomial() {
        testRuleInX("[x^2] + x = 3", EquationsRules.MultiplyByInverseOfLeadingCoefficient, null)
        testRuleInX(
            "[1/2][x^2] + 3x = 5",
            EquationsRules.MultiplyByInverseOfLeadingCoefficient,
            "([1/2][x^2] + 3x)*2 = 5*2",
        )
        testRuleInX(
            "2x - [2/3][x^2] = 10",
            EquationsRules.MultiplyByInverseOfLeadingCoefficient,
            "(2x - [2/3][x^2])(-[3/2]) = 10(-[3/2])",
        )
        testRuleInX(
            "2[x^2] - 3[x^3] = 2",
            EquationsRules.MultiplyByInverseOfLeadingCoefficient,
            "(2[x^2] - 3[x^3])(-[1/3]) = 2(-[1/3])",
        )
    }

    @Test
    fun testCompleteTheSquare() {
        testRuleInX(
            "[x^2] + 6x = 1",
            EquationsRules.CompleteTheSquare,
            "[x^2] + 6x + [([6/2])^2] = 1 + [([6/2])^2]",
        )
        testRuleInX(
            "[x^2] + [1/2]x = 10",
            EquationsRules.CompleteTheSquare,
            "[x^2] + [1/2]x + [([[1/2]/2])^2] = 10 + [([[1/2]/2])^2]",
        )
    }
}
