package expressions

data class Path(val parent : Path?, val index: Int) {
    fun child(index : Int) : Path {
        return Path(this, index)
    }
}

fun rootPath(index: Int) : Path = Path(null, index)

data class Subexpression(val path: Path?, val expr: Expression) {
    fun nthChild(index : Int): Subexpression {
        return Subexpression(Path(path, index), expr.children().elementAt(index))
    }
}
