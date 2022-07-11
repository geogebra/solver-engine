package methods.general

import methods.rules.RuleTest
import methods.rules.RuleTestCase
import java.util.stream.Stream

object AlgebraicFractionsTest : RuleTest {
    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("[x*y*z/a*y*c]", cancelCommonTerms, "[x*z/a*c]"),
        RuleTestCase("[5*2/5*3]", cancelCommonTerms, "[2/3]"),
        RuleTestCase("[x*y/a*y]", cancelCommonTerms, "[x / a]")
    )
}
