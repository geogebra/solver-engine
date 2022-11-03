package methods.polynomials

import methods.polynomials.PolynomialRules.CollectLikeTerms
import methods.rules.testRule
import org.junit.jupiter.api.Test

class PolynomialsRulesTest {

    @Test
    fun testCollectLikeTerms() {
        testRule("x + x", CollectLikeTerms, "(1+1)*x")
        testRule("2*y - 3*y", CollectLikeTerms, "(2 - 3)*y")
        testRule("z + [1/2]*z + [z / 2] - z*3", CollectLikeTerms, "(1 + [1/2] + [1/2] - 3)*z")
        testRule("t*sqrt[3] + 2*t - [t*sqrt[2]/2]", CollectLikeTerms, "(sqrt[3] + 2 - [sqrt[2]/2])*t")
    }
}
