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

package methods.mixednumbers

import engine.methods.testRule
import methods.mixednumbers.MixedNumbersRules.ConvertSumOfIntegerAndProperFractionToMixedNumber
import methods.mixednumbers.MixedNumbersRules.FractionToMixedNumber
import methods.mixednumbers.MixedNumbersRules.SplitMixedNumber
import org.junit.jupiter.api.Test

class MixedNumbersRulesTest {
    @Test
    fun testFractionToMixedNumber() {
        testRule("[4 / 21]", FractionToMixedNumber, null)
        testRule("[21 / 4]", FractionToMixedNumber, "[5 1/4]")
    }

    @Test
    fun testSplitMixedNumber() {
        testRule("[2 3/4]", SplitMixedNumber, "2 + [3/4]")
        testRule("[2 1/0]", SplitMixedNumber, "/undefined/")
        testRule("[2 0/0]", SplitMixedNumber, "/undefined/")
        testRule("[5 0/5]", SplitMixedNumber, "5")
    }

    @Test
    fun testConvertSumOfIntegerAndProperFractionToMixedNumber() {
        testRule("4 + [13 / 12]", ConvertSumOfIntegerAndProperFractionToMixedNumber, null)
        testRule("3 + [11 / 12]", ConvertSumOfIntegerAndProperFractionToMixedNumber, "[3 11/12]")
    }
}
