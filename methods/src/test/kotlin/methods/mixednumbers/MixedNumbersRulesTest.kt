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
        testRule("[2 1/0]", splitMixedNumber, "UNDEFINED")
        testRule("[2 0/0]", splitMixedNumber, "UNDEFINED")
        testRule("[5 0/5]", splitMixedNumber, "5")
    }

    @Test
    fun testConvertSumOfIntegerAndProperFractionToMixedNumber() {
        testRule("4 + [13 / 12]", convertSumOfIntegerAndProperFractionToMixedNumber, null)
        testRule("3 + [11 / 12]", convertSumOfIntegerAndProperFractionToMixedNumber, "[3 11/12]")
    }
}
