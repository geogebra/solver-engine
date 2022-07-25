package engine.patterns

import engine.expressions.Expression
import engine.expressions.Subexpression
import java.math.BigInteger

interface MatchCondition {
    fun checkMatch(match: Match): Boolean
}

data class ConditionPattern(val pattern: Pattern, val condition: MatchCondition) : Pattern {

    override val key = pattern.key

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        return pattern.findMatches(subexpression, match).filter { condition.checkMatch(it) }
    }
}

data class UnaryCondition(
    val ptn: Pattern,
    val condition: (Expression) -> Boolean,
) : MatchCondition {
    override fun checkMatch(match: Match): Boolean {
        return condition(ptn.getBoundExpr(match)!!)
    }
}

data class UnaryNumericCondition(
    val ptn: IntegerProvider,
    val condition: (BigInteger) -> Boolean
) : MatchCondition {
    override fun checkMatch(match: Match): Boolean {
        return condition(ptn.getBoundInt(match))
    }
}

data class BinaryNumericCondition(
    val ptn1: IntegerProvider,
    val ptn2: IntegerProvider,
    val condition: (BigInteger, BigInteger) -> Boolean
) : MatchCondition {
    override fun checkMatch(match: Match): Boolean {
        return condition(ptn1.getBoundInt(match), ptn2.getBoundInt(match))
    }
}

fun condition(
    ptn: Pattern,
    condition: (Expression) -> Boolean
) = UnaryCondition(ptn, condition)

fun numericCondition(
    ptn: IntegerProvider,
    condition: (BigInteger) -> Boolean,
) = UnaryNumericCondition(ptn, condition)

fun numericCondition(
    ptn1: IntegerProvider,
    ptn2: IntegerProvider,
    condition: (BigInteger, BigInteger) -> Boolean,
) = BinaryNumericCondition(ptn1, ptn2, condition)
