package rules

import java.util.stream.Stream

object AddIntegerToFractionTest : RuleTest() {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("5 + [2/4]", addIntegerToFraction, "[5*4/1*4] + [2/4]"),
        RuleTestCase("[2/4] + 5", addIntegerToFraction, "[2/4] + [5*4/1*4]"),
    )
}