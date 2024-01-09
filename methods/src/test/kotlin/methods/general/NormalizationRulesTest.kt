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

package methods.general

import engine.expressions.Decorator
import engine.methods.testMethod
import engine.methods.testRule
import methods.general.NormalizationRules.NormalizeNegativeSignOfIntegerInSum
import methods.general.NormalizationRules.RemoveBracketSumInSum
import methods.general.NormalizationRules.RemoveRedundantBracket
import org.junit.jupiter.api.Test
import parser.parseExpression

class NormalizationRulesTest {
    @Test
    fun testRemoveRedundantPlusSign() {
        testMethod {
            method = NormalizationRules.RemoveRedundantPlusSign
            inputExpr = "+1"

            check {
                toExpr = "1"

                shift("./0", ".")
                cancel(".:op")
            }
        }
    }

    @Test
    fun testAddClarifyingBracket() {
        val squareRoot = parseExpression("sqrt[2]").decorate(Decorator.MissingBracket)
        testRule(
            squareRoot,
            NormalizationRules.AddClarifyingBracket,
            "(sqrt[2])",
            testWithoutBrackets = false,
        )
    }
}

class RemoveBracketsTest {
    @Test
    fun testRemoveBracketsSumInSum() {
        testRule("1 + 2", RemoveBracketSumInSum, null)
        testRule("1 + (2 + 3) + 4", RemoveBracketSumInSum, "1 + 2 + 3 + 4")
        testRule("1 + (2 + x) + (3 + [x^2])", RemoveBracketSumInSum, "1 + 2 + x + (3 + [x^2])")
        testMethod {
            method = RemoveBracketSumInSum
            inputExpr = "1 + (2 + x)"

            check {
                toExpr = "1 + 2 + x"

                shift("./0", "./0")
                shift("./1/0", "./1")
                shift("./1/1", "./2")
                cancel("./1:decorator")
            }
        }
    }

    @Test
    fun testRemoveBracketProductInProduct() {
        testMethod {
            method = NormalizationRules.RemoveBracketProductInProduct
            inputExpr = "2 (2 sqrt[2]) (3 root[3, 3])"

            check {
                toExpr = "2 * 2 sqrt[2] (3 root[3, 3])"

                shift("./0", "./0")
                shift("./1/0", "./1")
                shift("./1/1", "./2")
                shift("./2", "./3")
                cancel("./1:decorator")
            }
        }
    }

    @Test
    fun testRemoveBracketAroundSignedIntegerInSum() {
        testMethod {
            method = NormalizeNegativeSignOfIntegerInSum
            inputExpr = "1 + (-1)"

            check {
                toExpr = "1 - 1"
                shift("./0", "./0")
                shift("./1", "./1")
                cancel("./1:decorator", "./1:outerOp")
            }
        }

        testMethod {
            method = NormalizeNegativeSignOfIntegerInSum
            inputExpr = "(-1) + 1"

            check {
                toExpr = "-1 + 1"
                shift("./0", "./0")
                shift("./1", "./1")
                cancel("./0:decorator", "./0:outerOp")
            }
        }

        testRule("{.-4.} - 3", NormalizeNegativeSignOfIntegerInSum, "-4 - 3")
        testRule("x + (-2)", NormalizeNegativeSignOfIntegerInSum, "x - 2")
        testRule("{.((-5)).} + u", NormalizeNegativeSignOfIntegerInSum, "-5 + u")
    }

    @Test
    fun testRemoveOuterBrackets() {
        testMethod {
            method = RemoveRedundantBracket
            inputExpr = "(x + y)"

            check {
                toExpr = "x + y"

                shift(".", ".")
                cancel(".:decorator")
            }
        }

        testRule("(1)", RemoveRedundantBracket, "1")
    }
}
