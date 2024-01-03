package methods.fallback

import engine.methods.testRule
import org.junit.jupiter.api.Test

class FallbackRulesTest {
    @Test
    fun testExpressionIsFullySimplified() {
        testRule("2", FallbackRules.ExpressionIsFullySimplified, "/void/")
        testRule("x+2", FallbackRules.ExpressionIsFullySimplified, "/void/")
        testRule("2[x^3]", FallbackRules.ExpressionIsFullySimplified, "/void/")
        testRule("1 + [x^2]", FallbackRules.ExpressionIsFullySimplified, "/void/")
        testRule("x(x+1)", FallbackRules.ExpressionIsFullySimplified, null)
    }
}
