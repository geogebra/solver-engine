package methods.equations

import engine.methods.testRuleInX
import methods.solvable.SolvableRules
import org.junit.jupiter.api.Test

class SolvableRulesTest {
    @Test
    fun testMoveConstantsToTheLeft() {
        testRuleInX(
            "[x^2] + 4x + 5 = 0",
            SolvableRules.MoveConstantsToTheLeft,
            null,
        )
    }

    @Test
    fun testMoveTermsNotContainingModulusToTheRight() {
        testRuleInX(
            "abs[x + 2] + 3 - abs[x] + [x ^ 2] = x",
            SolvableRules.MoveTermsNotContainingModulusToTheRight,
            "abs[x + 2] + 3 - abs[x] + [x ^ 2] - 3 - [x ^ 2] = x - 3 - [x ^ 2]",
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
        )
        testRuleInX(
            "abs[x + 2] + 3 >= 2",
            SolvableRules.MoveTermsNotContainingModulusToTheRight,
            "abs[x + 2] + 3 - 3 >= 2 - 3",
        )
        testRuleInX(
            "abs[x + 2] + 3 <= 2",
            SolvableRules.MoveTermsNotContainingModulusToTheRight,
            "abs[x + 2] + 3 - 3 <= 2 - 3",
        )
        testRuleInX(
            "abs[x + 2] + 3 < 2",
            SolvableRules.MoveTermsNotContainingModulusToTheRight,
            "abs[x + 2] + 3 - 3 < 2 - 3",
        )
    }

    @Test
    fun testMoveTermsNotContainingModulusToTheLeft() {
        testRuleInX(
            "x = abs[x + 2] + 3 - abs[x] + [x ^ 2]",
            SolvableRules.MoveTermsNotContainingModulusToTheLeft,
            " x - 3 - [x ^ 2] = abs[x + 2] + 3 - abs[x] + [x ^ 2] - 3 - [x ^ 2]",
        )
        testRuleInX(
            "x > abs[x + 2] + 3 - abs[x] + [x ^ 2]",
            SolvableRules.MoveTermsNotContainingModulusToTheLeft,
            " x - 3 - [x ^ 2] > abs[x + 2] + 3 - abs[x] + [x ^ 2] - 3 - [x ^ 2]",
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
}
