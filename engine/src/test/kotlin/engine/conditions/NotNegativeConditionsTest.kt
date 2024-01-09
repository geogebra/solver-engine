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

import engine.conditions.isDefinitelyNotNegative
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

class NotNegativeConditionsTest {
    private fun testNotNegative(exprString: String, notNegative: Boolean) {
        val expr = parseExpression(exprString)
        assertEquals(notNegative, expr.isDefinitelyNotNegative())
    }

    @Test
    fun testExpressionIsDefinitelyNotNegative() {
        testNotNegative("1", true)
        testNotNegative("[x^2]", true)
        testNotNegative("[x^3]", false)
        testNotNegative("2*(sqrt[3] - sqrt[2])", true)
        testNotNegative("abs[x]", true)
        testNotNegative("-3x", false)
        testNotNegative("2 - 3 + 5", true)
        testNotNegative("sqrt[2*sqrt[3] - 2]", true)
        testNotNegative("2 - sqrt[2]", true)
    }
}
