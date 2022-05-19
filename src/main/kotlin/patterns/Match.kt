package patterns

import expressions.Expression
import expressions.Path
import expressions.Subexpression

interface Match {

    fun getLastBinding(p: Pattern): Subexpression?

    fun getBoundExpr(p: Pattern): Expression?

    fun accPaths(p: Pattern, acc: MutableList<Path>)

    fun getBoundPaths(p: Pattern): List<Path>

    fun newChild(key: Pattern, value: Subexpression): Match = ChildMatch(key, value, this)
}

object RootMatch : Match {

    override fun getLastBinding(p: Pattern): Subexpression? = null
    override fun getBoundExpr(p: Pattern): Expression? = null

    override fun accPaths(p: Pattern, acc: MutableList<Path>) {}
    override fun getBoundPaths(p: Pattern): List<Path> = emptyList()
}

data class ChildMatch(
    private val key: Pattern,
    private val value: Subexpression,
    private val parent: Match
) : Match {

    override fun getLastBinding(p: Pattern): Subexpression? {
        return when {
            key === p -> value
            else -> parent.getLastBinding(p)
        }
    }

    override fun getBoundExpr(p: Pattern): Expression? {
        return if (key === p) value.expr else parent.getBoundExpr(p)
    }

    override fun accPaths(p: Pattern, acc: MutableList<Path>) {
        parent.accPaths(p, acc)
        if (key == p) {
            acc.add(value.path)
        }
    }

    override fun getBoundPaths(p: Pattern): List<Path> {
        var acc = mutableListOf<Path>()
        accPaths(p, acc)
        return acc
    }
}