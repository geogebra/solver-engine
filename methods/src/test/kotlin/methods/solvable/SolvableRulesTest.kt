package methods.solvable

import engine.methods.testMethod
import engine.methods.testRuleInX
import methods.solvable.SolvableRules.MultiplySolvableByLCD
import org.junit.jupiter.api.Test

class SolvableRulesTest {

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
}
