package methods.fractionroots

import methods.rules.RuleTest
import methods.rules.RuleTestCase
import java.util.stream.Stream

object FractionRootsRulesTest : RuleTest {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase(
            "sqrt[[2 / 3]]",
            distributeRadicalOverFraction,
            "[sqrt[2] / sqrt[3]]"
        ),
        RuleTestCase(
            "root[ [2 / 9], 4]",
            distributeRadicalOverFraction,
            "[ root[2, 4] / root[9, 4] ]"
        ),
        RuleTestCase(
            "[4 / sqrt[3]]",
            rationalizeSimpleDenominator,
            "[4 / sqrt[3]] * [sqrt[3] / sqrt[3]]"
        ),
        RuleTestCase(
            "[4 / 2 * sqrt[3]]",
            rationalizeSimpleDenominator,
            "[4 / 2 * sqrt[3]] * [sqrt[3] / sqrt[3]]"
        ),
        RuleTestCase(
            "[1 / 2 + 3]",
            rationalizeSumOfIntegerAndSquareRoot,
            null
        ),
        RuleTestCase(
            "[1 / 1 + sqrt[3]]",
            rationalizeSumOfIntegerAndSquareRoot,
            "[1 / 1 + sqrt[3]] * [1 - sqrt[3] / 1 - sqrt[3]]"
        ),
        RuleTestCase(
            "[1 / 1 + 2 * sqrt[3]]",
            rationalizeSumOfIntegerAndSquareRoot,
            "[1 / 1 + 2 * sqrt[3]] * [1 - 2 * sqrt[3] / 1 - 2 * sqrt[3]]"
        ),
        RuleTestCase(
            "[1 / 1 - sqrt[3]]",
            rationalizeSumOfIntegerAndSquareRoot,
            "[1 / 1 - sqrt[3]] * [1 + sqrt[3] / 1 + sqrt[3]]"
        ),
        RuleTestCase(
            "[1 / 1 - 2 * sqrt[3]]",
            rationalizeSumOfIntegerAndSquareRoot,
            "[1 / 1 - 2 * sqrt[3]] * [1 + 2 * sqrt[3] / 1 + 2 * sqrt[3]]"
        ),
        RuleTestCase(
            "[1 / 2 * sqrt[3] - 1]",
            rationalizeSumOfIntegerAndSquareRoot,
            "[1 / 2 * sqrt[3] - 1] * [2 * sqrt[3] + 1 / 2 * sqrt[3] + 1]"
        ),
        RuleTestCase(
            "[1 / 2 * sqrt[3] - sqrt[5]]",
            rationalizeSumOfIntegerAndSquareRoot,
            "[1 / 2 * sqrt[3] - sqrt[5]] * [2 * sqrt[3] + sqrt[5] / 2 * sqrt[3] + sqrt[5]]"
        ),
        RuleTestCase(
            "[1 / sqrt[3] + 4 * sqrt[5]]",
            rationalizeSumOfIntegerAndSquareRoot,
            "[1 / sqrt[3] + 4 * sqrt[5]] * [sqrt[3] - 4 * sqrt[5] / sqrt[3] - 4 * sqrt[5]]"
        ),
        RuleTestCase(
            "[2 / 2 * root[3, 3] + 3 * root[4, 3]]",
            rationalizeSumOfIntegerAndCubeRoot,
            "[2 / 2 * root[3, 3] + 3 * root[4, 3]] * " +
                "[[(2 * root[3, 3]) ^ 2] - (2 * root[3, 3]) * (3 * root[4, 3]) + " +
                "[(3 * root[4, 3]) ^ 2] / [(2 * root[3, 3]) ^ 2] - " +
                "(2 * root[3, 3]) * (3 * root[4, 3]) + [(3 * root[4, 3]) ^ 2]]"
        ),
        RuleTestCase(
            "[2 / 2 * root[3, 3] - 3 * root[4, 3]]",
            rationalizeSumOfIntegerAndCubeRoot,
            "[2 / 2 * root[3, 3] - 3 * root[4, 3]] * " +
                "[[(2 * root[3, 3]) ^ 2] + (2 * root[3, 3]) * (3 * root[4, 3]) + " +
                "[(3 * root[4, 3]) ^ 2] / [(2 * root[3, 3]) ^ 2] + " +
                "(2 * root[3, 3]) * (3 * root[4, 3]) + [(3 * root[4, 3]) ^ 2]]"
        ),
        RuleTestCase(
            "[2 / 2 - root[3, 3]]",
            rationalizeSumOfIntegerAndCubeRoot,
            "[2 / 2 - root[3, 3]] * " +
                "[ [2^2] + (2) * (root[3, 3]) + [(root[3, 3])^2] / [2^2] + (2) * (root[3, 3]) + [(root[3, 3])^2]]"
        ),
        RuleTestCase(
            "[2 / 3 + 2]",
            rationalizeSumOfIntegerAndCubeRoot,
            null
        ),
        RuleTestCase(
            "[2 / 3 - 2]",
            rationalizeSumOfIntegerAndCubeRoot,
            null
        ),
        RuleTestCase(
            "(root[5, 3] + root[3, 3]) * ([root[5, 3] ^ 2] - root[5, 3] * root[3, 3] + [root[3, 3]^2])",
            identityCubeSumDifference,
            "[(root[5, 3]) ^ 3] + [(root[3, 3]) ^ 3]"
        ),
        RuleTestCase(
            "(root[5, 3] - root[3, 3]) * ([root[5, 3] ^ 2] + root[5, 3] * root[3, 3] + [root[3, 3]^2])",
            identityCubeSumDifference,
            "[(root[5, 3]) ^ 3] - [(root[3, 3]) ^ 3]"
        ),
        RuleTestCase(
            "(2 * root[3, 3] + 3 * root[4, 3]) * ([(2 * root[3, 3]) ^ 2] - (2 * root[3, 3]) * " +
                "(3 * root[4, 3]) + [(3 * root[4, 3]) ^ 2])",
            identityCubeSumDifference,
            "[(2 * root[3, 3]) ^ 3] + [(3 * root[4, 3]) ^ 3]"
        ),
        RuleTestCase(
            "(2 * root[3, 3] - 3 * root[4, 3]) * ([(2 * root[3, 3]) ^ 2] + (2 * root[3, 3]) * (3 * root[4, 3]) " +
                "+ [(3 * root[4, 3]) ^ 2])",
            identityCubeSumDifference,
            "[(2 * root[3, 3]) ^ 3] - [(3 * root[4, 3]) ^ 3]"
        ),
        RuleTestCase(
            "[2 / -sqrt[2] + 3 * sqrt[5]]",
            flipRootsInDenominator,
            "[2 / 3 * sqrt[5] - sqrt[2]]"
        ),
        RuleTestCase(
            "[2 / -root[3, 3] + root[5, 3]]",
            flipRootsInDenominator,
            "[2 / root[5, 3] - root[3, 3]]"
        ),
        RuleTestCase(
            "[2 / root[3, 3] + root[5, 3]]",
            flipRootsInDenominator,
            null
        ),
        RuleTestCase(
            "[2 / -3 * root[3, 3] + sqrt[3]]",
            flipRootsInDenominator,
            "[2 / sqrt[3] - 3 * root[3, 3]]"
        ),
        RuleTestCase(
            "[ 9 / root[[2^2] * [3^2], 4] ]",
            higherOrderRationalizingTerm,
            "[9 / root[[2^2] * [3^2], 4]] * [root[[2^4 - 2] * [3^ 4 - 2], 4] / root[[2^4 - 2] * [3^ 4 - 2], 4]]"
        ),
        RuleTestCase(
            "[9 / root[2, 4]]",
            higherOrderRationalizingTerm,
            "[9 / root[2, 4]] * [ root[ [2^4-1], 4] / root[ [2^4-1], 4] ]"
        ),
        RuleTestCase(
            "[9 / root[[2^3], 4]]",
            higherOrderRationalizingTerm,
            "[9 / root[[2^3], 4]] * [ root[ [2^4-3], 4] / root[ [2^4-3], 4] ]"
        ),
        RuleTestCase(
            "[ 9 / root[[2^2] * [3^2] * 5, 4] ]",
            higherOrderRationalizingTerm,
            "[9 / root[[2 ^ 2] * [3 ^ 2] * 5, 4]] * " +
                "[root[[2 ^ 4 - 2] * [3 ^ 4 - 2] * [5 ^ 4 - 1], 4] / root[[2 ^ 4 - 2] * [3 ^ 4 - 2] * [5 ^ 4 - 1], 4]]"
        ),
        RuleTestCase(
            "[9 / root[ 2 * [3^2], 4]]",
            higherOrderRationalizingTerm,
            "[9 / root[2 * [3^2], 4]] * [root[ [2^4 - 1] * [3^4 - 2], 4] / root[ [2^4 - 1] * [3^4 - 2], 4]]"
        ),
        RuleTestCase(
            "[9 / 2 * root[ 2 * [3^2], 4]]",
            higherOrderRationalizingTerm,
            "[9 / 2 * root[2 * [3^2], 4]] * [root[ [2^4 - 1] * [3^4 - 2], 4] / root[ [2^4 - 1] * [3^4 - 2], 4]]"
        ),
        RuleTestCase(
            "[9 / root[18, 4]]",
            factorizeHigherOrderRadicand,
            "[9 / root[2 * [3^2], 4]]"
        ),
        RuleTestCase(
            "[9 / root[18, 2]]",
            factorizeHigherOrderRadicand,
            null
        ),
        RuleTestCase(
            "[9 / 2 * root[18, 4]]",
            factorizeHigherOrderRadicand,
            "[9 / 2 * root[2 * [3^2], 4]]"
        ),
        RuleTestCase(
            "[9 / root[2, 3]]",
            factorizeHigherOrderRadicand,
            null
        )
    )
}
