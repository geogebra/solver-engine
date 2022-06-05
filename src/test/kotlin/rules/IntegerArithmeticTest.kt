package rules

import java.util.stream.Stream

object IntegerArithmeticTest : RuleTest() {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("z*2*x*3*y", evaluateIntegerProduct, "z*6*x*y"),
        RuleTestCase("3*x*1*y*4", eliminateOneInProduct, "3*x*y*4")
    )
}
