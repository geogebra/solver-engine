/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package engine.parser

import engine.expressions.Constants
import engine.expressions.Decorator
import engine.expressions.Expression
import engine.expressions.ExpressionWithConstraint
import engine.expressions.FiniteSet
import engine.expressions.Inequation
import engine.expressions.ListExpression
import engine.expressions.Product
import engine.expressions.SetDifference
import engine.expressions.VoidExpression
import engine.expressions.absoluteValueOf
import engine.expressions.arsinhOf
import engine.expressions.bracketOf
import engine.expressions.cartesianProductOf
import engine.expressions.contradictionOf
import engine.expressions.curlyBracketOf
import engine.expressions.definiteIntegralOf
import engine.expressions.derivativeOf
import engine.expressions.divideBy
import engine.expressions.equationOf
import engine.expressions.explicitProductOf
import engine.expressions.expressionOf
import engine.expressions.finiteSetOf
import engine.expressions.fractionOf
import engine.expressions.greaterThanEqualOf
import engine.expressions.greaterThanOf
import engine.expressions.identityOf
import engine.expressions.implicitSolutionOf
import engine.expressions.indefiniteIntegralOf
import engine.expressions.inequationOf
import engine.expressions.lessThanEqualOf
import engine.expressions.lessThanOf
import engine.expressions.logBase10Of
import engine.expressions.logOf
import engine.expressions.matrixOf
import engine.expressions.missingBracketOf
import engine.expressions.nameXp
import engine.expressions.naturalLogOf
import engine.expressions.negOf
import engine.expressions.openIntervalOf
import engine.expressions.percentageOf
import engine.expressions.percentageOfOf
import engine.expressions.plusMinusOf
import engine.expressions.plusOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.rawRootOf
import engine.expressions.setSolutionOf
import engine.expressions.setUnionOf
import engine.expressions.sinOf
import engine.expressions.squareBracketOf
import engine.expressions.squareRootOf
import engine.expressions.statementSystemOf
import engine.expressions.statementUnionOf
import engine.expressions.sumOf
import engine.expressions.tupleOf
import engine.expressions.variableListOf
import engine.expressions.vectorOf
import engine.expressions.xp
import engine.operators.SumOperator
import parser.parseExpression
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ParserTest {
    private fun rawSumOf(vararg terms: Expression) =
        expressionOf(
            SumOperator,
            terms.asList(),
        )

    private fun rawPartialSumOf(vararg terms: Expression) = rawSumOf(*terms).decorate(Decorator.PartialBracket)

    private fun rawProductOf(vararg factors: Expression) = Product(factors.asList())

    private fun parsingFails(input: String) {
        assertFails {
            parseExpression(input)
        }
    }

    private fun parsesTo(input: String, expr: Expression) {
        val parsed = parseExpression(input)
        assertEquals(expr, parsed, input)
    }

    @Test
    fun testInvalidInput() {
        parsingFails("1+")
        parsingFails("[1/2")
        parsingFails("?x")
        parsingFails("[1/3]??")
        parsingFails("1-<.2+3.>")
        parsingFails("1 <. +2+3 .>")
        parsingFails("mat[1, 2 // 3, 4, 5]")
    }

    @Test
    fun testConstants() {
        parsesTo("/undefined/", Constants.Undefined)
        parsesTo("/infinity/", Constants.Infinity)
        parsesTo("/pi/", Constants.Pi)
        parsesTo("/e/", Constants.E)
        parsesTo("/i/", Constants.ImaginaryUnit)
    }

    @Test
    fun testName() {
        parsesTo("\"xx\"", nameXp("xx"))
        parsesTo("\"(eq1)\"", nameXp("(eq1)"))
    }

    @Test
    fun testVariable() {
        parsesTo("x", xp("x"))
        parsesTo("d", xp("d"))
        parsesTo("Z", xp("Z"))
        parsesTo("x_1", xp("x", "1"))
        parsesTo("\\alpha_x", xp("\\alpha", "x"))
        parsesTo("\\Omega_010", xp("\\Omega", "010"))
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
            rawProductOf(
                xp(2),
                powerOf(xp("x"), xp(3)),
                powerOf(xp("y"), xp(5)),
            ),
        )
        parsesTo(
            "[1/2][x^3]",
            rawProductOf(
                fractionOf(xp(1), xp(2)),
                powerOf(xp("x"), xp(3)),
            ),
        )
        parsesTo(
            "xyz",
            rawProductOf(xp("x"), xp("y"), xp("z")),
        )
        parsesTo(
            "[(x+1)^2]",
            powerOf(bracketOf(rawSumOf(xp("x"), xp(1))), xp(2)),
        )
    }

    @Test
    fun testProducts() {
        // Should fail
        parsesTo("2[2^2]", rawProductOf(xp(2), powerOf(xp(2), xp(2))))
        // Should give warning but be accepted
        parsesTo("2[1 / 3]", rawProductOf(xp(2), fractionOf(xp(1), xp(3))))
        parsesTo("abcd", rawProductOf(xp("a"), xp("b"), xp("c"), xp("d")))
    }

    @Test
    fun testDivisions() {
        parsesTo(
            "abc : cd",
            rawProductOf(
                xp("a"),
                xp("b"),
                xp("c"),
                divideBy(missingBracketOf(rawProductOf(xp("c"), xp("d")))),
            ),
        )
    }

    @Test
    fun testPowerOfAbsoluteValue() {
        parsesTo(
            "[abs[x] ^ 2]",
            powerOf(absoluteValueOf(xp("x")), xp(2)),
        )
        parsesTo(
            "[abs[x + 1] ^ 3]",
            powerOf(absoluteValueOf(sumOf(xp("x"), xp(1))), xp(3)),
        )
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
                        explicitProductOf(
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
            productOf(squareRootOf(xp(2)), squareRootOf(xp(3))),
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
    fun testLogarithms() {
        parsesTo("ln 3x", naturalLogOf(productOf(xp(3), xp("x"))))
        parsesTo("log 9 - 12", sumOf(logBase10Of(xp(9)), negOf(xp(12))))
        parsesTo(
            "1 + 4 log[5] [3 / 2]",
            sumOf(xp(1), productOf(xp(4), logOf(xp(5), fractionOf(xp(3), xp(2))))),
        )
    }

    @Test
    fun testTrigonometricFunctions() {
        parsesTo("sin /pi/x", sinOf(productOf(Constants.Pi, xp("x"))))
        parsesTo(
            "x arsinh [3 /pi/ / 2] + 1",
            sumOf(productOf(xp("x"), arsinhOf(fractionOf(productOf(xp(3), Constants.Pi), xp(2)))), xp(1)),
        )
    }

    @Test
    fun testPercentages() {
        parsesTo("3.14%", percentageOf(xp(3.14)))
        parsesTo(
            "10 %of 50 + 25 %of 100",
            sumOf(percentageOfOf(xp(10), xp(50)), percentageOfOf(xp(25), xp(100))),
        )
    }

    @Test
    fun testCalculus() {
        parsesTo("diff[sin x / x]", derivativeOf(sinOf(xp("x")), xp("x")))
        parsesTo(
            "[diff ^ 2][sin x * sin y / x y]",
            derivativeOf(Constants.Two, productOf(sinOf(xp("x")), sinOf(xp("y"))), xp("x"), xp("y")),
        )
        parsesTo(
            "[diff ^ 2][sin x / [x ^ 2]]",
            derivativeOf(Constants.Two, sinOf(xp("x")), powerOf(xp("x"), xp(2))),
        )
        parsesTo(
            "prim[arsinh(x + 1), x]",
            indefiniteIntegralOf(arsinhOf(sumOf(xp("x"), xp(1))), xp("x")),
        )
        parsesTo(
            "int[-/infinity/, /pi/, 3x + 1, x]",
            definiteIntegralOf(
                Constants.NegativeInfinity,
                Constants.Pi,
                sumOf(productOf(xp(3), xp("x")), xp(1)),
                xp("x"),
            ),
        )
    }

    @Test
    fun testLinearAlgebra() {
        parsesTo("vec[1, 2, 3]", vectorOf(xp(1), xp(2), xp(3)))
        parsesTo("mat[1, 2; 3, 4]", matrixOf(listOf(xp(1), xp(2)), listOf(xp(3), xp(4))))
    }

    @Test
    fun testEquations() {
        parsesTo(
            "3x + 4 = 4x - 5",
            equationOf(
                rawSumOf(rawProductOf(xp(3), xp("x")), xp(4)),
                rawSumOf(rawProductOf(xp(4), xp("x")), negOf(xp(5))),
            ),
        )
    }

    @Test
    fun testInequations() {
        parsesTo(
            "3x + 4 != 4x - 5",
            Inequation(
                rawSumOf(rawProductOf(xp(3), xp("x")), xp(4)),
                rawSumOf(rawProductOf(xp(4), xp("x")), negOf(xp(5))),
            ),
        )
    }

    @Test
    fun testInequalities() {
        parsesTo(
            "3x + 4 < 4x - 5",
            lessThanOf(
                rawSumOf(rawProductOf(xp(3), xp("x")), xp(4)),
                rawSumOf(rawProductOf(xp(4), xp("x")), negOf(xp(5))),
            ),
        )
        parsesTo(
            "sqrt[xy] <= [x + y / 2]",
            lessThanEqualOf(
                squareRootOf(rawProductOf(xp("x"), xp("y"))),
                fractionOf(sumOf(xp("x"), xp("y")), xp(2)),
            ),
        )
        parsesTo(
            "3x + 4 > 4x - 5",
            greaterThanOf(
                rawSumOf(rawProductOf(xp(3), xp("x")), xp(4)),
                rawSumOf(rawProductOf(xp(4), xp("x")), negOf(xp(5))),
            ),
        )
        parsesTo(
            "sqrt[xy] >= [2xy / x + y]",
            greaterThanEqualOf(
                squareRootOf(rawProductOf(xp("x"), xp("y"))),
                fractionOf(rawProductOf(xp(2), xp("x"), xp("y")), sumOf(xp("x"), xp("y"))),
            ),
        )
        parsesTo(
            "sqrt[xy] != [2xy / x + y]",
            inequationOf(
                squareRootOf(rawProductOf(xp("x"), xp("y"))),
                fractionOf(rawProductOf(xp(2), xp("x"), xp("y")), sumOf(xp("x"), xp("y"))),
            ),
        )
    }

    @Test
    fun testInequalitySystem() {
        parsesTo(
            "3x + 4 > 4x - 5 AND 3x + 5 < 6x + 7",
            statementSystemOf(
                greaterThanOf(
                    rawSumOf(rawProductOf(xp(3), xp("x")), xp(4)),
                    rawSumOf(rawProductOf(xp(4), xp("x")), negOf(xp(5))),
                ),
                lessThanOf(
                    rawSumOf(rawProductOf(xp(3), xp("x")), xp(5)),
                    rawSumOf(rawProductOf(xp(6), xp("x")), xp(7)),
                ),
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
    fun testExpressionWithConstraint() {
        parsesTo(
            "x + 1 GIVEN x > 0",
            ExpressionWithConstraint(sumOf(xp("x"), xp(1)), greaterThanOf(xp("x"), xp(0))),
        )
    }

    @Test
    fun testSets() {
        parsesTo("/reals/", Constants.Reals)
        parsesTo(
            "/reals/ \\ {1, 2, 3}",
            SetDifference(
                Constants.Reals,
                FiniteSet(listOf(xp(1), xp(2), xp(3))),
            ),
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
            "SetSolution[x: {1}]",
            setSolutionOf(variableListOf("x"), finiteSetOf(xp(1))),
        )
        parsesTo(
            "SetSolution[x, y : {(1, 2)}]",
            setSolutionOf(variableListOf("x", "y"), finiteSetOf(tupleOf(xp(1), xp(2)))),
        )
        parsesTo(
            "SetSolution[x, y : {1} * /reals/]",
            setSolutionOf(variableListOf("x", "y"), cartesianProductOf(finiteSetOf(xp(1)), Constants.Reals)),
        )
        parsesTo(
            "SetSolution[x : SetUnion[{1}, (2, 3)]]",
            setSolutionOf(variableListOf("x"), setUnionOf(finiteSetOf(xp(1)), openIntervalOf(xp(2), xp(3)))),
        )
    }

    @Test
    fun testStatements() {
        parsesTo(
            "x=1 GIVEN x<0 OR x=2",
            statementUnionOf(
                ExpressionWithConstraint(equationOf(xp("x"), xp(1)), lessThanOf(xp("x"), xp(0))),
                equationOf(xp("x"), xp(2)),
            ),
        )
    }

    @Test
    fun testList() {
        parsesTo(
            "3x + 2, 4x + 5",
            ListExpression(
                listOf(
                    sumOf(
                        productOf(xp(3), xp("x")),
                        xp(2),
                    ),
                    sumOf(
                        productOf(xp(4), xp("x")),
                        xp(5),
                    ),
                ),
            ),
        )
        parsesTo(
            "a = b, c != d, e < f",
            ListExpression(
                listOf(
                    equationOf(xp("a"), xp("b")),
                    inequationOf(xp("c"), xp("d")),
                    lessThanOf(xp("e"), xp("f")),
                ),
            ),
        )
    }

    @Test
    fun testVoid() {
        parsesTo(
            "/void/",
            VoidExpression(),
        )
    }
}
