/*
 * Copyright (c) 2024 GeoGebra GmbH, office@geogebra.org
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

import engine.operators.BinaryExpressionOperator
import engine.operators.Operator
import engine.operators.UnaryExpressionOperator
import engine.sign.Sign

abstract class Logarithm internal constructor(
    operator: Operator,
    operands: List<Expression>,
    meta: NodeMeta,
    open val powerInside: Boolean = true,
) : ValueExpression(operator, operands, meta) {
    abstract val argument: Expression
    abstract val base: Expression

    abstract fun withArgument(arg: Expression): Logarithm

    override fun fillJson(s: MutableMap<String, Any>) {
        s["type"] = javaClass.simpleName
        s["operands"] = operands.map { it.toJson() }
        s["powerInside"] = powerInside
    }

    override fun copyWithMeta(meta: NodeMeta, newOperands: List<Expression>): Expression =
        when (this) {
            is Log -> Log(newOperands[0], newOperands[1], meta, powerInside)
            is NaturalLog -> NaturalLog(newOperands[0], meta, powerInside)
            is LogBase10 -> LogBase10(newOperands[0], meta, powerInside)
            else -> error("Unknown logarithm subtype")
        }

    override fun equiv(other: Expression): Boolean =
        super.equiv(other) && (other as? Logarithm)?.powerInside == powerInside

    override fun equals(other: Any?): Boolean = super.equals(other) && (other as? Logarithm)?.powerInside == powerInside

    override fun hashCode(): Int = 31 * super.hashCode() + powerInside.hashCode()

    override fun signOf(): Sign {
        val signOfBase = base.signOf()
        val signOfArgument = argument.signOf()
        // Broke into two to improve readability and get rid of lint warning
        if (signOfBase == Sign.NONE || signOfArgument == Sign.NONE) {
            return Sign.NONE
        }
        if (signOfBase.implies(Sign.NON_POSITIVE) || signOfArgument.implies(Sign.NON_POSITIVE)) {
            return Sign.NONE
        }

        val baseComparedToOne = if (base == Constants.E || base == Constants.Pi) {
            Sign.POSITIVE
        } else {
            SimpleComparator.compareExpressions(base, Constants.One)
        }
        val argumentComparedToOne = SimpleComparator.compareExpressions(argument, Constants.One)

        return when (baseComparedToOne) {
            Sign.POSITIVE -> argumentComparedToOne
            Sign.NEGATIVE -> -argumentComparedToOne
            Sign.ZERO, Sign.NONE -> Sign.NONE
            else -> Sign.UNKNOWN
        }
    }
}

class Log(
    override val base: Expression,
    override val argument: Expression,
    meta: NodeMeta = BasicMeta(),
    override val powerInside: Boolean = true,
) : Logarithm(BinaryExpressionOperator.Log, listOf(base, argument), meta, powerInside) {
    override fun withArgument(arg: Expression) = logOf(base, arg, powerInside) as Logarithm
}

class NaturalLog(
    override val argument: Expression,
    meta: NodeMeta = BasicMeta(),
    override val powerInside: Boolean = true,
) : Logarithm(UnaryExpressionOperator.NaturalLog, listOf(argument), meta, powerInside) {
    override val base = Constants.E

    override fun withArgument(arg: Expression) = naturalLogOf(arg, powerInside) as Logarithm
}

class LogBase10(
    override val argument: Expression,
    meta: NodeMeta = BasicMeta(),
    override val powerInside: Boolean = true,
) : Logarithm(UnaryExpressionOperator.LogBase10, listOf(argument), meta, powerInside) {
    override val base = Constants.Ten

    override fun withArgument(arg: Expression) = logBase10Of(arg, powerInside) as Logarithm
}

fun Expression.isLogarithmicTerm(): Boolean =
    this is Logarithm ||
        this is Minus && this.firstChild.isLogarithmicTerm() ||
        this is Product && this.children.singleOrNull { factor ->
            factor is Logarithm
        } != null
