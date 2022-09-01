package methods.general

import methods.rules.RuleTest
import methods.rules.RuleTestCase
import org.junit.jupiter.api.Test
import java.util.stream.Stream

object BasicRulesTest : RuleTest {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("3*x*1*y*4", eliminateOneInProduct, "3*x*y*4"),
        RuleTestCase("[2^1]", eliminateLoneOneInExponent, "2"),
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

        RuleTestCase("1:8", rewriteDivisionAsFraction, "[1 / 8]")

    )
}

class GeneralRulesTest {

    @Test
    fun testDistributePowerOfProduct() {
        RuleTestCase(
            "[(2 * x * y) ^ 5]",
            distributePowerOfProduct,
            "[2 ^ 5] * [x ^ 5] * [y ^ 5]"
        ).assert()
        RuleTestCase(
            "[(sqrt[3] * root[5, 2]) ^ n]",
            distributePowerOfProduct,
            "[(sqrt[3]) ^ n] * [(root[5, 2]) ^ n]"
        ).assert()
    }

    @Test
    fun testExpandBinomialSquared() {
        RuleTestCase(
            "[(a + b) ^ 2]",
            expandBinomialSquared,
            "[a ^ 2] + 2 * a * b + [b ^ 2]"
        ).assert()
        RuleTestCase(
            "[(sqrt[2] + 1) ^ 2]",
            expandBinomialSquared,
            "[(sqrt[2]) ^ 2] + 2 * sqrt[2] * 1 + [1 ^ 2]"
        ).assert()
        RuleTestCase(
            "[(x - y) ^ 2]",
            expandBinomialSquared,
            "[x ^ 2] + 2 * x * (-y) + [(-y) ^ 2]"
        ).assert()
    }

    @Test
    fun testExpandProductOfSingleTermAndSum() {
        RuleTestCase(
            "sqrt[2] * (3 + sqrt[4])",
            distributeMultiplicationOverSum,
            "sqrt[2] * 3 + sqrt[2] * sqrt[4]"
        ).assert()
        RuleTestCase(
            "(3 + sqrt[4]) * sqrt[2]",
            distributeMultiplicationOverSum,
            "3 * sqrt[2] + sqrt[4] * sqrt[2]"
        ).assert()
        RuleTestCase(
            "(3 - sqrt[4]) * sqrt[2]",
            distributeMultiplicationOverSum,
            "3 * sqrt[2] - sqrt[4] * sqrt[2]"
        ).assert()
        RuleTestCase(
            "(3 + sqrt[4] + sqrt[5]) * sqrt[2]",
            distributeMultiplicationOverSum,
            "3 * sqrt[2] + sqrt[4] * sqrt[2] + sqrt[5] * sqrt[2]"
        ).assert()
    }
}
