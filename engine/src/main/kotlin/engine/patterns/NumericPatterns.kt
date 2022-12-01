package engine.patterns

import engine.context.Context
import engine.expressions.Expression
import engine.expressions.xp
import engine.operators.DecimalOperator
import engine.operators.IntegerOperator
import engine.operators.RecurringDecimalOperator
import engine.utility.RecurringDecimal
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

class UnsignedIntegerPattern : IntegerPattern, BasePattern() {

    override fun getBoundInt(m: Match): BigInteger {
        return when (val operator = m.getBoundExpr(this)!!.operator) {
            is IntegerOperator -> operator.value
            else -> throw InvalidMatch("Unsigned integer matched to $operator")
        }
    }

    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return when (subexpression.operator) {
            is IntegerOperator -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

class UnsignedDecimalPattern : NumberPattern, BasePattern() {

    override fun getBoundNumber(m: Match): BigDecimal {
        return when (val operator = m.getBoundExpr(this)!!.operator) {
            is DecimalOperator -> operator.value
            is IntegerOperator -> operator.value.toBigDecimal()
            else -> throw InvalidMatch("Unsigned decimal matched $operator")
        }
    }

    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return when (subexpression.operator) {
            is IntegerOperator, is DecimalOperator -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

class RecurringDecimalPattern : BasePattern() {

    fun getBoundRecurringDecimal(m: Match): RecurringDecimal {
        return when (val operator = m.getBoundExpr(this)!!.operator) {
            is RecurringDecimalOperator -> operator.value
            else -> throw InvalidMatch("Recurring decimal matched to $operator")
        }
    }

    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return when (subexpression.operator) {
            is RecurringDecimalOperator -> sequenceOf(match.newChild(this, subexpression))
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

class SignedNumberPattern : OptionalNegPattern<UnsignedDecimalPattern>(UnsignedDecimalPattern()), NumberPattern {
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
    private val default: BigInteger
) : ProviderWithDefault(integerProvider, xp(default)), IntegerProvider {

    override fun getBoundInt(m: Match): BigInteger {
        return if (integerProvider.getBoundExpr(m) != null) {
            integerProvider.getBoundInt(m)
        } else {
            default
        }
    }
}
