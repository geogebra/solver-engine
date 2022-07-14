package methods.fractionroots

import methods.rules.RuleTest
import methods.rules.RuleTestCase
import java.util.stream.Stream

object FractionRootsRulesTest : RuleTest {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase(
            "sqrt[ [2 / 3] ]",
            distributeRadicalRuleOverFractionsToNumeratorAndDenominator,
            "[ sqrt[2] / sqrt[3] ]"
        ),
        RuleTestCase(
            "sqrt[ [ 27 / 10 ] ]",
            distributeRadicalRuleOverFractionsToNumeratorAndDenominator,
            "[sqrt[ 27 ] / sqrt[ 10 ]]"
        ),
        RuleTestCase(
            "[4 / 2 * sqrt[3]]",
            writeAsMultiplicationWithUnitaryRadicalFraction,
            "[4 / 2 * sqrt[3]] * [sqrt[3] / sqrt[3]]"
        ),
        // RuleTestCase("[4 / sqrt[3]]",
        //     writeAsMultiplicationWithUnitaryRadicalFraction,
        //     "[4 / sqrt[3]] * [ sqrt[3] / sqrt[3] ]"
        // )
    )
}
