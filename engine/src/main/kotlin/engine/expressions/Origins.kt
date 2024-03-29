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

abstract class Origin(val path: Path? = null) {
    abstract fun computeChildOrigin(expression: Expression, index: Int): Expression

    open fun computeChildrenOrigin(expression: Expression): List<Expression> =
        List(expression.operands.size) { i -> this.computeChildOrigin(expression, i) }

    abstract fun computePathMappings(rootPath: Path, children: List<Expression>): Sequence<PathMapping>

    open fun fromPaths(children: List<Expression>): List<Path> = if (path == null) emptyList() else listOf(path)
}

class RootOrigin(private val rootPath: Path = RootPath()) : Origin(rootPath) {
    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Child(expression, index))

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        sequenceOf(
            PathMapping(
                listOf(Pair(this.rootPath, PathScope.default)),
                PathMappingType.Shift,
                listOf(Pair(rootPath, PathScope.default)),
            ),
        )

    override fun equals(other: Any?) =
        this === other ||
            other is RootOrigin && rootPath == other.rootPath

    override fun hashCode() = rootPath.hashCode()
}

class Child(val parent: Expression, val index: Int) : Origin(parent.origin.path?.child(index)) {
    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Child(expression, index))

    override fun computePathMappings(rootPath: Path, children: List<Expression>): Sequence<PathMapping> {
        return parent.pathMappings(rootPath)
            .map { m ->
                PathMapping(
                    m.fromPaths.map { it.first.child(index) to it.second },
                    m.type,
                    m.toPaths,
                )
            }
    }

    override fun equals(other: Any?) =
        this === other || (
            other is Child &&
                index == other.index &&
                (parent === other.parent || parent.origin == other.parent.origin)
        )

    override fun hashCode() = index.hashCode() * 31 + parent.origin.hashCode()
}

object New : Origin() {
    override fun computeChildOrigin(expression: Expression, index: Int) = expression.operands[index].withOrigin(New)

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        sequenceOf(PathMapping(emptyList(), PathMappingType.Introduce, listOf(Pair(rootPath, PathScope.default))))
}

object Build : Origin() {
    override fun computeChildOrigin(expression: Expression, index: Int) = expression.operands[index]

    override fun computeChildrenOrigin(expression: Expression) = expression.operands

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        when {
            children.isEmpty() -> sequenceOf(
                PathMapping(
                    emptyList(),
                    PathMappingType.Introduce,
                    listOf(Pair(rootPath, PathScope.default)),
                ),
            )
            else -> children.mapIndexed { i, child -> child.pathMappings(rootPath.child(i)) }.asSequence().flatten()
        }

    override fun fromPaths(children: List<Expression>): List<Path> {
        return children.flatMap { it.origin.fromPaths(it.children) }
    }
}

object Unknown : Origin() {
    override fun computeChildOrigin(expression: Expression, index: Int) = expression.operands[index].withOrigin(Unknown)

    override fun computePathMappings(rootPath: Path, children: List<Expression>) = emptySequence<PathMapping>()
}

class Move(val from: Expression) : Origin() {
    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Move(from.nthChild(index)))

    override fun computePathMappings(rootPath: Path, children: List<Expression>): Sequence<PathMapping> {
        val fromPath = from.origin.path
        return if (fromPath == null) {
            sequenceOf(PathMapping(listOf(), PathMappingType.Introduce, listOf(Pair(rootPath, PathScope.default))))
        } else {
            sequenceOf(
                PathMapping(
                    listOf(Pair(fromPath, PathScope.default)),
                    PathMappingType.Move,
                    listOf(Pair(rootPath, PathScope.default)),
                ),
            )
        }
    }
}

/**
 * origin: origin of the unary operator
 */
class MoveUnaryOperator(val origin: Origin) : Origin() {
    override fun computeChildOrigin(expression: Expression, index: Int) = expression.operands[index]

    override fun computePathMappings(rootPath: Path, children: List<Expression>): Sequence<PathMapping> {
        val childrenMappings = children.mapIndexed { i, child ->
            child.pathMappings(rootPath.child(i))
        }.asSequence().flatten()
        return if (origin.path == null) {
            childrenMappings
        } else {
            sequenceOf(
                PathMapping(
                    listOf(Pair(origin.path, PathScope.Operator)),
                    PathMappingType.Move,
                    listOf(Pair(rootPath, PathScope.Operator)),
                ),
            ) + childrenMappings
        }
    }
}

class Introduce(val from: List<Expression>) : Origin() {
    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Introduce(from))

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        sequenceOf(
            PathMapping(
                from.flatMap {
                    it.origin.fromPaths(it.children).map { path ->
                        path to PathScope.default
                    }
                },
                PathMappingType.Introduce,
                listOf(Pair(rootPath, PathScope.default)),
            ),
        )
}

class Combine(val from: List<Expression>) : Origin() {
    constructor(vararg fromExprs: Expression) : this(fromExprs.asList())

    override fun computeChildOrigin(expression: Expression, index: Int) = expression.operands[index].withOrigin(Unknown)

    override fun computePathMappings(rootPath: Path, children: List<Expression>): Sequence<PathMapping> {
        val fromPaths = from.flatMapIndexed { idx, expr ->
            val paths = expr.origin.fromPaths(expr.children).map { path -> path to PathScope.default }
            val path = expr.origin.path
            if (idx == 0 || path == null) {
                paths
            } else {
                paths + listOf(Pair(path, PathScope.OuterOperator))
            }
        }
        return sequenceOf(
            PathMapping(
                fromPaths,
                when (from.size) {
                    0 -> PathMappingType.Introduce
                    1 -> PathMappingType.Transform
                    else -> PathMappingType.Combine
                },
                listOf(Pair(rootPath, PathScope.default)),
            ),
        )
    }
}

class Factor(val from: List<Expression>, val pathScope: PathScope = PathScope.default) : Origin() {
    constructor(vararg fromExprs: Expression) : this(fromExprs.asList())

    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Factor(from.map { it.nthChild(index) }))

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        sequenceOf(
            PathMapping(
                from.flatMap {
                    it.origin.fromPaths(it.children).map { path ->
                        path to pathScope
                    }
                },
                PathMappingType.Factor,
                listOf(Pair(rootPath, pathScope)),
            ),
        )
}

class Distribute(val from: List<Expression>) : Origin() {
    constructor(fromExpr: Expression) : this(listOf(fromExpr))

    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Distribute(from.map { it.nthChild(index) }))

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        sequenceOf(
            PathMapping(
                from.flatMap {
                    it.origin.fromPaths(it.children).map { path ->
                        path to PathScope.default
                    }
                },
                PathMappingType.Distribute,
                listOf(Pair(rootPath, PathScope.default)),
            ),
        )
}

class Cancel(val origin: Origin, private val cancelParts: List<Pair<Expression, PathScope>>) : Origin(origin.path) {
    override fun computeChildOrigin(expression: Expression, index: Int): Expression {
        return origin.computeChildOrigin(expression, index)
    }

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        origin.computePathMappings(rootPath, children) +
            sequenceOf(
                PathMapping(
                    cancelParts.mapNotNull { it.first.origin.path?.let { path -> Pair(path, it.second) } },
                    PathMappingType.Cancel,
                    emptyList(),
                ),
            )
}

class Substitution(val from: List<Expression>) : Origin() {
    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Substitution(from))

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        sequenceOf(
            PathMapping(
                from.flatMap {
                    it.origin.fromPaths(it.children).map { path ->
                        path to PathScope.default
                    }
                },
                PathMappingType.Substitute,
                listOf(Pair(rootPath, PathScope.default)),
            ),
        )
}
