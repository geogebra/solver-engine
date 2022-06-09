package rules

import methods.rules.cancelInAFraction
import java.util.stream.Stream

object AlgebraicFractionsTest : RuleTest() {
    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("[x*y*z/a*y*c]", cancelInAFraction, "[x*z/a*c]"),
        RuleTestCase("[x*y/a*y]", cancelInAFraction, "[x / a]")
    )
}