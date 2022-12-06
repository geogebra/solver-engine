package engine.patterns

import engine.context.emptyContext
import engine.expressions.Root
import engine.expressions.xp
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NaryPatternTest {

    @Test
    fun testSinglePattern() {
        val expression = parseExpression("1 + 2 + 3")
        val terms = expression.operands

        val integerPattern = UnsignedIntegerPattern()
        val ptn = sumContaining(integerPattern)

        val matches = ptn.findMatches(emptyContext, RootMatch, expression)
        assertEquals(terms.count(), matches.count())

        for ((index, match) in matches.withIndex()) {
            assertEquals((index + 1).toBigInteger(), integerPattern.getBoundInt(match))
        }
    }

    @Test
    fun testTwoPatterns() {
        val expression = parseExpression("1 + 2 + 3")
        val ptn = sumContaining(UnsignedIntegerPattern(), UnsignedIntegerPattern())

        val matches = ptn.findMatches(emptyContext, RootMatch, expression)
        assertEquals(3, matches.count())
    }

    @Test
    fun testTwoDependentPatterns() {
        val expression = parseExpression("1 + 2 + 1 + 3")
        val intPtn = UnsignedIntegerPattern()
        val ptn = sumContaining(intPtn, intPtn)
        val matches = ptn.findMatches(emptyContext, RootMatch, expression)
        assertEquals(1, matches.count())
    }

    @Test
    fun testTwoDependentPatternsWithNoMatch() {
        val expression = parseExpression("1 + 2 + x + 3")
        val intPtn = UnsignedIntegerPattern()
        val ptn = sumContaining(intPtn, intPtn)
        val matches = ptn.findMatches(emptyContext, RootMatch, expression)
        assertEquals(0, matches.count())
    }

    @Test
    fun testComplexPattern() {
        val expression = parseExpression("1 + 2 + 1 + 1 + x + 3 + 4")
        val intPtn = UnsignedIntegerPattern()
        val ptn = sumContaining(intPtn, intPtn, ArbitraryVariablePattern(), UnsignedIntegerPattern())
        val matches = ptn.findMatches(emptyContext, RootMatch, expression)
        assertEquals(6, matches.count())
    }

    @Test
    fun testCommonExpressionInFraction() {
        val common = AnyPattern()
        val numerator = productContaining(common)
        val denominator = productContaining(common)

        val ptn = fractionOf(numerator, denominator)

        val expression = parseExpression("[x*y*z/a*y*c]")

        val matches = ptn.findMatches(emptyContext, RootMatch, expression)

        assertEquals(1, matches.count())
        assertEquals(xp("y"), matches.first().getBoundExpr(common))
    }

    @Test
    fun testWithImplicitProducts() {
        val radicand = UnsignedIntegerPattern()
        val radical = squareRootOf(radicand)
        val product = productContaining(radical, radical)

        val expression = parseExpression("4 sqrt[2] * sqrt[2]")

        assertTrue(product.matches(emptyContext, expression))
    }

    @Test
    fun testSimpleProductPattern() {
        val integer = UnsignedIntegerPattern()
        val root = integerOrderRootOf(UnsignedIntegerPattern())
        val product = productOf(integer, root)

        val expression1 = parseExpression("3 * root[4, 5]")
        assertTrue(product.matches(emptyContext, expression1))

        val expression2 = parseExpression("3 root[4, 5]")
        assertTrue(product.matches(emptyContext, expression2))
    }

    @Test
    fun testNestedProductPattern() {
        val integer = UnsignedIntegerPattern()
        val variable1 = ArbitraryVariablePattern()
        val variable2 = ArbitraryVariablePattern()
        val product = productOf(integer, variable1, variable2)

        val expression = parseExpression("3x * y")
        assertTrue(product.matches(emptyContext, expression))
    }

    @Test
    fun getRest() {
        val expression = parseExpression("1 + 2 + 1 + 3").withOrigin(Root())
        val intPtn = UnsignedIntegerPattern()
        val ptn = sumContaining(intPtn, intPtn)

        val matches = ptn.findMatches(emptyContext, RootMatch, expression)
        assertEquals(1, matches.count())
        val match = matches.elementAt(0)
        val rest = ptn.getRestSubexpressions(match)
        assertEquals(2, rest.size)
        assertEquals(xp(2), rest[0])
        assertEquals(xp(3), rest[1])
    }
}
