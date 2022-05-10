package transformations

import java.util.stream.Stream

object CancelInAFractionTest : RuleTest() {
    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("[x*y*z/a*y*c]", CancelInAFraction, "[x*z/a*c]"),
    )
}