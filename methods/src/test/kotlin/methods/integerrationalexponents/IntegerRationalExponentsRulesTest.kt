package methods.integerrationalexponents

import methods.rules.testRule
import org.junit.jupiter.api.Test

object IntegerRationalExponentsRulesTest {
    @Test
    fun testFactorizeIntegerUnderRationalExponent() {
        testRule(
            "[ 360 ^ [1 / 3] ]",
            factorizeIntegerUnderRationalExponent,
            "[ ([2^3] * [3^2] * 5) ^ [1 / 3] ]"
        )
        testRule(
            "[9 ^ [1 / 6]]",
            factorizeIntegerUnderRationalExponent,
            "[ ([3 ^ 2]) ^ [ 1 / 6]]"
        )
        testRule(
            "[6 ^ [2 / 5]]",
            factorizeIntegerUnderRationalExponent,
            null
        )
        testRule(
            "[32 ^ [2/5]]",
            factorizeIntegerUnderRationalExponent,
            "[ ([2^5]) ^ [2/5] ]"
        )
        testRule(
            // 480 = [2 ^ 5] * 3 * 5
            "[480 ^ [1 / 6]]",
            factorizeIntegerUnderRationalExponent,
            null
        )
        testRule(
            "[9 ^ [1/6] ]",
            factorizeIntegerUnderRationalExponent,
            "[ ([3^2]) ^ [1/6] ]"
        )
        testRule(
            "[100 ^ [1/6] ]",
            factorizeIntegerUnderRationalExponent,
            "[ ([2^2] * [5^2]) ^ [1/6] ]"
        )
        testRule(
            "[250000 ^ [1 / 8]]",
            factorizeIntegerUnderRationalExponent,
            // since there is something common b/w
            // prime factor powers and denominator of
            // rational power
            "[ ([2^4] * [5^6]) ^ [1/8] ]"
        )
    }

    @Test
    fun testNormaliseProductWithRationalExponents() {
        testRule(
            "[2 ^ [2 / 5]] * [3 ^ 2] * [5 ^ [4 / 5]] * [7 ^ [2 / 5]]",
            normaliseProductWithRationalExponents,
            "[3 ^ 2] * [2 ^ [2 / 5]] * [5 ^ [4 / 5]] * [7 ^ [2 / 5]]"
        )
        testRule(
            "[2 ^ [2 / 5]] * [3 ^ 2] * 5",
            normaliseProductWithRationalExponents,
            "[3 ^ 2] * 5 * [2 ^ [2 / 5]]"
        )
    }

    @Test
    fun testBringRationalExponentsToSameDenominator() {
        testRule(
            "[2 ^ [2 / 3]] * [3 ^ [1 / 2]]",
            findCommonDenominatorOfRationalExponents,
            "[2 ^ [2 * 2 / 3 * 2]] * [3 ^ [1 * 3 / 2 * 3]]"
        )
        testRule(
            "[2 ^ [2 / 3]] * [3 ^ [1 / 6]]",
            findCommonDenominatorOfRationalExponents,
            "[2 ^ [2 * 2 / 3 * 2]] * [3 ^ [1 / 6]]"
        )
        testRule(
            "[[2 ^ [2 / 3]] / [3 ^ [1 / 2]]]",
            findCommonDenominatorOfRationalExponents,
            "[[2 ^ [2 * 2 / 3 * 2]] / [3 ^ [1 * 3 / 2 * 3]]]"
        )
    }

    @Test
    fun testFactorDenominatorOfRationalExponents() {
        testRule(
            "[2 ^ [4 / 6]] * [3 ^ [3 / 6]]",
            factorDenominatorOfRationalExponents,
            "[([2 ^ 4] * [3 ^ 3]) ^ [1 / 6]]"
        )
        testRule(
            "[2 ^ [4 / 6]] * [3 ^ [1 / 6]]",
            factorDenominatorOfRationalExponents,
            "[([2 ^ 4] * 3) ^ [1 / 6]]"
        )
    }

    @Test
    fun testEvaluateNegativeToRationalExponentAsUndefined() {
        testRule(
            "[(-1 - 2) ^ [1/6]]",
            evaluateNegativeToRationalExponentAsUndefined,
            "UNDEFINED"
        )
        testRule(
            "[(-x) ^ [1/2]]",
            evaluateNegativeToRationalExponentAsUndefined,
            null
        )
        testRule(
            "[(-1) ^ [6 / 3]]",
            evaluateNegativeToRationalExponentAsUndefined,
            null
        )
        testRule(
            "[(-2) ^ -[1/2]]",
            evaluateNegativeToRationalExponentAsUndefined,
            "UNDEFINED"
        )
    }
}
