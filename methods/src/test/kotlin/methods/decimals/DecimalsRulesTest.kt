package methods.decimals

import methods.rules.RuleTestCase
import methods.rules.testRule
import org.junit.jupiter.api.Test

class DecimalsRulesTest {

    @Test
    fun testConvertTerminatingDecimalToFraction() {
        RuleTestCase("0.567", convertTerminatingDecimalToFraction, "[567 / 1000]").assert()
        RuleTestCase("0.00567", convertTerminatingDecimalToFraction, "[567 / 100000]").assert()
        RuleTestCase("31.07", convertTerminatingDecimalToFraction, "[3107 / 100]").assert()
        RuleTestCase("31.0[7]", convertTerminatingDecimalToFraction, null).assert()
    }

    @Test
    fun testConvertRecurringDecimalToFractionDirectly() {
        RuleTestCase("0.34", convertRecurringDecimalToFractionDirectly, null).assert()
        RuleTestCase(
            "3.14[15]",
            convertRecurringDecimalToFractionDirectly,
            "[31415 - 314 / 9900]"
        ).assert()
        RuleTestCase(
            "3.[15]",
            convertRecurringDecimalToFractionDirectly,
            "[315 - 3 / 99]"
        ).assert()
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
    }
}
