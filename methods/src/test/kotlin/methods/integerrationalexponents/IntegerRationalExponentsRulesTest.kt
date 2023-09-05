package methods.integerrationalexponents

import engine.methods.testMethod
import engine.methods.testRule
import methods.integerrationalexponents.IntegerRationalExponentsRules.EvaluateNegativeToRationalExponentAsUndefined
import methods.integerrationalexponents.IntegerRationalExponentsRules.FactorDenominatorOfRationalExponents
import methods.integerrationalexponents.IntegerRationalExponentsRules.FactorizeIntegerUnderRationalExponent
import methods.integerrationalexponents.IntegerRationalExponentsRules.FindCommonDenominatorOfRationalExponents
import methods.integerrationalexponents.IntegerRationalExponentsRules.NormaliseProductWithRationalExponents
import org.junit.jupiter.api.Test

object IntegerRationalExponentsRulesTest {
    @Test
    fun testFactorizeIntegerUnderRationalExponent() {
        testRule(
            "[ 360 ^ [1 / 3] ]",
            FactorizeIntegerUnderRationalExponent,
            "[ ([2^3] * [3^2] * 5) ^ [1 / 3] ]",
        )
        testRule(
            "[9 ^ [1 / 6]]",
            FactorizeIntegerUnderRationalExponent,
            "[ ([3 ^ 2]) ^ [ 1 / 6]]",
        )
        testRule(
            "[6 ^ [2 / 5]]",
            FactorizeIntegerUnderRationalExponent,
            null,
        )
        testRule(
            "[32 ^ [2/5]]",
            FactorizeIntegerUnderRationalExponent,
            "[ ([2^5]) ^ [2/5] ]",
        )
        testRule(
            // 480 = [2 ^ 5] * 3 * 5
            "[480 ^ [1 / 6]]",
            FactorizeIntegerUnderRationalExponent,
            null,
        )
        testRule(
            "[9 ^ [1/6] ]",
            FactorizeIntegerUnderRationalExponent,
            "[ ([3^2]) ^ [1/6] ]",
        )
        testRule(
            "[100 ^ [1/6] ]",
            FactorizeIntegerUnderRationalExponent,
            "[ ([2^2] * [5^2]) ^ [1/6] ]",
        )
        testRule(
            "[250000 ^ [1 / 8]]",
            FactorizeIntegerUnderRationalExponent,
            // since there is something common b/w
            // prime factor powers and denominator of
            // rational power
            "[ ([2^4] * [5^6]) ^ [1/8] ]",
        )
        testRule(
            "[25 ^ -[1/2]]",
            FactorizeIntegerUnderRationalExponent,
            "[([5^2]) ^ -[1/2]]",
        )
    }

    @Test
    fun testNormaliseProductWithRationalExponents() {
        testRule(
            "[2 ^ [2 / 5]] * [3 ^ 2] * [5 ^ [4 / 5]] * [7 ^ [2 / 5]]",
            NormaliseProductWithRationalExponents,
            "[3 ^ 2] * [2 ^ [2 / 5]] * [5 ^ [4 / 5]] * [7 ^ [2 / 5]]",
        )
        testRule(
            "[2 ^ [2 / 5]] * [3 ^ 2] * 5",
            NormaliseProductWithRationalExponents,
            "[3 ^ 2] * 5 * [2 ^ [2 / 5]]",
        )
    }

    @Test
    fun testBringRationalExponentsToSameDenominator() {
        testRule(
            "[2 ^ [2 / 3]] * [3 ^ [1 / 2]]",
            FindCommonDenominatorOfRationalExponents,
            "[2 ^ [2 * 2 / 3 * 2]] * [3 ^ [1 * 3 / 2 * 3]]",
        )
        testRule(
            "[2 ^ [2 / 3]] * [3 ^ [1 / 6]]",
            FindCommonDenominatorOfRationalExponents,
            "[2 ^ [2 * 2 / 3 * 2]] * [3 ^ [1 / 6]]",
        )
        testRule(
            "[[2 ^ [2 / 3]] / [3 ^ [1 / 2]]]",
            FindCommonDenominatorOfRationalExponents,
            "[[2 ^ [2 * 2 / 3 * 2]] / [3 ^ [1 * 3 / 2 * 3]]]",
        )
    }

    @Test
    fun testFactorDenominatorOfRationalExponents() {
        testRule(
            "[2 ^ [4 / 6]] * [3 ^ [3 / 6]]",
            FactorDenominatorOfRationalExponents,
            "[([2 ^ 4] * [3 ^ 3]) ^ [1 / 6]]",
        )
        testRule(
            "[2 ^ [4 / 6]] * [3 ^ [1 / 6]]",
            FactorDenominatorOfRationalExponents,
            "[([2 ^ 4] * 3) ^ [1 / 6]]",
        )
    }

    @Test
    fun testEvaluateNegativeToRationalExponentAsUndefined() {
        testRule(
            "[(-1 - 2) ^ [1/6]]",
            EvaluateNegativeToRationalExponentAsUndefined,
            "/undefined/",
        )
        testRule(
            "[(-x) ^ [1/2]]",
            EvaluateNegativeToRationalExponentAsUndefined,
            null,
        )
        testRule(
            "[(-1) ^ [6 / 3]]",
            EvaluateNegativeToRationalExponentAsUndefined,
            null,
        )
        testRule(
            "[(-2) ^ -[1/2]]",
            EvaluateNegativeToRationalExponentAsUndefined,
            "/undefined/",
        )
    }

    @Test
    fun testApplyReciprocalPowerRule() {
        testMethod {
            method = IntegerRationalExponentsRules.ApplyReciprocalPowerRule
            inputExpr = "[4 / 5*[3^[1/3]]]"

            check {
                toExpr = "[4 * [3 ^ -[1 / 3]] / 5]"

                shift("./0", "./0/0")
                transform("./1/1", "./0/1")
                shift("./1/0", "./1")
            }
        }

        testRule(
            "[1 / [3^[1/3]]]",
            IntegerRationalExponentsRules.ApplyReciprocalPowerRule,
            "[3^-[1/3]]",
        )
        testRule(
            "[4 / [3^[1/3]]]",
            IntegerRationalExponentsRules.ApplyReciprocalPowerRule,
            "4 * [3^-[1/3]]",
        )
        testRule(
            "[4 / 5 [3^[1/3]]]",
            IntegerRationalExponentsRules.ApplyReciprocalPowerRule,
            "[4 * [3^-[1/3]] / 5]",
        )
        testRule(
            "[4 / [3^-[1/3]]]",
            IntegerRationalExponentsRules.ApplyReciprocalPowerRule,
            "4 * [3^[1/3]]",
        )
        testRule(
            "[4 / 5 [3^-[1/3]]]",
            IntegerRationalExponentsRules.ApplyReciprocalPowerRule,
            "[4 * [3^[1/3]] / 5]",
        )
        testRule(
            "[3 / [3^-[1/3]]]",
            IntegerRationalExponentsRules.ApplyReciprocalPowerRule,
            "3 * [3^[1/3]]",
        )
    }
}
