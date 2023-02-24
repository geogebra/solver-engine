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
}
