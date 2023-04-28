package engine.parser

import engine.expressions.Constants
import engine.expressions.Decorator
import engine.expressions.Expression
import engine.expressions.bracketOf
import engine.expressions.buildExpression
import engine.expressions.cartesianProductOf
import engine.expressions.contradictionOf
import engine.expressions.curlyBracketOf
import engine.expressions.equationOf
import engine.expressions.fractionOf
import engine.expressions.greaterThanEqualOf
import engine.expressions.greaterThanOf
import engine.expressions.identityOf
import engine.expressions.implicitProductOf
import engine.expressions.implicitSolutionOf
import engine.expressions.lessThanEqualOf
import engine.expressions.lessThanOf
import engine.expressions.missingBracketOf
import engine.expressions.negOf
import engine.expressions.plusMinusOf
import engine.expressions.plusOf
import engine.expressions.powerOf
import engine.expressions.rawRootOf
import engine.expressions.setSolutionOf
import engine.expressions.solutionSetOf
import engine.expressions.squareBracketOf
import engine.expressions.squareRootOf
import engine.expressions.sumOf
import engine.expressions.tupleOf
import engine.expressions.variableListOf
import engine.expressions.xp
import engine.operators.NaryOperator
import engine.operators.UndefinedOperator
import parser.parseExpression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ParserTest {

    private fun rawSumOf(vararg terms: Expression) = Expression(
        NaryOperator.Sum,
        terms.asList(),
    )

    private fun rawPartialSumOf(vararg terms: Expression) = rawSumOf(*terms).decorate(Decorator.PartialSumBracket)

    private fun rawProductOf(vararg factors: Expression) = Expression(
        NaryOperator.Product,
        factors.asList(),
    )

    private fun parsingFails(input: String) {
        assertFails {
            parseExpression(input)
        }
    }

    private fun parsesTo(input: String, expr: Expression) {
        val parsed = parseExpression(input)
        assertEquals(expr, parsed, input)
    }

    private fun parsesTo(input: String, exprString: String) {
        val parsed = parseExpression(input)
        assertEquals(exprString, parsed.toString())
    }

    @Test
    fun testInvalidInput() {
        parsingFails("1+")
        parsingFails("[1/2")
        parsingFails("?x")
        parsingFails("[1/3]??")
        parsingFails("1-<.2+3.>")
        parsingFails("1 <. +2+3 .>")
    }

    @Test
    fun testUndefined() {
        parsesTo("UNDEFINED", buildExpression(UndefinedOperator, emptyList()))
    }

    @Test
    fun testOperators() {
        parsesTo("1+2", rawSumOf(xp(1), xp(2)))
        parsesTo("3-2*5", rawSumOf(xp(3), negOf(rawProductOf(xp(2), xp(5)))))
        parsesTo(
            "(-5+2)*7",
            rawProductOf(
                bracketOf(rawSumOf(negOf(xp(5)), xp(2))),
                xp(7),
            ),
        )
        parsesTo(
            "[[1/2]/[3/4]]",
            fractionOf(
                fractionOf(xp(1), xp(2)),
                fractionOf(xp(3), xp(4)),
            ),
        )
        parsesTo(
            "2[x^3][y^5]",
            implicitProductOf(
                xp(2),
                powerOf(xp("x"), xp(3)),
                powerOf(xp("y"), xp(5)),
            ),
        )
        parsesTo(
            "[1/2][x^3]",
            implicitProductOf(
                fractionOf(xp(1), xp(2)),
                powerOf(xp("x"), xp(3)),
            ),
        )
        parsesTo(
            "xyz",
            implicitProductOf(xp("x"), xp("y"), xp("z")),
        )
        parsesTo(
            "[(x+1)^2]",
            powerOf(bracketOf(rawSumOf(xp("x"), xp(1))), xp(2)),
        )
        parsesTo("2[2^2]", implicitProductOf(xp(2), powerOf(xp(2), xp(2)))) // Should that be correct?
    }

    @Test
    fun testTrickyMinusSigns() {
        parsesTo("3*-4", rawProductOf(xp(3), missingBracketOf(negOf(xp(4)))))
        parsesTo("-4*3", negOf(rawProductOf(xp(4), xp(3))))
        parsesTo("--2", negOf(missingBracketOf(negOf(xp(2)))))
        parsesTo("1+-3", rawSumOf(xp(1), missingBracketOf(negOf(xp(3)))))
        parsesTo(
            "3*+-+2",
            rawProductOf(
                xp(3),
                missingBracketOf(plusOf(missingBracketOf(negOf(missingBracketOf(plusOf(xp(2))))))),
            ),
        )
    }

    @Test
    fun testBrackets() {
        parsesTo(
            "(+1 + (3))",
            bracketOf(rawSumOf(plusOf(xp(1)), bracketOf(xp(3)))),
        )
    }

    @Test
    fun testBracketShapes() {
        parsesTo(
            "{.1 + [.2 * (3 - 6).].}",
            curlyBracketOf(
                rawSumOf(
                    xp(1),
                    squareBracketOf(
                        rawProductOf(
                            xp(2),
                            bracketOf(
                                rawSumOf(xp(3), negOf(xp(6))),
                            ),
                        ),
                    ),
                ),
            ),
        )
    }

    @Test
    fun testRoots() {
        parsesTo(
            "sqrt[2] * sqrt[3]",
            rawProductOf(squareRootOf(xp(2)), squareRootOf(xp(3))),
        )
        parsesTo(
            "[root[2, 3] / root[4, 5]]",
            fractionOf(rawRootOf(xp(2), xp(3)), rawRootOf(xp(4), xp(5))),
        )
        parsesTo(
            "[sqrt[3] ^ 2]",
            powerOf(missingBracketOf(squareRootOf(xp(3))), xp(2)),
        )
        parsesTo(
            "root[2, 3] * [sqrt[3] ^ 2] * [root[4, 5] ^ x]",
            rawProductOf(
                rawRootOf(xp(2), xp(3)),
                powerOf(missingBracketOf(squareRootOf(xp(3))), xp(2)),
                powerOf(missingBracketOf(rawRootOf(xp(4), xp(5))), xp("x")),
            ),
        )
    }

    @Test
    fun testEquations() {
        parsesTo(
            "3x + 4 = 4x - 5",
            equationOf(
                rawSumOf(implicitProductOf(xp(3), xp("x")), xp(4)),
                rawSumOf(implicitProductOf(xp(4), xp("x")), negOf(xp(5))),
            ),
        )
    }

    @Test
    fun testInequalities() {
        parsesTo(
            "3x + 4 < 4x - 5",
            lessThanOf(
                rawSumOf(implicitProductOf(xp(3), xp("x")), xp(4)),
                rawSumOf(implicitProductOf(xp(4), xp("x")), negOf(xp(5))),
            ),
        )
        parsesTo(
            "sqrt[xy] <= [x + y / 2]",
            lessThanEqualOf(
                squareRootOf(implicitProductOf(xp("x"), xp("y"))),
                fractionOf(sumOf(xp("x"), xp("y")), xp(2)),
            ),
        )
        parsesTo(
            "3x + 4 > 4x - 5",
            greaterThanOf(
                rawSumOf(implicitProductOf(xp(3), xp("x")), xp(4)),
                rawSumOf(implicitProductOf(xp(4), xp("x")), negOf(xp(5))),
            ),
        )
        parsesTo(
            "sqrt[xy] >= [2xy / x + y]",
            greaterThanEqualOf(
                squareRootOf(implicitProductOf(xp("x"), xp("y"))),
                fractionOf(implicitProductOf(xp(2), xp("x"), xp("y")), sumOf(xp("x"), xp("y"))),
            ),
        )
    }

    @Test
    fun testPartialSum() {
        parsesTo(
            "<. 1 + 2 .> + 3",
            rawSumOf(rawPartialSumOf(xp(1), xp(2)), xp(3)),
        )
        parsesTo(
            "1 + <. 2 - 3 .>",
            rawSumOf(xp(1), rawPartialSumOf(xp(2), negOf(xp(3)))),
        )
        parsesTo(
            "x + <. -y + z .>",
            rawSumOf(xp("x"), rawPartialSumOf(negOf(xp("y")), xp("z"))),
        )
    }

    @Test
    fun testPlusMinus() {
        parsesTo(
            "+/-x",
            plusMinusOf(xp("x")),
        )
        parsesTo(
            "1 +/- 2",
            rawSumOf(xp(1), plusMinusOf(xp(2))),
        )
        parsesTo(
            "3 * +/-2",
            rawProductOf(xp(3), missingBracketOf(plusMinusOf(xp(2)))),
        )
        parsesTo(
            "x + +/-y",
            rawSumOf(xp("x"), missingBracketOf(plusMinusOf(xp("y")))),
        )
    }

    @Test
    fun testSolutions() {
        parsesTo(
            "Identity[x, y : 1 = 1]",
            identityOf(variableListOf("x", "y"), equationOf(xp(1), xp(1))),
        )
        parsesTo(
            "Contradiction[z : 1 = 0]",
            contradictionOf(variableListOf("z"), equationOf(xp(1), xp(0))),
        )
        parsesTo(
            "ImplicitSolution[x, y, z : x + y = z]",
            implicitSolutionOf(variableListOf("x", "y", "z"), equationOf(sumOf(xp("x"), xp("y")), xp("z"))),
        )
        parsesTo(
            "SetSolution[x : {1}]",
            setSolutionOf(variableListOf("x"), solutionSetOf(xp(1))),
        )
        parsesTo(
            "SetSolution[x, y : {(1, 2)}]",
            setSolutionOf(variableListOf("x", "y"), solutionSetOf(tupleOf(xp(1), xp(2)))),
        )
        parsesTo(
            "SetSolution[x, y : {1} * REALS]",
            setSolutionOf(variableListOf("x", "y"), cartesianProductOf(solutionSetOf(xp(1)), Constants.Reals)),
        )
    }
}
