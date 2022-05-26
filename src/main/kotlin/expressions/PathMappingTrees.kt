package expressions


interface PathMappingTree {

    fun nthChild(index: Int): PathMappingTree

    fun childList(size: Int): List<PathMappingTree>

    fun pathMappings(root: Path): Sequence<PathMapping>

    fun subtree(path: Path): PathMappingTree {
        return when (path) {
            is RootPath -> this
            is ChildPath -> subtree(path.parent).nthChild(path.index)
            else -> throw IllegalArgumentException()
        }
    }

    fun composeWith(previous: PathMappingTree): PathMappingTree
}

data class PathMappingLeaf(val paths: List<Path>, val type: PathMappingType) : PathMappingTree {

    override fun nthChild(index: Int): PathMappingTree {
        return PathMappingLeaf(paths.map { it.child(index) }, type)
    }

    override fun childList(size: Int) =
        (0 until size).map { nthChild(it) }

    override fun pathMappings(root: Path) = sequenceOf(PathMapping(paths, type, root))

    override fun composeWith(previous: PathMappingTree): PathMappingTree {
        // TODO: this is WIP
        val combined = combinePathMappingTrees(paths.map { previous.subtree(it) })
        if (type == PathMappingType.Combine || type == PathMappingType.Move) {
            return combined
        }
        if (combined is PathMappingLeaf && combined.type == PathMappingType.Move) {
            return PathMappingLeaf(combined.paths, type)
        }

        when (type) {
            PathMappingType.Move -> return combined
            PathMappingType.Combine -> return combined
        }
        // TODO: this is wrong
        return combined
    }
}

data class PathMappingParent(val children: List<PathMappingTree>) : PathMappingTree {

    override fun nthChild(index: Int): PathMappingTree {
        return children[index]
    }

    override fun childList(size: Int): List<PathMappingTree> {
        return children
    }

    override fun pathMappings(root: Path): Sequence<PathMapping> {
        return children.mapIndexed { i, child -> child.pathMappings(root.child(i)) }.asSequence().flatten()
    }

    override fun composeWith(previous: PathMappingTree): PathMappingTree {
        return PathMappingParent(children.map { it.composeWith(previous) })
    }
}

fun combinePathMappingTrees(trees: List<PathMappingTree>): PathMappingTree {
    if (trees.size == 1) {
        return trees[0]
    }
    val paths = trees.map { if (it is PathMappingLeaf) it.paths else emptyList() }.flatten()
    return PathMappingLeaf(paths, PathMappingType.Combine)
}
