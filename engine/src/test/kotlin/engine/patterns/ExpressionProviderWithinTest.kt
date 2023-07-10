package engine.patterns

import engine.context.emptyContext
import engine.expressions.RootOrigin
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

class ExpressionProviderWithinTest {

    @Test
    fun testExpressionProviderWithin() {
        val expression = parseExpression("[2*4/2*7]").withOrigin(RootOrigin())

        val common = AnyPattern()
        val numerator = productContaining(common)
        val denominator = productContaining(common)
        val fraction = fractionOf(numerator, denominator)

        val match = fraction.findMatches(emptyContext, RootMatch, expression).single()
        val topCommon = common.within(numerator).getBoundExpr(match)!!
        assertEquals("./0/0", topCommon.origin.path.toString())
        val bottomCommon = common.within(denominator).getBoundExpr(match)!!
        assertEquals("./1/0", bottomCommon.origin.path.toString())
    }
}
