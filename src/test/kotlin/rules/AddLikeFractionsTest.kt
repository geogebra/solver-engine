package rules

import java.util.stream.Stream

object AddLikeFractionsTest : RuleTest() {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("1+[2/10]+z+[3/10]+x", addLikeFractions, "1+[2+3/10]+z+x"),
    )
}
