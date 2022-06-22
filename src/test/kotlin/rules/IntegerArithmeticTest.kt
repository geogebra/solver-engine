package rules

import methods.rules.eliminateOneInProduct
import methods.rules.eliminateZeroInSum
import methods.rules.evaluateIntegerProduct
import methods.rules.evaluateSignedIntegerAddition
import methods.rules.evaluateSignedIntegerPower
import methods.rules.evaluateSignedIntegerProduct
import methods.rules.evaluateUnsignedIntegerSubtraction
import methods.rules.simplifyDoubleNeg
import methods.rules.writeIntegerSquareAsMulWithOneAtStart
import methods.rules.writeIntegerSquareAsMulWithoutOneAtStart
import methods.rules.zeroInProduct
import java.util.stream.Stream

object IntegerArithmeticTest : RuleTest {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("0 + x + y", eliminateZeroInSum, "x + y"),
        RuleTestCase("0 + 1 + x", eliminateZeroInSum, "1 + x"),
        RuleTestCase("x + 0 + y", eliminateZeroInSum, "x + y"),
        // Fixed: Identified as bug
        RuleTestCase("x + 0", eliminateZeroInSum, "x"),
        RuleTestCase("1 + 0", eliminateZeroInSum, "1"),

        RuleTestCase("3*x*1*y*4", eliminateOneInProduct, "3*x*y*4"),
        RuleTestCase("3*1*4", eliminateOneInProduct, "3*4"),
        RuleTestCase("1*x", eliminateOneInProduct, "x"),

        RuleTestCase("z*x*0", zeroInProduct, "0"),
        RuleTestCase("0*1", zeroInProduct, "0"),
        RuleTestCase("(-2)*0*x", zeroInProduct, "0"),

        RuleTestCase("5 - 4", evaluateUnsignedIntegerSubtraction, "1"),
        RuleTestCase("4 - 5", evaluateUnsignedIntegerSubtraction, null),

        RuleTestCase("1 + x + 2", evaluateSignedIntegerAddition, "3 + x"),
        RuleTestCase("1 + x + (-2)", evaluateSignedIntegerAddition, "-1 + x"),
        RuleTestCase("(-2) + 3", evaluateSignedIntegerAddition, "1"),
        RuleTestCase("(-2) + (-3) + x", evaluateSignedIntegerAddition, "-5 + x"),

        RuleTestCase("z*2*x*3*y", evaluateIntegerProduct, "z*6*x*y"),
        RuleTestCase("2*3", evaluateIntegerProduct, "6"),

        RuleTestCase("1 * (-2)", evaluateSignedIntegerProduct, "-2"),
        RuleTestCase("(-2) * x * 5", evaluateSignedIntegerProduct, "(-10) * x"),
        RuleTestCase("10:2", evaluateSignedIntegerProduct, "5"),
        RuleTestCase("10:(-2)", evaluateSignedIntegerProduct, "-5"),

        RuleTestCase("[5^3]", evaluateSignedIntegerPower, "125"),
        RuleTestCase("[(-5) ^ 3]", evaluateSignedIntegerPower, "-125"),

        RuleTestCase("-(-5)", simplifyDoubleNeg, "5"),
        RuleTestCase("-(-x)", simplifyDoubleNeg, "x"),

        RuleTestCase("[4^2]", writeIntegerSquareAsMulWithOneAtStart, "1 * 4 * 4"),
        RuleTestCase("[4^2]", writeIntegerSquareAsMulWithoutOneAtStart, "4 * 4"),
        RuleTestCase("[(-3) ^ 2]", writeIntegerSquareAsMulWithoutOneAtStart, "(-3) * (-3)"),
        RuleTestCase("[(-3) ^ 2]", writeIntegerSquareAsMulWithOneAtStart, "1 * (-3) * (-3)")
    )
}
