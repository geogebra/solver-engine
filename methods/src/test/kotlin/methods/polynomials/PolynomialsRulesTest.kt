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

package methods.polynomials

import engine.methods.testRule
import methods.polynomials.PolynomialRules.NormalizeMonomial
import methods.polynomials.PolynomialRules.NormalizePolynomial
import methods.polynomials.PolynomialRules.RearrangeProductOfMonomials
import org.junit.jupiter.api.Test

class PolynomialsRulesTest {
    @Test
    fun testRearrangeProductOfMonomials() {
        testRule("3 x y", RearrangeProductOfMonomials, null)
        testRule("3 x y * y", RearrangeProductOfMonomials, "3 x (y * y)")
        testRule("3 y x y", RearrangeProductOfMonomials, "3 x (y * y)")
        testRule("3 y x [y ^ 2]", RearrangeProductOfMonomials, "3 x (y * [y ^ 2])")
        testRule("3 y x * 4 [y ^ 2]", RearrangeProductOfMonomials, "(3 * 4) x (y * [y ^ 2])")
        testRule("y a * 2 x b", RearrangeProductOfMonomials, "2 a b x y")
        testRule(
            "[2 / 3] b [x ^ 3] y * 5 a b [x ^ 2] y",
            RearrangeProductOfMonomials,
            "([2 / 3] * 5) a (b * b) ([x ^ 3] * [x ^ 2]) (y * y)",
        )
    }

    @Test
    fun testNormalizePolynomial() {
        testRule("1 + 2", NormalizePolynomial, null)
        testRule("x + 1", NormalizePolynomial, null)
        testRule("1 + x", NormalizePolynomial, "x + 1")
        testRule("1 + 2y + [[y^2]/2]", NormalizePolynomial, "[[y^2]/2] + 2y + 1")
        testRule("[t^10] + 2[t^3] + sqrt[3] + 1", NormalizePolynomial, null)
        testRule("1 + [t^10] + 2[t^3] + sqrt[3]", NormalizePolynomial, "[t^10] + 2[t^3] + 1 + sqrt[3]")
    }

    @Test
    fun testNormalizeMonomial() {
        testRule("[x/2] + 1", NormalizeMonomial, "[1/2]x + 1")
        testRule("[1/2]x - [3[x^2] / 2]", NormalizeMonomial, "[1/2]x - [3/2][x^2]")
    }
}
