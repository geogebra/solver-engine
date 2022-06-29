package engine.expressions

sealed interface Path {
    fun child(index: Int): Path {
        return ChildPath(this, index)
    }

    fun hasAncestor(path: Path) = this == path

    fun truncate(newLength: Int) = this

    val length: Int

    fun relativeTo(path: Path): Path
}

data class ChildPath(val parent: Path, val index: Int) : Path {
    override val length = parent.length + 1

    override fun toString() = "$parent/$index"

    override fun hasAncestor(path: Path) = when {
        path.length > length -> false
        else -> truncate(path.length) == path
    }

    override fun truncate(newLength: Int): Path = when {
        newLength < length -> parent.truncate(newLength)
        else -> this
    }

    override fun relativeTo(path: Path) = when {
        path is RootPath -> this
        this == path -> RootPath
        this.length <= path.length -> this
        else -> parent.relativeTo(path).child(index)
    }
}

object RootPath : Path {
    override fun toString() = "."
    override val length = 0

    override fun relativeTo(path: Path) = this
}

fun pathOf(vararg indexes: Int): Path {
    var path: Path = RootPath
    for (i in indexes) {
        path = path.child(i)
    }
    return path
}

fun parsePath(s: String): Path {
    val pieces = s.split('/')
    if (pieces[0] != ".") throw IllegalArgumentException("$s is not a valid path")

    var path: Path = RootPath
    for (piece in pieces.drop(1)) {
        path = path.child(piece.toInt())
    }

    return path
}
