package methods.integerroots

import methods.rules.testRule
import org.junit.jupiter.api.Test

class IntegerRootsRulesTest {

    @Test
    fun testSimplifyRootOfOne() {
        testRule("sqrt[1]", simplifyRootOfOne, "1")
        testRule("root[1, 7]", simplifyRootOfOne, "1")
        testRule("root[2, 3]", simplifyRootOfOne, null)
    }

    @Test
    fun testSimplifyRootOfZero() {
        testRule("sqrt[0]", simplifyRootOfZero, "0")
        testRule("root[0, 7]", simplifyRootOfZero, "0")
        testRule("root[2, 3]", simplifyRootOfZero, null)
    }

    @Test
    fun testFactorizeIntegerUnderSquareRoot() {
        testRule("sqrt[1]", factorizeIntegerUnderRoot, null)
        testRule("sqrt[144]", factorizeIntegerUnderRoot, "sqrt[[2 ^ 4] * [3 ^ 2]]")
        testRule("sqrt[125]", factorizeIntegerUnderRoot, "sqrt[[5 ^ 3]]")
        testRule("sqrt[147]", factorizeIntegerUnderRoot, "sqrt[3 * [7 ^ 2]]")
        testRule("sqrt[32]", factorizeIntegerUnderRoot, "sqrt[[2 ^ 5]]")
        testRule("root[4, 3]", factorizeIntegerUnderRoot, null)
        testRule("root[24, 3]", factorizeIntegerUnderRoot, "root[[2 ^ 3] * 3, 3]")
    }

    @Test
    fun testSplitRootOfProduct() {
        testRule(
            "sqrt[[2^3] * 5]",
            splitRootOfProduct,
            "sqrt[[2^3]] * sqrt[5]"
        )
        testRule(
            "sqrt[[2^3] * 5 * [7^2]]",
            splitRootOfProduct,
            "sqrt[[2^3]] * sqrt[5] * sqrt[[7^2]]"
        )
        testRule(
            "sqrt[49 * 100]",
            splitRootOfProduct,
            "sqrt[49] * sqrt[100]"
        )
    }

    @Test
    fun testSplitPowerUnderRoot() {
        testRule("sqrt[[2^4]]", splitPowerUnderRoot, null)
        testRule("sqrt[[3^5]]", splitPowerUnderRoot, "sqrt[[3 ^ 4] * 3]")
        testRule("sqrt[3]", splitPowerUnderRoot, null)
        testRule("root[[2^3], 4]", splitPowerUnderRoot, null)
        testRule("root[[3^5], 3]", splitPowerUnderRoot, "root[[3 ^ 3] * [3 ^ 2], 3]")
    }

    @Test
    fun testMultiplyNthRoots() {
        testRule("sqrt[6]*sqrt[6]", multiplyNthRoots, "sqrt[6 * 6]")
        testRule("sqrt[6]*root[6, 3] * root[6, 3]", multiplyNthRoots, "sqrt[6] * root[6 * 6, 3]")
    }

    @Test
    fun testSimplifyMultiplicationOfSquareRoots() {
        testRule("sqrt[6]*sqrt[6]", simplifyMultiplicationOfSquareRoots, "6")
        testRule(
            "sqrt[3] * sqrt[3]",
            simplifyMultiplicationOfSquareRoots,
            "3"
        )
    }

    @Test
    fun testCombineSamePowerUnderHigherRoot() {
        testRule(
            "root[[2^4] * [3^4], 5]",
            combineProductOfSamePowerUnderHigherRoot,
            null
        )
        testRule(
            "root[[2^4] * [3^5], 4]",
            combineProductOfSamePowerUnderHigherRoot,
            null
        )
        testRule(
            "root[[2^4] * [3^4], 4]",
            combineProductOfSamePowerUnderHigherRoot,
            "root[[(2 * 3) ^ 4], 4]"
        )
        testRule(
            "root[[2^4] * [3^4] * [5^4], 4]",
            combineProductOfSamePowerUnderHigherRoot,
            "root[[(2 * 3 * 5) ^ 4], 4]"
        )
        testRule(
            "2 * root[[2^4] * [3^4] * [5^4], 4]",
            combineProductOfSamePowerUnderHigherRoot,
            "2 * root[[(2 * 3 * 5) ^ 4], 4]"
        )
    }

    @Test
    fun testSimplifyNthRootOfNthPower() {
        testRule(
            "root[ [2^3], 3]",
            simplifyNthRootOfNthPower,
            "2"
        )
    }

    @Test
    fun testCollectPowersOfExponentsWithSameBase() {
        testRule(
            "4 * [3^3] * [3^2]",
            collectPowersOfExponentsWithSameBase,
            "4 * [3^3 + 2]"
        )

        testRule(
            "[3^3] * [5^2]",
            collectPowersOfExponentsWithSameBase,
            null
        )

        testRule(
            "[3^2] * 3",
            collectPowersOfExponentsWithSameBase,
            "[3^2 + 1]"
        )

        testRule(
            "3 * 3",
            collectPowersOfExponentsWithSameBase,
            "[3^1 + 1]"
        )
    }

    @Test
    fun testCollectLikeRoots() {
        testRule("sqrt[2] + 2*sqrt[2] + 2", collectLikeRoots, "(1 + 2)*sqrt[2] + 2")
        testRule("sqrt[3] + sqrt[3]", collectLikeRoots, "(1 + 1)*sqrt[3]")
        testRule(
            "sqrt[3] + sqrt[7] + sqrt[3] + sqrt[5]",
            collectLikeRoots,
            "(1 + 1) * sqrt[3] + sqrt[7] + sqrt[5]"
        )
        testRule(
            "sqrt[7] + sqrt[3] + sqrt[3] + sqrt[5]",
            collectLikeRoots,
            "sqrt[7] + (1 + 1) * sqrt[3] + sqrt[5]"
        )
        testRule(
            "sqrt[7] + root[5, 3] + sqrt[5] + root[5, 3]",
            collectLikeRoots,
            "sqrt[7] + (1 + 1) * root[5, 3] + sqrt[5]"
        )
        testRule(
            "[2 * sqrt[2] / 3] + [1 / 5] * sqrt[2] - 4 * sqrt[2]",
            collectLikeRoots,
            "([2 / 3] + [1 / 5] - 4) * sqrt[2]"
        )
    }
}
