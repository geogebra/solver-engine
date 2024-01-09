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

import engine.context.Context
import engine.context.emptyContext
import engine.methods.Method
import engine.methods.testSelectSuccessfulPlans
import methods.methodRegistry

fun testSelectPlanApi(inputExpr: String, expectedMethodSelections: Set<Method>, context: Context = emptyContext) =
    testSelectSuccessfulPlans(inputExpr, expectedMethodSelections, methodRegistry, context)

fun testSelectPlanApiInX(inputExpr: String, expectedMethodSelections: Set<Method>) =
    testSelectPlanApi(inputExpr, expectedMethodSelections, Context(solutionVariables = listOf("x")))
