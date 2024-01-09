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

package server

import methods.inequalities.InequalitiesPlans
import org.junit.jupiter.api.Test

class TestSelectPlansForInequality {
    @Test
    fun `test constant inequality`() {
        testSelectPlanApi(
            "3 + 4 <= 7",
            setOf(
                InequalitiesPlans.SolveConstantInequality,
            ),
        )
    }

    @Test
    fun `test linear inequality`() {
        testSelectPlanApiInX(
            "2x < 1",
            setOf(
                InequalitiesPlans.SolveLinearInequality,
            ),
        )
    }

    @Test
    fun `test linear inequality with simplification`() {
        testSelectPlanApiInX(
            "2x + x < 1 - 4",
            setOf(
                InequalitiesPlans.SolveLinearInequality,
            ),
        )
    }
}
