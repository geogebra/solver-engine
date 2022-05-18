package rules

import java.util.stream.Stream

object MixedNumbersTest : RuleTest() {
    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("[4/21]", fractionToMixedNumber, null),
        RuleTestCase("[21/4]", fractionToMixedNumber, "[5 1/4]"),
        RuleTestCase("[2 3/4]", splitMixedNumber, "2 + [3/4]"),
    )
}