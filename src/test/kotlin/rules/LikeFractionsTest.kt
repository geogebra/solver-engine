package rules

import java.util.stream.Stream

object LikeFractionsTest : RuleTest() {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("1+[2/10]+z+[3/10]+x", addLikeFractions, "1+[2+3/10]+z+x"),
        RuleTestCase("[3/10] - [2/10]", subtractLikeFraction, "[3 - 2 / 10]"),
        RuleTestCase("1 - [2/10]", subtractLikeFraction, null),
    )
}
