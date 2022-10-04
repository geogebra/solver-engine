package methods.general

import methods.rules.RuleTest
import methods.rules.RuleTestCase
import methods.rules.testRule
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
        RuleTestCase("0:1", evaluateProductContainingZero, null),
        RuleTestCase("0:(1+1)", evaluateProductContainingZero, null),
        RuleTestCase("0 * [1 / 1 + 1] * 3", evaluateProductContainingZero, "0"),
        // this test case doesn't pass right now
        // RuleTestCase("0 * [1 / 1 - 1] * 3", evaluateProductContainingZero, null),

        RuleTestCase("0:1", evaluateZeroDividedByAnyValue, "0"),
        RuleTestCase("0:0", evaluateZeroDividedByAnyValue, null),
        // the rule "can't see the future" right now that the denominator is non-zero
        RuleTestCase("0:(1+1)", evaluateZeroDividedByAnyValue, null),
        RuleTestCase("[0 / 2]", simplifyZeroNumeratorFractionToZero, "0"),
        RuleTestCase("[0 / -1]", simplifyZeroNumeratorFractionToZero, "0"),
        RuleTestCase("[0 / root[3, 3] + root[5, 3]]", simplifyZeroNumeratorFractionToZero, null),

        RuleTestCase("[2 / 0]", simplifyZeroDenominatorFractionToUndefined, "UNDEFINED"),
        RuleTestCase("[sqrt[2] / 0]", simplifyZeroDenominatorFractionToUndefined, "UNDEFINED"),

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

    @Test
    fun testRewritePowerAsProduct() {
        testRule("[3^3]", rewritePowerAsProduct, "3 * 3 * 3")
        testRule("[0.3 ^ 2]", rewritePowerAsProduct, "0.3 * 0.3")
        testRule("[(x + 1) ^ 2]", rewritePowerAsProduct, "(x + 1) * (x + 1)")
        testRule("[([1/2])^4]", rewritePowerAsProduct, "[1/2] * [1/2] * [1/2] * [1/2]")
        testRule("[x^5]", rewritePowerAsProduct, "x * x * x * x * x")
        testRule("[x^6]", rewritePowerAsProduct, null)
        testRule("[x^1]", rewritePowerAsProduct, null)
        testRule("[x^0]", rewritePowerAsProduct, null)
    }

    @Test
    fun testEvaluateProductDividedByZeroAsUndefined() {
        testRule("3 * 5 : 0", evaluateProductDividedByZeroAsUndefined, "UNDEFINED")
        testRule("x : 0 * y", evaluateProductDividedByZeroAsUndefined, "UNDEFINED")
    }
}
