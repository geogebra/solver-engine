package methods.equations

import engine.methods.testRuleInX
import methods.equations.EquationsRules.MultiplyByInverseCoefficientOfVariable
import methods.equations.EquationsRules.MultiplyByInverseOfLeadingCoefficient
import methods.equations.EquationsRules.TakeRootOfBothSides
import org.junit.jupiter.api.Test

class EquationRulesTest {

    @Test
    fun testMultiplyByInverseCoefficientOfSquaredMonomial() {
        testRuleInX(
            "[x^2] + x = 3",
            MultiplyByInverseOfLeadingCoefficient,
            null,
        )
        testRuleInX(
            "[1/2][x^2] + 3x = 5",
            MultiplyByInverseOfLeadingCoefficient,
            "([1/2][x^2] + 3x)*2 = 5*2",
        )
        testRuleInX(
            "2x - [2/3][x^2] = 10",
            MultiplyByInverseOfLeadingCoefficient,
            "(2x - [2/3][x^2])(-[3/2]) = 10(-[3/2])",
        )
        testRuleInX(
            "2[x^2] - 3[x^3] = 2",
            MultiplyByInverseOfLeadingCoefficient,
            "(2[x^2] - 3[x^3])(-[1/3]) = 2(-[1/3])",
        )
    }

    @Test
    fun testMultiplyByInverseCoefficientOfVariable() {
        testRuleInX(
            "3x = 1",
            MultiplyByInverseCoefficientOfVariable,
            null,
        )
        testRuleInX(
            "[x / 5] = 1",
            MultiplyByInverseCoefficientOfVariable,
            "[x / 5] * 5 = 1 * 5",
        )
        testRuleInX(
            "[3x / 2] = 1",
            MultiplyByInverseCoefficientOfVariable,
            "[3x / 2] * [2 / 3] = 1 * [2 / 3]",
        )
        testRuleInX(
            "-[x / 5] = 1",
            MultiplyByInverseCoefficientOfVariable,
            "(-[x / 5]) * (-5) = 1 * (-5)",
        )
        testRuleInX(
            "-[3x / 2] = 1",
            MultiplyByInverseCoefficientOfVariable,
            "(-[3x / 2]) (-[2 / 3]) = 1 (-[2 / 3])",
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

    @Test
    fun testTakeRootOfBothSides() {
        testRuleInX("[x ^ 2] = 4", TakeRootOfBothSides, "x = +/-sqrt[4]")
        testRuleInX("[x ^ 3] = 2", TakeRootOfBothSides, "x = root[2, 3]")
        testRuleInX("[x ^ 5] = -8", TakeRootOfBothSides, "x = root[-8, 5]")
        testRuleInX("[x ^ 4] = 0", TakeRootOfBothSides, "x = 0")
        testRuleInX("[x ^ 2] = -1", TakeRootOfBothSides, null)
    }
}
