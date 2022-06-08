package rules

import methods.rules.*
import java.util.stream.Stream

object IntegerFractionsTest : RuleTest() {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("5 + [2/4]", convertIntegerToFraction, "[5/1] + [2/4]"),
        RuleTestCase("[2/4] + 5", convertIntegerToFraction, "[2/4] + [5/1]"),
        RuleTestCase("1+[2/10]+z+[3/10]+x", addLikeFractions, "1+[2+3/10]+z+x"),
        RuleTestCase("[3/10] - [2/10]", subtractLikeFraction, "[3 - 2 / 10]"),
        RuleTestCase("1 - [2/10]", subtractLikeFraction, null),
        RuleTestCase("[3/8] + [5/12]", commonDenominator, "[3 * 3/8 * 3] + [5 * 2/12 * 2]"),
        RuleTestCase("[4/-5]", negativeDenominator, "-[4/5]"),
        RuleTestCase("[4/5]", negativeDenominator, null),
    )
}