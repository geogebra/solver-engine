package steps

import expressions.Path
import expressions.cancelPath

data class PathMapping(
    val fromPath: Path,
    val type: PathMappingType,
    val toPath: Path,
)

enum class PathMappingType {
    Move,
    Transform,
    Combine,
    Factor,
    Distribute,
    Relate,
    Cancel,
}

interface PathMapper {
    fun accPathMappings(path: Path, acc: MutableList<PathMapping>)
}

data class TypePathMapper(val fromPaths: List<Path>, val type: PathMappingType) : PathMapper {
    override fun accPathMappings(toPath: Path, acc: MutableList<PathMapping>) {
        for (fromPath in fromPaths) {
            acc.add(PathMapping(fromPath, type, toPath))
        }
    }

    override fun toString(): String {
        return "from=${fromPaths.joinToString(",")} type=$type"
    }
}

data class VanishingPathMapper(val fromPaths: List<Path>, val type: PathMappingType) : PathMapper {
    override fun accPathMappings(path: Path, acc: MutableList<PathMapping>) {
        for (fromPath in fromPaths) {
            acc.add(PathMapping(fromPath, type, cancelPath(fromPaths[0])))
        }
    }
}