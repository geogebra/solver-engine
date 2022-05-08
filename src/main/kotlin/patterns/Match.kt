package patterns

import expressions.Path
import expressions.Subexpression

interface Match {
    fun getBinding(p: Pattern): Subexpression?

    fun accPaths(p: Pattern, acc: MutableList<Path>)

    fun getPaths(p: Pattern): List<Path>

    fun childBindings(key: Pattern, value: Subexpression): Match = ChildMatch(key, value, this)
}

object RootMatch : Match {
    override fun getBinding(p: Pattern): Subexpression? = null

    override fun accPaths(p: Pattern, acc: MutableList<Path>) {}
    override fun getPaths(p: Pattern): List<Path> = emptyList()
}

data class ChildMatch(
    private val key: Pattern,
    private val value: Subexpression,
    private val parent: Match
) : Match {
    override fun getBinding(p: Pattern): Subexpression? {
        return if (key === p) value else parent.getBinding(p)
    }

    override fun accPaths(p: Pattern, acc: MutableList<Path>) {
        parent.accPaths(p, acc)
        if (key == p) {
            acc.add(value.path)
        }
    }

    override fun getPaths(p: Pattern): List<Path> {
        var acc = mutableListOf<Path>()
        accPaths(p, acc)
        return acc
    }
}