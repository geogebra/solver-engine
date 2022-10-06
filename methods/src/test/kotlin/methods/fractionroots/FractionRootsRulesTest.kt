package methods.fractionroots

import methods.rules.testRule
import org.junit.jupiter.api.Test

class FractionRootsRulesTest {

    @Test
    fun testDistributeRadicalOverFraction() {
        testRule(
            "sqrt[[2 / 3]]",
            distributeRadicalOverFraction,
            "[sqrt[2] / sqrt[3]]"
        )
        testRule(
            "root[ [2 / 9], 4]",
            distributeRadicalOverFraction,
            "[ root[2, 4] / root[9, 4] ]"
        )
    }

    @Test
    fun testRationalizeSimpleDenominator() {
        testRule(
            "[4 / sqrt[3]]",
            rationalizeSimpleDenominator,
            "[4 / sqrt[3]] * [sqrt[3] / sqrt[3]]"
        )
        testRule(
            "[4 / 2 * sqrt[3]]",
            rationalizeSimpleDenominator,
            "[4 / 2 * sqrt[3]] * [sqrt[3] / sqrt[3]]"
        )
    }

    @Test
    fun testRationalizeSumOfIntegerAndSquareRoot() {
        testRule(
            "[1 / 2 + 3]",
            rationalizeSumOfIntegerAndSquareRoot,
            null
        )
        testRule(
            "[1 / 1 + sqrt[3]]",
            rationalizeSumOfIntegerAndSquareRoot,
            "[1 / 1 + sqrt[3]] * [1 - sqrt[3] / 1 - sqrt[3]]"
        )
        testRule(
            "[1 / 1 + 2 * sqrt[3]]",
            rationalizeSumOfIntegerAndSquareRoot,
            "[1 / 1 + 2 * sqrt[3]] * [1 - 2 * sqrt[3] / 1 - 2 * sqrt[3]]"
        )
        testRule(
            "[1 / 1 - sqrt[3]]",
            rationalizeSumOfIntegerAndSquareRoot,
            "[1 / 1 - sqrt[3]] * [1 + sqrt[3] / 1 + sqrt[3]]"
        )
        testRule(
            "[1 / 1 - 2 * sqrt[3]]",
            rationalizeSumOfIntegerAndSquareRoot,
            "[1 / 1 - 2 * sqrt[3]] * [1 + 2 * sqrt[3] / 1 + 2 * sqrt[3]]"
        )
        testRule(
            "[1 / 2 * sqrt[3] - 1]",
            rationalizeSumOfIntegerAndSquareRoot,
            "[1 / 2 * sqrt[3] - 1] * [2 * sqrt[3] + 1 / 2 * sqrt[3] + 1]"
        )
        testRule(
            "[1 / 2 * sqrt[3] - sqrt[5]]",
            rationalizeSumOfIntegerAndSquareRoot,
            "[1 / 2 * sqrt[3] - sqrt[5]] * [2 * sqrt[3] + sqrt[5] / 2 * sqrt[3] + sqrt[5]]"
        )
        testRule(
            "[1 / sqrt[3] + 4 * sqrt[5]]",
            rationalizeSumOfIntegerAndSquareRoot,
            "[1 / sqrt[3] + 4 * sqrt[5]] * [sqrt[3] - 4 * sqrt[5] / sqrt[3] - 4 * sqrt[5]]"
        )
    }

    @Test
    fun testRationalizeSumOfIntegerAndCubeRoot() {
        testRule(
            "[2 / 2 * root[3, 3] + 3 * root[4, 3]]",
            rationalizeSumOfIntegerAndCubeRoot,
            "[2 / 2 * root[3, 3] + 3 * root[4, 3]] * " +
                "[[(2 * root[3, 3]) ^ 2] - 2 * root[3, 3] * 3 * root[4, 3] + " +
                "[(3 * root[4, 3]) ^ 2] / [(2 * root[3, 3]) ^ 2] - " +
                "2 * root[3, 3] * 3 * root[4, 3] + [(3 * root[4, 3]) ^ 2]]"
        )
        testRule(
            "[2 / 2 * root[3, 3] - 3 * root[4, 3]]",
            rationalizeSumOfIntegerAndCubeRoot,
            "[2 / 2 * root[3, 3] - 3 * root[4, 3]] * " +
                "[[(2 * root[3, 3]) ^ 2] + 2 * root[3, 3] * 3 * root[4, 3] + " +
                "[(3 * root[4, 3]) ^ 2] / [(2 * root[3, 3]) ^ 2] + " +
                "2 * root[3, 3] * 3 * root[4, 3] + [(3 * root[4, 3]) ^ 2]]"
        )
        testRule(
            "[2 / 2 - root[3, 3]]",
            rationalizeSumOfIntegerAndCubeRoot,
            "[2 / 2 - root[3, 3]] * " +
                "[ [2^2] + 2 * root[3, 3] + [(root[3, 3])^2] / [2^2] + 2 * root[3, 3] + [(root[3, 3])^2]]"
        )
        testRule(
            "[2 / 3 + 2]",
            rationalizeSumOfIntegerAndCubeRoot,
            null
        )
        testRule(
            "[2 / 3 - 2]",
            rationalizeSumOfIntegerAndCubeRoot,
            null
        )
    }

    @Test
    fun testIdentifyCubeSumDifference() {
        testRule(
            "(root[5, 3] + root[3, 3]) * ([(root[5, 3]) ^ 2] - root[5, 3] * root[3, 3] + [(root[3, 3]) ^ 2])",
            identifyCubeSumDifference,
            "[(root[5, 3]) ^ 3] + [(root[3, 3]) ^ 3]"
        )
        testRule(
            "(root[5, 3] - root[3, 3]) * ([(root[5, 3]) ^ 2] + root[5, 3] * root[3, 3] + [(root[3, 3]) ^ 2])",
            identifyCubeSumDifference,
            "[(root[5, 3]) ^ 3] - [(root[3, 3]) ^ 3]"
        )
        testRule(
            "(2 * root[3, 3] + 3 * root[4, 3]) * ([(2 * root[3, 3]) ^ 2] - 2 * root[3, 3] * " +
                "3 * root[4, 3] + [(3 * root[4, 3]) ^ 2])",
            identifyCubeSumDifference,
            "[(2 * root[3, 3]) ^ 3] + [(3 * root[4, 3]) ^ 3]"
        )
        testRule(
            "(2 * root[3, 3] - 3 * root[4, 3]) * ([(2 * root[3, 3]) ^ 2] + 2 * root[3, 3] * 3 * root[4, 3] " +
                "+ [(3 * root[4, 3]) ^ 2])",
            identifyCubeSumDifference,
            "[(2 * root[3, 3]) ^ 3] - [(3 * root[4, 3]) ^ 3]"
        )
        testRule(
            "(2 - root[3, 3]) * ([2 ^ 2] + 2 * root[3, 3] + [(root[3, 3]) ^ 2])",
            identifyCubeSumDifference,
            "[2 ^ 3] - [(root[3, 3]) ^ 3]"
        )
    }

    @Test
    fun testFlipRootsInDenominator() {
        testRule(
            "[2 / -sqrt[2] + 3 * sqrt[5]]",
            flipRootsInDenominator,
            "[2 / 3 * sqrt[5] - sqrt[2]]"
        )
        testRule(
            "[2 / -root[3, 3] + root[5, 3]]",
            flipRootsInDenominator,
            "[2 / root[5, 3] - root[3, 3]]"
        )
        testRule(
            "[2 / root[3, 3] + root[5, 3]]",
            flipRootsInDenominator,
            null
        )
        testRule(
            "[2 / -3 * root[3, 3] + sqrt[3]]",
            flipRootsInDenominator,
            "[2 / sqrt[3] - 3 * root[3, 3]]"
        )
    }

    @Test
    fun testHigherOrderRationalizingTerm() {
        testRule(
            "[ 9 / root[[2^2] * [3^2], 4] ]",
            higherOrderRationalizingTerm,
            "[9 / root[[2^2] * [3^2], 4]] * [root[[2^4 - 2] * [3^ 4 - 2], 4] / root[[2^4 - 2] * [3^ 4 - 2], 4]]"
        )
        testRule(
            "[9 / root[2, 4]]",
            higherOrderRationalizingTerm,
            "[9 / root[2, 4]] * [ root[ [2^4-1], 4] / root[ [2^4-1], 4] ]"
        )
        testRule(
            "[9 / root[[2^3], 4]]",
            higherOrderRationalizingTerm,
            "[9 / root[[2^3], 4]] * [ root[ [2^4-3], 4] / root[ [2^4-3], 4] ]"
        )
        testRule(
            "[ 9 / root[[2^2] * [3^2] * 5, 4] ]",
            higherOrderRationalizingTerm,
            "[9 / root[[2 ^ 2] * [3 ^ 2] * 5, 4]] * " +
                "[root[[2 ^ 4 - 2] * [3 ^ 4 - 2] * [5 ^ 4 - 1], 4] / root[[2 ^ 4 - 2] * [3 ^ 4 - 2] * [5 ^ 4 - 1], 4]]"
        )
        testRule(
            "[9 / root[ 2 * [3^2], 4]]",
            higherOrderRationalizingTerm,
            "[9 / root[2 * [3^2], 4]] * [root[ [2^4 - 1] * [3^4 - 2], 4] / root[ [2^4 - 1] * [3^4 - 2], 4]]"
        )
        testRule(
            "[9 / 2 * root[ 2 * [3^2], 4]]",
            higherOrderRationalizingTerm,
            "[9 / 2 * root[2 * [3^2], 4]] * [root[ [2^4 - 1] * [3^4 - 2], 4] / root[ [2^4 - 1] * [3^4 - 2], 4]]"
        )
    }

    @Test
    fun testFactorizeHigherOrderRadicand() {
        testRule(
            "[9 / root[18, 4]]",
            factorizeHigherOrderRadicand,
            "[9 / root[2 * [3^2], 4]]"
        )
        testRule(
            "[9 / root[18, 2]]",
            factorizeHigherOrderRadicand,
            null
        )
        testRule(
            "[9 / 2 * root[18, 4]]",
            factorizeHigherOrderRadicand,
            "[9 / 2 * root[2 * [3^2], 4]]"
        )
        testRule(
            "[9 / root[2, 3]]",
            factorizeHigherOrderRadicand,
            null
        )
    }
}
