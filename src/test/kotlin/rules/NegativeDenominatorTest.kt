package rules

import java.util.stream.Stream

object NegativeDenominatorTest : RuleTest() {
    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("[4/-5]", negativeDenominator, "-[4/5]"),
        RuleTestCase("[4/5]", negativeDenominator, null),
    )
}