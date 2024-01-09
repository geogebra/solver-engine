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

import engine.operators.BinaryExpressionOperator
import engine.sign.Sign
import engine.utility.isEven

class Fraction(
    numerator: Expression,
    denominator: Expression,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
        operator = BinaryExpressionOperator.Fraction,
        operands = listOf(numerator, denominator),
        meta,
    ) {
    val numerator get() = firstChild
    val denominator get() = secondChild

    override fun signOf() = numerator.signOf() / denominator.signOf()
}

class Power(
    base: Expression,
    exponent: Expression,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
        operator = BinaryExpressionOperator.Power,
        operands = listOf(base, exponent),
        meta,
    ) {
    val base get() = firstChild
    val exponent get() = secondChild

    override fun signOf() =
        when (val sign = base.signOf()) {
            Sign.POSITIVE, Sign.NON_NEGATIVE -> sign
            Sign.ZERO -> if (exponent.signOf() == Sign.POSITIVE) Sign.ZERO else Sign.NONE
            Sign.NEGATIVE, Sign.NON_POSITIVE, Sign.UNKNOWN, Sign.NOT_ZERO -> {
                val intExp = exponent.asInteger()
                when {
                    intExp == null || sign.canBeZero && intExp.signum() <= 0 -> Sign.NONE
                    intExp.isEven() -> Sign.POSITIVE.orMaybeZero(sign.canBeZero)
                    else -> sign
                }
            }
            Sign.NONE -> Sign.NONE
        }
}

class Root(
    radicand: Expression,
    index: Expression,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
        operator = BinaryExpressionOperator.Root,
        operands = listOf(radicand, index),
        meta,
    ) {
    val radicand get() = firstChild
    val index get() = secondChild

    override fun childrenInVisitingOrder(): List<Expression> {
        return listOf(secondChild, firstChild)
    }

    override fun signOf(): Sign {
        // This is not quite right because we should check the order as well.
        return radicand.signOf().truncateToPositive()
    }
}

class PercentageOf(
    part: Expression,
    base: Expression,
    meta: NodeMeta = BasicMeta(),
) : ValueExpression(
        operator = BinaryExpressionOperator.PercentageOf,
        operands = listOf(part, base),
        meta = meta,
    ) {
    val part get() = firstChild
    val base get() = secondChild

    override fun signOf() = part.signOf() * base.signOf()
}
