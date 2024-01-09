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

class ProductTest {
    /**
     * Checks whether a product sign is required between [left] and [right]
     */
    fun testSignRequired(left: String, right: String, expectedRequired: Boolean) {
        val e1 = parseExpression(left)
        val e2 = parseExpression(right)
        val required = productSignRequired(e1, e2)
        assertEquals(expectedRequired, required)
    }

    /**
     * Assuming [product] parses to a product with two factors, checks whether a product sign is required between the
     * factors
     */
    fun testSignRequired(product: String, expectedRequired: Boolean) {
        val e = parseExpression(product)
        assert(e is Product && e.children.size == 2)
        val required = productSignRequired(e.firstChild, e.secondChild)
        assertEquals(expectedRequired, required)
    }

    /**
     * Assuming [product] parses to a product with two factors, checks whether a product sign is forced between the
     * factors (i.e. it is present but not required)
     */
    fun testSignForced(expr: String, expectedForced: Boolean) {
        val e = parseExpression(expr)
        assert(e is Product && e.children.size == 2)
        if (e is Product && e.children.size == 2) {
            assertEquals(e.forcedSigns.size == 1, expectedForced, "$expr")
        }
    }

    @Test
    fun testProductSigns() {
        // Between numbers and variables
        testSignRequired("2", "5", true)
        testSignRequired("x", "2", true)
        testSignRequired("15", "-5", true)
        testSignRequired("[1/2]", "x", false)
        testSignRequired("[1/3]", "2", true)
        testSignRequired("[2/5]", "[1/2]", true)

        // Between variables
        testSignRequired("x", "y", false)
        testSignRequired("x", "x", true)
        testSignRequired("x_1", "y_2", false)
        testSignRequired("z_1", "z_2", true)

        // Between roots and variables / numbers
        testSignRequired("sqrt[2]", "3", true)
        testSignRequired("5", "sqrt[3]", false)
        testSignRequired("n", "root[5, 3]", false)
        testSignRequired("sqrt[2]", "sqrt[3]", true)

        // Negative signs
        testSignRequired("x", "-y", true)
        testSignRequired("x", "(-y)", false)
        testSignRequired("3", "-x", true)
        testSignRequired("3*-x", true)

        // Powers and variables / numbers
        testSignRequired("2", "[x^2]", false)
        testSignRequired("[3^x]", "x", false)
        testSignRequired("[x^3]", "x", true)
        testSignRequired("2", "[2^x]", true)

        // Division
        testSignRequired("2:x", false)
        testSignRequired("x:2", false)

        // Test whether signs are forced
        testSignForced("x*y", true)
        testSignForced("x*x", false)
    }
}
