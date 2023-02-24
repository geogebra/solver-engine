package methods.equations

import engine.methods.testRuleInX
import methods.equations.EquationsRules.ApplyQuadraticFormula
import methods.equations.EquationsRules.EliminateConstantFactorOfLhsWithZeroRhs
import methods.equations.EquationsRules.MultiplyByInverseCoefficientOfVariable
import methods.equations.EquationsRules.MultiplyByInverseOfLeadingCoefficient
import methods.equations.EquationsRules.SeparatePlusMinusQuadraticSolutions
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

    @Test
    fun testCancelGcfOfCoefficients() {
        testRuleInX(
            "2 ([x^2] + x + 1) = 0",
            EliminateConstantFactorOfLhsWithZeroRhs,
            "[x^2] + x + 1 = 0",
        )
    }

    @Test
    fun testApplyQuadraticFormula() {
        testRuleInX(
            "[x^2] + 2x + 1 = 0",
            ApplyQuadraticFormula,
            "x = [-2 +/- sqrt[[2 ^ 2] - 4 * 1 * 1] / 2 * 1]",
        )
    }

    @Test
    fun testSeparatePlusMinusQuadraticSolutions() {
        testRuleInX(
            "x = [-2 +/- sqrt[[2 ^ 2] - 4 * 1 * 1] / 2 * 1]",
            SeparatePlusMinusQuadraticSolutions,
            "x = [-2 - sqrt[[2 ^ 2] - 4 * 1 * 1] / 2 * 1] OR " +
                "x = [-2 + sqrt[[2 ^ 2] - 4 * 1 * 1] / 2 * 1]]",
        )
    }

    @Test
    fun testFactorNegativeSign() {
        testRuleInX(
            "-[x^2] - 2x + 1 = 0",
            EquationsRules.FactorNegativeSignOfLeadingCoefficient,
            "(-1) ([x^2] + 2x - 1) = 0",
        )
        testRuleInX(
            "[x^2] + 2x - 1 = 0",
            EquationsRules.FactorNegativeSignOfLeadingCoefficient,
            null,
        )
        testRuleInX(
            "-[x^2] + 2x + 2 = 0",
            EquationsRules.FactorNegativeSignOfLeadingCoefficient,
            "(-1)([x^2] - 2x - 2) = 0",
        )
    }

    @Test
    fun testExtractSolutionFromEquationInUnionForm() {
        testRuleInX(
            "x = [-1 + sqrt[11] / 2] OR x = [-1 - sqrt[11] / 2]",
            EquationsRules.ExtractSolutionFromEquationInUnionForm,
            "Solution[x, {[-1 + sqrt[11] / 2], [-1 - sqrt[11] / 2]}]",
        )
    }
}
