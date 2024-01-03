package methods.integerroots

import engine.methods.testMethod
import engine.methods.testRule
import methods.integerroots.IntegerRootsRules.CombineProductOfSamePowerUnderHigherRoot
import methods.integerroots.IntegerRootsRules.FactorGreatestCommonSquareIntegerFactor
import methods.integerroots.IntegerRootsRules.FactorizeIntegerPowerUnderRoot
import methods.integerroots.IntegerRootsRules.FactorizeIntegerUnderRoot
import methods.integerroots.IntegerRootsRules.MoveSquareFactorOutOfRoot
import methods.integerroots.IntegerRootsRules.MultiplyNthRoots
import methods.integerroots.IntegerRootsRules.SimplifyMultiplicationOfSquareRoots
import methods.integerroots.IntegerRootsRules.SimplifyNthRootOfNthPower
import methods.integerroots.IntegerRootsRules.SimplifyNthRootToThePowerOfN
import methods.integerroots.IntegerRootsRules.SimplifyRootOfOne
import methods.integerroots.IntegerRootsRules.SimplifyRootOfZero
import methods.integerroots.IntegerRootsRules.SplitPowerUnderRoot
import methods.integerroots.IntegerRootsRules.SplitRootOfProduct
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
            "sqrt[[2^3]] * sqrt[5]",
        )
        testRule(
            "sqrt[[2^3] * 5 * [7^2]]",
            SplitRootOfProduct,
            "sqrt[[2^3]] * sqrt[5] * sqrt[[7^2]]",
        )
        testRule(
            "sqrt[49 * 100]",
            SplitRootOfProduct,
            "sqrt[49] * sqrt[100]",
        )
    }

    @Test
    fun testSimplifyNthRootToThePowerOfN() {
        testMethod {
            method = SimplifyNthRootToThePowerOfN
            inputExpr = "[root[a, 5] ^ 5]"

            check {
                toExpr = "a"

                shift("./0/0", ".")
                cancel("./0/1", "./1")
            }
        }
    }

    @Test
    fun testSimplifyNthRootOfNthPower() {
        // exponent is an odd integer
        testRule("root[ [7^3], 3 ]", SimplifyNthRootOfNthPower, "7")
        testRule("root[ [(-7)^5], 5 ]", SimplifyNthRootOfNthPower, "-7")
        testRule("root[ [a^5], 5 ]", SimplifyNthRootOfNthPower, "a")

        // exponent is an even integer
        testRule("root[ [7^4], 4 ]", SimplifyNthRootOfNthPower, "7")
        testRule("root[ [(-7)^4], 4 ]", SimplifyNthRootOfNthPower, null)
        testRule("root[ [a^4], 4 ]", SimplifyNthRootOfNthPower, null)
        testRule("root[ [(2 + sqrt[2])^4], 4 ]", SimplifyNthRootOfNthPower, "2 + sqrt[2]")
        testRule("root[ [(-2 - sqrt[2])^4], 4 ]", SimplifyNthRootOfNthPower, null)

        testMethod {
            method = SimplifyNthRootOfNthPower
            inputExpr = "root[[x ^ 7], 7]"

            check {
                toExpr = "x"

                shift("./0/0", ".")
                cancel("./0/1", "./1")
            }
        }
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
            "3",
        )
    }

    @Test
    fun testCombineSamePowerUnderHigherRoot() {
        testRule(
            "root[[2^4] * [3^4], 5]",
            CombineProductOfSamePowerUnderHigherRoot,
            null,
        )
        testRule(
            "root[[2^4] * [3^5], 4]",
            CombineProductOfSamePowerUnderHigherRoot,
            null,
        )
        testRule(
            "root[[2^4] * [3^4], 4]",
            CombineProductOfSamePowerUnderHigherRoot,
            "root[[(2 * 3) ^ 4], 4]",
        )
        testRule(
            "root[[2^4] * [3^4] * [5^4], 4]",
            CombineProductOfSamePowerUnderHigherRoot,
            "root[[(2 * 3 * 5) ^ 4], 4]",
        )
        testRule(
            "2 root[[2^4] * [3^4] * [5^4], 4]",
            CombineProductOfSamePowerUnderHigherRoot,
            "2 root[[(2 * 3 * 5) ^ 4], 4]",
        )
    }

    @Test
    fun testFactorGreatestCommonSquareIntegerFactor() {
        testRule("4x + 8y", FactorGreatestCommonSquareIntegerFactor, "4(x + 2y)")
        testRule("16x + 12", FactorGreatestCommonSquareIntegerFactor, "4(4x + 3)")
        testRule("18sqrt[3] - 45", FactorGreatestCommonSquareIntegerFactor, "9(2sqrt[3] - 5)")
        testRule("50xy", FactorGreatestCommonSquareIntegerFactor, "25 * 2xy")
        testRule("1x + 1y", FactorGreatestCommonSquareIntegerFactor, null)
    }

    @Test
    fun testMoveSquareFactorOutOfSquareRoot() {
        testRule("sqrt[4x]", MoveSquareFactorOutOfRoot, "sqrt[4]sqrt[x]")
        testRule("sqrt[1x]", MoveSquareFactorOutOfRoot, null)
        testRule("sqrt[x*9y]", MoveSquareFactorOutOfRoot, "sqrt[9] * sqrt[xy]")
    }
}
