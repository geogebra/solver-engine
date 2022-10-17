package methods.approximation

import methods.rules.testRule
import org.junit.jupiter.api.Test

class ApproximationRulesTest {

    @Test
    fun testRoundTerminatingDecimal() {
        testRule("42.123", roundTerminatingDecimal, null)
        testRule("3.1415", roundTerminatingDecimal, "3.142")
        testRule("0.0001", roundTerminatingDecimal, "0.000")
    }

    @Test
    fun testExpandRecurringDecimal() {
        testRule("0.[6]", expandRecurringDecimal, "0.666[6]")
        testRule("0.12[34]", expandRecurringDecimal, null)
        testRule("0.[123]", expandRecurringDecimal, "0.123[123]")
    }

    @Test
    fun testRoundRecurringDecimal() {
        testRule("2.7182[82]", roundRecurringDecimal, "2.718")
        testRule("0.12[3]", roundRecurringDecimal, null)
        testRule("167.12[35]", roundRecurringDecimal, "167.124")
    }

    @Test
    fun testApproximateDecimalProductAndDivision() {
        testRule("3.5001 * 1.9999", approximateDecimalProductAndDivision, "7.000")
        testRule("2.1 * 3", approximateDecimalProductAndDivision, "6.300")
        testRule("1 : 3", approximateDecimalProductAndDivision, "0.333")
        testRule("4 : 0.0", approximateDecimalProductAndDivision, null)
    }
}
