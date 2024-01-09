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
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class FindPatternTest {
    @Test
    fun testFindPattern() {
        val n1 = UnsignedIntegerPattern()
        val n2 = UnsignedIntegerPattern()
        val intProd = productContaining(n1, n2)
        val findPattern = FindPattern(intProd)

        val expr = parseExpression("2 + 3 * 5 + [5/3 * x * 3]")
        val matches = findPattern.findMatches(emptyContext, RootMatch, expr)
        assertEquals(2, matches.count())
        assertContentEquals(
            listOf(15, 9).map { it.toBigInteger() },
            matches.map { n1.getBoundInt(it) * n2.getBoundInt(it) }.toList(),
        )
    }
}
