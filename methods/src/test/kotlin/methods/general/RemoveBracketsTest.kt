package methods.general

import methods.rules.testRule
import org.junit.jupiter.api.Test

object RemoveBracketsTest {

    @Test
    fun testRemoveBracketsSumInSum() {
        testRule("1 + 2", removeBracketSumInSum, null)
        testRule("1 + (2 + 3) + 4", removeBracketSumInSum, "1 + 2 + 3 + 4")
        testRule("(x - y) + (z + t) - 3", removeBracketSumInSum, "x - y + z + t - 3")
    }

    @Test
    fun testRemoveBracketAroundSignedIntegerInSum() {
        testRule("1 + (-1)", removeBracketAroundSignedIntegerInSum, "1 - 1")
        testRule("{.-4.} - 3", removeBracketAroundSignedIntegerInSum, "-4 - 3")
        testRule("x + (-2)", removeBracketAroundSignedIntegerInSum, "x - 2")
        testRule("{.((-5)).} + u", removeBracketAroundSignedIntegerInSum, "-5 + u")
    }

    @Test
    fun testRemoveOuterBrackets() {
        testRule("(1)", removeOuterBracket, "1")
        testRule("(x + y)", removeOuterBracket, "x + y")
    }
}
