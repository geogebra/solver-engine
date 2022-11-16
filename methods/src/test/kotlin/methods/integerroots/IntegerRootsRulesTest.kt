package methods.integerroots

import methods.integerroots.IntegerRootsRules.CollectLikeRoots
import methods.integerroots.IntegerRootsRules.CollectPowersOfExponentsWithSameBase
import methods.integerroots.IntegerRootsRules.CombineProductOfSamePowerUnderHigherRoot
import methods.integerroots.IntegerRootsRules.FactorizeIntegerPowerUnderRoot
import methods.integerroots.IntegerRootsRules.FactorizeIntegerUnderRoot
import methods.integerroots.IntegerRootsRules.MultiplyNthRoots
import methods.integerroots.IntegerRootsRules.SimplifyMultiplicationOfSquareRoots
import methods.integerroots.IntegerRootsRules.SimplifyNthRootOfNthPower
import methods.integerroots.IntegerRootsRules.SimplifyRootOfOne
import methods.integerroots.IntegerRootsRules.SimplifyRootOfZero
import methods.integerroots.IntegerRootsRules.SplitPowerUnderRoot
import methods.integerroots.IntegerRootsRules.SplitRootOfProduct
import methods.rules.testRule
import org.junit.jupiter.api.Test

class IntegerRootsRulesTest {

    @Test
    fun testSimplifyRootOfOne() {
        testRule("sqrt[1]", SimplifyRootOfOne, "1")
        testRule("root[1, 7]", SimplifyRootOfOne, "1")
        testRule("root[2, 3]", SimplifyRootOfOne, null)
    }

    @Test
    fun testSimplifyRootOfZero() {
        testRule("sqrt[0]", SimplifyRootOfZero, "0")
        testRule("root[0, 7]", SimplifyRootOfZero, "0")
        testRule("root[2, 3]", SimplifyRootOfZero, null)
    }

    @Test
    fun testFactorizeIntegerUnderRoot() {
        testRule("sqrt[1]", FactorizeIntegerUnderRoot, null)
        testRule("root[9, 4]", FactorizeIntegerUnderRoot, "root[[3^2], 4]")
        testRule("sqrt[144]", FactorizeIntegerUnderRoot, "sqrt[[2 ^ 4] * [3 ^ 2]]")
        testRule("sqrt[125]", FactorizeIntegerUnderRoot, "sqrt[[5 ^ 3]]")
        testRule("sqrt[147]", FactorizeIntegerUnderRoot, "sqrt[3 * [7 ^ 2]]")
        testRule("sqrt[32]", FactorizeIntegerUnderRoot, "sqrt[[2 ^ 5]]")
        testRule("root[4, 3]", FactorizeIntegerUnderRoot, null)
        testRule("root[24, 3]", FactorizeIntegerUnderRoot, "root[[2 ^ 3] * 3, 3]")
    }

    @Test
    fun testFactorizeIntegerPowerUnderRoot() {
        testRule("root[ [12^4], 3]", FactorizeIntegerPowerUnderRoot, "root[ [([2^2] * 3) ^ 4], 3]")
        testRule("root[ [12^2], 5]", FactorizeIntegerPowerUnderRoot, null)
        testRule("root[ [24^2], 3]", FactorizeIntegerPowerUnderRoot, "root[ [([2^3]*3) ^ 2], 3]")
    }

    @Test
    fun testSplitRootOfProduct() {
        testRule(
            "sqrt[[2^3] * 5]",
            SplitRootOfProduct,
            "sqrt[[2^3]] * sqrt[5]"
        )
        testRule(
            "sqrt[[2^3] * 5 * [7^2]]",
            SplitRootOfProduct,
            "sqrt[[2^3]] * sqrt[5] * sqrt[[7^2]]"
        )
        testRule(
            "sqrt[49 * 100]",
            SplitRootOfProduct,
            "sqrt[49] * sqrt[100]"
        )
    }

    @Test
    fun testSplitPowerUnderRoot() {
        testRule("sqrt[[2^4]]", SplitPowerUnderRoot, null)
        testRule("sqrt[[3^5]]", SplitPowerUnderRoot, "sqrt[[3 ^ 4] * 3]")
        testRule("sqrt[3]", SplitPowerUnderRoot, null)
        testRule("root[[2^3], 4]", SplitPowerUnderRoot, null)
        testRule("root[[3^5], 3]", SplitPowerUnderRoot, "root[[3 ^ 3] * [3 ^ 2], 3]")
        testRule("root[[12^5], 4]", SplitPowerUnderRoot, "root[[12^4] * 12, 4]")
    }

    @Test
    fun testMultiplyNthRoots() {
        testRule("sqrt[6]*sqrt[6]", MultiplyNthRoots, "sqrt[6 * 6]")
        testRule("sqrt[6]*root[6, 3] * root[6, 3]", MultiplyNthRoots, "sqrt[6] * root[6 * 6, 3]")
    }

    @Test
    fun testSimplifyMultiplicationOfSquareRoots() {
        testRule("sqrt[6]*sqrt[6]", SimplifyMultiplicationOfSquareRoots, "6")
        testRule(
            "sqrt[3] * sqrt[3]",
            SimplifyMultiplicationOfSquareRoots,
            "3"
        )
    }

    @Test
    fun testCombineSamePowerUnderHigherRoot() {
        testRule(
            "root[[2^4] * [3^4], 5]",
            CombineProductOfSamePowerUnderHigherRoot,
            null
        )
        testRule(
            "root[[2^4] * [3^5], 4]",
            CombineProductOfSamePowerUnderHigherRoot,
            null
        )
        testRule(
            "root[[2^4] * [3^4], 4]",
            CombineProductOfSamePowerUnderHigherRoot,
            "root[[(2 * 3) ^ 4], 4]"
        )
        testRule(
            "root[[2^4] * [3^4] * [5^4], 4]",
            CombineProductOfSamePowerUnderHigherRoot,
            "root[[(2 * 3 * 5) ^ 4], 4]"
        )
        testRule(
            "2 root[[2^4] * [3^4] * [5^4], 4]",
            CombineProductOfSamePowerUnderHigherRoot,
            "2 root[[(2 * 3 * 5) ^ 4], 4]"
        )
    }

    @Test
    fun testSimplifyNthRootOfNthPower() {
        testRule(
            "root[ [2^3], 3]",
            SimplifyNthRootOfNthPower,
            "2"
        )
    }

    @Test
    fun testCollectPowersOfExponentsWithSameBase() {
        testRule(
            "4 * [3^3] * [3^2]",
            CollectPowersOfExponentsWithSameBase,
            "4 * [3^3 + 2]"
        )

        testRule(
            "[3^3] * [5^2]",
            CollectPowersOfExponentsWithSameBase,
            null
        )

        testRule(
            "[3^2] * 3",
            CollectPowersOfExponentsWithSameBase,
            "[3^2 + 1]"
        )

        testRule(
            "3 * 3",
            CollectPowersOfExponentsWithSameBase,
            "[3^1 + 1]"
        )
    }

    @Test
    fun testCollectLikeRoots() {
        testRule("sqrt[2] + 2*sqrt[2] + 2", CollectLikeRoots, "(1 + 2)sqrt[2] + 2")
        testRule("sqrt[3] + sqrt[3]", CollectLikeRoots, "(1 + 1)sqrt[3]")
        testRule(
            "sqrt[3] + sqrt[7] + sqrt[3] + sqrt[5]",
            CollectLikeRoots,
            "(1 + 1)  sqrt[3] + sqrt[7] + sqrt[5]"
        )
        testRule(
            "sqrt[7] + sqrt[3] + sqrt[3] + sqrt[5]",
            CollectLikeRoots,
            "sqrt[7] + (1 + 1)  sqrt[3] + sqrt[5]"
        )
        testRule(
            "sqrt[7] + root[5, 3] + sqrt[5] + root[5, 3]",
            CollectLikeRoots,
            "sqrt[7] + (1 + 1)  root[5, 3] + sqrt[5]"
        )
        testRule(
            "[2 * sqrt[2] / 3] + [1 / 5] * sqrt[2] - 4 * sqrt[2]",
            CollectLikeRoots,
            "([2 / 3] + [1 / 5] - 4)  sqrt[2]"
        )
    }
}
