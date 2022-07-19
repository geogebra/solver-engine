package methods.general

import methods.rules.RuleTest
import methods.rules.RuleTestCase
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

        RuleTestCase("0 + x + y", eliminateZeroInSum, "x + y"),
        RuleTestCase("0 + 1 + x", eliminateZeroInSum, "1 + x"),
        RuleTestCase("x + 0 + y", eliminateZeroInSum, "x + y"),
        // Fixed: Identified as bug
        RuleTestCase("x + 0", eliminateZeroInSum, "x"),
        RuleTestCase("1 + 0", eliminateZeroInSum, "1"),

        RuleTestCase("3*x*1*y*4", eliminateOneInProduct, "3*x*y*4"),
        RuleTestCase("3*1*4", eliminateOneInProduct, "3*4"),
        RuleTestCase("1*x", eliminateOneInProduct, "x"),

        RuleTestCase("z*x*0", evaluateProductContainingZero, "0"),
        RuleTestCase("0*1", evaluateProductContainingZero, "0"),
        RuleTestCase("(-2)*0*x", evaluateProductContainingZero, "0"),

        RuleTestCase("-(-5)", simplifyDoubleMinus, "5"),
        RuleTestCase("-(-x)", simplifyDoubleMinus, "x"),

    )
}
