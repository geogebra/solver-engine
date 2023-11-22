package methods.equationsystems

import engine.methods.testRuleInXY
import org.junit.jupiter.api.Test

class EquationSystemsRulesTest {

    @Test
    fun testGuessIntegerSolutionsOfSystemContainingXYEqualsInteger() {
        testRuleInXY(
            "x + y = 10 AND xy = 21",
            EquationSystemsRules.GuessIntegerSolutionsOfSystemContainingXYEqualsInteger,
            "x = 3 AND y = 7",

        )
        testRuleInXY(
            "[x^2] - [y^2] = 75 AND xy = 50",
            EquationSystemsRules.GuessIntegerSolutionsOfSystemContainingXYEqualsInteger,
            "x = 10 AND y = 5",
        )
        testRuleInXY(
            "x + y = -10 AND xy = 21",
            EquationSystemsRules.GuessIntegerSolutionsOfSystemContainingXYEqualsInteger,
            "x = -3 AND y = -7",
        )
    }
}
