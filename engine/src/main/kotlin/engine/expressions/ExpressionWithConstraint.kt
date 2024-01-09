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

import engine.operators.ExpressionWithConstraintOperator

class ExpressionWithConstraint(
    expression: Expression,
    constraint: Expression,
    meta: NodeMeta = BasicMeta(),
) : Expression(
        operator = ExpressionWithConstraintOperator,
        operands = listOf(expression, constraint),
        meta = meta,
    ) {
    val expression get() = firstChild
    val constraint get() = secondChild
}

fun expressionWithConstraintOf(expression: Expression, constraint: Expression?) =
    if (constraint == null) expression else ExpressionWithConstraint(expression, constraint)
