/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package engine.expressions

interface PathMappingTree {
    fun nthChild(index: Int): PathMappingTree

    fun childList(size: Int): List<PathMappingTree>

    fun pathMappings(root: Path): Sequence<PathMapping>

    fun subtree(path: Path): PathMappingTree {
        return when (path) {
            is RootPath -> this
            is ChildPath -> subtree(path.parent).nthChild(path.index)
        }
    }

    fun composeWith(previous: PathMappingTree): PathMappingTree
}

data class PathMappingLeaf(val paths: List<Path>, val type: PathMappingType) : PathMappingTree {
    override fun nthChild(index: Int): PathMappingTree {
        return PathMappingLeaf(paths.map { it.child(index) }, type)
    }

    override fun childList(size: Int) = (0 until size).map { nthChild(it) }

    override fun pathMappings(root: Path) =
        sequenceOf(
            PathMapping(
                paths.map { it to PathScope.default },
                type,
                listOf(root to PathScope.default),
            ),
        )

    override fun composeWith(previous: PathMappingTree): PathMappingTree {
        // WIP
        return previous
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
