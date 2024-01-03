package engine.patterns

import engine.context.emptyContext
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

class RationalPatternTest {
    @Test
    fun testRationalPattern() {
        val expr = parseExpression("-[1/2]")
        val pattern = RationalPattern()
        val matches = pattern.findMatches(emptyContext, RootMatch, expr)
        assertEquals(1, matches.count())
        val match = matches.first()
        assertEquals(expr, match.getBoundExpr(pattern))
    }
}
