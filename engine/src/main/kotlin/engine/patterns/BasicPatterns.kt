package engine.patterns

import engine.context.Context
import engine.expressions.Expression

/**
 * A pattern which matches only the exact expression it was
 * created with
 */
data class FixedPattern(val expr: Expression) : BasePattern() {

    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return when {
            subexpression.equiv(expr) -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

/**
 * Used to create a `Pattern` to match with any possible
 * expression
 */
class AnyPattern : BasePattern() {

    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return sequenceOf(match.newChild(this, subexpression))
    }
}

class ConstantPattern : BasePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return when {
            subexpression.isConstant() -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

class ConstantInSolutionVariablePattern : BasePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return when {
            subexpression.isConstantIn(context.solutionVariable) -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

open class OptionalNegPattern<T : Pattern>(val unsignedPattern: T) :
    OptionalWrappingPattern(unsignedPattern, ::negOf) {

    fun isNeg(m: Match) = isWrapping(m)
}

data class SameSignPatten(val from: OptionalNegPattern<Pattern>, val to: Pattern) : BasePattern() {

    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        val ptn = if (from.isNeg(match)) negOf(to) else to
        return ptn.findMatches(context, match, subexpression).map { it.newChild(this, subexpression) }
    }
}

data class OppositeSignPatten(val from: OptionalNegPattern<Pattern>, val to: Pattern) : BasePattern() {

    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        val ptn = if (from.isNeg(match)) to else negOf(to)
        return ptn.findMatches(context, match, subexpression).map { it.newChild(this, subexpression) }
    }
}

open class OptionalWrappingPattern(val pattern: Pattern, wrapper: (Pattern) -> Pattern) : KeyedPattern() {

    private val wrappingPattern = wrapper(pattern)
    private val ptn = oneOf(wrappingPattern, pattern)

    override val key = ptn

    fun isWrapping(m: Match) = m.getLastBinding(wrappingPattern) != null
}

fun optional(pattern: Pattern, wrapper: (Pattern) -> Pattern) = OptionalWrappingPattern(pattern, wrapper)

fun optionalNegOf(operand: Pattern) = OptionalNegPattern(operand)
fun optionalDivideBy(pattern: Pattern) = OptionalWrappingPattern(pattern, ::divideBy)

fun sameSignPattern(from: OptionalNegPattern<Pattern>, to: Pattern) = SameSignPatten(from, to)

fun oppositeSignPattern(from: OptionalNegPattern<Pattern>, to: Pattern) = OppositeSignPatten(from, to)
