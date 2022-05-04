package steps

import expressions.Path

data class PathMapping(
    val fromPath: Path,
    val type: PathMappingType,
    val toPath: Path,
)

enum class PathMappingType {
    Move,
    Transform,
    Combine,
    Distribute,
    Relate,
}

interface PathMapper {
    fun accPathMaps(path: Path, acc: MutableList<PathMapping>)
}

data class TypePathMapper(val fromPaths: List<Path>, val type: PathMappingType): PathMapper {
    override fun accPathMaps(toPath: Path, acc: MutableList<PathMapping>) {
        for (fromPath in fromPaths) {
            acc.add(PathMapping(fromPath, type, toPath))
        }
    }

    override fun toString(): String {
        return "from=${fromPaths.joinToString("," )} type=$type"
    }
}
