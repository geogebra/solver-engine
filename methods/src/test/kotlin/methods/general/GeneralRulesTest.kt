package methods.general

import engine.methods.testMethod
import engine.methods.testRule
import methods.general.GeneralRules.CancelAdditiveInverseElements
import methods.general.GeneralRules.DistributePowerOfProduct
import methods.general.GeneralRules.DistributeSumOfPowers
import methods.general.GeneralRules.EliminateOneInProduct
import methods.general.GeneralRules.EliminateZeroInSum
import methods.general.GeneralRules.EvaluateExpressionToThePowerOfZero
import methods.general.GeneralRules.EvaluateOneToAnyPower
import methods.general.GeneralRules.EvaluateProductContainingZero
import methods.general.GeneralRules.EvaluateProductDividedByZeroAsUndefined
import methods.general.GeneralRules.EvaluateZeroDividedByAnyValue
import methods.general.GeneralRules.EvaluateZeroToAPositivePower
import methods.general.GeneralRules.FlipFractionUnderNegativePower
import methods.general.GeneralRules.MoveSignOfNegativeFactorOutOfProduct
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
import methods.general.GeneralRules.SimplifyUnitFractionToOne
import methods.general.GeneralRules.SimplifyZeroDenominatorFractionToUndefined
import methods.general.GeneralRules.SimplifyZeroNumeratorFractionToZero
import org.junit.jupiter.api.Test
import engine.methods.SerializedGmAction as GmAction

@Suppress("LargeClass")
class GeneralRulesTest {

    @Test
    fun testEliminateOneInProduct() {
        testRule("3*x*1*y*4", EliminateOneInProduct, "3xy*4", GmAction("Tap", "./2"))
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
        testRule(
            "(-2) * (-3)",
            SimplifyProductWithTwoNegativeFactors,
            "2 * 3",
            GmAction("Drag", "./1/0:op", "./0/0:op"),
        )
        testRule(
            "-3*(-a)",
            SimplifyProductWithTwoNegativeFactors,
            "3 a",
            GmAction("Drag", "./0/1/0:op", ".:op"),
        )
        testRule("(-x) * y * (-12) * 5", SimplifyProductWithTwoNegativeFactors, "x y * 12 * 5")
        testRule("-x * y * (-12) * 5", SimplifyProductWithTwoNegativeFactors, "x y * 12 * 5")
        testRule("(-2):(-3)", SimplifyProductWithTwoNegativeFactors, "2:3")
        testRule("-2:(-3)", SimplifyProductWithTwoNegativeFactors, "2:3")
        testRule("-2:-3", SimplifyProductWithTwoNegativeFactors, "2:3")
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
        testRule("1 - 0", EliminateZeroInSum, "1")
        testRule("-0 - x + y", EliminateZeroInSum, "-x + y")
        testRule("z +/- 0", EliminateZeroInSum, "z")
        testRule("+/-0 + 1 + 2", EliminateZeroInSum, "1 + 2")

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
    fun testSimplifyUnitFractionToOne() {
        testRule("[sqrt[2] + sqrt[3] / sqrt[2] + sqrt[3]]", SimplifyUnitFractionToOne, "1")
        testRule("[2 - 2 / 2 - 2]", SimplifyUnitFractionToOne, null)
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
    fun testDistributePowerOfProduct() {
        testMethod {
            method = DistributePowerOfProduct
            inputExpr = "[(2 * x * y) ^ 5]"

            check {
                toExpr = "[2 ^ 5] [x ^ 5] [y ^ 5]"

                distribute {
                    fromPaths("./1")
                    toPaths("./0/1", "./1/1", "./2/1")
                }
                move("./0/0", "./0/0")
                move("./0/1", "./1/0")
                move("./0/2", "./2/0")
            }
        }
        testRule(
            "[(sqrt[3] * root[5, 2]) ^ n]",
            DistributePowerOfProduct,
            "[(sqrt[3]) ^ n] * [(root[5, 2]) ^ n]",
        )
        testRule(
            "[([(sqrt[2]) ^ 3] * 9 [a ^ 2]) ^ [1 / 2]]",
            DistributePowerOfProduct,
            "[([(sqrt[2]) ^ 3]) ^ [1 / 2]] * [9 ^ [1 / 2]] [([a ^ 2]) ^ [1 / 2]]",
        )
    }

    @Test
    fun testDistributeSumOfPowers() {
        testMethod {
            method = DistributeSumOfPowers
            inputExpr = "[2 ^ a + b + c]"

            check {
                toExpr = "[2^a] * [2^b] * [2^c]"

                distribute {
                    fromPaths("./0")
                    toPaths("./0/0", "./1/0", "./2/0")
                }
                move("./1/0", "./0/1")
                move("./1/1", "./1/1")
                move("./1/2", "./2/1")
            }
        }
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
            "(4[x^2] - 3) (4[x^2] - 3) (4[x^2] - 3)",
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
            "1 + 2",
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
            null,
        )
        testRule(
            "[2 ^ [1 / 2]] * [([4 / 3]) ^ -[1 / 2]]",
            RewriteProductOfPowersWithNegatedExponent,
            "[2 ^ [1 / 2]] * [([3 / 4]) ^ [1 / 2]]",
        )
        testRule(
            "[2 ^ [1 / 2]] * [3 ^ -[1 / 2]]",
            RewriteProductOfPowersWithNegatedExponent,
            "[2 ^ [1 / 2]] * [([1 / 3]) ^ [1 / 2]]",
        )
        testRule(
            "[3 ^ -[1 / 2]] * [2 ^ [1 / 2]]",
            RewriteProductOfPowersWithNegatedExponent,
            "[([1 / 3]) ^ [1 / 2]] * [2 ^ [1 / 2]]",
        )
        testRule(
            "[2 ^ [1 / 2]] * [([2 / 3]) ^ -[1 / 2]]",
            RewriteProductOfPowersWithNegatedExponent,
            "[2 ^ [1 / 2]] * [([3 / 2]) ^ [1 / 2]]",
        )
        testRule(
            "[([2 / 3]) ^ -[1 / 2]] * [2 ^ [1 / 2]]",
            RewriteProductOfPowersWithNegatedExponent,
            "[([3 / 2]) ^ [1 / 2]] * [2 ^ [1 / 2]]",
        )
    }

    @Test
    fun testRewriteProductOfPowersWithInverseFractionBase() {
        testRule(
            "[([2 / 3]) ^ [1 / 2]] * [([2 / 3]) ^ [2 / 5]]",
            RewriteProductOfPowersWithInverseFractionBase,
            null,
        )
        testRule(
            "[([2 / 3]) ^ [1 / 2]] * [([3 / 2]) ^ [2 / 5]]",
            RewriteProductOfPowersWithInverseFractionBase,
            "[([2 / 3]) ^ [1 / 2]] * [([2 / 3]) ^ -[2 / 5]]",
        )
    }

    @Test
    fun testRewriteProductOfPowersWithInverseBase() {
        testRule(
            "[2 ^ [1 / 2]] * [([1 / 3]) ^ [2 / 5]]",
            RewriteProductOfPowersWithInverseBase,
            null,
        )
        testRule(
            "[2 ^ [1 / 2]] * [([1 / 2]) ^ [2 / 5]]",
            RewriteProductOfPowersWithInverseBase,
            "[2 ^ [1 / 2]] * [2 ^ -[2 / 5]]",
        )
        testRule(
            "[([1 / 2]) ^ [2 / 5]] * [2 ^ [1 / 2]]",
            RewriteProductOfPowersWithInverseBase,
            "[2 ^ -[2 / 5]] * [2 ^ [1 / 2]]",
        )
    }

    @Test
    fun testRewriteOddRootOfNegative() {
        testRule("root[-8, 3]", GeneralRules.RewriteOddRootOfNegative, "-root[8, 3]")
        testRule("root[8, 3]", GeneralRules.RewriteOddRootOfNegative, null)
        testRule("root[-8, 4]", GeneralRules.RewriteOddRootOfNegative, null)
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
            "root[[7 ^ 2], 3]",
        )
        testRule(
            "root[[(-7) ^ 5 * 2], 3 * 2]",
            GeneralRules.CancelRootIndexAndExponent,
            null,
        )
        testRule(
            "root[[(-7) ^ 2 * 3], 4 * 3]",
            GeneralRules.CancelRootIndexAndExponent,
            "root[[(-7) ^ 2], 4]",
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
            "[7 ^ 2]",
        )
        testRule(
            "root[[7 ^ 2], 2]",
            GeneralRules.CancelRootIndexAndExponent,
            "7",
        )
    }

    @Test
    fun testNormaliseProduct() {
        testRule(
            "(x + 1)*5",
            NormalizationRules.NormaliseSimplifiedProductRule,
            "5(x+1)",
        )
        testRule(
            "(x + 1)*5x",
            NormalizationRules.NormaliseSimplifiedProductRule,
            "5x(x+1)",
        )
        testRule(
            "5*(x+1)*sqrt[2]",
            NormalizationRules.NormaliseSimplifiedProductRule,
            "5 sqrt[2] (x+1)",
        )
        testRule(
            "5x * sqrt[2]",
            NormalizationRules.NormaliseSimplifiedProductRule,
            "5 sqrt[2] * x",
        )
        testRule(
            "5(1 + sqrt[2])*sqrt[3]",
            NormalizationRules.NormaliseSimplifiedProductRule,
            "5 sqrt[3] (1 + sqrt[2])",
        )
        testRule(
            "sqrt[3] * (1 + sqrt[2]) * 5",
            NormalizationRules.NormaliseSimplifiedProductRule,
            "5 sqrt[3] (1 + sqrt[2])",
        )
        testRule(
            "2*sqrt[2]",
            NormalizationRules.NormaliseSimplifiedProductRule,
            "2 sqrt[2]",
        )
    }
}
