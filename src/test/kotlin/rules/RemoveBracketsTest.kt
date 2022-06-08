package rules

import methods.rules.removeBracketsSum
import java.util.stream.Stream

object RemoveBracketsTest : RuleTest() {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("1 + (2 + 3) + 4", removeBracketsSum, "1 + 2 + 3 + 4"),
    )
}
