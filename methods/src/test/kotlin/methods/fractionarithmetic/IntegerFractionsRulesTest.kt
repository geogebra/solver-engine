package methods.fractionarithmetic

import methods.rules.RuleTest
import methods.rules.RuleTestCase
import java.util.stream.Stream

object IntegerFractionsRulesTest : RuleTest {

    @JvmStatic
    fun testCaseProvider(): Stream<RuleTestCase> = Stream.of(
        RuleTestCase("5 + [2/4]", convertIntegerToFraction, "[5/1] + [2/4]"),
        RuleTestCase("[2/4] + 5", convertIntegerToFraction, "[2/4] + [5/1]"),

        RuleTestCase("1+[2/10]+z+[3/10]+x", addLikeFractions, "1+[2+3/10]+z+x"),
        RuleTestCase("1+[2/10]-x", addLikeFractions, null),

        RuleTestCase("[3/10] - [2/10]", addLikeFractions, "[3 - 2 / 10]"),
        RuleTestCase("-[3/10] - [2/10]", addLikeFractions, "[-3 - 2 / 10]"),
        RuleTestCase("1 - [2/10]", addLikeFractions, null),

        RuleTestCase("[3/8] + [5/12]", bringToCommonDenominator, "[3 * 3/8 * 3] + [5 * 2/12 * 2]"),
        RuleTestCase("[3/8] - [5/12]", bringToCommonDenominator, "[3 * 3/8 * 3] - [5 * 2/12 * 2]"),
        RuleTestCase("-[3/8] + [5/12]", bringToCommonDenominator, "-[3 * 3/8 * 3] + [5 * 2/12 * 2]"),

        RuleTestCase("[4/-5]", simplifyNegativeInDenominator, "-[4/5]"),
        RuleTestCase("[4/5]", simplifyNegativeInDenominator, null),

        RuleTestCase("[6/10]", findCommonFactorInFraction, "[2 * 3/2 * 5]"),
        RuleTestCase("[5/7]", findCommonFactorInFraction, null),
        RuleTestCase("[700/500]", findCommonFactorInFraction, "[100 * 7/100 * 5]"),
        RuleTestCase("[1/10]", findCommonFactorInFraction, null),

        RuleTestCase("[2/3] * [4/5]", multiplyFractions, "[2*4/3*5]"),

        RuleTestCase("5 : [2/3]", simplifyDividingByAFraction, "5 * [3/2]"),
        RuleTestCase("[1/2] : [11/10] * 5", simplifyDividingByAFraction, "[1/2] * [10/11] * 5"),

        RuleTestCase("6 : 8 * 3", turnDivisionToFraction, "[6 / 8] * 3"),
        RuleTestCase("4 * x : 8", turnDivisionToFraction, "[4 / 8] * x"),

        RuleTestCase("[5 / [2/3]]", simplifyFractionWithFractionDenominator, "5 * [3/2]"),
        RuleTestCase("[[1/2] / [3/4]]", simplifyFractionWithFractionDenominator, "[1/2] * [4/3]"),

        RuleTestCase("[[1 / 2] / 3]", simplifyFractionWithFractionNumerator, "[1/2] * [1/3]"),

        RuleTestCase("[([2 / 3]) ^ 5]", distributeFractionPositivePower, "[[2 ^ 5] / [3 ^ 5]]"),
        RuleTestCase("[([2 / 3]) ^ 1]", distributeFractionPositivePower, null),
        RuleTestCase("[([2 / 3]) ^ 0]", distributeFractionPositivePower, null),
        RuleTestCase("[([2 / 3]) ^ -2]", distributeFractionPositivePower, null),

        RuleTestCase("[([3 / 5]) ^ -2]", simplifyFractionNegativePower, "[([5 / 3]) ^ 2]"),
        RuleTestCase("[([7 / 10]) ^ (-5)]", simplifyFractionNegativePower, "[([10 / 7]) ^ 5]"),
        RuleTestCase("[([1 / 3]) ^ -1]", simplifyFractionNegativePower, null),

        RuleTestCase("[([1 / 3]) ^ -1]", simplifyFractionToMinusOne, "[3 / 1]"),

        RuleTestCase("[3 ^ -1]", turnIntegerToMinusOneToFraction, "[1 / 3]"),
        RuleTestCase("[3 ^ -2]", turnNegativePowerOfIntegerToFraction, "[1 / [3 ^ 2]]"),

        RuleTestCase(
            "[4 / 3 * sqrt[3]] * [sqrt[3] / sqrt[3]]",
            writeMultiplicationOfFractionsAsFraction,
            "[4 * sqrt[3] / 3 * sqrt[3] * sqrt[3]]"
        )
    )
}
