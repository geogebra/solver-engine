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

package methods.units

import engine.methods.testRule
import org.junit.jupiter.api.Test

class UnitsRulesTest {
    @Test
    fun testUnitProductWithConstant() {
        testRule("degree[45] * 3", UnitsRules.EvaluateUnitProductAndDivision, "degree[135]")
        testRule("3 * degree[45]", UnitsRules.EvaluateUnitProductAndDivision, "degree[135]")
    }

    @Test
    fun testUnitDivisionWithConstant() {
        testRule("degree[135] : 3", UnitsRules.EvaluateUnitProductAndDivision, "degree[45]")
    }

    @Test
    fun testCancellingUnits() {
        testRule("[degree[45] / degree[180]]", UnitsRules.SimplifyFractionOfUnits, "[45 / 180]")
    }

    @Test
    fun testFractionOfUnitAndConstant() {
        testRule("[degree[45] / 3]", UnitsRules.SimplifyFractionOfUnitAndConstantToInteger, "degree[15]")
        testRule(
            "[3 * degree[180] / 4]",
            UnitsRules.FindCommonIntegerFactorInFractionOfUnitAndConstant,
            "[3 * 4 * degree[45] / 4 * 1]",
        )
    }
}
