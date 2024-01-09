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
import engine.expressions.RootOrigin
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals

class ExpressionProviderWithinTest {
    @Test
    fun testExpressionProviderWithin() {
        val expression = parseExpression("[2*4/2*7]").withOrigin(RootOrigin())

        val common = AnyPattern()
        val numerator = productContaining(common)
        val denominator = productContaining(common)
        val fraction = fractionOf(numerator, denominator)

        val match = fraction.findMatches(emptyContext, RootMatch, expression).single()
        val topCommon = common.within(numerator).getBoundExpr(match)!!
        assertEquals("./0/0", topCommon.origin.path.toString())
        val bottomCommon = common.within(denominator).getBoundExpr(match)!!
        assertEquals("./1/0", bottomCommon.origin.path.toString())
    }
}
