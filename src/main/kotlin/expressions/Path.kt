package expressions

interface Path {
    fun child(index: Int) : Path {
        return ChildPath(this, index)
    }
}
data class ChildPath(val parent : Path, val index: Int): Path {
    override fun toString() ="${parent}/${index}"
}

object RootPath : Path {
    override fun toString() = "."
}

data class Subexpression(val path: Path, val expr: Expression) {
    fun nthChild(index : Int): Subexpression {
        return Subexpression(path.child(index), expr.children().elementAt(index))
    }

}

fun pathOf(vararg indexes: Int): Path {
    var path : Path = RootPath
    for (i in indexes) {
        path = path.child(i)
    }
    return path
}