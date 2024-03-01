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
) : ValueExpression(operator, operands, meta) {
    abstract val argument: Expression
    abstract val base: Expression

    abstract fun withArgument(arg: Expression): Logarithm

    override fun signOf(): Sign {
        val signOfBase = base.signOf()
        val signOfArgument = argument.signOf()
        return when {
            signOfBase == Sign.NONE || signOfArgument == Sign.NONE -> Sign.NONE
            signOfBase.implies(Sign.NON_POSITIVE) -> Sign.NONE
            signOfArgument.implies(Sign.NON_POSITIVE) -> Sign.NONE
            else -> SimpleComparator.compareExpressions(argument, Constants.One)
        }
    }
}

class Log(
    override val base: Expression,
    override val argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : Logarithm(BinaryExpressionOperator.Log, listOf(base, argument), meta) {
    override fun withArgument(arg: Expression) = logOf(base, arg) as Logarithm
}

class NaturalLog(
    override val argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : Logarithm(UnaryExpressionOperator.NaturalLog, listOf(argument), meta) {
    override val base = Constants.E

    override fun withArgument(arg: Expression) = naturalLogOf(arg) as Logarithm
}

class LogBase10(
    override val argument: Expression,
    meta: NodeMeta = BasicMeta(),
) : Logarithm(UnaryExpressionOperator.LogBase10, listOf(argument), meta) {
    override val base = Constants.Ten

    override fun withArgument(arg: Expression) = logBase10Of(arg) as Logarithm
}
