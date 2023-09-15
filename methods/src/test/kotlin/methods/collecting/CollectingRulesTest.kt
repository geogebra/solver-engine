package methods.collecting

import engine.context.Context
import engine.methods.testRule
import methods.collecting.CollectingRules.CollectLikeRoots
import methods.collecting.CollectingRules.CollectLikeTerms
import methods.collecting.CollectingRules.CombineTwoSimpleLikeTerms
import org.junit.jupiter.api.Test

class CollectingRulesTest {

    @Test
    fun testCollectLikeRoots() {
        testRule("sqrt[2] + 2*sqrt[2] + 2", CollectLikeRoots, "(1 + 2)sqrt[2] + 2")
        testRule("sqrt[3] + sqrt[3]", CollectLikeRoots, "(1 + 1)sqrt[3]")
        testRule(
            "sqrt[3] + sqrt[7] + sqrt[3] + sqrt[5]",
            CollectLikeRoots,
            "(1 + 1)  sqrt[3] + sqrt[7] + sqrt[5]",
        )
        testRule(
            "sqrt[7] + sqrt[3] + sqrt[3] + sqrt[5]",
            CollectLikeRoots,
            "sqrt[7] + (1 + 1)  sqrt[3] + sqrt[5]",
        )
        testRule(
            "sqrt[7] + root[5, 3] + sqrt[5] + root[5, 3]",
            CollectLikeRoots,
            "sqrt[7] + (1 + 1)  root[5, 3] + sqrt[5]",
        )
        testRule(
            "[2 * sqrt[2] / 3] + [1 / 5] * sqrt[2] - 4 * sqrt[2]",
            CollectLikeRoots,
            "([2 / 3] + [1 / 5] - 4)  sqrt[2]",
        )
    }

    @Test
    fun testCollectLikeTerms() {
        testRule("x + x", CollectLikeTerms, "(1+1) x")
        testRule("2*y - 3*y", CollectLikeTerms, "(2 - 3) y")
        testRule("z + [1/2]*z + [z / 2] - z*3", CollectLikeTerms, "(1 + [1/2] + [1/2] - 3) z")
        testRule("t*sqrt[3] + 2*t - [t*sqrt[2]/2]", CollectLikeTerms, "(sqrt[3] + 2 - [sqrt[2]/2]) t")
        // the factors should be simplified first
        testRule("3xy*y + 2xy*y", CollectLikeTerms, null)
    }

    @Test
    fun testCombineTwoSimpleLikeTerms() {
        fun t(input: String, output: String?) =
            testRule(input, CombineTwoSimpleLikeTerms, output, null, Context(gmFriendly = true))
        t("x + x", "2x")
        t("x - x", "0x") // cancel opposite terms will overrule this with a result of 0
        t("4x - 4x", "0x") // cancel opposite terms will overrule this with a result of 0
        t("1+2x-3+5x", "1+7x-3")
        t("2*y+y", "3y")
        t("1+2a-3a+1", "1-a+1")
        t("1-2a-3a+1", "1-5a+1")
        t("1-2a+3a+1", "1+a+1")
        t("z + [1/2]*z + [z / 2] - 3z", "-2z + [1/2]*z + [z/2]")
        // Someday come back to this example, it should work
        // t("z + [1/2]*z + [z / 2] - z*3", "-2z + [1/2]*z + [z/2]")
        t("z + [1/2]*z + [z / 2] - z*3", null)
        t("t*sqrt[3] + 2*t - [t*sqrt[2]/2]", null)
    }
}
