package engine.patterns

import engine.expressions.Expression
import engine.expressions.Path
import engine.expressions.Subexpression

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

interface Pattern : PathProvider {
    /**
     * Gives a `Sequence` of all possible matches of `Pattern` object
     * in the `subexpression` using the built-up `match` until the
     * `subexpression` and extending it
     */
    fun findMatches(subexpression: Subexpression, match: Match = RootMatch): Sequence<Match>

    /**
     * Returns a list of `Path` objects from the root of
     * the tree to the where the pattern is present in the
     * match `m`.
     */
    override fun getBoundPaths(m: Match) = m.getBoundPaths(this)

    override fun getBoundExpr(m: Match) = m.getBoundExpr(this)

    fun matches(expression: Expression): Boolean {
        return findMatches(Subexpression(expression), RootMatch).any()
    }

    /**
     * Returns `true` when either the expression value
     * of match is `null` (i.e. root object) or is equivalent
     * to the passed `expr` else return `false`
     */
    fun checkPreviousMatch(expr: Expression, match: Match): Boolean {
        val previous = getBoundExpr(match)
        return previous == null || previous.equiv(expr)
    }

    val key get() = this
}
