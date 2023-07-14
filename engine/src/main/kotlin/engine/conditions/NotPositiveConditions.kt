package engine.conditions

import engine.expressions.Expression
import engine.sign.Sign

/**
 * Returns true if the expression is definitely known to be non-positive (negative or zero),
 * according to some heuristics
 */
fun Expression.isDefinitelyNotPositive(): Boolean = when (signOf()) {
    Sign.NONE, Sign.POSITIVE, Sign.NON_NEGATIVE -> false
    Sign.ZERO, Sign.NEGATIVE, Sign.NON_POSITIVE -> true
    else -> notPositiveBasedOnDoubleValue()
}

fun Expression.notPositiveBasedOnDoubleValue(): Boolean {
    val eval = this.doubleValue
    return !(eval.isNaN() || eval >= 0)
}
