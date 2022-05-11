package transformations

import java.util.stream.Stream

object CommonDenominatorTest : RuleTest() {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("[3/8] + [5/12]", CommonDenominator, "[3 * 3/8 * 3] + [5 * 2/12 * 2]"),
    )
}