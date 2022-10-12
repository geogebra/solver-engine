package methods.general

import methods.rules.RuleTest
import methods.rules.RuleTestCase
import java.util.stream.Stream

object RemoveBracketsTest : RuleTest {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("1 + (2 + 3) + 4", removeBracketsInSum, "1 + 2 + 3 + 4"),

        RuleTestCase("1 + (-1)", removeBracketAroundSignedIntegerInSum, "1 - 1"),
        RuleTestCase("x + y + (-2)", removeBracketAroundSignedIntegerInSum, "x + y - 2"),

        RuleTestCase("(1)", removeOuterBracket, "1")
    )
}
