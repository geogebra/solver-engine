package methods.polynomials

import methods.rules.testRule
import org.junit.jupiter.api.Test

class PolynomialsRulesTest {

    @Test
    fun testCollectLikeTerms() {
        testRule("x + x", collectLikeTerms, "(1+1)*x")
        testRule("2*y - 3*y", collectLikeTerms, "(2 - 3)*y")
        testRule("z + [1/2]*z + [z / 2] - z*3", collectLikeTerms, "(1 + [1/2] + [1/2] - 3)*z")
        testRule("t*sqrt[3] + 2*t - [t*sqrt[2]/2]", collectLikeTerms, "(sqrt[3] + 2 - [sqrt[2]/2])*t")
    }
}
