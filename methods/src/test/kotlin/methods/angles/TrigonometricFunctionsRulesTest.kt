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

class TrigonometricFunctionsRulesTest {
    @Test
    fun testApplyingNegativeIdentityOfTrigFunctions() {
        testRule(
            "sin[-degree[30]]",
            TrigonometricFunctionsRules.ApplyNegativeIdentityOfTrigFunction,
            "- sin[degree[30]]",
        )
        testRule(
            "cos[-[/pi/ / 4]]",
            TrigonometricFunctionsRules.ApplyNegativeIdentityOfTrigFunction,
            "cos[[/pi/ / 4]]",
        )
        testRule(
            "tan[-x]",
            TrigonometricFunctionsRules.ApplyNegativeIdentityOfTrigFunction,
            "- tan[x]",
        )
        testRule(
            "sec[-x]",
            TrigonometricFunctionsRules.ApplyNegativeIdentityOfTrigFunction,
            "sec[x]",
        )
    }

    @Test
    fun testApplyPythagoreanIdentity() {
        testRule(
            "[sin ^ 2][x] + [cos ^ 2][x]",
            TrigonometricFunctionsRules.ApplyPythagoreanIdentity,
            "1",
        )
        testRule(
            "[sin[x] ^ 2] + [cos[x] ^ 2]",
            TrigonometricFunctionsRules.ApplyPythagoreanIdentity,
            "1",
        )
        testRule(
            "sin[[x ^ 2]] + cos[[x ^ 2]]",
            TrigonometricFunctionsRules.ApplyPythagoreanIdentity,
            null,
        )
        testRule(
            "[sin ^ 2][x] + [cos ^ 2][y]",
            TrigonometricFunctionsRules.ApplyPythagoreanIdentity,
            null,
        )
    }

    @Test
    fun testApplyTrigonometricFunctionIdentity() {
        testRule(
            "cos[x + y]",
            TrigonometricFunctionsRules.ApplyCosineIdentity,
            "cos[x] * cos[y] - sin[x] * sin[y]",
        )
        testRule(
            "cos[x - y]",
            TrigonometricFunctionsRules.ApplyCosineIdentity,
            "cos[x] * cos[y] + sin[x] * sin[y]",
        )
        testRule(
            "sin[x - y]",
            TrigonometricFunctionsRules.ApplySineIdentity,
            "sin[x] * cos[y] - cos[x] * sin[y]",
        )
        testRule(
            "sin[x + y]",
            TrigonometricFunctionsRules.ApplySineIdentity,
            "sin[x] * cos[y] + cos[x] * sin[y]",
        )
        testRule(
            "tan[x - y]",
            TrigonometricFunctionsRules.ApplyTangentIdentity,
            "[tan[x] - tan[y] / 1 + tan[x] * tan [y]]",
        )
        testRule(
            "tan[x + y]",
            TrigonometricFunctionsRules.ApplyTangentIdentity,
            "[tan[x] + tan[y] / 1 - tan[x] * tan [y]]",
        )
    }
}
