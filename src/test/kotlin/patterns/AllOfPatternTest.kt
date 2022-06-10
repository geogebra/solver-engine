package patterns

import engine.expressions.RootPath
import engine.expressions.Subexpression
import engine.expressions.parsePath
import engine.expressions.xp
import engine.patterns.FixedPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.allOf
import engine.patterns.sumContaining
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
        val matches = pattern.findMatches(Subexpression(RootPath, expr))
        assertEquals(1, matches.count())
        val match = matches.first()
        assertEquals(xp(1), match.getBoundExpr(n))
        assertEquals(match.getBoundPaths(x), listOf(parsePath("./1")))
    }
}
