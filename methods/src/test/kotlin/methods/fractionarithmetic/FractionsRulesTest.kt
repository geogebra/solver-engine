package methods.fractionarithmetic

import engine.methods.testMethod
import engine.methods.testRule
import engine.methods.testRuleInX
import methods.fractionarithmetic.FractionArithmeticRules.AddLikeFractions
import methods.fractionarithmetic.FractionArithmeticRules.BringToCommonDenominator
import methods.fractionarithmetic.FractionArithmeticRules.BringToCommonDenominatorWithNonFractionalTerm
import methods.fractionarithmetic.FractionArithmeticRules.CancelCommonFactorInFraction
import methods.fractionarithmetic.FractionArithmeticRules.ConvertIntegerToFraction
import methods.fractionarithmetic.FractionArithmeticRules.DistributeFractionalPowerOverFraction
import methods.fractionarithmetic.FractionArithmeticRules.DistributePositiveIntegerPowerOverFraction
import methods.fractionarithmetic.FractionArithmeticRules.FindCommonIntegerFactorInFraction
import methods.fractionarithmetic.FractionArithmeticRules.MultiplyFractions
import methods.fractionarithmetic.FractionArithmeticRules.RewriteDivisionAsFraction
import methods.fractionarithmetic.FractionArithmeticRules.RewriteDivisionAsMultiplicationByReciprocal
import methods.fractionarithmetic.FractionArithmeticRules.SimplifyFractionToInteger
import methods.fractionarithmetic.FractionArithmeticRules.SimplifyFractionToMinusOne
import methods.fractionarithmetic.FractionArithmeticRules.SimplifyFractionWithFractionDenominator
import methods.fractionarithmetic.FractionArithmeticRules.SimplifyFractionWithFractionNumerator
import methods.fractionarithmetic.FractionArithmeticRules.SimplifyNegativeInDenominator
import methods.fractionarithmetic.FractionArithmeticRules.SimplifyNegativeInNumerator
import methods.fractionarithmetic.FractionArithmeticRules.SimplifyNegativeNumeratorAndDenominator
import methods.fractionarithmetic.FractionArithmeticRules.TurnFactorIntoFractionInProduct
import methods.fractionarithmetic.FractionArithmeticRules.TurnIntegerToMinusOneToFraction
import methods.fractionarithmetic.FractionArithmeticRules.TurnNegativePowerOfIntegerToFraction
import methods.fractionarithmetic.FractionArithmeticRules.TurnNegativePowerOfZeroToPowerOfFraction
import org.junit.jupiter.api.Test

class FractionsRulesTest {

    @Test
    fun testRewriteDivisionByFractionAsProduct() {
        testRule("3 : [2 / 4a]", RewriteDivisionAsMultiplicationByReciprocal, "3 * [4a / 2]")
        testRule("[2 / 4a] : 3", RewriteDivisionAsMultiplicationByReciprocal, "[2 / 4a] * [1 / 3]")
        testRule("5 : 2", RewriteDivisionAsMultiplicationByReciprocal, null)
        testRule("5 : (1 + [2 / 3])", RewriteDivisionAsMultiplicationByReciprocal, null)
    }

    @Test
    fun testRewriteDivisionAsFraction() {
        testRule("1:8", RewriteDivisionAsFraction, "[1 / 8]")
    }

    @Test
    fun testConvertIntegerToFraction() {
        testRule("5 + [2/4]", ConvertIntegerToFraction, "[5/1] + [2/4]")
        testRule("[2/4] + 5", ConvertIntegerToFraction, "[2/4] + [5/1]")
    }

    @Test
    fun testAddLikeFractions() {
        testRule("[2/10] + [3/10]", AddLikeFractions, "[2+3/10]")
        testRule("1+[2/10]-x", AddLikeFractions, null)
        testRule("[3/10] - [2/10]", AddLikeFractions, "[3 - 2 / 10]")
        testRule("-[3/10] - [2/10]", AddLikeFractions, "[-3 - 2 / 10]")
        testRule("1 - [2/10]", AddLikeFractions, null)
        testMethod {
            method = AddLikeFractions
            inputExpr = "[7/10] - [3/10]"

            check {
                toExpr = "[7 - 3/10]"
                explanation {
                    key = Explanation.SubtractLikeFractions
                    param { expr = "[7/10]" }
                    param { expr = "[3/10]" }
                }
            }
        }
        testMethod {
            method = AddLikeFractions
            inputExpr = "-[7/10] - [3/10]"

            check {
                toExpr = "[-7 - 3/10]"
                explanation {
                    key = Explanation.AddLikeFractions
                    param { expr = "-[7/10]" }
                    param { expr = "-[3/10]" }
                }
            }
        }
    }

    @Test
    fun testBringToCommonDenominator() {
        testRule("[3/8] + [5/12]", BringToCommonDenominator, "[3 * 3/8 * 3] + [5 * 2/12 * 2]")
        testRule("[3/8] - [5/12]", BringToCommonDenominator, "[3 * 3/8 * 3] - [5 * 2/12 * 2]")
        testRule("-[3/8] + [5/12]", BringToCommonDenominator, "-[3 * 3/8 * 3] + [5 * 2/12 * 2]")
        testRule("[1/2] + [1/4]", BringToCommonDenominator, "[1 * 2/2 * 2] + [1/4]")
        testRule("[3/10] - [1/2]", BringToCommonDenominator, "[3/10] - [1 * 5/2 * 5]")
    }

    @Test
    fun testSimplifyNegativeInDenominator() {
        testRule("[4/-5]", SimplifyNegativeInDenominator, "-[4/5]")
        testRule("[4/5]", SimplifyNegativeInDenominator, null)
    }

    @Test
    fun testSimplifyFractionToInteger() {
        testRule("[10/1]", SimplifyFractionToInteger, "10")
        testRule("[8/4]", SimplifyFractionToInteger, "2")
        testRule("[9/5]", SimplifyFractionToInteger, null)
        testRule("[10/0]", SimplifyFractionToInteger, null)
    }

    @Test
    fun testFindCommonFactorInFraction() {
        testRule("[6/10]", FindCommonIntegerFactorInFraction, "[2 * 3/2 * 5]")
        testRule("[5/7]", FindCommonIntegerFactorInFraction, null)
        testRule("[700/500]", FindCommonIntegerFactorInFraction, "[100 * 7/100 * 5]")
        testRule("[1/10]", FindCommonIntegerFactorInFraction, null)
    }

    @Test
    fun testSimplifyCommonFactorInFraction() {
        testMethod {
            method = CancelCommonFactorInFraction
            inputExpr = "[4 * 3 / 3 * 5]"

            check {
                shift("./0/0", "./0")
                shift("./1/1", "./1")
                cancel("./0/1", "./1/0")
            }
        }

        testRule("[x y / a y]", CancelCommonFactorInFraction, "[x / a]")
        testRule("[x / 2x]", CancelCommonFactorInFraction, "[1 / 2]")
        testRule("[x y z / a y c]", CancelCommonFactorInFraction, "[x z / a c]")
        testRule("[3x / 2[x^2]]", CancelCommonFactorInFraction, "[3 / 2x]")
    }

    @Test
    fun testSimplifyNegativeInNumerator() {
        testRule("[-2/3]", SimplifyNegativeInNumerator, "-[2/3]")
        testRule("[(-2)/3]", SimplifyNegativeInNumerator, "-[2/3]")
        testRule("[-(2 + 1)/3]", SimplifyNegativeInNumerator, "-[2 + 1/3]")
    }

    @Test
    fun testSimplifyNegativeNumeratorAndDenominator() {
        testRule("[-2/-3]", SimplifyNegativeNumeratorAndDenominator, "[2/3]")
        testRule("[(-2)/-3]", SimplifyNegativeNumeratorAndDenominator, "[2/3]")
        testRule("[-(2 + 1)/-(3+2)]", SimplifyNegativeNumeratorAndDenominator, "[2 + 1/3 + 2]")
    }

    @Test
    fun testTurnFactorIntoFractionInProduct() {
        testRule("[2/5] * 3", TurnFactorIntoFractionInProduct, "[2/5] * [3/1]")
        testRule("[2/5] * (-3)", TurnFactorIntoFractionInProduct, "[2/5] * [-3/1]")
        testRule("[2/5] * sqrt[3]", TurnFactorIntoFractionInProduct, "[2/5] * [sqrt[3]/1]")
        testRule("[2/5] * [2/3]", TurnFactorIntoFractionInProduct, null)
        testRule("[2/5] * (-[2/3])", TurnFactorIntoFractionInProduct, null)
        testRule("[2/5] * [([3/5])^[2 / 3]]", TurnFactorIntoFractionInProduct, null)
        testRule("[2/5] * -[([3/5])^[2 / 3]]", TurnFactorIntoFractionInProduct, null)
        testRule("[1/2] * (1 + sqrt[3])", TurnFactorIntoFractionInProduct, "[1/2] * [1 + sqrt[3] / 1]")
        testRule("[1/2] * (1 + [2 ^ [1/2]])", TurnFactorIntoFractionInProduct, "[1/2] * [1 + [2 ^ [1/2]] / 1]")
        testRule("[2/3] * (5 + [([1/2]) ^ 2])", TurnFactorIntoFractionInProduct, null)
        testRule("[2/3] * (1 - [1/3])", TurnFactorIntoFractionInProduct, null)
    }

    @Test
    fun testTurnProductOfFractionAndNonFractionFactorIntoFraction() {
        testRuleInX(
            "[12 / x - 3] (x - 3) (x - 4) (x - 5)",
            FractionArithmeticRules.TurnProductOfFractionAndNonFractionFactorsIntoFraction,
            "[12 (x - 3) (x - 4) (x - 5) / x - 3]",
        )
        testRuleInX(
            "[9 x / x + 1] (x + 2) (x + 3)",
            FractionArithmeticRules.TurnProductOfFractionAndNonFractionFactorsIntoFraction,
            "[9 x (x + 2) (x + 3) / x + 1]",
        )
        testRuleInX(
            "[9 x / x + 1] (-x) (x + 3)",
            FractionArithmeticRules.TurnProductOfFractionAndNonFractionFactorsIntoFraction,
            null,
        )
        testRuleInX(
            "[9 x / x + 1] (x + 2) : (x + 3)",
            FractionArithmeticRules.TurnProductOfFractionAndNonFractionFactorsIntoFraction,
            null,
        )
    }

    @Test
    fun testTurnSumOfFractionAndIntegerToFractionSum() {
        testRule("[1/3] + 2", BringToCommonDenominatorWithNonFractionalTerm, "[1/3] + [2 * 3/3]")
        testRule("[1/2] - 2", BringToCommonDenominatorWithNonFractionalTerm, "[1/2] - [2 * 2/2]")
        testRule("3 - [1/4]", BringToCommonDenominatorWithNonFractionalTerm, "[3 * 4/4] - [1/4]")
    }

    @Test
    fun testMultiplyFractions() {
        testRule("[2/3] * [4/5]", MultiplyFractions, "[2*4/3*5]")
        testRule("[2/3] * [4/(5)]", MultiplyFractions, "[2*4/3*5]")
        testRule("[-4/5] * ([2/(-3)])", MultiplyFractions, "[(-4) * 2 / 5 * (-3)]")
        testRule("[-4/5] * [2/{. -3 .}]", MultiplyFractions, "[(-4) * 2 / 5 * {. -3 .}]")
    }

    @Test
    fun testSimplifyFractionWithFractionNumerator() {
        testRule("[[1 / 2] / 3]", SimplifyFractionWithFractionNumerator, "[1/2] * [1/3]")
        testRule("[([1 / -2]) / (-3)]", SimplifyFractionWithFractionNumerator, "[1/-2] * [1/-3]")
    }

    @Test
    fun testSimplifyFractionWithFractionDenominator() {
        testRule("[5 / [2/3]]", SimplifyFractionWithFractionDenominator, "5 * [3/2]")
        testRule("[[1/2] / [3/4]]", SimplifyFractionWithFractionDenominator, "[1/2] * [4/3]")
    }

    @Test
    fun testDistributeFractionPositivePower() {
        testRule("[([2 / 3]) ^ 5]", DistributePositiveIntegerPowerOverFraction, "[[2 ^ 5] / [3 ^ 5]]")
        testRule("[([2 / 3]) ^ 1]", DistributePositiveIntegerPowerOverFraction, null)
        testRule("[([2 / 3]) ^ 0]", DistributePositiveIntegerPowerOverFraction, null)
        testRule("[([2 / 3]) ^ -2]", DistributePositiveIntegerPowerOverFraction, null)
    }

    @Test
    fun testDistributeFractionPositiveFractionPower() {
        testRule("[([2 / 3]) ^ [1 / 2]]", DistributeFractionalPowerOverFraction, null)
        testRule("[([2 / 3]) ^ [4 / 3]]", DistributeFractionalPowerOverFraction, null)
        testRule("[([4 / 9]) ^ [1 / 2]]", DistributeFractionalPowerOverFraction, "[[4 ^ [1 / 2]] / [9 ^ [1 / 2]]]")
        testRule("[([4 / 5]) ^ [1 / 2]]", DistributeFractionalPowerOverFraction, "[[4 ^ [1 / 2]] / [5 ^ [1 / 2]]]")
        testRule("[([2 / 9]) ^ [1 / 2]]", DistributeFractionalPowerOverFraction, "[[2 ^ [1 / 2]] / [9 ^ [1 / 2]]]")
    }

    @Test
    fun testSimplifyFractionToMinusOne() {
        testRule("[([1 / 3]) ^ -1]", SimplifyFractionToMinusOne, "[3 / 1]")
        testRule("[([4 / 3]) ^ (-1)]", SimplifyFractionToMinusOne, "[3 / 4]")
    }

    @Test
    fun testTurnIntegerToMinusOneToFraction() {
        testRule("[3 ^ -1]", TurnIntegerToMinusOneToFraction, "[1 / 3]")
        testRule("[100 ^ (-1)]", TurnIntegerToMinusOneToFraction, "[1 / 100]")
    }

    @Test
    fun testTurnNegativePowerOfIntegerToFraction() {
        testRule("[3 ^ -2]", TurnNegativePowerOfIntegerToFraction, "[1 / [3 ^ 2]]")
        testRule("[5 ^ [. -6 .]]", TurnNegativePowerOfIntegerToFraction, "[1 / [5 ^ 6]]")
    }

    @Test
    fun testTurnNegativePowerOfZeroToPowerOfFraction() {
        testRule("[0 ^ -[3 / 2]]", TurnNegativePowerOfZeroToPowerOfFraction, "[([1 / 0]) ^ [3 / 2]]")
        // this case is not handled yet
        testRule("[0 ^ sqrt[3] - sqrt[5]]", TurnNegativePowerOfZeroToPowerOfFraction, null)
    }
}
