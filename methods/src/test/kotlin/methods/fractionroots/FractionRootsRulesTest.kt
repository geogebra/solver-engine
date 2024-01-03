package methods.fractionroots

import engine.methods.testRule
import methods.fractionroots.FractionRootsRules.DistributeRadicalOverFraction
import methods.fractionroots.FractionRootsRules.FactorizeHigherOrderRadicand
import methods.fractionroots.FractionRootsRules.FlipRootsInDenominator
import methods.fractionroots.FractionRootsRules.HigherOrderRationalizingTerm
import methods.fractionroots.FractionRootsRules.IdentifyCubeSumDifference
import methods.fractionroots.FractionRootsRules.RationalizeSimpleDenominator
import methods.fractionroots.FractionRootsRules.RationalizeSumOfIntegerAndCubeRoot
import methods.fractionroots.FractionRootsRules.RationalizeSumOfIntegerAndSquareRoot
import org.junit.jupiter.api.Test

class FractionRootsRulesTest {
    @Test
    fun testDistributeRadicalOverFraction() {
        testRule(
            "sqrt[[2 / 3]]",
            DistributeRadicalOverFraction,
            "[sqrt[2] / sqrt[3]]",
        )
        testRule(
            "root[ [2 / 9], 4]",
            DistributeRadicalOverFraction,
            "[ root[2, 4] / root[9, 4] ]",
        )
    }

    @Test
    fun testRationalizeSimpleDenominator() {
        testRule(
            "[4 / sqrt[3]]",
            RationalizeSimpleDenominator,
            "[4 / sqrt[3]] * [sqrt[3] / sqrt[3]]",
        )
        testRule(
            "[4 / 2 * sqrt[3]]",
            RationalizeSimpleDenominator,
            "[4 / 2 * sqrt[3]] * [sqrt[3] / sqrt[3]]",
        )
    }

    @Test
    fun testRationalizeSumOfIntegerAndSquareRoot() {
        testRule(
            "[1 / 2 + 3]",
            RationalizeSumOfIntegerAndSquareRoot,
            null,
        )
        testRule(
            "[1 / 1 + sqrt[3]]",
            RationalizeSumOfIntegerAndSquareRoot,
            "[1 / 1 + sqrt[3]] * [1 - sqrt[3] / 1 - sqrt[3]]",
        )
        testRule(
            "[1 / 1 + 2 * sqrt[3]]",
            RationalizeSumOfIntegerAndSquareRoot,
            "[1 / 1 + 2 * sqrt[3]] * [1 - 2 * sqrt[3] / 1 - 2 * sqrt[3]]",
        )
        testRule(
            "[1 / 1 - sqrt[3]]",
            RationalizeSumOfIntegerAndSquareRoot,
            "[1 / 1 - sqrt[3]] * [1 + sqrt[3] / 1 + sqrt[3]]",
        )
        testRule(
            "[1 / 1 - 2 * sqrt[3]]",
            RationalizeSumOfIntegerAndSquareRoot,
            "[1 / 1 - 2 * sqrt[3]] * [1 + 2 * sqrt[3] / 1 + 2 * sqrt[3]]",
        )
        testRule(
            "[1 / 2 * sqrt[3] - 1]",
            RationalizeSumOfIntegerAndSquareRoot,
            "[1 / 2 * sqrt[3] - 1] * [2 * sqrt[3] + 1 / 2 * sqrt[3] + 1]",
        )
        testRule(
            "[1 / 2 * sqrt[3] - sqrt[5]]",
            RationalizeSumOfIntegerAndSquareRoot,
            "[1 / 2 * sqrt[3] - sqrt[5]] * [2 * sqrt[3] + sqrt[5] / 2 * sqrt[3] + sqrt[5]]",
        )
        testRule(
            "[1 / sqrt[3] + 4 * sqrt[5]]",
            RationalizeSumOfIntegerAndSquareRoot,
            "[1 / sqrt[3] + 4 * sqrt[5]] * [sqrt[3] - 4 * sqrt[5] / sqrt[3] - 4 * sqrt[5]]",
        )
    }

    @Test
    fun testRationalizeSumOfIntegerAndCubeRoot() {
        testRule(
            "[2 / 2 root[3, 3] + 3 root[4, 3]]",
            RationalizeSumOfIntegerAndCubeRoot,
            "[2 / 2 root[3, 3] + 3 root[4, 3]] * " +
                "[[(2 root[3, 3]) ^ 2] - 2 root[3, 3] * 3 root[4, 3] + " +
                "[(3 root[4, 3]) ^ 2] / [(2 root[3, 3]) ^ 2] - " +
                "2 root[3, 3] * 3 root[4, 3] + [(3 root[4, 3]) ^ 2]]",
        )
        testRule(
            "[2 / 2 root[3, 3] - 3 root[4, 3]]",
            RationalizeSumOfIntegerAndCubeRoot,
            "[2 / 2 root[3, 3] - 3 root[4, 3]] * " +
                "[[(2 root[3, 3]) ^ 2] + 2 root[3, 3] * 3 root[4, 3] + " +
                "[(3 root[4, 3]) ^ 2] / [(2 root[3, 3]) ^ 2] + " +
                "2 root[3, 3] * 3 root[4, 3] + [(3 root[4, 3]) ^ 2]]",
        )
        testRule(
            "[2 / 2 - root[3, 3]]",
            RationalizeSumOfIntegerAndCubeRoot,
            "[2 / 2 - root[3, 3]] * " +
                "[ [2^2] + 2 root[3, 3] + [(root[3, 3])^2] / [2^2] + 2 root[3, 3] + [(root[3, 3])^2]]",
        )
        testRule(
            "[2 / 3 + 2]",
            RationalizeSumOfIntegerAndCubeRoot,
            null,
        )
        testRule(
            "[2 / 3 - 2]",
            RationalizeSumOfIntegerAndCubeRoot,
            null,
        )
    }

    @Test
    fun testIdentifyCubeSumDifference() {
        testRule(
            "(root[5, 3] + root[3, 3]) * ([(root[5, 3]) ^ 2] - root[5, 3] * root[3, 3] + [(root[3, 3]) ^ 2])",
            IdentifyCubeSumDifference,
            "[(root[5, 3]) ^ 3] + [(root[3, 3]) ^ 3]",
        )
        testRule(
            "(root[5, 3] - root[3, 3]) * ([(root[5, 3]) ^ 2] + root[5, 3] * root[3, 3] + [(root[3, 3]) ^ 2])",
            IdentifyCubeSumDifference,
            "[(root[5, 3]) ^ 3] - [(root[3, 3]) ^ 3]",
        )
        testRule(
            "(2 * root[3, 3] + 3 * root[4, 3]) * ([(2 * root[3, 3]) ^ 2] - 2 * root[3, 3] * " +
                "3 * root[4, 3] + [(3 * root[4, 3]) ^ 2])",
            IdentifyCubeSumDifference,
            "[(2 * root[3, 3]) ^ 3] + [(3 * root[4, 3]) ^ 3]",
        )
        testRule(
            "(2 * root[3, 3] - 3 * root[4, 3]) * ([(2 * root[3, 3]) ^ 2] + 2 * root[3, 3] * 3 * root[4, 3] " +
                "+ [(3 * root[4, 3]) ^ 2])",
            IdentifyCubeSumDifference,
            "[(2 * root[3, 3]) ^ 3] - [(3 * root[4, 3]) ^ 3]",
        )
        testRule(
            "(2 - root[3, 3]) * ([2 ^ 2] + 2 * root[3, 3] + [(root[3, 3]) ^ 2])",
            IdentifyCubeSumDifference,
            "[2 ^ 3] - [(root[3, 3]) ^ 3]",
        )
    }

    @Test
    fun testFlipRootsInDenominator() {
        testRule(
            "[2 / -sqrt[2] + 3 * sqrt[5]]",
            FlipRootsInDenominator,
            "[2 / 3 * sqrt[5] - sqrt[2]]",
        )
        testRule(
            "[2 / -root[3, 3] + root[5, 3]]",
            FlipRootsInDenominator,
            "[2 / root[5, 3] - root[3, 3]]",
        )
        testRule(
            "[2 / root[3, 3] + root[5, 3]]",
            FlipRootsInDenominator,
            null,
        )
        testRule(
            "[2 / -3 * root[3, 3] + sqrt[3]]",
            FlipRootsInDenominator,
            "[2 / sqrt[3] - 3 * root[3, 3]]",
        )
    }

    @Test
    fun testHigherOrderRationalizingTerm() {
        testRule(
            "[ 9 / root[[2^2] * [3^2], 4] ]",
            HigherOrderRationalizingTerm,
            "[9 / root[[2^2] * [3^2], 4]] * [root[[2^4 - 2] * [3^ 4 - 2], 4] / root[[2^4 - 2] * [3^ 4 - 2], 4]]",
        )
        testRule(
            "[9 / root[2, 4]]",
            HigherOrderRationalizingTerm,
            "[9 / root[2, 4]] * [ root[ [2^4-1], 4] / root[ [2^4-1], 4] ]",
        )
        testRule(
            "[9 / root[[2^3], 4]]",
            HigherOrderRationalizingTerm,
            "[9 / root[[2^3], 4]] * [ root[ [2^4-3], 4] / root[ [2^4-3], 4] ]",
        )
        testRule(
            "[ 9 / root[[2^2] * [3^2] * 5, 4] ]",
            HigherOrderRationalizingTerm,
            "[9 / root[[2 ^ 2] * [3 ^ 2] * 5, 4]] * " +
                "[root[[2 ^ 4 - 2] * [3 ^ 4 - 2] * [5 ^ 4 - 1], 4] / root[[2 ^ 4 - 2] * [3 ^ 4 - 2] * [5 ^ 4 - 1], 4]]",
        )
        testRule(
            "[9 / root[ 2 * [3^2], 4]]",
            HigherOrderRationalizingTerm,
            "[9 / root[2 * [3^2], 4]] * [root[ [2^4 - 1] * [3^4 - 2], 4] / root[ [2^4 - 1] * [3^4 - 2], 4]]",
        )
        testRule(
            "[9 / 2 * root[ 2 * [3^2], 4]]",
            HigherOrderRationalizingTerm,
            "[9 / 2 * root[2 * [3^2], 4]] * [root[ [2^4 - 1] * [3^4 - 2], 4] / root[ [2^4 - 1] * [3^4 - 2], 4]]",
        )
    }

    @Test
    fun testFactorizeHigherOrderRadicand() {
        testRule(
            "[9 / root[18, 4]]",
            FactorizeHigherOrderRadicand,
            "[9 / root[2 * [3^2], 4]]",
        )
        testRule(
            "[9 / root[18, 2]]",
            FactorizeHigherOrderRadicand,
            null,
        )
        testRule(
            "[9 / 2 root[18, 4]]",
            FactorizeHigherOrderRadicand,
            "[9 / 2 root[2 * [3^2], 4]]",
        )
        testRule(
            "[9 / root[2, 3]]",
            FactorizeHigherOrderRadicand,
            null,
        )
    }
}
