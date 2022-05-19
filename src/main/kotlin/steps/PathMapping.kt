package steps

import expressions.Path
import expressions.cancelPath

data class PathMapping(
    val fromPath: Path,
    val type: PathMappingType,
    val toPath: Path,
) {
    fun composeWith(other: PathMapping): PathMapping {
        if (!this.toPath.hasAncestor(other.fromPath)) {
            return this
        }

        return this
    }

    fun parent(indexInParent: Int) = PathMapping(fromPath, type, toPath.parent(indexInParent))

    fun offsetIndex(offset: Int): PathMapping {
        TODO()
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

interface PathMapper {
    fun accPathMappings(toPath: Path, acc: MutableList<PathMapping>)
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
    override fun accPathMappings(toPath: Path, acc: MutableList<PathMapping>) {
        for (fromPath in fromPaths) {
            acc.add(PathMapping(fromPath, type, cancelPath(fromPaths[0])))
        }
    }
}