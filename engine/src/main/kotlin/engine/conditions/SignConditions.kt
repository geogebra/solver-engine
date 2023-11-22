package engine.conditions

import engine.expressions.Expression
import engine.sign.Sign

/**
 * Returns true if the expression is definitely known to be non-positive (negative or zero),
 * according to some heuristics
 */
fun Expression.isDefinitelyNotPositive(): Boolean = isDefinitely(Sign.NON_POSITIVE)

fun Expression.isDefinitelyNotNegative(): Boolean = isDefinitely(Sign.NON_NEGATIVE)

fun Expression.isDefinitelyNegative() = isDefinitely(Sign.NEGATIVE)

fun Expression.isDefinitelyPositive() = isDefinitely(Sign.POSITIVE)

fun Expression.isDefinitely(wantedSign: Sign): Boolean {
    val sign = signOf()
    return when {
        sign.implies(wantedSign) -> true
        sign.implies(wantedSign.negation()) -> false
        else -> Sign.fromDouble(doubleValue).implies(wantedSign)
    }
}
