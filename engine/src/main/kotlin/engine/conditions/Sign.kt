package engine.conditions

import engine.expressions.Expression
import engine.operators.BinaryExpressionOperator
import engine.operators.DecimalOperator
import engine.operators.IntegerOperator
import engine.operators.MixedNumberOperator
import engine.operators.NaryOperator
import engine.operators.RecurringDecimalOperator
import engine.operators.UnaryExpressionOperator
import engine.operators.UndefinedOperator
import engine.operators.VariableOperator
import engine.utility.isEven
import java.math.BigInteger

private const val NOT_KNOWN_SIGNUM = 10

/**
 * The sign of a mathematical expression
 */
enum class Sign(val signum: Int) {
    /**
     * Negative and not zero
     */
    NEGATIVE(-1),

    /**
     * Only zero
     */
    ZERO(0),

    /**
     * Positive and not zero
     */
    POSITIVE(1),

    /**
     * Sign unknown
     */
    UNKNOWN(NOT_KNOWN_SIGNUM),

    /**
     * No sign (e.g. undefined values have no sign)
     */
    NONE(NOT_KNOWN_SIGNUM),
    ;

    fun isKnown() = signum != NOT_KNOWN_SIGNUM

    fun inverse() = when (this) {
        POSITIVE -> POSITIVE
        NEGATIVE -> NEGATIVE
        else -> NONE
    }

    operator fun unaryMinus() = when (this) {
        POSITIVE -> NEGATIVE
        NEGATIVE -> POSITIVE
        else -> this
    }

    fun truncateToPositive() = when (this) {
        NEGATIVE -> NONE
        else -> this
    }

    operator fun times(other: Sign) = when {
        this == NONE || other == NONE -> NONE
        this == ZERO || other == ZERO -> ZERO
        this == UNKNOWN || other == UNKNOWN -> UNKNOWN
        else -> fromInt(signum * other.signum)
    }

    operator fun div(other: Sign) = this * other.inverse()

    operator fun plus(other: Sign) = when {
        this == NONE || other == NONE -> NONE
        this == UNKNOWN || other == UNKNOWN -> UNKNOWN
        this == other || other == ZERO -> this
        this == ZERO -> other
        else -> UNKNOWN
    }

    companion object {
        fun fromInt(s: Int) = when {
            s < 0 -> NEGATIVE
            s == 0 -> ZERO
            s > 0 -> POSITIVE
            else -> NONE
        }
    }
}

fun Expression.integerValue(): BigInteger? = when (operator) {
    is IntegerOperator -> operator.value
    UnaryExpressionOperator.Minus -> -this.firstChild.integerValue()!!
    else -> null
}

private fun UnaryExpressionOperator.signOf(operand: Expression) = when (this) {
    UnaryExpressionOperator.DivideBy -> operand.signOf().inverse()
    UnaryExpressionOperator.Plus -> operand.signOf()
    UnaryExpressionOperator.Minus -> -operand.signOf()
    UnaryExpressionOperator.SquareRoot -> operand.signOf().truncateToPositive()
    UnaryExpressionOperator.PlusMinus -> Sign.UNKNOWN
    UnaryExpressionOperator.AbsoluteValue -> when (val sign = operand.signOf()) {
        Sign.NEGATIVE -> Sign.POSITIVE
        else -> sign // zero for zero, undefined for undefined etc.
    }
    UnaryExpressionOperator.NaturalLog -> TODO()
}

private fun BinaryExpressionOperator.signOf(left: Expression, right: Expression) = when (this) {
    BinaryExpressionOperator.Fraction -> left.signOf() / right.signOf()
    BinaryExpressionOperator.Power -> signOfPowerExpression(left, right)
    BinaryExpressionOperator.Root -> {
        // This is not quite right because we should check the order as well.
        left.signOf().truncateToPositive()
    }
}

private fun signOfPowerExpression(base: Expression, exponent: Expression) = when (base.signOf()) {
    Sign.POSITIVE -> Sign.POSITIVE
    Sign.ZERO -> if (exponent.signOf() == Sign.POSITIVE) Sign.ZERO else Sign.NONE
    Sign.NEGATIVE -> {
        val intExp = exponent.integerValue()
        when {
            intExp == null -> Sign.NONE
            intExp.isEven() -> Sign.POSITIVE
            else -> Sign.NEGATIVE
        }
    }
    Sign.UNKNOWN -> {
        val intExp = exponent.integerValue()
        when {
            intExp == null -> Sign.NONE
            intExp.isEven() && base.isNotZeroNotBasedOnSign() -> Sign.POSITIVE
            else -> Sign.UNKNOWN
        }
    }
    else -> Sign.NONE
}

/**
 * Returns the sign of the expression if it can definitely be ascertained. [Sign.NONE] is returned if the expression
 * could be undefined, [Sign.UNKNOWN] is returned if the expression has a value but its sign could not be narrowed down.
 */
fun Expression.signOf(): Sign = when (operator) {
    is IntegerOperator -> Sign.fromInt(operator.value.signum())
    is UndefinedOperator -> Sign.UNKNOWN
    is DecimalOperator -> Sign.fromInt(operator.value.signum())
    is RecurringDecimalOperator -> Sign.POSITIVE // If it was 0, it would not be recurring
    is VariableOperator -> Sign.UNKNOWN
    is MixedNumberOperator -> Sign.POSITIVE // If it was 0, it would not be a mixed number
    is UnaryExpressionOperator -> operator.signOf(operands[0])
    is BinaryExpressionOperator -> operator.signOf(operands[0], operands[1])
    is NaryOperator -> when (operator) {
        NaryOperator.Sum -> operands.map { it.signOf() }.reduce(Sign::plus)
        NaryOperator.Product, NaryOperator.ImplicitProduct -> operands.map { it.signOf() }.reduce(Sign::times)
    }
    else -> Sign.NONE
}

fun Expression.isDefinitelyNotUndefined() = signOf() != Sign.NONE
