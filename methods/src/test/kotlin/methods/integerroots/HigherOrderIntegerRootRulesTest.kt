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

package methods.integerroots

import engine.methods.testRule
import methods.integerroots.IntegerRootsRules.PutRootCoefficientUnderRoot
import methods.integerroots.IntegerRootsRules.SimplifyRootOfRoot
import methods.integerroots.IntegerRootsRules.TurnPowerOfRootToRootOfPower
import org.junit.jupiter.api.Test

class HigherOrderIntegerRootRulesTest {
    @Test
    fun testTurnPowerOfRootToRootOfPower() {
        testRule(
            "[(root[5, 4]) ^ 3]",
            TurnPowerOfRootToRootOfPower,
            "root[[5 ^ 3], 4]",
        )
        testRule(
            "[(sqrt[3]) ^ 5]",
            TurnPowerOfRootToRootOfPower,
            "sqrt[[3 ^ 5]]",
        )
    }

    @Test
    fun testSimplifyRootOfRoot() {
        testRule(
            "root[root[5, 3], 4]",
            SimplifyRootOfRoot,
            "root[5, 4 * 3]",
        )
        testRule(
            "sqrt[sqrt[3]]",
            SimplifyRootOfRoot,
            "root[3, 2 * 2]",
        )
        testRule(
            "root[sqrt[6], 3]",
            SimplifyRootOfRoot,
            "root[6, 3 * 2]",
        )
    }

    @Test
    fun testPutRootCoefficientUnderRoot() {
        testRule(
            "7 * root[20, 3]",
            PutRootCoefficientUnderRoot,
            "root[[7 ^ 3] * 20, 3]",
        )
        testRule(
            "8 * sqrt[40]",
            PutRootCoefficientUnderRoot,
            "sqrt[[8 ^ 2] * 40]",
        )
    }
}
