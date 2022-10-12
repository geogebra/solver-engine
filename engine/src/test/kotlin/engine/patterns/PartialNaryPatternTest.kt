package engine.patterns

import engine.expressions.Subexpression
import engine.expressions.xp
import engine.operators.NaryOperator
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

class PartialNaryPatternTest {

    @Test
    fun testSinglePattern() {
        val expression = parseExpression("1 + 2 + 3")
        val terms = expression.operands

        val integerPattern = UnsignedIntegerPattern()
        val ptn = PartialNaryPattern(NaryOperator.Sum, listOf(integerPattern))

        val matches = ptn.findMatches(Subexpression(expression), RootMatch)
        assertEquals(terms.count(), matches.count())

        for ((index, match) in matches.withIndex()) {
            assertEquals((index + 1).toBigInteger(), integerPattern.getBoundInt(match))
        }
    }

    @Test
    fun testTwoPatterns() {
        val expression = parseExpression("1 + 2 + 3")
        val ptn = PartialNaryPattern(NaryOperator.Sum, listOf(UnsignedIntegerPattern(), UnsignedIntegerPattern()))

        val matches = ptn.findMatches(Subexpression(expression), RootMatch)
        assertEquals(3, matches.count())
    }

    @Test
    fun testTwoDependentPatterns() {
        val expression = parseExpression("1 + 2 + 1 + 3")
        val intPtn = UnsignedIntegerPattern()
        val ptn = PartialNaryPattern(NaryOperator.Sum, listOf(intPtn, intPtn))
        val matches = ptn.findMatches(Subexpression(expression), RootMatch)
        assertEquals(1, matches.count())
    }

    @Test
    fun testTwoDependentPatternsWithNoMatch() {
        val expression = parseExpression("1 + 2 + x + 3")
        val intPtn = UnsignedIntegerPattern()
        val ptn = PartialNaryPattern(NaryOperator.Sum, listOf(intPtn, intPtn))
        val matches = ptn.findMatches(Subexpression(expression), RootMatch)
        assertEquals(0, matches.count())
    }

    @Test
    fun testComplexPattern() {
        val expression = parseExpression("1 + 2 + 1 + 1 + x + 3 + 4")
        val intPtn = UnsignedIntegerPattern()
        val ptn =
            PartialNaryPattern(NaryOperator.Sum, listOf(intPtn, intPtn, VariablePattern(), UnsignedIntegerPattern()))
        val matches = ptn.findMatches(Subexpression(expression), RootMatch)
        assertEquals(6, matches.count())
    }

    @Test
    fun testCommonExpressionInFraction() {
        val common = AnyPattern()
        val numerator = PartialNaryPattern(NaryOperator.Product, listOf(common))
        val denominator = PartialNaryPattern(NaryOperator.Product, listOf(common))

        val ptn = fractionOf(numerator, denominator)

        val expression = parseExpression("[x*y*z/a*y*c]")

        val matches = ptn.findMatches(Subexpression(expression), RootMatch)

        assertEquals(1, matches.count())
        assertEquals(xp("y"), matches.first().getBoundExpr(common))
    }

    @Test
    fun getRest() {
        val expression = parseExpression("1 + 2 + 1 + 3")
        val intPtn = UnsignedIntegerPattern()
        val ptn = PartialNaryPattern(NaryOperator.Sum, listOf(intPtn, intPtn))

        val matches = ptn.findMatches(Subexpression(expression), RootMatch)
        assertEquals(1, matches.count())
        val match = matches.elementAt(0)
        val rest = ptn.getRestSubexpressions(match)
        assertEquals(rest.size, 2)
        assertEquals(xp(2), rest[0].expr)
        assertEquals(xp(3), rest[1].expr)
    }
}
