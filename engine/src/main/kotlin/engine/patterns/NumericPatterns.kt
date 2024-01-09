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

package engine.patterns

import engine.context.Context
import engine.expressions.DecimalExpression
import engine.expressions.Expression
import engine.expressions.IntegerExpression
import engine.expressions.RecurringDecimalExpression
import engine.expressions.xp
import engine.utility.RecurringDecimal
import java.math.BigDecimal
import java.math.BigInteger

class InvalidMatch(message: String) : Exception(message)

interface NumberProvider : ExpressionProvider {
    fun getBoundNumber(m: Match): BigDecimal
}

interface IntegerProvider : NumberProvider {
    fun getBoundInt(m: Match): BigInteger

    override fun getBoundNumber(m: Match) = getBoundInt(m).toBigDecimal()
}

interface NumberPattern : Pattern, NumberProvider

interface IntegerPattern : Pattern, IntegerProvider

class UnsignedIntegerPattern : IntegerPattern, BasePattern() {
    fun getBoundInt(m: Match, default: BigInteger): BigInteger {
        return when (val expr = m.getBoundExpr(this)) {
            null -> default
            is IntegerExpression -> expr.value
            else -> throw InvalidMatch("Unsigned integer matched to $expr")
        }
    }

    override fun getBoundInt(m: Match): BigInteger {
        return when (val expr = m.getBoundExpr(this)!!) {
            is IntegerExpression -> expr.value
            else -> throw InvalidMatch("Unsigned integer matched to $expr")
        }
    }

    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return when (subexpression) {
            is IntegerExpression -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

class UnsignedNumberPattern : NumberPattern, BasePattern() {
    override fun getBoundNumber(m: Match): BigDecimal {
        return when (val expr = m.getBoundExpr(this)!!) {
            is DecimalExpression -> expr.value
            is IntegerExpression -> expr.value.toBigDecimal()
            else -> throw InvalidMatch("Unsigned decimal matched ${expr.operator}")
        }
    }

    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return when (subexpression) {
            is IntegerExpression, is DecimalExpression ->
                sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

class RecurringDecimalPattern : BasePattern() {
    fun getBoundRecurringDecimal(m: Match): RecurringDecimal {
        return when (val expr = m.getBoundExpr(this)!!) {
            is RecurringDecimalExpression -> expr.value
            else -> throw InvalidMatch("Recurring decimal matched to $expr")
        }
    }

    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return when (subexpression) {
            is RecurringDecimalExpression -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

class SignedIntegerPattern : OptionalNegPattern<UnsignedIntegerPattern>(UnsignedIntegerPattern()), IntegerPattern {
    override fun getBoundInt(m: Match): BigInteger {
        val value = unsignedPattern.getBoundInt(m)
        return if (isNeg(m)) -value else value
    }
}

class SignedNumberPattern : OptionalNegPattern<UnsignedNumberPattern>(UnsignedNumberPattern()), NumberPattern {
    override fun getBoundNumber(m: Match): BigDecimal {
        val value = unsignedPattern.getBoundNumber(m)
        return if (isNeg(m)) -value else value
    }
}

/**
 * This wraps a PathProvider so that it is given a default value if did not match.
 */
class IntegerProviderWithDefault(
    private val integerProvider: IntegerProvider,
    private val default: BigInteger,
    private val optionalSign: OptionalNegPattern<*>? = null,
) : ProviderWithDefault(integerProvider, xp(default)), IntegerProvider {
    override fun getBoundInt(m: Match): BigInteger {
        return if (integerProvider.getBoundExpr(m) != null) {
            val int = integerProvider.getBoundInt(m)
            if (optionalSign != null && optionalSign.isNeg(m)) int.negate() else int
        } else {
            if (optionalSign != null && optionalSign.isNeg(m)) default.negate() else default
        }
    }
}
