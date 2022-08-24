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
            "[4 * sqrt[3] / sqrt[3] * sqrt[3]]"
        ),
        RuleTestCase(
            "[4 / 2 * sqrt[3]]",
            rationalizeSimpleDenominatorWithCoefficient,
            "[4 * sqrt[3] / 2 * sqrt[3] * sqrt[3]]"
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
            "[2 / -root[3, 3] + root[5, 3]]",
            rewriteCubeRootDenominator,
            "[2 / root[5, 3] - root[3, 3]]"
        ),
        RuleTestCase(
            "[2 / root[3, 3] + root[5, 3]]",
            rewriteCubeRootDenominator,
            null
        )
    )
}
