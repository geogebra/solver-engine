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

import engine.operators.VoidOperator

/**
 * A VoidExpression means "no expression". It only makes sense as the output of a rule in the specific case when
 * we want to state something about the input expression but it cannot be transformed. E.g.
 *
 * - the expression is fully simplified
 * - the polynomial is irreducible
 */
class VoidExpression(
    meta: NodeMeta = BasicMeta(),
) : Expression(
        operator = VoidOperator,
        operands = emptyList(),
        meta = meta,
    )
