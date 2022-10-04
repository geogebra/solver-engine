package methods.decimals

import methods.rules.testRule
import org.junit.jupiter.api.Test

class DecimalsRulesTest {

    @Test
    fun testConvertTerminatingDecimalToFraction() {
        testRule("0.567", convertTerminatingDecimalToFraction, "[567 / 1000]")
        testRule("0.00567", convertTerminatingDecimalToFraction, "[567 / 100000]")
        testRule("31.07", convertTerminatingDecimalToFraction, "[3107 / 100]")
        testRule("31.0[7]", convertTerminatingDecimalToFraction, null)
    }

    @Test
    fun testConvertRecurringDecimalToFractionDirectly() {
        testRule("0.34", convertRecurringDecimalToFractionDirectly, null)
        testRule(
            "3.14[15]",
            convertRecurringDecimalToFractionDirectly,
            "[31415 - 314 / 9900]"
        )
        testRule(
            "3.[15]",
            convertRecurringDecimalToFractionDirectly,
            "[315 - 3 / 99]"
        )
    }

    @Test
    fun testMakeEquationSystemForRecurringDecimal() {
        testRule(
            "x = 3.14[15]",
            makeEquationSystemForRecurringDecimal,
            "100 * x = 314.[15], 10000 * x = 31415.[15]"
        )
    }

    @Test
    fun testMultiplyFractionOfDecimalsByPowerOfTen() {
        testRule("[0.1/0.001]", multiplyFractionOfDecimalsByPowerOfTen, "[0.1*1000/0.001*1000]")
        testRule("[0.02/4]", multiplyFractionOfDecimalsByPowerOfTen, "[0.02*100/4*100]")
        testRule("[10/0.1]", multiplyFractionOfDecimalsByPowerOfTen, "[10*10/0.1*10]")
    }

    @Test
    fun testEvaluateDecimalProductAndDivision() {
        testRule("0.2 * 0.1", evaluateDecimalProductAndDivision, "0.02")
        testRule("2 * 0.4", evaluateDecimalProductAndDivision, "0.8")
        testRule("0.002 * 3000", evaluateDecimalProductAndDivision, "6")
        testRule("2 * 4", evaluateDecimalProductAndDivision, "8")
        testRule("6 : 2", evaluateDecimalProductAndDivision, "3")
    }

    @Test
    fun testExpandFractionToPowerOfTenDenominator() {
        testRule("[1 / 30]", expandFractionToPowerOfTenDenominator, null)
        testRule("[1 / 20]", expandFractionToPowerOfTenDenominator, "[1 * 5 / 20 * 5]")
    }

    @Test
    fun testConvertFractionWithPowerOfTenDenominatorToDecimal() {
        testRule("[2/100]", convertFractionWithPowerOfTenDenominatorToDecimal, "0.02")
        testRule("[1/5]", convertFractionWithPowerOfTenDenominatorToDecimal, null)
        testRule("[12345/1000]", convertFractionWithPowerOfTenDenominatorToDecimal, "12.345")
        testRule("[1000/100]", convertFractionWithPowerOfTenDenominatorToDecimal, "10.00")
    }

    @Test
    fun testTurnDivisionOfDecimalsIntoFraction() {
        testRule("0.1 : 2", turnDivisionOfDecimalsIntoFraction, "[0.1 / 2]")
        testRule("5 : 0.7 * 3", turnDivisionOfDecimalsIntoFraction, "[5 / 0.7] * 3")
        testRule("3 : 5", turnDivisionOfDecimalsIntoFraction, "[3 / 5]")
        testRule("1.25 : 120.77", turnDivisionOfDecimalsIntoFraction, "[1.25 / 120.77]")
    }
}
