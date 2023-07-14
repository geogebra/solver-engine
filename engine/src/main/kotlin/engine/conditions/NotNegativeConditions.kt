package engine.conditions

import engine.expressions.Expression
import engine.sign.Sign

/**
 * Returns true if the expression is definitely known to be non-negative (positive or zero),
 * according to some heuristics
 */
fun Expression.isDefinitelyNotNegative(): Boolean = when (signOf()) {
    Sign.NONE, Sign.NEGATIVE, Sign.NON_POSITIVE -> false
    Sign.ZERO, Sign.POSITIVE, Sign.NON_NEGATIVE -> true
    else -> notNegativeBasedOnDoubleValue()
}

fun Expression.notNegativeBasedOnDoubleValue(): Boolean {
    val eval = this.doubleValue
    return !(eval.isNaN() || eval <= 0)
}
