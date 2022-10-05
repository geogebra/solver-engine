package methods.mixednumbers

import methods.rules.testRule
import org.junit.jupiter.api.Test

class MixedNumbersRulesTest {

    @Test
    fun testFractionToMixedNumber() {
        testRule("[4 / 21]", fractionToMixedNumber, null)
        testRule("[21 / 4]", fractionToMixedNumber, "[5 1/4]")
    }

    @Test
    fun testSplitMixedNumber() {
        testRule("[2 3/4]", splitMixedNumber, "2 + [3/4]")
    }

    @Test
    fun testConvertSumOfIntegerAndProperFractionToMixedNumber() {
        testRule("4 + [13 / 12]", convertSumOfIntegerAndProperFractionToMixedNumber, null)
        testRule("3 + [11 / 12]", convertSumOfIntegerAndProperFractionToMixedNumber, "[3 11/12]")
    }
}
