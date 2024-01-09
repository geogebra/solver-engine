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

import engine.operators.NameOperator
import engine.operators.VariableListOperator
import engine.operators.VariableOperator
import engine.sign.Sign

class Variable(
    val variableName: String,
    val subscript: String? = null,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
        operator = VariableOperator(variableName, subscript),
        operands = listOf(),
        meta = meta,
    ) {
    override fun signOf() = Sign.UNKNOWN

    override fun fillJson(s: MutableMap<String, Any>) {
        s["type"] = "Variable"
        s["value"] = variableName
        if (subscript != null) {
            s["subscript"] = subscript
        }
    }
}

class VariableList(
    variables: List<Variable>,
    meta: NodeMeta = BasicMeta(),
) : Expression(VariableListOperator, variables, meta) {
    val variableExpressions get() = children.map { it as Variable }
}

class Name(
    val text: String,
    meta: NodeMeta = BasicMeta(),
) : Expression(NameOperator(text), listOf(), meta) {
    override fun fillJson(s: MutableMap<String, Any>) {
        s["type"] = "Name"
        s["value"] = text
    }
}
