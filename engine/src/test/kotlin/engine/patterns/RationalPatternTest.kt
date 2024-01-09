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
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

class RationalPatternTest {
    @Test
    fun testRationalPattern() {
        val expr = parseExpression("-[1/2]")
        val pattern = RationalPattern()
        val matches = pattern.findMatches(emptyContext, RootMatch, expr)
        assertEquals(1, matches.count())
        val match = matches.first()
        assertEquals(expr, match.getBoundExpr(pattern))
    }
}
