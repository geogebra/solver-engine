package engine.patterns

import engine.expressions.Expression
import engine.expressions.Subexpression
import engine.operators.VariableOperator

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

open class OptionalNegPatternBase<T : Pattern>(val unsignedPattern: T) : Pattern {

    private val neg = negOf(unsignedPattern)
    private val ptn = OneOfPattern(listOf(unsignedPattern, neg, bracketOf(neg)))

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

data class SameSignPatten(val from: OptionalNegPattern, val to: Pattern) : Pattern {

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        val ptn = if (from.isNeg(match)) negOf(to) else to
        return ptn.findMatches(subexpression, match)
    }
}

data class OppositeSignPatten(val from: OptionalNegPattern, val to: Pattern) : Pattern {

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        val ptn = if (from.isNeg(match)) to else negOf(to)
        return ptn.findMatches(subexpression, match)
    }
}

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

class OptionalWrappingPattern(val pattern: Pattern, wrapper: (Pattern) -> Pattern) : Pattern {

    private val wrappingPattern = wrapper(pattern)
    private val ptn = oneOf(pattern, wrappingPattern)

    override val key = ptn

    fun isWrapping(m: Match) = m.getLastBinding(wrappingPattern) != null

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        return ptn.findMatches(subexpression, match)
    }
}

fun optional(pattern: Pattern, wrapper: (Pattern) -> Pattern) = OptionalWrappingPattern(pattern, wrapper)

fun optionalDivideBy(pattern: Pattern) = OptionalWrappingPattern(pattern, ::divideBy)
fun optionalBracketOf(pattern: Pattern) = OptionalWrappingPattern(pattern, ::bracketOf)

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

fun sameSignPattern(from: OptionalNegPattern, to: Pattern) = SameSignPatten(from, to)

fun oppositeSignPattern(from: OptionalNegPattern, to: Pattern) = OppositeSignPatten(from, to)
