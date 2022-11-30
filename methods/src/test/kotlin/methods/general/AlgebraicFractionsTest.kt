package methods.general

import engine.methods.testRule
import methods.general.GeneralRules.CancelCommonTerms
import org.junit.jupiter.api.Test

object AlgebraicFractionsTest {

    @Test
    fun testCancelCommonTerms() {
        testRule("[xyz/ayc]", CancelCommonTerms, "[x z / a c]")
        testRule("[5*2/5*3]", CancelCommonTerms, "[2 / 3]")
        testRule("[xy/ay]", CancelCommonTerms, "[x / a]")
    }
}
