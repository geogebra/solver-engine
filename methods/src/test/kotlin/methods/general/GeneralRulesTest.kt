package methods.general

import methods.general.GeneralRules.CancelAdditiveInverseElements
import methods.general.GeneralRules.DistributeMultiplicationOverSum
import methods.general.GeneralRules.DistributePowerOfProduct
import methods.general.GeneralRules.EliminateOneInProduct
import methods.general.GeneralRules.EliminateZeroInSum
import methods.general.GeneralRules.EvaluateExpressionToThePowerOfZero
import methods.general.GeneralRules.EvaluateOneToAnyPower
import methods.general.GeneralRules.EvaluateProductContainingZero
import methods.general.GeneralRules.EvaluateProductDividedByZeroAsUndefined
import methods.general.GeneralRules.EvaluateZeroDividedByAnyValue
import methods.general.GeneralRules.EvaluateZeroToAPositivePower
import methods.general.GeneralRules.ExpandBinomialSquared
import methods.general.GeneralRules.FlipFractionUnderNegativePower
import methods.general.GeneralRules.MoveSignOfNegativeFactorOutOfProduct
import methods.general.GeneralRules.RewriteDivisionAsFraction
import methods.general.GeneralRules.RewriteFractionOfPowersWithSameBase
import methods.general.GeneralRules.RewriteFractionOfPowersWithSameExponent
import methods.general.GeneralRules.RewritePowerAsProduct
import methods.general.GeneralRules.RewriteProductOfPowersWithInverseBase
import methods.general.GeneralRules.RewriteProductOfPowersWithInverseFractionBase
import methods.general.GeneralRules.RewriteProductOfPowersWithNegatedExponent
import methods.general.GeneralRules.RewriteProductOfPowersWithSameBase
import methods.general.GeneralRules.RewriteProductOfPowersWithSameExponent
import methods.general.GeneralRules.SimplifyDoubleMinus
import methods.general.GeneralRules.SimplifyExpressionToThePowerOfOne
import methods.general.GeneralRules.SimplifyProductWithTwoNegativeFactors
import methods.general.GeneralRules.SimplifyZeroDenominatorFractionToUndefined
import methods.general.GeneralRules.SimplifyZeroNumeratorFractionToZero
import methods.rules.testRule
import org.junit.jupiter.api.Test

class GeneralRulesTest {

    @Test
    fun testEliminateOneInProduct() {
        testRule("3*x*1*y*4", EliminateOneInProduct, "3xy*4")
        testRule("3*x*1*y*4", EliminateOneInProduct, "3xy*4")
        testRule("3*1*4", EliminateOneInProduct, "3*4")
        testRule("1*x", EliminateOneInProduct, "x")
    }

    @Test
    fun testSimplifyProductWithTwoNegativeFactors() {
        testRule("(-2) * (-3)", SimplifyProductWithTwoNegativeFactors, "2 * 3")
        testRule("(-x) * y * (-12) * 5", SimplifyProductWithTwoNegativeFactors, "x y * 12 * 5")
        testRule("(-2):(-3)", SimplifyProductWithTwoNegativeFactors, "2:3")
    }

    @Test
    fun testMoveSignOfNegativeFactorOutOfProduct() {
        testRule("3 * (-5)", MoveSignOfNegativeFactorOutOfProduct, "- 3 * 5")
        testRule("x * (-y) * z", MoveSignOfNegativeFactorOutOfProduct, "-xyz")
        testRule("x*3:(-5)", MoveSignOfNegativeFactorOutOfProduct, "- x*3:5")
    }

    @Test
    fun testEliminateZeroInSum() {
        testRule("0 + x + y", EliminateZeroInSum, "x + y")
        testRule("0 + 1 + x", EliminateZeroInSum, "1 + x")
        testRule("x + 0 + y", EliminateZeroInSum, "x + y")
        testRule("x + 0", EliminateZeroInSum, "x")
        testRule("1 + 0", EliminateZeroInSum, "1")
    }

    @Test
    fun testEvaluateProductContainingZero() {
        testRule("z*x*0", EvaluateProductContainingZero, "0")
        testRule("0*1", EvaluateProductContainingZero, "0")
        testRule("(-2)*0*x", EvaluateProductContainingZero, "0")
        testRule("0:1", EvaluateProductContainingZero, null)
        testRule("0:(1+1)", EvaluateProductContainingZero, null)
        testRule("0 * [1 / 1 + 1] * 3", EvaluateProductContainingZero, "0")
        testRule("0 * [1 / 1 - 1] * 3", EvaluateProductContainingZero, null)
    }

    @Test
    fun testEvaluateZeroDividedByAnyValue() {
        testRule("0:1", EvaluateZeroDividedByAnyValue, "0")
        testRule("0:0", EvaluateZeroDividedByAnyValue, null)
        testRule("0:(1+1)", EvaluateZeroDividedByAnyValue, null)
    }

    @Test
    fun testSimplifyZeroNumeratorFractionToZero() {
        testRule("[0 / 2]", SimplifyZeroNumeratorFractionToZero, "0")
        testRule("[0 / -1]", SimplifyZeroNumeratorFractionToZero, "0")
        testRule("[0 / root[3, 3] + root[5, 3]]", SimplifyZeroNumeratorFractionToZero, "0")
        testRule("[0 / 3 * (sqrt[2] - 1)]", SimplifyZeroNumeratorFractionToZero, "0")
        testRule("[0 / 1 - 1]", SimplifyZeroNumeratorFractionToZero, null)

        // This one works because the denominator is "obviously" positive
        testRule("[0 / sqrt[2] + sqrt[2]]", SimplifyZeroNumeratorFractionToZero, "0")
    }

    @Test
    fun testSimplifyZeroDenominatorFractionToUndefined() {
        testRule("[2 / 0]", SimplifyZeroDenominatorFractionToUndefined, "UNDEFINED")
        testRule("[sqrt[2] / 0]", SimplifyZeroDenominatorFractionToUndefined, "UNDEFINED")
    }

    @Test
    fun testSimplifyDoubleMinus() {
        testRule("-(-5)", SimplifyDoubleMinus, "5")
        testRule("-(-x)", SimplifyDoubleMinus, "x")
    }

    @Test
    fun testRewriteDivisionAsFraction() {
        testRule("1:8", RewriteDivisionAsFraction, "[1 / 8]")
    }

    @Test
    fun testDistributePowerOfProduct() {
        testRule(
            "[(2 * x * y) ^ 5]",
            DistributePowerOfProduct,
            "[2 ^ 5] [x ^ 5] [y ^ 5]"
        )
        testRule(
            "[(sqrt[3] * root[5, 2]) ^ n]",
            DistributePowerOfProduct,
            "[(sqrt[3]) ^ n] * [(root[5, 2]) ^ n]"
        )
    }

    @Test
    fun testExpandBinomialSquared() {
        testRule(
            "[(a + b) ^ 2]",
            ExpandBinomialSquared,
            "[a ^ 2] + 2ab + [b ^ 2]"
        )
        testRule(
            "[(sqrt[2] + 1) ^ 2]",
            ExpandBinomialSquared,
            "[(sqrt[2]) ^ 2] + 2 sqrt[2] * 1 + [1 ^ 2]"
        )
        testRule(
            "[(x - y) ^ 2]",
            ExpandBinomialSquared,
            "[x ^ 2] + 2 x (-y) + [(-y) ^ 2]"
        )
    }

    @Test
    fun testExpandProductOfSingleTermAndSum() {
        testRule(
            "sqrt[2] * (3 + sqrt[4])",
            DistributeMultiplicationOverSum,
            "sqrt[2] * 3 + sqrt[2] * sqrt[4]"
        )
        testRule(
            "(3 + sqrt[4]) * sqrt[2]",
            DistributeMultiplicationOverSum,
            "3 sqrt[2] + sqrt[4] * sqrt[2]"
        )
        testRule(
            "(3 - sqrt[4]) * sqrt[2]",
            DistributeMultiplicationOverSum,
            "3 sqrt[2] - sqrt[4] * sqrt[2]"
        )
        testRule(
            "(3 + sqrt[4] + sqrt[5]) * sqrt[2]",
            DistributeMultiplicationOverSum,
            "3 sqrt[2] + sqrt[4] * sqrt[2] + sqrt[5] * sqrt[2]"
        )
    }

    @Test
    fun testRewritePowerAsProduct() {
        testRule("[3^3]", RewritePowerAsProduct, "3 * 3 * 3")
        testRule("[0.3 ^ 2]", RewritePowerAsProduct, "0.3 * 0.3")
        testRule("[(x + 1) ^ 2]", RewritePowerAsProduct, "(x + 1) (x + 1)")
        testRule("[x^5]", RewritePowerAsProduct, "x * x * x * x * x")
        testRule("[x^6]", RewritePowerAsProduct, null)
        testRule("[x^1]", RewritePowerAsProduct, null)
        testRule("[x^0]", RewritePowerAsProduct, null)
        testRule("[([1/2])^4]", RewritePowerAsProduct, "[1/2] * [1/2] * [1/2] * [1/2]")
    }

    @Test
    fun testEvaluateProductDividedByZeroAsUndefined() {
        testRule("3 * 5 : 0", EvaluateProductDividedByZeroAsUndefined, "UNDEFINED")
        testRule("7 : 0.00", EvaluateProductDividedByZeroAsUndefined, "UNDEFINED")
        testRule("x : 0 * y", EvaluateProductDividedByZeroAsUndefined, "UNDEFINED")
    }

    @Test
    fun testCancelAdditiveInverseElements() {
        testRule("sqrt[12] - sqrt[12] + 1", CancelAdditiveInverseElements, "1")
        testRule(
            "(sqrt[2] + root[3, 3])  + 1 - (sqrt[2] + root[3, 3]) + 2",
            CancelAdditiveInverseElements,
            "1 + 2"
        )
        testRule("sqrt[12] - sqrt[12]", CancelAdditiveInverseElements, "0")
        testRule("-sqrt[12] + sqrt[12]", CancelAdditiveInverseElements, "0")
        testRule("(x + 1 - y) - (x + 1 - y)", CancelAdditiveInverseElements, "0")
    }

    @Test
    fun testSimplifyExpressionToThePowerOfOne() {
        testRule("[(sqrt[2] + 1) ^ 1]", SimplifyExpressionToThePowerOfOne, "sqrt[2] + 1")
        testRule("[2 ^ 1]", SimplifyExpressionToThePowerOfOne, "2")
    }

    @Test
    fun testEvaluateOneToAnyPower() {
        testRule("[1 ^ sqrt[2] + 1]", EvaluateOneToAnyPower, "1")
        testRule("[1 ^ [1 / 1 - 1]]", EvaluateOneToAnyPower, null)
    }

    @Test
    fun testEvaluateExpressionToThePowerOfZero() {
        testRule("[(sqrt[2] + 1) ^ 0]", EvaluateExpressionToThePowerOfZero, "1")
        testRule("[(1 - 1) ^ 0]", EvaluateExpressionToThePowerOfZero, null)
    }

    @Test
    fun testEvaluateZeroToAPositivePower() {
        testRule("[0 ^ [3 / 2]]", EvaluateZeroToAPositivePower, "0")
        testRule("[0 ^ sqrt[3] - sqrt[5]]", EvaluateZeroToAPositivePower, null)
    }

    @Test
    fun testRewriteProductOfPowersWithSameBase() {
        testRule("[x^2]*[y^2]", RewriteProductOfPowersWithSameBase, null)
        testRule("[x^2]*[x^3]", RewriteProductOfPowersWithSameBase, "[x ^ 2 + 3]")
        testRule("x*[3^4]*[3^-9]", RewriteProductOfPowersWithSameBase, "x*[3 ^ 4 - 9]")
        testRule("y*[3^[1 / 2]]*z*[3^[2 / 3]]", RewriteProductOfPowersWithSameBase, "y*[3 ^ [1 / 2] + [2 / 3]]z")
    }

    @Test
    fun testRewriteProductOfPowersWithSameExponent() {
        testRule("[x^2]*[x^3]", RewriteProductOfPowersWithSameExponent, null)
        testRule("[x^2]*[y^2]", RewriteProductOfPowersWithSameExponent, "[(x y) ^ 2]")
        testRule("x*[3^4]*[2^4]", RewriteProductOfPowersWithSameExponent, "x*[(3 * 2) ^ 4]")
        testRule("y*[3^[2 / 3]]*z*[4^[2 / 3]]", RewriteProductOfPowersWithSameExponent, "y*[(3 * 4) ^ [2 / 3]]*z")
    }

    @Test
    fun testRewriteFractionOfPowersWithSameBase() {
        testRule("[[x^2] / [y^2]]", RewriteFractionOfPowersWithSameBase, null)
        testRule("[[x^2] / [x^3]]", RewriteFractionOfPowersWithSameBase, "[x ^ 2 - 3]")
        testRule("[[3^4] / [3^-9]]", RewriteFractionOfPowersWithSameBase, "[3 ^ 4 -(- 9)]")
        testRule("[[3^[1 / 2]] / [3^[2 / 3]]]", RewriteFractionOfPowersWithSameBase, "[3 ^ [1 / 2] - [2 / 3]]")
    }

    @Test
    fun testRewriteFractionOfPowersWithSameExponent() {
        testRule("[[x^2] / [x^3]]", RewriteFractionOfPowersWithSameExponent, null)
        testRule("[[x^2] / [y^2]]", RewriteFractionOfPowersWithSameExponent, "[([x / y]) ^ 2]")
        testRule("[[3^4] / [2^4]]", RewriteFractionOfPowersWithSameExponent, "[([3 / 2]) ^ 4]")
        testRule("[[3^[2 / 3]] / [4^[2 / 3]]]", RewriteFractionOfPowersWithSameExponent, "[([3 / 4]) ^ [2 / 3]]")
    }

    @Test
    fun testSimplifyRootOfPower() {
        testRule("root[ [7^6], 8]", GeneralRules.RewritePowerUnderRoot, "root[ [7^3*2], 4*2]")
        testRule("root[ [7^4], 8]", GeneralRules.RewritePowerUnderRoot, "root[ [7^4], 2*4]")
    }

    @Test
    fun testFlipFractionUnderNegativePower() {
        testRule("[([2 / 3]) ^ [4 / 5]]", FlipFractionUnderNegativePower, null)
        testRule("[([2 / 3]) ^ -[4 / 5]]", FlipFractionUnderNegativePower, "[([3 / 2]) ^ [4 / 5]]")
    }

    @Test
    fun testRewriteProductOfPowersWithNegatedExponent() {
        testRule(
            "[2 ^ [1 / 2]] * [3 ^ -[1 / 3]]",
            RewriteProductOfPowersWithNegatedExponent,
            null
        )
        testRule(
            "[2 ^ [1 / 2]] * [([4 / 3]) ^ -[1 / 2]]",
            RewriteProductOfPowersWithNegatedExponent,
            "[2 ^ [1 / 2]] * [([3 / 4]) ^ [1 / 2]]"
        )
        testRule(
            "[2 ^ [1 / 2]] * [3 ^ -[1 / 2]]",
            RewriteProductOfPowersWithNegatedExponent,
            "[2 ^ [1 / 2]] * [([1 / 3]) ^ [1 / 2]]"
        )
        testRule(
            "[3 ^ -[1 / 2]] * [2 ^ [1 / 2]]",
            RewriteProductOfPowersWithNegatedExponent,
            "[([1 / 3]) ^ [1 / 2]] * [2 ^ [1 / 2]]"
        )
        testRule(
            "[2 ^ [1 / 2]] * [([2 / 3]) ^ -[1 / 2]]",
            RewriteProductOfPowersWithNegatedExponent,
            "[2 ^ [1 / 2]] * [([3 / 2]) ^ [1 / 2]]"
        )
        testRule(
            "[([2 / 3]) ^ -[1 / 2]] * [2 ^ [1 / 2]]",
            RewriteProductOfPowersWithNegatedExponent,
            "[([3 / 2]) ^ [1 / 2]] * [2 ^ [1 / 2]]"
        )
    }

    @Test
    fun testRewriteProductOfPowersWithInverseFractionBase() {
        testRule(
            "[([2 / 3]) ^ [1 / 2]] * [([2 / 3]) ^ [2 / 5]]",
            RewriteProductOfPowersWithInverseFractionBase,
            null
        )
        testRule(
            "[([2 / 3]) ^ [1 / 2]] * [([3 / 2]) ^ [2 / 5]]",
            RewriteProductOfPowersWithInverseFractionBase,
            "[([2 / 3]) ^ [1 / 2]] * [([2 / 3]) ^ -[2 / 5]]"
        )
    }

    @Test
    fun testRewriteProductOfPowersWithInverseBase() {
        testRule(
            "[2 ^ [1 / 2]] * [([1 / 3]) ^ [2 / 5]]",
            RewriteProductOfPowersWithInverseBase,
            null
        )
        testRule(
            "[2 ^ [1 / 2]] * [([1 / 2]) ^ [2 / 5]]",
            RewriteProductOfPowersWithInverseBase,
            "[2 ^ [1 / 2]] * [2 ^ -[2 / 5]]"
        )
        testRule(
            "[([1 / 2]) ^ [2 / 5]] * [2 ^ [1 / 2]]",
            RewriteProductOfPowersWithInverseBase,
            "[2 ^ -[2 / 5]] * [2 ^ [1 / 2]]"
        )
    }

    @Test
    fun testRewriteIntegerOrderRootAsPower() {
        testRule("root[5, 3]", GeneralRules.RewriteIntegerOrderRootAsPower, "[5 ^ [1/3]]")
        testRule("sqrt[x + 2]", GeneralRules.RewriteIntegerOrderRootAsPower, "[(x + 2) ^ [1/2]]")
    }

    fun testCancelCommonPowers() {
        testRule(
            "root[ [7^2*2], 3*2 ]",
            GeneralRules.CancelRootIndexAndExponent,
            "root[ [7^2], 3]"
        )
        testRule(
            "root[[7^2], 3*2]",
            GeneralRules.CancelRootIndexAndExponent,
            "root[7, 3]"
        )
    }
}
