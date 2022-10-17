package engine.patterns

import engine.expressions.Subexpression
import engine.expressions.parsePath
import engine.expressions.xp
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

class AllOfPatternTest {

    @Test
    fun simpleTest() {
        val expr = parseExpression("1+x")
        val n = UnsignedIntegerPattern()
        val x = FixedPattern(xp("x"))
        val pattern = allOf(sumContaining(n), sumContaining(x))
        val matches = pattern.findMatches(Subexpression(expr))
        assertEquals(1, matches.count())
        val match = matches.first()
        assertEquals(xp(1), match.getBoundExpr(n))
        assertEquals(match.getBoundPaths(x), listOf(parsePath("./1")))
    }

    @Test
    fun testRationalPattern() {
        val expr = parseExpression("-[1/2]")
        val pattern = RationalPattern()
        val matches = pattern.findMatches(Subexpression(expr))
        assertEquals(1, matches.count())
        val match = matches.first()
    }
}
