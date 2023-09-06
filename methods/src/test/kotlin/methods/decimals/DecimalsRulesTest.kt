package methods.decimals

import engine.methods.testRule
import methods.decimals.DecimalRules.ConvertFractionWithPowerOfTenDenominatorToDecimal
import methods.decimals.DecimalRules.ConvertRecurringDecimalToFractionDirectly
import methods.decimals.DecimalRules.ConvertTerminatingDecimalToFraction
import methods.decimals.DecimalRules.EvaluateDecimalProductAndDivision
import methods.decimals.DecimalRules.ExpandFractionToPowerOfTenDenominator
import methods.decimals.DecimalRules.MakeEquationSystemForRecurringDecimal
import methods.decimals.DecimalRules.MultiplyFractionOfDecimalsByPowerOfTen
import methods.decimals.DecimalRules.TurnDivisionOfDecimalsIntoFraction
import org.junit.jupiter.api.Test

class DecimalsRulesTest {

    @Test
    fun testConvertTerminatingDecimalToFraction() {
        testRule("0.567", ConvertTerminatingDecimalToFraction, "[567 / 1000]")
        testRule("0.00567", ConvertTerminatingDecimalToFraction, "[567 / 100000]")
        testRule("31.07", ConvertTerminatingDecimalToFraction, "[3107 / 100]")
        testRule("31.0[7]", ConvertTerminatingDecimalToFraction, null)
    }

    @Test
    fun testStripTrailingZeros() {
        testRule("1.100", DecimalRules.StripTrailingZerosAfterDecimal, "1.1")
        testRule("1.1", DecimalRules.StripTrailingZerosAfterDecimal, null)
        testRule("(-9)", DecimalRules.StripTrailingZerosAfterDecimal, null)
    }

    @Test
    fun testEvaluateSignedDecimalAddition() {
        testRule("1.1 - 1.1", DecimalRules.EvaluateSignedDecimalAddition, "0")
        testRule("1.25 - 1.15", DecimalRules.EvaluateSignedDecimalAddition, "0.1")
    }

    @Test
    fun testConvertRecurringDecimalToFractionDirectly() {
        testRule("0.34", ConvertRecurringDecimalToFractionDirectly, null)
        testRule(
            "3.14[15]",
            ConvertRecurringDecimalToFractionDirectly,
            "[31415 - 314 / 9900]",
        )
        testRule(
            "3.[15]",
            ConvertRecurringDecimalToFractionDirectly,
            "[315 - 3 / 99]",
        )
    }

    @Test
    fun testMakeEquationSystemForRecurringDecimal() {
        testRule(
            "x = 3.14[15]",
            MakeEquationSystemForRecurringDecimal,
            "100 x = 314.[15] AND 10000 x = 31415.[15]",
        )
    }

    @Test
    fun testMultiplyFractionOfDecimalsByPowerOfTen() {
        testRule("[0.1/0.001]", MultiplyFractionOfDecimalsByPowerOfTen, "[0.1*1000/0.001*1000]")
        testRule("[0.02/4]", MultiplyFractionOfDecimalsByPowerOfTen, "[0.02*100/4*100]")
        testRule("[10/0.1]", MultiplyFractionOfDecimalsByPowerOfTen, "[10*10/0.1*10]")
    }

    @Test
    fun testEvaluateDecimalProductAndDivision() {
        testRule("0.2 * 0.1", EvaluateDecimalProductAndDivision, "0.02")
        testRule("2 * 0.4", EvaluateDecimalProductAndDivision, "0.8")
        testRule("0.002 * 3000", EvaluateDecimalProductAndDivision, "6")
        testRule("2 * 4", EvaluateDecimalProductAndDivision, "8")
        testRule("6 : 2", EvaluateDecimalProductAndDivision, "3")
    }

    @Test
    fun testExpandFractionToPowerOfTenDenominator() {
        testRule("[1 / 30]", ExpandFractionToPowerOfTenDenominator, null)
        testRule("[1 / 20]", ExpandFractionToPowerOfTenDenominator, "[1 * 5 / 20 * 5]")
    }

    @Test
    fun testConvertFractionWithPowerOfTenDenominatorToDecimal() {
        testRule("[2/100]", ConvertFractionWithPowerOfTenDenominatorToDecimal, "0.02")
        testRule("[1/5]", ConvertFractionWithPowerOfTenDenominatorToDecimal, null)
        testRule("[12345/1000]", ConvertFractionWithPowerOfTenDenominatorToDecimal, "12.345")
        testRule("[1000/100]", ConvertFractionWithPowerOfTenDenominatorToDecimal, "10.00")
    }

    @Test
    fun testTurnDivisionOfDecimalsIntoFraction() {
        testRule("0.1 : 2", TurnDivisionOfDecimalsIntoFraction, "[0.1 / 2]")
        testRule("5 : 0.7 * 3", TurnDivisionOfDecimalsIntoFraction, "[5 / 0.7] * 3")
        testRule("3 : 5", TurnDivisionOfDecimalsIntoFraction, "[3 / 5]")
        testRule("1.25 : 120.77", TurnDivisionOfDecimalsIntoFraction, "[1.25 / 120.77]")
    }
}
