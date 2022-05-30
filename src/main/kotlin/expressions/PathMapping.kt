package expressions

data class PathMapping(
    val fromPaths: List<Path>,
    val type: PathMappingType,
    val toPaths: List<Path>,
) {
    fun relativeTo(fromRoot: Path = RootPath, toRoot: Path = RootPath): PathMapping {
        return PathMapping(fromPaths.map { it.relativeTo(fromRoot) }, type, toPaths.map { it.relativeTo(toRoot) })
    }
}

enum class PathMappingType {
    Move {
        override fun composeWith(other: PathMappingType) = other
    },
    Relate,
    Combine,
    Factor,
    Distribute,

    /**
     * Used when two or more expressions cancel with each other,
     * e.g. `a - a` or `[ab / ac]`
     */
    Cancel,

    /**
     * Used when a single subexpression changes into another
     * independently of others, e.g. `3 * 4 * 0 * 5 -> 0`
     * or `d/dx sin(x) -> cos(x)`
     */
    Transform,

    Introduce;

    open fun composeWith(other: PathMappingType): PathMappingType {
        return when (other) {
            Move -> this
            Cancel -> Cancel
            this -> this
            else -> Relate
        }
    }
}
