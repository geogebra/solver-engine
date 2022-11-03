package methods.general

import methods.general.NormalizationRules.RemoveBracketAroundSignedIntegerInSum
import methods.general.NormalizationRules.RemoveBracketSumInSum
import methods.general.NormalizationRules.RemoveOuterBracket
import methods.rules.testRule
import org.junit.jupiter.api.Test

object RemoveBracketsTest {

    @Test
    fun testRemoveBracketsSumInSum() {
        testRule("1 + 2", RemoveBracketSumInSum, null)
        testRule("1 + (2 + 3) + 4", RemoveBracketSumInSum, "1 + 2 + 3 + 4")
        testRule("(x - y) + (z + t) - 3", RemoveBracketSumInSum, "x - y + z + t - 3")
    }

    @Test
    fun testRemoveBracketAroundSignedIntegerInSum() {
        testRule("1 + (-1)", RemoveBracketAroundSignedIntegerInSum, "1 - 1")
        testRule("{.-4.} - 3", RemoveBracketAroundSignedIntegerInSum, "-4 - 3")
        testRule("x + (-2)", RemoveBracketAroundSignedIntegerInSum, "x - 2")
        testRule("{.((-5)).} + u", RemoveBracketAroundSignedIntegerInSum, "-5 + u")
    }

    @Test
    fun testRemoveOuterBrackets() {
        testRule("(1)", RemoveOuterBracket, "1")
        testRule("(x + y)", RemoveOuterBracket, "x + y")
    }
}
