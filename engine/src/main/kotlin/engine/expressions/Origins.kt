package engine.expressions

abstract class Origin(val path: Path? = null) {

    abstract fun computeChildOrigin(expression: Expression, index: Int): Expression

    open fun computeChildrenOrigin(expression: Expression): List<Expression> =
        List(expression.operands.size) { i -> this.computeChildOrigin(expression, i) }

    abstract fun computePathMappings(rootPath: Path, children: List<Expression>): Sequence<PathMapping>
}

class Root(private val rootPath: Path = RootPath()) : Origin(rootPath) {
    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Child(expression, index))

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        sequenceOf(PathMapping(listOf(this.rootPath), PathMappingType.Shift, listOf(rootPath)))

    override fun equals(other: Any?) = this === other ||
        other is Root && rootPath == other.rootPath

    override fun hashCode() = rootPath.hashCode()
}

class Child(val parent: Expression, val index: Int) : Origin(parent.origin.path?.child(index)) {

    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Child(expression, index))

    override fun computePathMappings(rootPath: Path, children: List<Expression>): Sequence<PathMapping> {
        return parent.pathMappings(rootPath)
            .map { m -> PathMapping(m.fromPaths.map { it.child(index) }, m.type, m.toPaths) }
    }

    override fun equals(other: Any?) = this === other || (
        other is Child &&
            index == other.index &&
            (parent === other.parent || parent.origin == other.parent.origin)
        )

    override fun hashCode() = index.hashCode() * 31 + parent.origin.hashCode()

    internal fun replaceInParent(newExpr: Expression) = parent.replaceNthChild(index, newExpr)
}

object New : Origin() {

    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(New)

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        sequenceOf(PathMapping(emptyList(), PathMappingType.Introduce, listOf(rootPath)))
}

object Build : Origin() {

    override fun computeChildOrigin(expression: Expression, index: Int) = expression.operands[index]

    override fun computeChildrenOrigin(expression: Expression) = expression.operands

    override fun computePathMappings(rootPath: Path, children: List<Expression>) = when {
        children.isEmpty() -> sequenceOf(PathMapping(emptyList(), PathMappingType.Introduce, listOf(rootPath)))
        else -> children.mapIndexed { i, child -> child.pathMappings(rootPath.child(i)) }.asSequence().flatten()
    }
}

object Unknown : Origin() {

    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Unknown)

    override fun computePathMappings(rootPath: Path, children: List<Expression>) = emptySequence<PathMapping>()
}

class Move(val from: Expression) : Origin() {

    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Move(from.nthChild(index)))

    override fun computePathMappings(rootPath: Path, children: List<Expression>): Sequence<PathMapping> {
        val fromPath = from.origin.path
        return if (fromPath == null) {
            sequenceOf(PathMapping(listOf(), PathMappingType.Introduce, listOf(rootPath)))
        } else {
            sequenceOf(PathMapping(listOf(fromPath), PathMappingType.Move, listOf(rootPath)))
        }
    }
}

class Combine(val from: List<Expression>) : Origin() {

    constructor(vararg fromExprs: Expression) : this(fromExprs.asList())

    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Unknown)

    override fun computePathMappings(rootPath: Path, children: List<Expression>) = sequenceOf(
        PathMapping(
            from.mapNotNull { it.origin.path },
            when (from.size) {
                0 -> PathMappingType.Introduce
                1 -> PathMappingType.Transform
                else -> PathMappingType.Combine
            },
            listOf(rootPath)
        )
    )
}

class Factor(val from: List<Expression>) : Origin() {

    constructor(vararg fromExprs: Expression) : this(fromExprs.asList())

    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Factor(from.map { it.nthChild(index) }))

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        sequenceOf(PathMapping(from.mapNotNull { it.origin.path }, PathMappingType.Factor, listOf(rootPath)))
}

class Distribute(val from: List<Expression>) : Origin() {

    constructor(fromExpr: Expression) : this(listOf(fromExpr))

    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Distribute(from.map { it.nthChild(index) }))

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        sequenceOf(PathMapping(from.mapNotNull { it.origin.path }, PathMappingType.Distribute, listOf(rootPath)))
}

class Cancel(val origin: Origin, val cancelParts: List<Expression>) : Origin(origin.path) {

    override fun computeChildOrigin(expression: Expression, index: Int): Expression {
        return origin.computeChildOrigin(expression, index)
    }

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        origin.computePathMappings(rootPath, children) +
            sequenceOf(PathMapping(cancelParts.mapNotNull { it.origin.path }, PathMappingType.Cancel, emptyList()))
}
