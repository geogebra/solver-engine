package rules

import methods.rules.cancelInAFraction
import java.util.stream.Stream

object AlgebraicFractionsTest : RuleTest() {
    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("[x*y*z/a*y*c]", cancelInAFraction, "[x*z/a*c]"),
        RuleTestCase("[5*2/5*3]", cancelInAFraction, "[2/3]"),
        RuleTestCase("[x*y/a*y]", cancelInAFraction, "[x / a]")
    )
}