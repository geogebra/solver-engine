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

package engine.methods

import engine.context.Context
import engine.context.emptyContext
import parser.parseExpression
import kotlin.test.assertEquals

fun testSelectSuccessfulPlans(
    inputExpr: String,
    expectedMethodSelections: Set<Method>,
    methodRegistry: MethodRegistry,
    context: Context = emptyContext,
) {
    val expr = parseExpression(inputExpr)
    val actualSelections = methodRegistry.selectSuccessfulPlansMethodIdAndTransformation(expr, context)
    val actualMethodSelections = actualSelections.map {
        methodRegistry.getMethodByName(it.first.toString())
    }.toSet()
    assertEquals(expectedMethodSelections, actualMethodSelections)
}
