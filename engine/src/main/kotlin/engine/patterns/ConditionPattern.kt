package engine.patterns

import engine.context.Context
import engine.expressionbuilder.MappedExpressionBuilder
import engine.expressions.Expression
import java.math.BigDecimal
import java.math.BigInteger

data class PureConditionPattern(private val condition: Context.(Expression) -> Boolean) : BasePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression) =
        if (context.condition(subexpression)) sequenceOf(match.newChild(this, subexpression)) else sequenceOf()
}

fun interface MatchCondition {
    fun checkMatch(context: Context, match: Match, subexpression: Expression): Boolean
}

class BuilderCondition(private val condition: MappedExpressionBuilder.() -> Boolean) : MatchCondition {
    override fun checkMatch(context: Context, match: Match, subexpression: Expression): Boolean {
        return MappedExpressionBuilder(context, subexpression, match).condition()
    }
}

data class ConditionPattern(
    private val pattern: Pattern,
    private val condition: MatchCondition,
) : Pattern {
    override val key = pattern.key

    override fun findMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return pattern.findMatches(context, match, subexpression).filter {
            condition.checkMatch(context, it, subexpression)
        }
    }

    override val minDepth = pattern.minDepth
}

data class IntegerConditionPattern(
    private val pattern: IntegerPattern,
    private val condition: (BigInteger) -> Boolean,
) : IntegerPattern by pattern {
    override val key = pattern.key

    override fun findMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return pattern.findMatches(context, match, subexpression).filter { condition(pattern.getBoundInt(it)) }
    }
}

data class NumericConditionPattern(
    private val pattern: NumberPattern,
    private val condition: (BigDecimal) -> Boolean,
) : NumberPattern by pattern {
    override val key = pattern.key

    override fun findMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return pattern.findMatches(context, match, subexpression).filter { condition(pattern.getBoundNumber(it)) }
    }
}

data class BinaryIntegerCondition(
    val ptn1: IntegerProvider,
    val ptn2: IntegerProvider,
    val condition: Context.(BigInteger, BigInteger) -> Boolean,
) : MatchCondition {
    override fun checkMatch(context: Context, match: Match, subexpression: Expression): Boolean {
        return context.condition(ptn1.getBoundInt(match), ptn2.getBoundInt(match))
    }
}

data class TernaryIntegerCondition(
    val ptn1: IntegerProvider,
    val ptn2: IntegerProvider,
    val ptn3: IntegerProvider,
    val condition: Context.(BigInteger, BigInteger, BigInteger) -> Boolean,
) : MatchCondition {
    override fun checkMatch(context: Context, match: Match, subexpression: Expression): Boolean {
        return context.condition(
            ptn1.getBoundInt(match),
            ptn2.getBoundInt(match),
            ptn3.getBoundInt(match),
        )
    }
}

data class BinaryNumericCondition(
    val ptn1: NumberProvider,
    val ptn2: NumberProvider,
    val condition: Context.(BigDecimal, BigDecimal) -> Boolean,
) : MatchCondition {
    override fun checkMatch(context: Context, match: Match, subexpression: Expression): Boolean {
        return context.condition(ptn1.getBoundNumber(match), ptn2.getBoundNumber(match))
    }
}

fun condition(condition: Context.(Expression) -> Boolean) = PureConditionPattern(condition)

fun condition(ptn: Pattern, condition: Context.(Expression) -> Boolean) =
    ConditionPattern(ptn) { context, _, expression -> context.condition(expression) }

fun integerCondition(ptn: IntegerPattern, condition: (BigInteger) -> Boolean) = IntegerConditionPattern(ptn, condition)

fun integerCondition(
    ptn1: IntegerProvider,
    ptn2: IntegerProvider,
    condition: Context.(BigInteger, BigInteger) -> Boolean,
) = BinaryIntegerCondition(ptn1, ptn2, condition)

fun integerCondition(
    ptn1: IntegerProvider,
    ptn2: IntegerProvider,
    ptn3: IntegerProvider,
    condition: Context.(BigInteger, BigInteger, BigInteger) -> Boolean,
) = TernaryIntegerCondition(ptn1, ptn2, ptn3, condition)

fun numericCondition(ptn: NumberPattern, condition: (BigDecimal) -> Boolean) = NumericConditionPattern(ptn, condition)

fun numericCondition(
    ptn1: NumberProvider,
    ptn2: NumberProvider,
    condition: Context.(BigDecimal, BigDecimal) -> Boolean,
) = BinaryNumericCondition(ptn1, ptn2, condition)

/**
 * Wraps [ptn] in a condition which is only true when the expression matching [ptn] contains exactly one variable and
 * that is the context's solution variable.
 */
fun inSolutionVariables(ptn: Pattern) =
    condition(ptn) { it.variables.size == solutionVariables.size && it.variables.containsAll(solutionVariables) }
