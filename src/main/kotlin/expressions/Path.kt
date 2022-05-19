package expressions

interface Path {
    fun child(index: Int): Path {
        return ChildPath(this, index)
    }

    fun hasAncestor(path: Path) = this == path

    fun truncate(newLength: Int) = this
    
    val length: Int
}

data class ChildPath(val parent: Path, val index: Int) : Path {
    override val length = parent.length + 1

    override fun toString() = "${parent}/${index}"

    override fun hasAncestor(path: Path) = when {
        path.length > length -> false
        else -> truncate(path.length) == path
    }

    override fun truncate(newLength: Int): Path = when {
        newLength < length -> parent.truncate(newLength)
        else -> this
    }
}

object RootPath : Path {
    override fun toString() = "."
    override val length = 0
}

object CancelRootPath : Path {
    override fun toString() = "x"
    override val length = 0
}

object IntroduceRootPath : Path {
    override fun toString() = "+"
    override val length = 0
}

data class Subexpression(val path: Path, val expr: Expression) {
    fun nthChild(index: Int): Subexpression {
        return Subexpression(path.child(index), expr.children().elementAt(index))
    }

    fun children(): List<Subexpression> {
        return expr.children().mapIndexed { i, child -> Subexpression(path.child(i), child) }
    }

    fun index() = when (path) {
        is ChildPath -> path.index
        else -> 0
    }

    fun substitute(sub: Subexpression): Expression {
        return when {
            path == sub.path -> sub.expr
            !sub.path.hasAncestor(path) -> expr
            else -> expr.mapChildrenIndexed { i, child -> Subexpression(path.child(i), child).substitute(sub) }
        }
    }

    fun subst(sub: Subexpression): Subexpression {
        return Subexpression(path, substitute(sub))
    }
}

fun pathOf(vararg indexes: Int): Path {
    var path: Path = RootPath
    for (i in indexes) {
        path = path.child(i)
    }
    return path
}

fun cancelPath(path: Path): Path {
    return when (path) {
        is ChildPath -> ChildPath(cancelPath(path.parent), path.index)
        else -> CancelRootPath
    }
}