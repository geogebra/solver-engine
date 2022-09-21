package methods.integerroots

import methods.rules.RuleTest
import methods.rules.RuleTestCase
import org.junit.jupiter.api.Test
import java.util.stream.Stream

object IntegerRootsRulesTest : RuleTest {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("sqrt[1]", simplifyRootOfOne, "1"),
        RuleTestCase("root[1, 7]", simplifyRootOfOne, "1"),
        RuleTestCase("root[2, 3]", simplifyRootOfOne, null),
        RuleTestCase("sqrt[0]", simplifyRootOfZero, "0"),
        RuleTestCase("root[0, 7]", simplifyRootOfZero, "0"),
        RuleTestCase("root[2, 3]", simplifyRootOfZero, null),
        RuleTestCase("sqrt[1]", factorizeIntegerUnderSquareRoot, null),
        RuleTestCase("sqrt[144]", factorizeIntegerUnderSquareRoot, "sqrt[[2 ^ 4] * [3 ^ 2]]"),
        RuleTestCase("sqrt[125]", factorizeIntegerUnderSquareRoot, "sqrt[[5 ^ 3]]"),
        RuleTestCase("sqrt[147]", factorizeIntegerUnderSquareRoot, "sqrt[3 * [7 ^ 2]]"),

        RuleTestCase(
            "sqrt[[2^3] * 5]",
            splitRootOfProduct,
            "sqrt[[2^3]] * sqrt[5]"
        ),
        RuleTestCase(
            "sqrt[[2^3] * 5 * [7^2]]",
            splitRootOfProduct,
            "sqrt[[2^3]] * sqrt[5] * sqrt[[7^2]]"
        )
    )
}

class SeparateIntegerRootsRulesTest {

    @Test
    fun testFactorizeIntegerUnderSquareRoot() {
        RuleTestCase(
            "sqrt[32]",
            factorizeIntegerUnderSquareRoot,
            "sqrt[[2 ^ 5]]"
        ).assert()
        RuleTestCase(
            "root[4, 3]",
            factorizeIntegerUnderSquareRoot,
            null
        ).assert()
        RuleTestCase(
            "root[24, 3]",
            factorizeIntegerUnderSquareRoot,
            "root[[2 ^ 3] * 3, 3]"
        ).assert()
    }

    @Test
    fun testSplitPowerUnderRoot() {
        RuleTestCase("sqrt[[2^4]]", splitPowerUnderRoot, null).assert()
        RuleTestCase("sqrt[[3^5]]", splitPowerUnderRoot, "sqrt[[3 ^ 4] * 3]").assert()
        RuleTestCase("sqrt[3]", splitPowerUnderRoot, null).assert()
        RuleTestCase("root[[2^3], 4]", splitPowerUnderRoot, null).assert()
        RuleTestCase("root[[3^5], 3]", splitPowerUnderRoot, "root[[3 ^ 3] * [3 ^ 2], 3]").assert()
    }

    @Test
    fun testMultiplyNthRoots() {
        RuleTestCase("sqrt[6]*sqrt[6]", multiplyNthRoots, "sqrt[6 * 6]").assert()
        RuleTestCase("sqrt[6]*root[6, 3] * root[6, 3]", multiplyNthRoots, "sqrt[6] * root[6 * 6, 3]").assert()
    }

    @Test
    fun testSimplifyMultiplicationOfSquareRoots() {
        RuleTestCase("sqrt[6]*sqrt[6]", simplifyMultiplicationOfSquareRoots, "6").assert()
        RuleTestCase(
            "sqrt[3] * sqrt[3]",
            simplifyMultiplicationOfSquareRoots,
            "3"
        ).assert()
    }

    @Test
    fun testCollectLikeRoots() {
        RuleTestCase("sqrt[2] + 2*sqrt[2] + 2", collectLikeRoots, "(1 + 2)*sqrt[2] + 2").assert()
        RuleTestCase("sqrt[3] + sqrt[3]", collectLikeRoots, "(1 + 1)*sqrt[3]").assert()
        RuleTestCase(
            "sqrt[3] + sqrt[7] + sqrt[3] + sqrt[5]",
            collectLikeRoots,
            "(1 + 1) * sqrt[3] + sqrt[7] + sqrt[5]"
        ).assert()
        RuleTestCase(
            "sqrt[7] + sqrt[3] + sqrt[3] + sqrt[5]",
            collectLikeRoots,
            "sqrt[7] + (1 + 1) * sqrt[3] + sqrt[5]"
        ).assert()
        RuleTestCase(
            "sqrt[7] + root[5, 3] + sqrt[5] + root[5, 3]",
            collectLikeRoots,
            "sqrt[7] + (1 + 1) * root[5, 3] + sqrt[5]"
        ).assert()
        RuleTestCase(
            "[2 * sqrt[2] / 3] + [1 / 5] * sqrt[2] - 4 * sqrt[2]",
            collectLikeRoots,
            "([2 / 3] + [1 / 5] - 4) * sqrt[2]"
        ).assert()
    }

    @Test
    fun testCombineSamePowerUnderHigherRoot() {
        RuleTestCase(
            "root[[2^4] * [3^4], 5]",
            combineProductOfSamePowerUnderHigherRoot,
            null
        ).assert()
        RuleTestCase(
            "root[[2^4] * [3^5], 4]",
            combineProductOfSamePowerUnderHigherRoot,
            null
        ).assert()
        RuleTestCase(
            "root[[2^4] * [3^4], 4]",
            combineProductOfSamePowerUnderHigherRoot,
            "root[[(2 * 3) ^ 4], 4]"
        ).assert()
        RuleTestCase(
            "root[[2^4] * [3^4] * [5^4], 4]",
            combineProductOfSamePowerUnderHigherRoot,
            "root[[(2 * 3 * 5) ^ 4], 4]"
        ).assert()
        RuleTestCase(
            "2 * root[[2^4] * [3^4] * [5^4], 4]",
            combineProductOfSamePowerUnderHigherRoot,
            "2 * root[[(2 * 3 * 5) ^ 4], 4]"
        ).assert()
    }

    @Test
    fun testSimplifyNthRootOfNthPower() {
        RuleTestCase(
            "root[ [2^3], 3]",
            simplifyNthRootOfNthPower,
            "2"
        ).assert()
    }

    @Test
    fun testFun() {
        RuleTestCase(
            "4 * [3^3] * [3^2]",
            collectPowersOfExponentsWithSameBase,
            "4 * [3^3 + 2]"
        ).assert()

        RuleTestCase(
            "[3^3] * [5^2]",
            collectPowersOfExponentsWithSameBase,
            null
        ).assert()

        RuleTestCase(
            "[3^2] * 3",
            collectPowersOfExponentsWithSameBase,
            "[3^2 + 1]"
        ).assert()

        RuleTestCase(
            "3 * 3",
            collectPowersOfExponentsWithSameBase,
            "[3^1 + 1]"
        ).assert()
    }
}