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

import engine.conditions.isDefinitelyNotPositive
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

class NotPositiveConditionsTest {
    private fun testNotPositive(exprString: String, notPositive: Boolean) {
        val expr = parseExpression(exprString)
        assertEquals(notPositive, expr.isDefinitelyNotPositive())
    }

    @Test
    fun testExpressionIsDefinitelyNotNegative() {
        testNotPositive("-1", true)
        testNotPositive("-[x^2]", true)
        testNotPositive("[x^3]", false)
        testNotPositive("2*(sqrt[2] - sqrt[3])", true)
        testNotPositive("-abs[x]", true)
        testNotPositive("-1 - abs[x]", true)
        testNotPositive("3x", false)
        testNotPositive("sqrt[3] - sqrt[4]", true)
        testNotPositive("-2 + sqrt[2]", true)
    }
}
