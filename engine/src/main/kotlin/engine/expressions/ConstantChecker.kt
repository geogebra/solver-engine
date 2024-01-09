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

package engine.expressions

import engine.context.Context

/**
 * A ConstantChecker is able to check whether an expression is constant. There can be different notions of an
 * expression being constant (e.g. it has no variables or its only variables belong to a list of "constant variables").
 */
fun interface ConstantChecker {
    /**
     * Return true if [expression] is considered to be constant in the given [context]
     */
    fun isConstant(context: Context, expression: Expression): Boolean
}

/**
 * Checks for expressions without any variables
 */
val defaultConstantChecker = ConstantChecker { _, e -> e.isConstant() }

/**
 * Checks for expressions whose only variables are not in the list of solution variables
 */
val solutionVariableConstantChecker = ConstantChecker { ctx, e -> e.isConstantIn(ctx.solutionVariables) }
