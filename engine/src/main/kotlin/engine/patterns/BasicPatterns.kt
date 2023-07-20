package engine.patterns

import engine.context.Context
import engine.expressions.Child
import engine.expressions.Expression
import engine.operators.UnaryExpressionOperator

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

class VariableExpressionPattern : BasePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return when {
            !subexpression.isConstantIn(context.solutionVariables) -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

class ConstantInSolutionVariablePattern : BasePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return when {
            subexpression.isConstantIn(context.solutionVariables) -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

open class OptionalNegPattern<T : Pattern>(val unsignedPattern: T) :
    OptionalWrappingPattern(unsignedPattern, ::negOf) {

    fun isNeg(m: Match) = isWrapping(m)
}

/**
 * Like [OptionalNegPattern] but doesn't match if the parent would match because it adds a negative sign.
 * Additionally, with [stickAtInitialPositionOnly] gives control over whether we stick the negative sign
 * only when "-subexpression" is at the initial position. For e.g. `-Subexpression + rest` would stick
 * the "-" sign with "subexpression" (it's in initial position and the pattern wouldn't match),
 * while for `rest - subexpression` wouldn't stick the negative sign with "subexpression" (this pattern
 * would match)
 */
class StickyOptionalNegPattern<T : Pattern>(unsignedPattern: T, private val stickAtInitialPositionOnly: Boolean) :
    OptionalNegPattern<T>(unsignedPattern) {
    override fun findMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        // does the subexpression have a Minus operator parent
        val subExpressionHasMinusParent = subexpression.operator != UnaryExpressionOperator.Minus &&
            subexpression.parent?.operator == UnaryExpressionOperator.Minus
        // return true when parent subexpression is present as: "-Subexpression + rest" or "-SubExpression"
        // else false (i.e. when present as: "rest - SubExpression")
        val parentChildOrigin = subexpression.parent?.origin as? Child
        val negatedSubexpressionIsFirstChildOrWithoutParent = parentChildOrigin == null || parentChildOrigin.index == 0

        return if (
            subExpressionHasMinusParent && (
                !stickAtInitialPositionOnly ||
                    negatedSubexpressionIsFirstChildOrWithoutParent
                )
        ) {
            emptySequence()
        } else {
            super.findMatches(context, match, subexpression)
        }
    }
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

open class OptionalWrappingPattern(val pattern: Pattern, wrapper: (Pattern) -> Pattern) : KeyedPattern {

    private val wrappingPattern = wrapper(pattern)
    private val ptn = oneOf(wrappingPattern, pattern)

    override val key = ptn

    fun isWrapping(m: Match) = m.getLastBinding(wrappingPattern) != null
}

fun optional(pattern: Pattern, wrapper: (Pattern) -> Pattern) = OptionalWrappingPattern(pattern, wrapper)

fun optionalNegOf(operand: Pattern) = OptionalNegPattern(operand)

fun stickyOptionalNegOf(operand: Pattern, initialPositionOnly: Boolean = false) =
    StickyOptionalNegPattern(operand, initialPositionOnly)

fun optionalDivideBy(pattern: Pattern) = OptionalWrappingPattern(pattern, ::divideBy)

fun sameSignPattern(from: OptionalNegPattern<Pattern>, to: Pattern) = SameSignPatten(from, to)

fun oppositeSignPattern(from: OptionalNegPattern<Pattern>, to: Pattern) = OppositeSignPatten(from, to)
