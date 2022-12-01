package engine.patterns

import engine.context.Context
import engine.expressions.Expression
import engine.expressions.New
import engine.expressions.Path

interface PathProvider {

    /**
     * Returns a list of `Path` objects from the root of
     * the tree to the where the pattern is present in the
     * match `m`.
     */
    fun getBoundPaths(m: Match): List<Path>

    /**
     * Returns the `Expression` value of the given pattern
     * with the provided `match` "m"
     */
    fun getBoundExpr(m: Match): Expression?
}

fun interface ExpressionProvider : PathProvider {

    fun getExpression(m: Match): Expression?
    override fun getBoundPaths(m: Match) = getExpression(m)?.getBoundPaths(m) ?: emptyList()

    override fun getBoundExpr(m: Match) = getExpression(m)
}

open class ProviderWithDefault(
    private val provider: PathProvider,
    private val default: Expression
) : PathProvider {
    override fun getBoundPaths(m: Match): List<Path> {
        return provider.getBoundPaths(m)
    }

    override fun getBoundExpr(m: Match): Expression {
        return provider.getBoundExpr(m) ?: default.withOrigin(New)
    }
}

/**
 * Patterns are used to detect certain shapes in a [Subexpression].
 */
interface Pattern : PathProvider {
    /**
     * Gives a [Sequence] of all possible matches of [Pattern] object
     * in the [subexpression] building on the provided [match].
     */
    fun findMatches(context: Context, match: Match = RootMatch, subexpression: Expression): Sequence<Match>

    /**
     * Returns a list of [Path] objects from the root of
     * the tree to the where the pattern is present in the
     * match [m].
     */
    override fun getBoundPaths(m: Match) = m.getBoundPaths(this.key)

    override fun getBoundExpr(m: Match) = m.getBoundExpr(this.key)

    fun matches(context: Context, expression: Expression): Boolean {
        return findMatches(context, RootMatch, expression).any()
    }

    val key: Pattern
}

/**
 * A type of pattern which defines a basic way of matching - its [key] is always equal to the instance itself.
 */
abstract class BasePattern : Pattern {

    internal abstract fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match>

    /**
     * Checks any potential existing match for this pattern is equivalent to [subexpression] and then use the
     * [doFindMatches] function to return all possible matches.
     */
    final override fun findMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        if (!this.checkPreviousMatch(subexpression, match)) {
            return emptySequence()
        }
        return doFindMatches(context, match, subexpression)
    }

    /**
     * Returns `true` when either the expression value
     * of match is `null` (i.e. root object) or is equivalent
     * to the passed `expr` else return `false`
     */
    private fun checkPreviousMatch(expr: Expression, match: Match): Boolean {
        val previous = getBoundExpr(match)
        return previous == null || previous.equiv(expr)
    }

    final override val key: Pattern get() = this
}

/**
 * A type of pattern whose matching is defined by the value of [key].  It can be subclassed for commonly used non-basic
 * patterns and if we want to add extra behaviour (e.g. see [FractionPattern]).
 */
abstract class KeyedPattern : Pattern {
    final override fun findMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return key.findMatches(context, match, subexpression)
    }
}
