/*
 * Copyright (c) 2025 GeoGebra GmbH, office@geogebra.org
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

package methods.angles

import engine.methods.testRule
import kotlin.test.Test

class AnglesRulesTest {
    @Test
    fun testDegreeConversion() {
        testRule("degree[45]", AnglesRules.UseDegreeConversionFormula, "degree[45] * [/pi/ / degree[180]]")
    }

    @Test
    fun testRadianConversion() {
        testRule("[/pi/ / 4]", AnglesRules.UseRadianConversionFormula, "[/pi/ / 4] * [degree[180] / /pi/]")
    }
}
