package methods.general

import methods.rules.testRule
import org.junit.jupiter.api.Test

object AlgebraicFractionsTest {

    @Test
    fun testCancelCommonTerms() {
        testRule("[x*y*z/a*y*c]", cancelCommonTerms, "[x*z/a*c]")
        testRule("[5*2/5*3]", cancelCommonTerms, "[2/3]")
        testRule("[x*y/a*y]", cancelCommonTerms, "[x / a]")
    }
}
