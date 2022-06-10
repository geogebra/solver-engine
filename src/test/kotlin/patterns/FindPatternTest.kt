package patterns

import engine.expressions.RootPath
import engine.expressions.Subexpression
import engine.patterns.FindPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.productContaining
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class FindPatternTest {

    @Test
    fun testFindPattern() {
        val n1 = UnsignedIntegerPattern()
        val n2 = UnsignedIntegerPattern()
        val intProd = productContaining(n1, n2)
        val findPattern = FindPattern(intProd)

        val expr = parseExpression("2 + 3 * 5 + [5/3 * x * 3]")
        val matches = findPattern.findMatches(Subexpression(RootPath, expr))
        assertEquals(2, matches.count())
        assertContentEquals(
            listOf(15, 9).map { it.toBigInteger() },
            matches.map { n1.getBoundInt(it) * n2.getBoundInt(it) }.toList()
        )
    }
}
