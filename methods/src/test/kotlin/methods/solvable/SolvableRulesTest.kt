package methods.solvable

import engine.context.Context
import engine.methods.Method
import engine.methods.testMethod
import engine.methods.testRule
import engine.methods.testRuleInX
import methods.solvable.SolvableRules.MultiplyByInverseCoefficientOfVariable
import methods.solvable.SolvableRules.MultiplySolvableByLCD
import org.junit.jupiter.api.Test

class SolvableRulesTest {

    private fun testRuleInX(
        inputExpr: String,
        rule: Method,
        outputExpr: String,
        advandedBalancingOutputExpr: String = "",
    ) {
        engine.methods.testRuleInX(inputExpr, rule, outputExpr)
        if (advandedBalancingOutputExpr != "") {
            testRule(
                inputExpr,
                rule,
                advandedBalancingOutputExpr,
                context = Context(solutionVariables = listOf("x"), advancedBalancing = true),
            )
        }
    }

    @Test
    fun testCancelCommonTermsOnBothSides() {
        testMethod {
            method = SolvableRules.CancelCommonTermsOnBothSides
            inputExpr = "x + y = z + 1 + x"

            check {
                toExpr = "y = z + 1"

                keep("./1/0", "./1/1")
                shift("./0/1", "./0")
                cancel("./0/0", "./1/2")
            }
        }
    }

    @Test
    fun testMoveConstantsToTheLeft() {
        testRuleInX(
            "[x^2] + 4x + 5 = 0",
            SolvableRules.MoveConstantsToTheLeft,
            null,
        )
        testRuleInX(
            "10 = 3x + 5",
            SolvableRules.MoveConstantsToTheLeft,
            "10 - 5 = 3x + 5 - 5",
            "10 - 5 = 3x",
        )
    }

    @Test
    fun testMoveConstantsToTheRight() {
        testRuleInX(
            "0 = [x^2] + 4x + 5",
            SolvableRules.MoveConstantsToTheRight,
            null,
        )
        testRuleInX(
            "3x + 5 = 10",
            SolvableRules.MoveConstantsToTheRight,
            "3x + 5 - 5 = 10 - 5",
            "3x = 10 - 5",
        )
    }

    @Test
    fun testMoveVariablesToTheLeft() {
        testRuleInX(
            "3x = [x^2] - 7",
            SolvableRules.MoveVariablesToTheLeft,
            "3x - [x^2] =  [x^2] - 7 - [x^2]",
            "3x - [x^2] =  -7",
        )
        testRuleInX(
            "10x = 2",
            SolvableRules.MoveVariablesToTheLeft,
            null,
        )
        testRuleInX(
            "0 = -5[x^2] + 2x + 10",
            SolvableRules.MoveVariablesToTheLeft,
            "5[x^2] - 2x = -5[x^2] + 2x + 10 + 5[x^2] - 2x",
            "5[x^2] - 2x = 10",
        )
    }

    @Test
    fun testMoveVariablesToTheRight() {
        testRuleInX(
            "[x^3] + sqrt[2] = x",
            SolvableRules.MoveVariablesToTheRight,
            "[x^3] + sqrt[2] - [x^3] = x - [x^3]",
            "sqrt[2] = x - [x^3]",
        )
        testRuleInX(
            "10 = 2x",
            SolvableRules.MoveVariablesToTheRight,
            null,
        )
        testRuleInX(
            "x + [x^2] = 7",
            SolvableRules.MoveVariablesToTheRight,
            "x + [x^2] - x - [x^2] = 7 - x - [x^2]",
            "0 = 7 - x - [x^2]",
        )
    }

    @Test
    fun testMoveTermsNotContainingModulusToTheRight() {
        testRuleInX(
            "abs[x + 2] + 3 - abs[x] + [x ^ 2] = x",
            SolvableRules.MoveTermsNotContainingModulusToTheRight,
            "abs[x + 2] + 3 - abs[x] + [x ^ 2] - 3 - [x ^ 2] = x - 3 - [x ^ 2]",
            "abs[x + 2] - abs[x] = x - 3 - [x ^ 2]",
        )
        testRuleInX(
            "abs[x + 2] + 3 - abs[x] + [x ^ 2] = x - abs[x + 1]",
            SolvableRules.MoveTermsNotContainingModulusToTheRight,
            null,
        )
        testRuleInX(
            "x + 4 = abs[x]",
            SolvableRules.MoveTermsNotContainingModulusToTheRight,
            null,
        )
        testRuleInX(
            "abs[x + 2] + 3 > 2",
            SolvableRules.MoveTermsNotContainingModulusToTheRight,
            "abs[x + 2] + 3 - 3 > 2 - 3",
            "abs[x + 2] > 2 - 3",
        )
        testRuleInX(
            "abs[x + 2] + 3 >= 2",
            SolvableRules.MoveTermsNotContainingModulusToTheRight,
            "abs[x + 2] + 3 - 3 >= 2 - 3",
            "abs[x + 2] >= 2 - 3",
        )
        testRuleInX(
            "abs[x + 2] + 3 <= 2",
            SolvableRules.MoveTermsNotContainingModulusToTheRight,
            "abs[x + 2] + 3 - 3 <= 2 - 3",
            "abs[x + 2] <= 2 - 3",
        )
        testRuleInX(
            "abs[x + 2] + 3 < 2",
            SolvableRules.MoveTermsNotContainingModulusToTheRight,
            "abs[x + 2] + 3 - 3 < 2 - 3",
            "abs[x + 2] < 2 - 3",
        )
    }

    @Test
    fun testMoveTermsNotContainingModulusToTheLeft() {
        testRuleInX(
            "x = abs[x + 2] + 3 - abs[x] + [x ^ 2]",
            SolvableRules.MoveTermsNotContainingModulusToTheLeft,
            "x - 3 - [x ^ 2] = abs[x + 2] + 3 - abs[x] + [x ^ 2] - 3 - [x ^ 2]",
            "x - 3 - [x ^ 2] = abs[x + 2] - abs[x]",

        )
        testRuleInX(
            "x > abs[x + 2] + 3 - abs[x] + [x ^ 2]",
            SolvableRules.MoveTermsNotContainingModulusToTheLeft,
            "x - 3 - [x ^ 2] > abs[x + 2] + 3 - abs[x] + [x ^ 2] - 3 - [x ^ 2]",
            "x - 3 - [x ^ 2] > abs[x + 2] - abs[x]",

        )
        testRuleInX(
            "x - abs[x + 1] = abs[x + 2] + 3 - abs[x] + [x ^ 2]",
            SolvableRules.MoveTermsNotContainingModulusToTheLeft,
            null,
        )
        testRuleInX(
            "abs[x] = x + 4",
            SolvableRules.MoveTermsNotContainingModulusToTheLeft,
            null,
        )
        testRuleInX(
            "abs[x] > x + 4",
            SolvableRules.MoveTermsNotContainingModulusToTheLeft,
            null,
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
            "x = 1 * 5",
        )
        testRuleInX(
            "[3x / 2] = 1",
            MultiplyByInverseCoefficientOfVariable,
            "[3x / 2] * [2 / 3] = 1 * [2 / 3]",
            "x = 1 * [2 / 3]",

        )
        testRuleInX(
            "-[x / 5] = 1",
            MultiplyByInverseCoefficientOfVariable,
            "(-[x / 5]) * (-5) = 1 * (-5)",
            "x = 1 * (-5)",

        )
        testRuleInX(
            "-[3x / 2] = 1",
            MultiplyByInverseCoefficientOfVariable,
            "(-[3x / 2]) (-[2 / 3]) = 1 (-[2 / 3])",
            "x = 1 (-[2 / 3])",

        )

        testRuleInX(
            "3x > 1",
            MultiplyByInverseCoefficientOfVariable,
            null,
        )
        testRuleInX(
            "[x / 5] > 1",
            MultiplyByInverseCoefficientOfVariable,
            "[x / 5] * 5 > 1 * 5",
            "x > 1 * 5",

        )
        testRuleInX(
            "[3x / 2] > 1",
            MultiplyByInverseCoefficientOfVariable,
            "[3x / 2] * [2 / 3] > 1 * [2 / 3]",
            "x > 1 * [2 / 3]",

        )
        testRuleInX(
            "-[x / 5] > 1",
            MultiplyByInverseCoefficientOfVariable,
            "(-[x / 5]) * (-5) < 1 * (-5)",
            "x < 1 * (-5)",

        )
        testRuleInX(
            "-[3x / 2] > 1",
            MultiplyByInverseCoefficientOfVariable,
            "(-[3x / 2]) (-[2 / 3]) < 1 (-[2 / 3])",
            "x < 1 (-[2 / 3])",

        )
    }

    @Test
    fun testMultiplySolvableByLCD() {
        testRuleInX(
            "10 ([x / 2] + [x / 3]) = 3",
            MultiplySolvableByLCD,
            null,
        )
        testRuleInX(
            "[x / 2] + [x / 3] = 3",
            MultiplySolvableByLCD,
            "([x / 2] + [x / 3]) * 6 = 3 * 6",
        )
        testRuleInX(
            "[x / 2] = [x / 3]",
            MultiplySolvableByLCD,
            "[x / 2] * 6 = [x / 3] * 6",
        )
        testRuleInX(
            "[x / 2] + [x / 5] = [x / 3]",
            MultiplySolvableByLCD,
            "([x / 2] + [x / 5]) * 30 = [x / 3] * 30",
        )
        testRuleInX(
            "[[x^2] / 2] + [x / 5] = [x / 3]",
            MultiplySolvableByLCD,
            "([[x^2] / 2] + [x / 5])*30 = [x / 3] * 30",
        )
        testRuleInX(
            "[[x^2] / 6] + [x / 3] + [1 / 8] = 0",
            MultiplySolvableByLCD,
            "([[x ^ 2] / 6] + [x / 3] + [1 / 8]) * 24 = 0 * 24",
        )
        testRuleInX(
            "[x / 2] - [1 / 3] = 2",
            MultiplySolvableByLCD,
            "([x / 2] - [1 / 3]) * 6 = 2 * 6",
        )
        testRuleInX(
            "[x^2] - [x/6] - [1/3] = 0",
            MultiplySolvableByLCD,
            "([x^2] - [x/6] - [1/3]) * 6 = 0 * 6",
        )
    }

    @Test
    fun testTakeRootOfBothSides() {
        testRuleInX("[x ^ 2] = 4", SolvableRules.TakeRootOfBothSides, "x = +/-sqrt[4]")
        testRuleInX("[x ^ 3] = 2", SolvableRules.TakeRootOfBothSides, "x = root[2, 3]")
        testRuleInX("[x ^ 5] = -8", SolvableRules.TakeRootOfBothSides, "x = root[-8, 5]")
        testRuleInX("[x ^ 4] = 0", SolvableRules.TakeRootOfBothSides, "x = 0")
        testRuleInX("[x ^ 2] = -1", SolvableRules.TakeRootOfBothSides, null)
    }
}
