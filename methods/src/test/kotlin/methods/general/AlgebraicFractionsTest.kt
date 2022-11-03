package methods.general

import methods.general.GeneralRules.CancelCommonTerms
import methods.rules.testRule
import org.junit.jupiter.api.Test

object AlgebraicFractionsTest {

    @Test
    fun testCancelCommonTerms() {
        testRule("[x*y*z/a*y*c]", CancelCommonTerms, "[x*z/a*c]")
        testRule("[5*2/5*3]", CancelCommonTerms, "[2/3]")
        testRule("[x*y/a*y]", CancelCommonTerms, "[x / a]")
    }
}
