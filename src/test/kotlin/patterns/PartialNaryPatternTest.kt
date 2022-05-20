package patterns

import expressions.*
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class PartialNaryPatternTest {

    @Test
    fun testSinglePattern() {
        val terms = listOf(IntegerExpr(1), IntegerExpr(2), IntegerExpr(3))
        val expression = NaryExpr(NaryOperator.Sum, terms)

        val integerPattern = IntegerPattern()
        val ptn = PartialNaryPattern(NaryOperator.Sum, listOf(integerPattern))

        val matches = ptn.findMatches(Subexpression(RootPath, expression), RootMatch)
        assertEquals(terms.count(), matches.count())

        for ((index, match) in matches.withIndex()) {
            assertEquals(terms[index], integerPattern.getBoundInt(match))
        }
    }

    @Test
    fun testTwoPatterns() {
        val expression = NaryExpr(NaryOperator.Sum, listOf(IntegerExpr(1), IntegerExpr(2), IntegerExpr(3)))
        val ptn = PartialNaryPattern(NaryOperator.Sum, listOf(IntegerPattern(), IntegerPattern()))

        val matches = ptn.findMatches(Subexpression(RootPath, expression), RootMatch)
        assertEquals(3, matches.count())
    }

    @Test
    fun testTwoDependentPatterns() {
        val expression =
            NaryExpr(NaryOperator.Sum, listOf(IntegerExpr(1), IntegerExpr(2), IntegerExpr(1), IntegerExpr(3)))
        val intPtn = IntegerPattern()
        val ptn = PartialNaryPattern(NaryOperator.Sum, listOf(intPtn, intPtn))
        val matches = ptn.findMatches(Subexpression(RootPath, expression), RootMatch)
        assertEquals(1, matches.count())
    }

    @Test
    fun testTwoDependentPatternsWithNoMatch() {
        val expression =
            NaryExpr(NaryOperator.Sum, listOf(IntegerExpr(1), IntegerExpr(2), VariableExpr("x"), IntegerExpr(3)))
        val intPtn = IntegerPattern()
        val ptn = PartialNaryPattern(NaryOperator.Sum, listOf(intPtn, intPtn))
        val matches = ptn.findMatches(Subexpression(RootPath, expression), RootMatch)
        assertEquals(0, matches.count())
    }

    @Test
    fun testComplexPattern() {
        val expression = NaryExpr(
            NaryOperator.Sum, listOf(
                IntegerExpr(1), IntegerExpr(2), IntegerExpr(1), IntegerExpr(1),
                VariableExpr("x"), IntegerExpr(3), IntegerExpr(4)
            )
        )
        val intPtn = IntegerPattern()
        val ptn = PartialNaryPattern(NaryOperator.Sum, listOf(intPtn, intPtn, VariablePattern(), IntegerPattern()))
        val matches = ptn.findMatches(Subexpression(RootPath, expression), RootMatch)
        assertEquals(6, matches.count())
    }

    @Test
    fun testCommonExpressionInFraction() {
        val common = AnyPattern()
        val numerator = PartialNaryPattern(NaryOperator.Product, listOf(common))
        val denominator = PartialNaryPattern(NaryOperator.Product, listOf(common))

        val ptn = fractionOf(numerator, denominator)

        val expression = fractionOf(
            productOf(VariableExpr("x"), VariableExpr("y"), VariableExpr("z")),
            productOf(VariableExpr("a"), VariableExpr("y"), VariableExpr("c"))
        )

        val matches = ptn.findMatches(Subexpression(RootPath, expression), RootMatch)

        assertEquals(1, matches.count())
        assertEquals(VariableExpr("y"), matches.first().getBoundExpr(common))
    }

    @Test
    fun getRest() {
        val expression =
            NaryExpr(NaryOperator.Sum, listOf(IntegerExpr(1), IntegerExpr(2), IntegerExpr(1), IntegerExpr(3)))
        val intPtn = IntegerPattern()
        val ptn = PartialNaryPattern(NaryOperator.Sum, listOf(intPtn, intPtn))

        val matches = ptn.findMatches(Subexpression(RootPath, expression), RootMatch)
        assertEquals(1, matches.count())
        val match = matches.elementAt(0)
        val rest = ptn.getRestSubexpressions(match)
        assertContentEquals(listOf<Expression>(IntegerExpr(2), IntegerExpr(3)), rest.map { it.expr })
    }
}