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

package methods.approximation

import engine.methods.testRule
import methods.approximation.ApproximationRules.ApproximateDecimalProductAndDivision
import methods.approximation.ApproximationRules.ExpandRecurringDecimal
import methods.approximation.ApproximationRules.RoundRecurringDecimal
import methods.approximation.ApproximationRules.RoundTerminatingDecimal
import org.junit.jupiter.api.Test

class ApproximationRulesTest {
    @Test
    fun testRoundTerminatingDecimal() {
        testRule("42.123", RoundTerminatingDecimal, null)
        testRule("3.1415", RoundTerminatingDecimal, "3.142")
        testRule("0.0001", RoundTerminatingDecimal, "0.000")
    }

    @Test
    fun testExpandRecurringDecimal() {
        testRule("0.[6]", ExpandRecurringDecimal, "0.666[6]")
        testRule("0.12[34]", ExpandRecurringDecimal, null)
        testRule("0.[123]", ExpandRecurringDecimal, "0.123[123]")
    }

    @Test
    fun testRoundRecurringDecimal() {
        testRule("2.7182[82]", RoundRecurringDecimal, "2.718")
        testRule("0.12[3]", RoundRecurringDecimal, null)
        testRule("167.12[35]", RoundRecurringDecimal, "167.124")
    }

    @Test
    fun testApproximateDecimalProductAndDivision() {
        testRule("3.5001 * 1.9999", ApproximateDecimalProductAndDivision, "7.000")
        testRule("2.1 * 3", ApproximateDecimalProductAndDivision, "6.300")
        testRule("1 : 3", ApproximateDecimalProductAndDivision, "0.333")
        testRule("4 : 0.0", ApproximateDecimalProductAndDivision, null)
    }
}
