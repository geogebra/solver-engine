package engine.patterns

import engine.expressions.Expression

/**
 * An interface for matching with a pattern. Can be
 * thought of as a linked-list of (pattern, pattern's value)
 */
interface Match {

    /**
     * Returns the `Expression` of the given pattern
     * with the provided `match` "m". It differs from
     * getBoundExpr as it returns the "Expression"
     * , i.e. value and the path
     */
    fun getLastBinding(p: Pattern): Expression?

    fun isBound(p: Pattern) = getLastBinding(p) != null

    /**
     * Returns the `Expression` value of the given match
     * with the provided `Pattern` "p".
     */
    fun getBoundExpr(p: Pattern): Expression?

    /**
     * Appends to the list `acc` the `Path`s from
     * root to the start of the pattern `p` in
     * a provided match
     *
     * @see getBoundExprs
     */
    fun accumulateExprs(p: Pattern, acc: MutableList<Expression>)

    /**
     * Returns a list of `Path` objects from the root of
     * the tree to the where the pattern `p` is present
     * in a provided match object
     */
    fun getBoundExprs(p: Pattern): List<Expression>

    fun newChild(key: Pattern, value: Expression): Match = ChildMatch(key, value, this)
}

/**
 * Object to refer to the matching with the root of
 * expression tree.
 */
object RootMatch : Match {

    override fun getLastBinding(p: Pattern): Expression? = null
    override fun getBoundExpr(p: Pattern): Expression? = null

    override fun accumulateExprs(p: Pattern, acc: MutableList<Expression>) { /* do nothing */
    }

    override fun getBoundExprs(p: Pattern): List<Expression> = emptyList()
}

/**
 * Used to create a non-root `Match` object. Created
 * with `Pattern` pointing to a `Subexpression` value.
 *
 * @param key the pattern to be searched for
 * @param value the expression in which to search for the pattern
 * @param parent the parent Match object of the ChildMatch object
 */
data class ChildMatch(
    private val key: Pattern,
    private val value: Expression,
    private val parent: Match
) : Match {

    override fun getLastBinding(p: Pattern): Expression? {
        return when {
            key === p.key -> value
            else -> parent.getLastBinding(p)
        }
    }

    override fun getBoundExpr(p: Pattern): Expression? {
        return if (key === p.key) value else parent.getBoundExpr(p)
    }

    override fun accumulateExprs(p: Pattern, acc: MutableList<Expression>) {
        parent.accumulateExprs(p, acc)
        if (key == p.key) {
            acc.add(value)
        }
    }

    override fun getBoundExprs(p: Pattern): List<Expression> {
        val acc = mutableListOf<Expression>()
        accumulateExprs(p, acc)
        return acc
    }
}
