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

package engine.conditions

import engine.sign.Sign
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

class SignTest {
    @Test
    fun testSignFromInt() {
        assertEquals(Sign.NEGATIVE, Sign.fromInt(-10))
        assertEquals(Sign.POSITIVE, Sign.fromInt(100))
        assertEquals(Sign.ZERO, Sign.fromInt(0))
    }

    @Test
    fun testSignUnaryMinus() {
        assertEquals(Sign.NEGATIVE, -Sign.POSITIVE)
        assertEquals(Sign.POSITIVE, -Sign.NEGATIVE)
        assertEquals(Sign.ZERO, -Sign.ZERO)
        assertEquals(Sign.UNKNOWN, -Sign.UNKNOWN)
        assertEquals(Sign.NONE, -Sign.NONE)
    }

    @Test
    fun testTimes() {
        assertEquals(Sign.NONE, Sign.NONE * Sign.POSITIVE)
        assertEquals(Sign.UNKNOWN, Sign.NEGATIVE * Sign.UNKNOWN)
        assertEquals(Sign.ZERO, Sign.ZERO * Sign.UNKNOWN)
        assertEquals(Sign.POSITIVE, Sign.NEGATIVE * Sign.NEGATIVE)
        assertEquals(Sign.NEGATIVE, Sign.NEGATIVE * Sign.POSITIVE)
    }

    @Test
    fun testInverse() {
        assertEquals(Sign.NOT_ZERO, Sign.NOT_ZERO.inverse())
        assertEquals(Sign.NONE, Sign.UNKNOWN.inverse())
        assertEquals(Sign.NEGATIVE, Sign.NEGATIVE.inverse())
        assertEquals(Sign.NONE, Sign.ZERO.inverse())
    }

    @Test
    fun testPlus() {
        assertEquals(Sign.POSITIVE, Sign.ZERO + Sign.POSITIVE)
        assertEquals(Sign.UNKNOWN, Sign.POSITIVE + Sign.NEGATIVE)
        assertEquals(Sign.NONE, Sign.ZERO + Sign.NONE)
        assertEquals(Sign.NEGATIVE, Sign.NEGATIVE + Sign.NEGATIVE)
    }
}

class SignOfTest {
    private fun assertSign(exprString: String, sign: Sign) {
        val expr = parseExpression(exprString)
        assertEquals(sign, expr.signOf())
    }

    @Test
    fun testSignOfConstantExpression() {
        assertSign("1", Sign.POSITIVE)
        assertSign("-0.1", Sign.NEGATIVE)
        assertSign("1.1[23]", Sign.POSITIVE)
        assertSign("-[1 1/2]", Sign.NEGATIVE)
        assertSign("sqrt[-1]", Sign.NONE)
        assertSign("sqrt[2]", Sign.POSITIVE)
        assertSign("[(-2)^1000000000]", Sign.POSITIVE)
        assertSign("[(-200000)^1000000001]", Sign.NEGATIVE)
        assertSign("sqrt[2] + 1", Sign.POSITIVE)
        assertSign("- 10 - root[10, 3]", Sign.NEGATIVE)
        assertSign("10 * sqrt[20] + 5", Sign.POSITIVE)
        assertSign("0 * sqrt[10]", Sign.ZERO)
        assertSign("[1 / 0]", Sign.NONE)
        assertSign("-[(2 - sqrt[5]) ^ 2]", Sign.NEGATIVE)
        assertSign("sqrt[2*sqrt[3] - 2]", Sign.UNKNOWN)
        assertSign("abs[-1]", Sign.POSITIVE)
        assertSign("abs[0]", Sign.ZERO)
        assertSign("[2 - 2sqrt[5] / 4]", Sign.NOT_ZERO)
    }

    @Test
    fun testSignOfAlgebraicExpression() {
        assertSign("x", Sign.UNKNOWN)
        assertSign("[x^2]", Sign.NON_NEGATIVE)
        assertSign("abs[x - 1]", Sign.NON_NEGATIVE)
        assertSign("abs[x] + 1", Sign.POSITIVE)
        assertSign("abs[abs[x] + 1]", Sign.POSITIVE)
        assertSign("-abs[x] - 1", Sign.NEGATIVE)
        assertSign("sqrt[2 - x]", Sign.UNKNOWN)
    }
}
