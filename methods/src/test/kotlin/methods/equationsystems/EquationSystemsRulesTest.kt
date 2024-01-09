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

package methods.equationsystems

import engine.methods.testRuleInXY
import org.junit.jupiter.api.Test

class EquationSystemsRulesTest {
    @Test
    fun testGuessIntegerSolutionsOfSystemContainingXYEqualsInteger() {
        testRuleInXY(
            "x + y = 10 AND xy = 21",
            EquationSystemsRules.GuessIntegerSolutionsOfSystemContainingXYEqualsInteger,
            "x = 3 AND y = 7",
        )
        testRuleInXY(
            "[x^2] - [y^2] = 75 AND xy = 50",
            EquationSystemsRules.GuessIntegerSolutionsOfSystemContainingXYEqualsInteger,
            "x = 10 AND y = 5",
        )
        testRuleInXY(
            "x + y = -10 AND xy = 21",
            EquationSystemsRules.GuessIntegerSolutionsOfSystemContainingXYEqualsInteger,
            "x = -3 AND y = -7",
        )
    }
}
