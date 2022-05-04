package patterns

import expressions.Subexpression

interface Match {
    fun getBinding(p: Pattern): Subexpression?
    fun childBindings(key: Pattern, value: Subexpression): Match = ChildMatch(key, value, this)
}

object RootMatch : Match {
    override fun getBinding(p: Pattern): Subexpression? = null
}

data class ChildMatch(
    private val key: Pattern,
    private val value: Subexpression,
    private val parent: Match
) : Match {
    override fun getBinding(p: Pattern): Subexpression? {
        return if (key == p) value else parent.getBinding(p)
    }
}