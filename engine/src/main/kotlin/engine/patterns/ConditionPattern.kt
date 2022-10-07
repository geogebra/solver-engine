package engine.patterns

import engine.expressions.Expression
import engine.expressions.Subexpression
import java.math.BigDecimal
import java.math.BigInteger

fun interface MatchCondition {
    fun checkMatch(match: Match): Boolean
}

data class ConditionPattern(
    private val pattern: Pattern,
    private val condition: MatchCondition
) : Pattern {

    override val key = pattern.key

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        return pattern.findMatches(subexpression, match).filter { condition.checkMatch(it) }
    }
}

data class IntegerConditionPattern(
    private val pattern: IntegerPattern,
    private val condition: (BigInteger) -> Boolean,
) : IntegerPattern by pattern {

    override val key = pattern.key

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        return pattern.findMatches(subexpression, match).filter { condition(pattern.getBoundInt(it)) }
    }
}

data class NumericConditionPattern(
    private val pattern: NumberPattern,
    private val condition: (BigDecimal) -> Boolean,
) : NumberPattern by pattern {

    override val key = pattern.key

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        return pattern.findMatches(subexpression, match).filter { condition(pattern.getBoundNumber(it)) }
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

data class BinaryIntegerCondition(
    val ptn1: IntegerProvider,
    val ptn2: IntegerProvider,
    val condition: (BigInteger, BigInteger) -> Boolean
) : MatchCondition {
    override fun checkMatch(match: Match): Boolean {
        return condition(ptn1.getBoundInt(match), ptn2.getBoundInt(match))
    }
}

data class TernaryIntegerCondition(
    val ptn1: IntegerProvider,
    val ptn2: IntegerProvider,
    val ptn3: IntegerProvider,
    val condition: (BigInteger, BigInteger, BigInteger) -> Boolean
) : MatchCondition {
    override fun checkMatch(match: Match): Boolean {
        return condition(
            ptn1.getBoundInt(match),
            ptn2.getBoundInt(match),
            ptn3.getBoundInt(match)
        )
    }
}

data class BinaryNumericCondition(
    val ptn1: NumberProvider,
    val ptn2: NumberProvider,
    val condition: (BigDecimal, BigDecimal) -> Boolean
) : MatchCondition {
    override fun checkMatch(match: Match): Boolean {
        return condition(ptn1.getBoundNumber(match), ptn2.getBoundNumber(match))
    }
}

fun condition(
    ptn: Pattern,
    condition: (Expression) -> Boolean
) = ConditionPattern(ptn, UnaryCondition(ptn, condition))

fun integerCondition(
    ptn: IntegerPattern,
    condition: (BigInteger) -> Boolean,
) = IntegerConditionPattern(ptn, condition)

fun integerCondition(
    ptn1: IntegerProvider,
    ptn2: IntegerProvider,
    ptn3: IntegerProvider,
    condition: (BigInteger, BigInteger, BigInteger) -> Boolean,
) = TernaryIntegerCondition(ptn1, ptn2, ptn3, condition)

fun integerCondition(
    ptn1: IntegerProvider,
    ptn2: IntegerProvider,
    condition: (BigInteger, BigInteger) -> Boolean,
) = BinaryIntegerCondition(ptn1, ptn2, condition)

fun numericCondition(
    ptn: NumberPattern,
    condition: (BigDecimal) -> Boolean,
) = NumericConditionPattern(ptn, condition)

fun numericCondition(
    ptn1: NumberProvider,
    ptn2: NumberProvider,
    condition: (BigDecimal, BigDecimal) -> Boolean,
) = BinaryNumericCondition(ptn1, ptn2, condition)
