package engine.patterns

import engine.expressions.Expression
import engine.expressions.Subexpression
import engine.expressions.VariableOperator

/**
 * A pattern which matches only the exact expression it was
 * created with
 */
data class FixedPattern(val expr: Expression) : Pattern {

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        return when {
            subexpression.expr.equiv(expr) -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

/**
 * Used to create a `Pattern` to match with any possible
 * expression
 */
class AnyPattern : Pattern {

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        return sequenceOf(match.newChild(this, subexpression))
    }
}

open class OptionalNegPatternBase<T : Pattern>(val pattern: T) : Pattern {

    private val neg = negOf(pattern)
    private val ptn = OneOfPattern(listOf(pattern, neg, bracketOf(neg)))

    override val key = ptn

    fun isNeg(m: Match) = m.getLastBinding(neg) != null

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        return ptn.findMatches(subexpression, match)
    }
}

class OptionalNegPattern(pattern: Pattern) : OptionalNegPatternBase<Pattern>(pattern)

/**
 * A pattern to match with a variable (i.e. symbol)
 */
class VariablePattern : Pattern {
    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }

        return when (subexpression.expr.operator) {
            is VariableOperator -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

/**
 * Used to find matches in a `Subexpression` object, either containing
 * the `Pattern` pattern or division by the pattern
 */
data class OptionalDivideBy(val pattern: Pattern) : Pattern {

    private val divide = divideBy(pattern)
    private val ptn = oneOf(pattern, divide)

    override val key = ptn

    fun isDivide(m: Match) = m.getLastBinding(divide) != null

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        return ptn.findMatches(subexpression, match)
    }
}

fun optionalNegOf(operand: Pattern) = OptionalNegPattern(operand)
