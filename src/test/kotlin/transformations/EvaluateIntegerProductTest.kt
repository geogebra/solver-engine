package transformations

import java.util.stream.Stream

object EvaluateIntegerProductTest : RuleTest() {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("z*2*x*3*y", EvaluateIntegerProduct, "z*6*x*y"),
        RuleTestCase("3*x*1*y*4", EliminateOneInProduct, "3*x*y*4")
    )
}
