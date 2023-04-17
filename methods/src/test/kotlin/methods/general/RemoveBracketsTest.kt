package methods.general

import engine.methods.testRule
import methods.general.NormalizationRules.NormalizeNegativeSignOfIntegerInSum
import methods.general.NormalizationRules.RemoveBracketSumInSum
import methods.general.NormalizationRules.RemoveRedundantBracket
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
        testRule("1 + (-1)", NormalizeNegativeSignOfIntegerInSum, "1 - 1")
        testRule("{.-4.} - 3", NormalizeNegativeSignOfIntegerInSum, "-4 - 3")
        testRule("x + (-2)", NormalizeNegativeSignOfIntegerInSum, "x - 2")
        testRule("{.((-5)).} + u", NormalizeNegativeSignOfIntegerInSum, "-5 + u")
    }

    @Test
    fun testRemoveOuterBrackets() {
        testRule("(1)", RemoveRedundantBracket, "1")
        testRule("(x + y)", RemoveRedundantBracket, "x + y")
    }
}
