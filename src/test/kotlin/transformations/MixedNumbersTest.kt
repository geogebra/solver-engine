package transformations

import java.util.stream.Stream

object MixedNumbersTest : RuleTest() {
    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("[4/21]", FractionToMixedNumber, null),
        RuleTestCase("[21/4]", FractionToMixedNumber, "[5 1/4]"),
        RuleTestCase("[2 3/4]", SplitMixedNumber, "2 + [3/4]"),
    )
}