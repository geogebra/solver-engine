package methods.approximation

import methods.rules.RuleTestCase
import org.junit.jupiter.api.Test

class ApproximationRulesTest {

    @Test
    fun testRoundTerminatingDecimal() {
        RuleTestCase("42.123", roundTerminatingDecimal, null).assert()
        RuleTestCase("3.1415", roundTerminatingDecimal, "3.142").assert()
        RuleTestCase("0.0001", roundTerminatingDecimal, "0.000").assert()
    }

    @Test
    fun testExpandRecurringDecimal() {
        RuleTestCase("0.[6]", expandRecurringDecimal, "0.666[6]").assert()
        RuleTestCase("0.12[34]", expandRecurringDecimal, null).assert()
        RuleTestCase("0.[123]", expandRecurringDecimal, "0.123[123]").assert()
    }

    @Test
    fun testRoundRecurringDecimal() {
        RuleTestCase("2.7182[82]", roundRecurringDecimal, "2.718").assert()
        RuleTestCase("0.12[3]", roundRecurringDecimal, null).assert()
        RuleTestCase("167.12[35]", roundRecurringDecimal, "167.124").assert()
    }
}
