package engine.parser

import engine.expressions.Expression
import engine.expressions.bracketOf
import engine.expressions.curlyBracketOf
import engine.expressions.equationOf
import engine.expressions.fractionOf
import engine.expressions.implicitProductOf
import engine.expressions.missingBracketOf
import engine.expressions.negOf
import engine.expressions.plusOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.rootOf
import engine.expressions.squareBracketOf
import engine.expressions.squareRootOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.operators.UndefinedOperator
import parser.parseExpression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ParserTest {

    private fun parsingFails(input: String) {
        assertFails {
            parseExpression(input)
        }
    }

    private fun parsesTo(input: String, expr: Expression) {
        assertEquals(expr, parseExpression(input), input)
    }

    @Test
    fun testInvalidInput() {
        parsingFails("1+")
        parsingFails("[1/2")
        parsingFails("?x")
        parsingFails("[1/3]??")
    }

    @Test
    fun testUndefined() {
        parsesTo("UNDEFINED", Expression(UndefinedOperator, emptyList()))
    }

    @Test
    fun testOperators() {
        parsesTo("1+2", sumOf(xp(1), xp(2)))
        parsesTo("3-2*5", sumOf(xp(3), negOf(productOf(xp(2), xp(5)))))
        parsesTo(
            "(-5+2)*7",
            productOf(
                bracketOf(sumOf(negOf(xp(5)), xp(2))),
                xp(7)
            )
        )
        parsesTo(
            "[[1/2]/[3/4]]",
            fractionOf(
                fractionOf(xp(1), xp(2)),
                fractionOf(xp(3), xp(4))
            )
        )
        parsesTo(
            "2[x^3][y^5]",
            implicitProductOf(
                xp(2),
                powerOf(xp("x"), xp(3)),
                powerOf(xp("y"), xp(5))
            )
        )
        parsesTo(
            "[1/2][x^3]",
            implicitProductOf(
                fractionOf(xp(1), xp(2)),
                powerOf(xp("x"), xp(3))
            )
        )
        parsesTo(
            "xyz",
            implicitProductOf(xp("x"), xp("y"), xp("z"))
        )
        parsesTo(
            "[(x+1)^2]",
            powerOf(bracketOf(sumOf(xp("x"), xp(1))), xp(2))
        )
        parsesTo("2[2^2]", implicitProductOf(xp(2), powerOf(xp(2), xp(2)))) // Should that be correct?
    }

    @Test
    fun testTrickyMinusSigns() {
        parsesTo("3*-4", productOf(xp(3), missingBracketOf(negOf(xp(4)))))
        parsesTo("-4*3", negOf(productOf(xp(4), xp(3))))
        parsesTo("--2", negOf(missingBracketOf(negOf(xp(2)))))
        parsesTo("1+-3", sumOf(xp(1), missingBracketOf(negOf(xp(3)))))
        parsesTo(
            "3*+-+2",
            productOf(
                xp(3),
                missingBracketOf(plusOf(missingBracketOf(negOf(missingBracketOf(plusOf(xp(2)))))))
            )
        )
    }

    @Test
    fun testBracketShapes() {
        parsesTo(
            "{.1 + [.2 * (3 - 6).].}",
            curlyBracketOf(
                sumOf(
                    xp(1),
                    squareBracketOf(
                        productOf(
                            xp(2),
                            bracketOf(
                                sumOf(xp(3), negOf(xp(6)))
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    fun testRoots() {
        parsesTo(
            "sqrt[2] * sqrt[3]",
            productOf(squareRootOf(xp(2)), squareRootOf(xp(3)))
        )
        parsesTo(
            "[root[2, 3] / root[4, 5]]",
            fractionOf(rootOf(xp(2), xp(3)), rootOf(xp(4), xp(5)))
        )
        parsesTo(
            "[sqrt[3] ^ 2]",
            powerOf(missingBracketOf(squareRootOf(xp(3))), xp(2))
        )
        parsesTo(
            "root[2, 3] * [sqrt[3] ^ 2] * [root[4, 5] ^ x]",
            productOf(
                rootOf(xp(2), xp(3)),
                powerOf(missingBracketOf(squareRootOf(xp(3))), xp(2)),
                powerOf(missingBracketOf(rootOf(xp(4), xp(5))), xp("x"))
            )
        )
    }

    @Test
    fun testEquations() {
        parsesTo(
            "3x + 4 = 4x - 5",
            equationOf(
                sumOf(implicitProductOf(xp(3), xp("x")), xp(4)),
                sumOf(implicitProductOf(xp(4), xp("x")), negOf(xp(5)))
            )
        )
    }
}
