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

import engine.methods.testMethod
import org.junit.jupiter.api.Test

class AnglesPlansTest {
    @Test
    fun testDegreeConversion() {
        testMethod {
            method = AnglesPlans.ConvertDegreesToRadians
            inputExpr = "degree[ 45 ]"

            check {
                step { toExpr = "degree[ 45 ] * [/pi/ / degree[ 180 ]]" }
                step { toExpr = "[/pi/ / 4]" }
            }
        }
    }

    @Test
    fun testRadianConversion() {
        testMethod {
            method = AnglesPlans.ConvertRadiansToDegrees
            inputExpr = "[3 * /pi/ / 4]"

            check {
                step { toExpr = "[3 * /pi/ / 4] * [degree[ 180 ] / /pi/]" }
                step { toExpr = "3 * degree[ 45 ]" }
                step { toExpr = "degree[ 135 ]" }
            }
        }
    }
}
