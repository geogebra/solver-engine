package methods.general

import methods.rules.testRule
import org.junit.jupiter.api.Test

class GeneralRulesTest {

    @Test
    fun testEliminateOneInProduct() {
        testRule("3*x*1*y*4", eliminateOneInProduct, "3*x*y*4")
        testRule("3*x*1*y*4", eliminateOneInProduct, "3*x*y*4")
        testRule("3*1*4", eliminateOneInProduct, "3*4")
        testRule("1*x", eliminateOneInProduct, "x")
    }

    @Test
    fun testSimplifyProductWithTwoNegativeFactors() {
        testRule("(-2) * (-3)", simplifyProductWithTwoNegativeFactors, "2 * 3")
        testRule("(-x) * y * (-12) * 5", simplifyProductWithTwoNegativeFactors, "x * y * 12 * 5")
        testRule("(-2):(-3)", simplifyProductWithTwoNegativeFactors, "2:3")
    }

    @Test
    fun testMoveSignOfNegativeFactorOutOfProduct() {
        testRule("3 * (-5)", moveSignOfNegativeFactorOutOfProduct, "- 3 * 5")
        testRule("x * (-y) * z", moveSignOfNegativeFactorOutOfProduct, "-x * y * z")
        testRule("x*3:(-5)", moveSignOfNegativeFactorOutOfProduct, "- x*3:5")
    }

    @Test
    fun testEliminateZeroInSum() {
        testRule("0 + x + y", eliminateZeroInSum, "x + y")
        testRule("0 + 1 + x", eliminateZeroInSum, "1 + x")
        testRule("x + 0 + y", eliminateZeroInSum, "x + y")
        testRule("x + 0", eliminateZeroInSum, "x")
        testRule("1 + 0", eliminateZeroInSum, "1")
    }

    @Test
    fun testEvaluateProductContainingZero() {
        testRule("z*x*0", evaluateProductContainingZero, "0")
        testRule("0*1", evaluateProductContainingZero, "0")
        testRule("(-2)*0*x", evaluateProductContainingZero, "0")
        testRule("0:1", evaluateProductContainingZero, null)
        testRule("0:(1+1)", evaluateProductContainingZero, null)
        testRule("0 * [1 / 1 + 1] * 3", evaluateProductContainingZero, "0")
        // this test case doesn't pass right now
        // testRule("0 * [1 / 1 - 1] * 3", evaluateProductContainingZero, null)
    }

    @Test
    fun testEvaluateZeroDividedByAnyValue() {
        testRule("0:1", evaluateZeroDividedByAnyValue, "0")
        testRule("0:0", evaluateZeroDividedByAnyValue, null)
        // the rule "can't see the future" right now that the denominator is non-zero
        testRule("0:(1+1)", evaluateZeroDividedByAnyValue, null)
    }

    @Test
    fun testSimplifyZeroNumeratorFractionToZero() {
        testRule("[0 / 2]", simplifyZeroNumeratorFractionToZero, "0")
        testRule("[0 / -1]", simplifyZeroNumeratorFractionToZero, "0")
        testRule("[0 / root[3, 3] + root[5, 3]]", simplifyZeroNumeratorFractionToZero, null)
    }

    @Test
    fun testSimplifyZeroDenominatorFractionToUndefined() {
        testRule("[2 / 0]", simplifyZeroDenominatorFractionToUndefined, "UNDEFINED")
        testRule("[sqrt[2] / 0]", simplifyZeroDenominatorFractionToUndefined, "UNDEFINED")
    }

    @Test
    fun testSimplifyDoubleMinus() {
        testRule("-(-5)", simplifyDoubleMinus, "5")
        testRule("-(-x)", simplifyDoubleMinus, "x")
    }

    @Test
    fun testRewriteDivisionAsFraction() {
        testRule("1:8", rewriteDivisionAsFraction, "[1 / 8]")
    }

    @Test
    fun testDistributePowerOfProduct() {
        testRule(
            "[(2 * x * y) ^ 5]",
            distributePowerOfProduct,
            "[2 ^ 5] * [x ^ 5] * [y ^ 5]"
        )
        testRule(
            "[(sqrt[3] * root[5, 2]) ^ n]",
            distributePowerOfProduct,
            "[(sqrt[3]) ^ n] * [(root[5, 2]) ^ n]"
        )
    }

    @Test
    fun testExpandBinomialSquared() {
        testRule(
            "[(a + b) ^ 2]",
            expandBinomialSquared,
            "[a ^ 2] + 2 * a * b + [b ^ 2]"
        )
        testRule(
            "[(sqrt[2] + 1) ^ 2]",
            expandBinomialSquared,
            "[(sqrt[2]) ^ 2] + 2 * sqrt[2] * 1 + [1 ^ 2]"
        )
        testRule(
            "[(x - y) ^ 2]",
            expandBinomialSquared,
            "[x ^ 2] + 2 * x * (-y) + [(-y) ^ 2]"
        )
    }

    @Test
    fun testExpandProductOfSingleTermAndSum() {
        testRule(
            "sqrt[2] * (3 + sqrt[4])",
            distributeMultiplicationOverSum,
            "sqrt[2] * 3 + sqrt[2] * sqrt[4]"
        )
        testRule(
            "(3 + sqrt[4]) * sqrt[2]",
            distributeMultiplicationOverSum,
            "3 * sqrt[2] + sqrt[4] * sqrt[2]"
        )
        testRule(
            "(3 - sqrt[4]) * sqrt[2]",
            distributeMultiplicationOverSum,
            "3 * sqrt[2] - sqrt[4] * sqrt[2]"
        )
        testRule(
            "(3 + sqrt[4] + sqrt[5]) * sqrt[2]",
            distributeMultiplicationOverSum,
            "3 * sqrt[2] + sqrt[4] * sqrt[2] + sqrt[5] * sqrt[2]"
        )
    }

    @Test
    fun testRewritePowerAsProduct() {
        testRule("[3^3]", rewritePowerAsProduct, "3 * 3 * 3")
        testRule("[0.3 ^ 2]", rewritePowerAsProduct, "0.3 * 0.3")
        testRule("[(x + 1) ^ 2]", rewritePowerAsProduct, "(x + 1) * (x + 1)")
        testRule("[x^5]", rewritePowerAsProduct, "x * x * x * x * x")
        testRule("[x^6]", rewritePowerAsProduct, null)
        testRule("[x^1]", rewritePowerAsProduct, null)
        testRule("[x^0]", rewritePowerAsProduct, null)
        testRule("[([1/2])^4]", rewritePowerAsProduct, "[1/2] * [1/2] * [1/2] * [1/2]")
    }

    @Test
    fun testEvaluateProductDividedByZeroAsUndefined() {
        testRule("3 * 5 : 0", evaluateProductDividedByZeroAsUndefined, "UNDEFINED")
        testRule("7 : 0.00", evaluateProductDividedByZeroAsUndefined, "UNDEFINED")
        testRule("x : 0 * y", evaluateProductDividedByZeroAsUndefined, "UNDEFINED")
    }

    @Test
    fun testCancelAdditiveInverseElements() {
        testRule("sqrt[12] - sqrt[12] + 1", cancelAdditiveInverseElements, "1")
        testRule(
            "(sqrt[2] + root[3, 3])  + 1 - (sqrt[2] + root[3, 3]) + 2",
            cancelAdditiveInverseElements,
            "1 + 2"
        )
        testRule("sqrt[12] - sqrt[12]", cancelAdditiveInverseElements, "0")
        testRule("-sqrt[12] + sqrt[12]", cancelAdditiveInverseElements, "0")
        testRule("(x + 1 - y) - (x + 1 - y)", cancelAdditiveInverseElements, "0")
    }

    @Test
    fun testSimplifyExpressionToThePowerOfOne() {
        testRule("[ (sqrt[2] + 1) ^ 1]", simplifyExpressionToThePowerOfOne, "sqrt[2] + 1")
        testRule("[2 ^ 1]", simplifyExpressionToThePowerOfOne, "2")
    }
}
