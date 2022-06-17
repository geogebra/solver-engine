package rules

import methods.rules.eliminateOneInProduct
import methods.rules.moveSignOfNegativeFactorOutOfProduct
import methods.rules.simplifyProductWithTwoNegativeFactors
import java.util.stream.Stream

object BasicRulesTest : RuleTest {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("3*x*1*y*4", eliminateOneInProduct, "3*x*y*4"),
        RuleTestCase("(-2) * (-3)", simplifyProductWithTwoNegativeFactors, "2 * 3"),
        RuleTestCase("(-x) * y * (-12) * 5", simplifyProductWithTwoNegativeFactors, "x * y * 12 * 5"),
        RuleTestCase("(-2):(-3)", simplifyProductWithTwoNegativeFactors, "2:3"),
        RuleTestCase("3 * (-5)", moveSignOfNegativeFactorOutOfProduct, "- 3 * 5"),
        RuleTestCase("x * (-y) * z", moveSignOfNegativeFactorOutOfProduct, "-x * y * z"),
        RuleTestCase("x*3:(-5)", moveSignOfNegativeFactorOutOfProduct, "- x*3:5"),
    )
}
