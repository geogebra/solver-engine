package engine.patterns

import engine.expressions.DecimalOperator
import engine.expressions.IntegerOperator
import engine.expressions.RecurringDecimalOperator
import engine.expressions.Subexpression
import java.math.BigDecimal
import java.math.BigInteger

class InvalidMatch(message: String) : Exception(message)

interface NumberProvider : PathProvider {
    fun getBoundNumber(m: Match): BigDecimal
}

interface IntegerProvider : NumberProvider {
    fun getBoundInt(m: Match): BigInteger

    override fun getBoundNumber(m: Match) = getBoundInt(m).toBigDecimal()
}

interface NumberPattern : Pattern, NumberProvider

interface IntegerPattern : Pattern, IntegerProvider

class UnsignedIntegerPattern : IntegerPattern {

    override fun getBoundInt(m: Match): BigInteger {
        return when (val operator = m.getBoundExpr(this)!!.operator) {
            is IntegerOperator -> operator.value
            else -> throw InvalidMatch("unsigned integer matched to non-numeric value")
        }
    }

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        return when (subexpression.expr.operator) {
            is IntegerOperator -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

class UnsignedDecimalPattern : NumberPattern {

    override fun getBoundNumber(m: Match): BigDecimal {
        return when (val operator = m.getBoundExpr(this)!!.operator) {
            is DecimalOperator -> operator.value
            is IntegerOperator -> operator.value.toBigDecimal()
            else -> throw InvalidMatch("unsigned decimal matched to non-numeric value")
        }
    }

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        return when (subexpression.expr.operator) {
            is IntegerOperator, is DecimalOperator -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

class RecurringDecimalPattern : Pattern {
    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        return when (subexpression.expr.operator) {
            is RecurringDecimalOperator -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

class SignedIntegerPattern : OptionalNegPatternBase<UnsignedIntegerPattern>(UnsignedIntegerPattern()), IntegerPattern {
    override fun getBoundInt(m: Match): BigInteger {
        val value = unsignedPattern.getBoundInt(m)
        return if (isNeg(m)) -value else value
    }
}

class SignedNumberPattern : OptionalNegPatternBase<UnsignedDecimalPattern>(UnsignedDecimalPattern()), NumberPattern {
    override fun getBoundNumber(m: Match): BigDecimal {
        val value = unsignedPattern.getBoundNumber(m)
        return if (isNeg(m)) -value else value
    }
}
