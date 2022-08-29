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
            rationalizeSumOfIntegerAndRadical,
            null
        ),
        RuleTestCase(
            "[1 / 1 + sqrt[3]]",
            rationalizeSumOfIntegerAndRadical,
            "[1 / 1 + sqrt[3]] * [1 - sqrt[3] / 1 - sqrt[3]]"
        ),
        RuleTestCase(
            "[1 / 1 + 2 * sqrt[3]]",
            rationalizeSumOfIntegerAndRadical,
            "[1 / 1 + 2 * sqrt[3]] * [1 - 2 * sqrt[3] / 1 - 2 * sqrt[3]]"
        ),
        RuleTestCase(
            "[1 / 1 - sqrt[3]]",
            rationalizeSumOfIntegerAndRadical,
            "[1 / 1 - sqrt[3]] * [1 + sqrt[3] / 1 + sqrt[3]]"
        ),
        RuleTestCase(
            "[1 / 1 - 2 * sqrt[3]]",
            rationalizeSumOfIntegerAndRadical,
            "[1 / 1 - 2 * sqrt[3]] * [1 + 2 * sqrt[3] / 1 + 2 * sqrt[3]]"
        ),
        RuleTestCase(
            "[1 / 2 * sqrt[3] - 1]",
            rationalizeSumOfIntegerAndRadical,
            "[1 / 2 * sqrt[3] - 1] * [2 * sqrt[3] + 1 / 2 * sqrt[3] + 1]"
        ),
        RuleTestCase(
            "[1 / 2 * sqrt[3] - sqrt[5]]",
            rationalizeSumOfIntegerAndRadical,
            "[1 / 2 * sqrt[3] - sqrt[5]] * [2 * sqrt[3] + sqrt[5] / 2 * sqrt[3] + sqrt[5]]"
        ),
        RuleTestCase(
            "[1 / sqrt[3] + 4 * sqrt[5]]",
            rationalizeSumOfIntegerAndRadical,
            "[1 / sqrt[3] + 4 * sqrt[5]] * [sqrt[3] - 4 * sqrt[5] / sqrt[3] - 4 * sqrt[5]]"
        ),
        RuleTestCase(
            "[2 / 2 * root[3, 3] + 3 * root[4, 3]]",
            rationalizeCubeRootDenominator,
            "[2 / 2 * root[3, 3] + 3 * root[4, 3]] * " +
                "[[(2 * root[3, 3]) ^ 2] - (2 * root[3, 3]) * (3 * root[4, 3]) + " +
                "[(3 * root[4, 3]) ^ 2] / [(2 * root[3, 3]) ^ 2] - " +
                "(2 * root[3, 3]) * (3 * root[4, 3]) + [(3 * root[4, 3]) ^ 2]]"
        ),
        RuleTestCase(
            "[2 / 2 * root[3, 3] - 3 * root[4, 3]]",
            rationalizeCubeRootDenominator,
            "[2 / 2 * root[3, 3] - 3 * root[4, 3]] * " +
                "[[(2 * root[3, 3]) ^ 2] + (2 * root[3, 3]) * (3 * root[4, 3]) + " +
                "[(3 * root[4, 3]) ^ 2] / [(2 * root[3, 3]) ^ 2] + " +
                "(2 * root[3, 3]) * (3 * root[4, 3]) + [(3 * root[4, 3]) ^ 2]]"
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
        )
    )
}
