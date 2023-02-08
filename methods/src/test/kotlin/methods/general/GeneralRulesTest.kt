package methods.general

import engine.methods.testMethod
import engine.methods.testRule
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
import methods.general.GeneralRules.ExpandBinomialCubedUsingIdentity
import methods.general.GeneralRules.ExpandBinomialSquaredUsingIdentity
import methods.general.GeneralRules.ExpandProductOfSumAndDifference
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
import org.junit.jupiter.api.Test

class GeneralRulesTest {

    @Test
    fun testEliminateOneInProduct() {
        testRule("3*x*1*y*4", EliminateOneInProduct, "3xy*4")
        testRule("3*x*1*y*4", EliminateOneInProduct, "3xy*4")
        testRule("3*1*4", EliminateOneInProduct, "3*4")
        testRule("1*x", EliminateOneInProduct, "x")

        testMethod {
            method = EliminateOneInProduct
            inputExpr = "3x*1*y"

            check {
                toExpr = "3xy"

                shift("./0/0", "./0")
                shift("./0/1", "./1")
                cancel("./1")
                shift("./2", "./2")
            }
        }
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

        testMethod {
            method = EliminateZeroInSum
            inputExpr = "x + y + 0"

            check {
                toExpr = "x + y"

                keep("./0", "./1")
                cancel("./2")
            }
        }
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
            ExpandBinomialSquaredUsingIdentity,
            "[a ^ 2] + 2ab + [b ^ 2]"
        )
        testRule(
            "[(sqrt[2] + 1) ^ 2]",
            ExpandBinomialSquaredUsingIdentity,
            "[(sqrt[2]) ^ 2] + 2 sqrt[2] * 1 + [1 ^ 2]"
        )
        testRule(
            "[(x - y) ^ 2]",
            ExpandBinomialSquaredUsingIdentity,
            "[x ^ 2] + 2 x (-y) + [(-y) ^ 2]"
        )
        testRule(
            "[(2x - 3)^2]",
            ExpandBinomialSquaredUsingIdentity,
            "[(2 x) ^ 2] + 2 * 2 x * (-3) + [(-3) ^ 2]"
        )
    }

    @Test
    fun testExpandBinomialCubed() {
        testRule(
            "[(a + b) ^ 3]",
            ExpandBinomialCubedUsingIdentity,
            "[a^3] + 3[a^2]b + 3a[b^2] + [b^3]"
        )
        testRule(
            "[(a - b)^3]",
            ExpandBinomialCubedUsingIdentity,
            "[a^3] + 3[a^2](-b) + 3a * [(-b)^2] + [(-b)^3]"
        )
        testRule(
            "[(2x - 4) ^ 3]",
            ExpandBinomialCubedUsingIdentity,
            "[(2 x) ^ 3] + 3 * [(2 x) ^ 2] * (-4) + 3 * 2 x * [(-4) ^ 2] + [(-4) ^ 3]"
        )
        testRule(
            "[(-1 + 2x) ^ 3]",
            ExpandBinomialCubedUsingIdentity,
            "[(-1) ^ 3] + 3 * [(-1) ^ 2] * 2 x + 3 * (-1) [(2 x) ^ 2] + [(2 x) ^ 3]"
        )
    }

    @Test
    fun testIdentityProductOfSumAndDifference() {
        testRule(
            "(1 + sqrt[2]) (1 - sqrt[2])",
            ExpandProductOfSumAndDifference,
            "[1^2] - [(sqrt[2]) ^ 2]"
        )
        testRule(
            "(1 - sqrt[2]) (1 + sqrt[2])",
            ExpandProductOfSumAndDifference,
            "[1^2] - [(sqrt[2]) ^ 2]"
        )
        testRule(
            "(sqrt[2] - 1) (1 + sqrt[2])",
            ExpandProductOfSumAndDifference,
            "[(sqrt[2]) ^ 2] - [1^2]"
        )
        testRule(
            "(-1 + sqrt[2]) (sqrt[2] + 1)",
            ExpandProductOfSumAndDifference,
            "[(sqrt[2]) ^ 2] - [1^2]"
        )
        testRule(
            "(1 + sqrt[2])*(1 - sqrt[2])",
            ExpandProductOfSumAndDifference,
            "[1 ^ 2] - [(sqrt[2]) ^ 2]"
        )
        testRule(
            "(2x - 3) (2x + 3)",
            ExpandProductOfSumAndDifference,
            "[(2x)^2] - [3^2]"
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
            "sqrt[2] * (3 - sqrt[4])",
            DistributeMultiplicationOverSum,
            "sqrt[2] * 3 + sqrt[2] * (-sqrt[4])"
        )
        testRule(
            "sqrt[2] * (3 + sqrt[4] + sqrt[5])",
            DistributeMultiplicationOverSum,
            "sqrt[2] * 3 + sqrt[2] * sqrt[4] + sqrt[2] * sqrt[5]"
        )
        testRule(
            "-2 * (3 + sqrt[5])",
            DistributeMultiplicationOverSum,
            "(-2) * 3 + (-2) * sqrt[5]"
        )
        testRule(
            "-sqrt[2] * (3 + sqrt[5])",
            DistributeMultiplicationOverSum,
            "(-sqrt[2]) * 3 + (-sqrt[2]) * sqrt[5]"
        )
        testRule(
            "-2 * (-3 - sqrt[5])",
            DistributeMultiplicationOverSum,
            "(-2) *( -3) + (-2) * (-sqrt[5])"
        )
        testRule(
            "2 (4x - 3)",
            DistributeMultiplicationOverSum,
            "2*4x + 2*(-3)"
        )
        testRule(
            "2*sqrt[2]*(1 + sqrt[3])",
            DistributeMultiplicationOverSum,
            "2 sqrt[2]*1 + 2 sqrt[2]*sqrt[3]"
        )
        testRule(
            "3 sqrt[2]*[x^2] * (2x - 7)",
            DistributeMultiplicationOverSum,
            "3 sqrt[2]*[x^2]*2x + 3 sqrt[2] * [x^2]*(-7)"
        )
        testRule(
            "x*(1 + sqrt[3])",
            DistributeMultiplicationOverSum,
            null
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
        testRule(
            "[(4[x^2] - 3) ^ 3]",
            RewritePowerAsProduct,
            "(4[x^2] - 3) (4[x^2] - 3) (4[x^2] - 3)"
        )
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

        testMethod {
            method = CancelAdditiveInverseElements
            inputExpr = "2 + 1 - 2"

            check {
                toExpr = "1"

                shift("./1", ".")
                cancel("./0", "./2/0")
            }
        }
    }

    @Test
    fun testSimplifyExpressionToThePowerOfOne() {
        testRule("[(sqrt[2] + 1) ^ 1]", SimplifyExpressionToThePowerOfOne, "sqrt[2] + 1")
        testRule("[2 ^ 1]", SimplifyExpressionToThePowerOfOne, "2")
        testMethod {
            method = SimplifyExpressionToThePowerOfOne
            inputExpr = "[x ^ 1]"

            check {
                toExpr = "x"

                shift("./0", ".")
                cancel("./1")
            }
        }
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

    @Test
    fun testCancelCommonPowers() {
        testRule(
            "root[[7 ^ 2 * 2], 3 * 2]",
            GeneralRules.CancelRootIndexAndExponent,
            "root[[7 ^ 2], 3]"
        )
        testMethod {
            method = GeneralRules.CancelRootIndexAndExponent
            inputExpr = "root[[7 ^ 2], 3 * 2]"

            check {
                toExpr = "root[7, 3]"

                shift("./0/0", "./0")
                shift("./1/0", "./1")
                cancel("./0/1", "./1/1")
            }
        }
        testRule(
            "root[[7 ^ 2 * 2], 2]",
            GeneralRules.CancelRootIndexAndExponent,
            "[7 ^ 2]"
        )
        testRule(
            "root[[7 ^ 2], 2]",
            GeneralRules.CancelRootIndexAndExponent,
            "7"
        )
    }

    @Test
    fun testApplyFoilMethod() {
        testRule(
            "(1 + sqrt[2]) * (1 + sqrt[2])",
            GeneralRules.ApplyFoilMethod,
            "1*1 + 1 sqrt[2] + sqrt[2]*1 + sqrt[2]*sqrt[2]"
        )
        testRule(
            "(4x - 3) * (4x - 3)",
            GeneralRules.ApplyFoilMethod,
            "4x * 4x + 4x * (-3) + (-3) * 4x + (-3) * (-3)"
        )
        testRule(
            "(4x - 5[x^3]) * (2[x^2] - 3x)",
            GeneralRules.ApplyFoilMethod,
            "4 x * 2 [x ^ 2] + 4 x (-3 x) + (-5 [x ^ 3]) * 2 [x ^ 2] + (-5 [x ^ 3]) (-3 x)"
        )
        testRule(
            "(2x - 3) * (3x + 3)",
            GeneralRules.ApplyFoilMethod,
            "2 x * 3 x + 2 x * 3 + (-3) * 3 x + (-3) * 3"
        )
        testRule(
            "(x + [x^2]) (5x + [x^2])",
            GeneralRules.ApplyFoilMethod,
            "x * 5x + x * [x^2] + [x^2] * 5x + [x^2] * [x^2]"
        )
        testRule(
            "(2x - 3) (2x - 3)",
            GeneralRules.ApplyFoilMethod,
            "2x * 2x + 2x * (-3) + (-3) * 2x + (-3) * (-3)"
        )
    }

    @Test
    fun testDistributeParentheses() {
        testRule(
            "([x^2] + 5x - 2) * (3x - 5)",
            GeneralRules.ExpandDoubleBrackets,
            "[x^2]*3x + [x^2]*(-5) + 5x * 3x + 5x * (-5) + (-2)*3x + (-2)*(-5)"
        )
    }

    @Test
    fun testExpandTrinomialSquared() {
        testRule(
            "[(1 + sqrt[2] + sqrt[3])^2]",
            GeneralRules.ExpandTrinomialSquaredUsingIdentity,
            "[1 ^ 2] + [(sqrt[2]) ^ 2] + [(sqrt[3]) ^ 2] + " +
                "2 * 1 sqrt[2] + 2 sqrt[2] * sqrt[3] + 2 sqrt[3] * 1"
        )
        testRule(
            "[(1 - sqrt[2] - sqrt[3])^2]",
            GeneralRules.ExpandTrinomialSquaredUsingIdentity,
            "[1 ^ 2] + [(-sqrt[2]) ^ 2] + [(-sqrt[3]) ^ 2] + " +
                "2 * 1 (-sqrt[2]) + 2 (-sqrt[2]) (-sqrt[3]) + 2 (-sqrt[3]) * 1"
        )
        testRule(
            "[(1 - x - y)^2]",
            GeneralRules.ExpandTrinomialSquaredUsingIdentity,
            "[1 ^ 2] + [(-x) ^ 2] + [(-y) ^ 2] + 2 * 1 (-x) + 2 (-x) (-y) + 2 (-y) * 1"
        )
    }

    @Test
    fun testDistributeNegativeOverBracket() {
        testRule(
            "-(sqrt[2]+7)",
            GeneralRules.DistributeNegativeOverBracket,
            "-sqrt[2] - 7"
        )
        testRule(
            "5 - (sqrt[2] + 7)",
            GeneralRules.DistributeNegativeOverBracket,
            "5 - sqrt[2] - 7"
        )
        testRule(
            "5 - (sqrt[2] - 7)",
            GeneralRules.DistributeNegativeOverBracket,
            "5 - sqrt[2] + 7"
        )
        testRule(
            "5 - (-sqrt[2] + 7)",
            GeneralRules.DistributeNegativeOverBracket,
            "5 + sqrt[2] - 7"
        )
        testRule(
            "5 - (-sqrt[2] - 7)",
            GeneralRules.DistributeNegativeOverBracket,
            "5 + sqrt[2] + 7"
        )
        testRule(
            "-(-a - 2)",
            GeneralRules.DistributeNegativeOverBracket,
            "a + 2"
        )
        testRule(
            "sqrt[2] - (-a + 2)",
            GeneralRules.DistributeNegativeOverBracket,
            "sqrt[2] + a - 2"
        )
        testRule(
            "sqrt[2] - (a - 2)",
            GeneralRules.DistributeNegativeOverBracket,
            "sqrt[2] - a + 2"
        )
        testRule(
            "sqrt[2] - (-5a - 7)",
            GeneralRules.DistributeNegativeOverBracket,
            "sqrt[2] + 5a + 7"
        )
    }

    @Test
    fun testNormaliseProduct() {
        testRule(
            "(x + 1)*5",
            NormalizationRules.NormaliseSimplifiedProduct,
            "5(x+1)"
        )
        testRule(
            "(x + 1)*5x",
            NormalizationRules.NormaliseSimplifiedProduct,
            "5x(x+1)"
        )
        testRule(
            "5*(x+1)*sqrt[2]",
            NormalizationRules.NormaliseSimplifiedProduct,
            "5 sqrt[2] (x+1)"
        )
        testRule(
            "5x * sqrt[2]",
            NormalizationRules.NormaliseSimplifiedProduct,
            "5 sqrt[2] * x"
        )
        testRule(
            "5(1 + sqrt[2])*sqrt[3]",
            NormalizationRules.NormaliseSimplifiedProduct,
            "5 sqrt[3] (1 + sqrt[2])"
        )
        testRule(
            "sqrt[3] * (1 + sqrt[2]) * 5",
            NormalizationRules.NormaliseSimplifiedProduct,
            "5 sqrt[3] (1 + sqrt[2])"
        )
        testRule(
            "2*sqrt[2]",
            NormalizationRules.NormaliseSimplifiedProduct,
            "2 sqrt[2]"
        )
    }
}
