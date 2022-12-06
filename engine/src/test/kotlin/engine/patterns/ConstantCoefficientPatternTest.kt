package engine.patterns

import engine.context.Context
import engine.expressions.Root
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

class ConstantCoefficientPatternTest {

    private fun assertCoefficientEquals(expr: String, coefficient: String?) {
        val solutionVariablePattern = SolutionVariablePattern()
        val constantCoefficientPattern = withOptionalConstantCoefficient(solutionVariablePattern)

        val exprValue = parseExpression(expr).withOrigin(Root())

        val matches = constantCoefficientPattern
            .findMatches(Context(solutionVariable = "x"), RootMatch, exprValue)

        if (coefficient == null) {
            assertEquals(0, matches.count())
        } else {
            val coefficientValue = parseExpression(coefficient)
            assertEquals(coefficientValue, constantCoefficientPattern.coefficient(matches.single()))
        }
    }

    @Test
    fun testConstantCoefficientPattern() {
        assertCoefficientEquals("x", "1")
        assertCoefficientEquals("y", null)
        assertCoefficientEquals("x * y", null)
        assertCoefficientEquals("[3 * x / 2]", "[3 / 2]")
        assertCoefficientEquals("[3 * x / 2 * sqrt[3]]", "[3 / 2 * sqrt[3]]")
        assertCoefficientEquals("[x / 2 * sqrt[3]]", "[1 / 2 * sqrt[3]]")
        assertCoefficientEquals("[x / 2 * y]", null)
    }
}
