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
    Cancel,
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
