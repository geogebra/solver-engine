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

package engine.patterns

import engine.context.emptyContext
import engine.expressions.Constants
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RootPatternTest {
    @Test
    fun testMatchesBothSquareAndHigherOrderRoot() {
        val pattern = rootOf(AnyPattern())

        val expression1 = parseExpression("3[x^2]")
        assertFalse(pattern.matches(emptyContext, expression1))

        val expression2 = parseExpression("sqrt[3x]")
        assertTrue(pattern.matches(emptyContext, expression2))

        val expression3 = parseExpression("root[[y ^ 2], 3]")
        assertTrue(pattern.matches(emptyContext, expression3))
    }

    @Test
    fun testOrderIsCorrect() {
        val order = AnyPattern()
        val pattern = rootOf(AnyPattern(), order)

        val expression1 = parseExpression("sqrt[3x]")
        val match1 = pattern.findMatches(emptyContext, RootMatch, expression1).single()
        assertEquals(Constants.Two, order.getBoundExpr(match1))

        val expression2 = parseExpression("root[[y ^ 2], 3]")
        val match2 = pattern.findMatches(emptyContext, RootMatch, expression2).single()
        assertEquals(Constants.Three, order.getBoundExpr(match2))
    }

    @Test
    fun testIntegerOrderRootPattern() {
        val pattern = integerOrderRootOf(AnyPattern())

        val expression1 = parseExpression("sqrt[3x]")
        val match1 = pattern.findMatches(emptyContext, RootMatch, expression1).single()
        assertEquals(Constants.Two, pattern.order.getBoundExpr(match1))

        val expression2 = parseExpression("root[[y ^ 2], 3]")
        val match2 = pattern.findMatches(emptyContext, RootMatch, expression2).single()
        assertEquals(Constants.Three, pattern.order.getBoundExpr(match2))

        val expression3 = parseExpression("root[[y ^ 2], 3 * 2]")
        assertFalse(pattern.matches(emptyContext, expression3))
    }
}
