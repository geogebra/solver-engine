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

import engine.operators.InverseNotationType
import engine.operators.TrigonometricFunctionOperator
import engine.operators.TrigonometricFunctionType
import engine.operators.UnaryExpressionOperator
import engine.operators.UnitExpressionOperator
import engine.operators.UnitType
import engine.sign.Sign

class Minus(
    argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
        operator = UnaryExpressionOperator.Minus,
        operands = listOf(argument),
        meta,
    ) {
    val argument get() = firstChild

    override fun signOf() = -argument.signOf()
}

class Plus(
    argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
        operator = UnaryExpressionOperator.Plus,
        operands = listOf(argument),
        meta,
    ) {
    val argument get() = firstChild

    override fun signOf() = argument.signOf()
}

class PlusMinus(
    argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
        operator = UnaryExpressionOperator.PlusMinus,
        operands = listOf(argument),
        meta,
    ) {
    val argument get() = firstChild

    override fun signOf() =
        when (argument.signOf()) {
            Sign.POSITIVE, Sign.NEGATIVE, Sign.NOT_ZERO -> Sign.NOT_ZERO
            Sign.NONE -> Sign.NONE
            else -> Sign.UNKNOWN
        }
}

class AbsoluteValue(
    argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
        operator = UnaryExpressionOperator.AbsoluteValue,
        operands = listOf(argument),
        meta,
    ) {
    val argument get() = firstChild

    override fun signOf() =
        when (val sign = argument.signOf()) {
            Sign.NONE, Sign.ZERO -> sign
            else -> Sign.POSITIVE.orMaybeZero(sign.canBeZero)
        }
}

class SquareRoot(
    argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
        operator = UnaryExpressionOperator.SquareRoot,
        operands = listOf(argument),
        meta,
    ) {
    val argument get() = firstChild

    override fun signOf() = argument.signOf().truncateToPositive()
}

class Percentage(
    argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
        operator = UnaryExpressionOperator.Percentage,
        operands = listOf(argument),
        meta = meta,
    ) {
    val argument get() = firstChild

    override fun signOf() = argument.signOf()
}

class UnitExpression(
    value: Expression,
    val unit: UnitType,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
        operator = UnitExpressionOperator(unit),
        operands = listOf(value),
        meta = meta,
    ) {
    val argument get() = firstChild

    override fun signOf() = argument.signOf()
}

class TrigonometricExpression(
    val functionType: TrigonometricFunctionType,
    operand: Expression,
    val powerInside: Boolean,
    val inverseNotation: InverseNotationType,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
        operator = TrigonometricFunctionOperator(functionType, powerInside, inverseNotation),
        operands = listOf(operand),
        meta = meta,
    ) {
    val argument get() = firstChild

    override fun fillJson(s: MutableMap<String, Any>) {
        s["type"] = functionType.name
        s["operands"] = operands.map { it.toJson() }
        s["inverseNotation"] = inverseNotation.name.camelCase()
        s["powerInside"] = powerInside
    }
}

/**
 * Modify first letter of an enum type name to turn it into
 * camelcase.
 * e.g. ArcPrefix --> arcPrefix
 */
private fun String.camelCase(): String = replaceFirstChar(Char::lowercase)
