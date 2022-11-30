package methods.mixednumbers

import engine.methods.testRule
import methods.mixednumbers.MixedNumbersRules.ConvertSumOfIntegerAndProperFractionToMixedNumber
import methods.mixednumbers.MixedNumbersRules.FractionToMixedNumber
import methods.mixednumbers.MixedNumbersRules.SplitMixedNumber
import org.junit.jupiter.api.Test

class MixedNumbersRulesTest {

    @Test
    fun testFractionToMixedNumber() {
        testRule("[4 / 21]", FractionToMixedNumber, null)
        testRule("[21 / 4]", FractionToMixedNumber, "[5 1/4]")
    }

    @Test
    fun testSplitMixedNumber() {
        testRule("[2 3/4]", SplitMixedNumber, "2 + [3/4]")
        testRule("[2 1/0]", SplitMixedNumber, "UNDEFINED")
        testRule("[2 0/0]", SplitMixedNumber, "UNDEFINED")
        testRule("[5 0/5]", SplitMixedNumber, "5")
    }

    @Test
    fun testConvertSumOfIntegerAndProperFractionToMixedNumber() {
        testRule("4 + [13 / 12]", ConvertSumOfIntegerAndProperFractionToMixedNumber, null)
        testRule("3 + [11 / 12]", ConvertSumOfIntegerAndProperFractionToMixedNumber, "[3 11/12]")
    }
}
