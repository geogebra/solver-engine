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

package engine.expressions

import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

fun e(s: String) = parseExpression(s)

class TestExpression {
    @Test
    fun testSplitPlusMinus() {
        assertEquals(listOf(e("1 + 1")), e("1 + 1").splitPlusMinus())
        assertEquals(listOf(e("4 - 1"), e("4 + 1")), e("4 +/- 1").splitPlusMinus())
        assertEquals(listOf(e("[2 - 5/6]"), e("[2 + 5/6]")), e("[2 +/- 5/6]").splitPlusMinus())
        assertEquals(
            listOf(e("(-4 sqrt[2]) sqrt[3]"), e("(4 sqrt[2]) sqrt[3]")),
            e("(+/-4 sqrt[2]) sqrt[3]").splitPlusMinus(),
        )
        assertEquals(
            listOf(e("1 - 2 - 3"), e("1 - 2 + 3"), e("1 + 2 - 3"), e("1 + 2 + 3")),
            e("1 +/- 2 +/- 3").splitPlusMinus(),
        )
    }

    private fun assertDoubleValue(v: Double, s: String) {
        assertEquals(v, e(s).doubleValue)
    }

    @Test
    fun testDoubleValue() {
        assertDoubleValue(25.0, "25")
        assertDoubleValue(2.5, "[2*3*2 + 8 / 7 - 2 + 3]")
        assertDoubleValue(4.0, "[(-sqrt[4]) ^ 2]")
        assertDoubleValue(Double.NaN, "x")
        assertDoubleValue(Double.NaN, "1 +/- 2")
        assertDoubleValue(2.0, "2 +/- 0")
        assertDoubleValue(Double.NaN, "/undefined/")
        assertDoubleValue(Double.NaN, "sqrt[-2]")
        assertDoubleValue(-2.0, "root[-8, 3]")
        assertDoubleValue(1.2, "[1 1/5]")
        assertDoubleValue(Double.POSITIVE_INFINITY, "1 : 0")
        assertDoubleValue(Double.NEGATIVE_INFINITY, "-[1/0]")
        assertDoubleValue(1.121212121212121, "1.[12]")
    }
}
