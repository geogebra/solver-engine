package methods.fractionarithmetic

import methods.plans.testMethod
import methods.rules.testRule
import org.junit.jupiter.api.Test

class IntegerFractionsRulesTest {

    @Test
    fun testConvertIntegerToFraction() {
        testRule("5 + [2/4]", convertIntegerToFraction, "[5/1] + [2/4]")
        testRule("[2/4] + 5", convertIntegerToFraction, "[2/4] + [5/1]")
    }

    @Test
    fun testAddLikeFractions() {
        testRule("1+[2/10]+z+[3/10]+x", addLikeFractions, "1+[2+3/10]+z+x")
        testRule("1+[2/10]-x", addLikeFractions, null)
        testRule("[3/10] - [2/10]", addLikeFractions, "[3 - 2 / 10]")
        testRule("-[3/10] - [2/10]", addLikeFractions, "[-3 - 2 / 10]")
        testRule("1 - [2/10]", addLikeFractions, null)
        testMethod {
            method = addLikeFractions
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
            method = addLikeFractions
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
        testRule("[3/8] + [5/12]", bringToCommonDenominator, "[3 * 3/8 * 3] + [5 * 2/12 * 2]")
        testRule("[3/8] - [5/12]", bringToCommonDenominator, "[3 * 3/8 * 3] - [5 * 2/12 * 2]")
        testRule("-[3/8] + [5/12]", bringToCommonDenominator, "-[3 * 3/8 * 3] + [5 * 2/12 * 2]")
    }

    @Test
    fun testSimplifyNegativeInDenominator() {
        testRule("[4/-5]", simplifyNegativeInDenominator, "-[4/5]")
        testRule("[4/5]", simplifyNegativeInDenominator, null)
    }

    @Test
    fun testSimplifyFractionToInteger() {
        testRule("[10/1]", simplifyFractionToInteger, "10")
        testRule("[8/4]", simplifyFractionToInteger, "2")
        testRule("[9/5]", simplifyFractionToInteger, null)
        testRule("[10/0]", simplifyFractionToInteger, null)
    }

    @Test
    fun testFindCommonFactorInFraction() {
        testRule("[6/10]", findCommonFactorInFraction, "[2 * 3/2 * 5]")
        testRule("[5/7]", findCommonFactorInFraction, null)
        testRule("[700/500]", findCommonFactorInFraction, "[100 * 7/100 * 5]")
        testRule("[1/10]", findCommonFactorInFraction, null)
    }

    @Test
    fun testSimplifyNegativeInNumerator() {
        testRule("[-2/3]", simplifyNegativeInNumerator, "-[2/3]")
        testRule("[(-2)/3]", simplifyNegativeInNumerator, "-[2/3]")
        testRule("[-(2 + 1)/3]", simplifyNegativeInNumerator, "-[2 + 1/3]")
    }

    @Test
    fun testSimplifyNegativeNumeratorAndDenominator() {
        testRule("[-2/-3]", simplifyNegativeNumeratorAndDenominator, "[2/3]")
        testRule("[(-2)/-3]", simplifyNegativeNumeratorAndDenominator, "[2/3]")
        testRule("[-(2 + 1)/-(3+2)]", simplifyNegativeNumeratorAndDenominator, "[2 + 1/3 + 2]")
    }

    @Test
    fun testTurnFactorIntoFractionInProduct() {
        testRule("[2/5] * 3", turnFactorIntoFractionInProduct, "[2/5] * [3/1]")
        testRule("[2/5] * (-3)", turnFactorIntoFractionInProduct, "[2/5] * [-3/1]")
    }

    @Test
    fun testTurnSumOfFractionAndIntegerToFractionSum() {
        testRule("[1/3] + 2", turnSumOfFractionAndIntegerToFractionSum, "[1/3] + [2 * 3/3]")
        testRule("[1/2] - 2", turnSumOfFractionAndIntegerToFractionSum, "[1/2] - [2 * 2/2]")
        testRule("3 - [1/4]", turnSumOfFractionAndIntegerToFractionSum, "[3 * 4/4] - [1/4]")
    }

    @Test
    fun testMultiplyFractions() {
        testRule("[2/3] * [4/5]", multiplyFractions, "[2*4/3*5]")
        testRule("[2/3] * [4/(5)]", multiplyFractions, "[2*4/3*5]")
        testRule("[-4/5] * ([2/(-3)])", multiplyFractions, "[(-4) * 2 / 5 * (-3)]")
        testRule("[-4/5] * [2/{. -3 .}]", multiplyFractions, "[(-4) * 2 / 5 * {. -3 .}]")
    }

    @Test
    fun testSimplifyFractionWithFractionNumerator() {
        testRule("[[1 / 2] / 3]", simplifyFractionWithFractionNumerator, "[1/2] * [1/3]")
        testRule("[([1 / -2]) / (-3)]", simplifyFractionWithFractionNumerator, "[1/-2] * [1/-3]")
    }

    @Test
    fun testSimplifyFractionWithFractionDenominator() {
        testRule("[5 / [2/3]]", simplifyFractionWithFractionDenominator, "5 * [3/2]")
        testRule("[[1/2] / [3/4]]", simplifyFractionWithFractionDenominator, "[1/2] * [4/3]")
    }

    @Test
    fun testDistributeFractionPositivePower() {
        testRule("[([2 / 3]) ^ 5]", distributeFractionPositivePower, "[[2 ^ 5] / [3 ^ 5]]")
        testRule("[([2 / 3]) ^ 1]", distributeFractionPositivePower, null)
        testRule("[([2 / 3]) ^ 0]", distributeFractionPositivePower, null)
        testRule("[([2 / 3]) ^ -2]", distributeFractionPositivePower, null)
    }

    @Test
    fun testSimplifyFractionNegativePower() {
        testRule("[([3 / 5]) ^ -2]", simplifyFractionNegativePower, "[([5 / 3]) ^ 2]")
        testRule("[([7 / 10]) ^ (-5)]", simplifyFractionNegativePower, "[([10 / 7]) ^ 5]")
        testRule("[([1 / 3]) ^ -1]", simplifyFractionNegativePower, null)
    }

    @Test
    fun testSimplifyFractionToMinusOne() {
        testRule("[([1 / 3]) ^ -1]", simplifyFractionToMinusOne, "[3 / 1]")
        testRule("[([4 / 3]) ^ (-1)]", simplifyFractionToMinusOne, "[3 / 4]")
    }

    @Test
    fun testTurnIntegerToMinusOneToFraction() {
        testRule("[3 ^ -1]", turnIntegerToMinusOneToFraction, "[1 / 3]")
        testRule("[100 ^ (-1)]", turnIntegerToMinusOneToFraction, "[1 / 100]")
    }

    @Test
    fun testTurnNegativePowerOfIntegerToFraction() {
        testRule("[3 ^ -2]", turnNegativePowerOfIntegerToFraction, "[1 / [3 ^ 2]]")
        testRule("[5 ^ [. -6 .]]", turnNegativePowerOfIntegerToFraction, "[1 / [5 ^ 6]]")
    }

    @Test
    fun testTurnNegativePowerOfZeroToPowerOfFraction() {
        testRule("[0 ^ -[3 / 2]]", turnNegativePowerOfZeroToPowerOfFraction, "[([1 / 0]) ^ [3 / 2]]")
        // this case is not handled yet
        testRule("[0 ^ sqrt[3] - sqrt[5]]", turnNegativePowerOfZeroToPowerOfFraction, null)
    }
}
