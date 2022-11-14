package engine.expressions

abstract class Origin(val path: Path? = null) {

    abstract fun computeChildOrigin(expression: Expression, index: Int): Expression

    open fun computeChildrenOrigin(expression: Expression): List<Expression> =
        List(expression.operands.size) { i -> this.computeChildOrigin(expression, i) }

    abstract fun computePathMappings(rootPath: Path, children: List<Expression>): Sequence<PathMapping>
}

class Root(private val rootPath: Path = RootPath) : Origin(rootPath) {
    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Child(expression, index))

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        sequenceOf(PathMapping(listOf(this.rootPath), PathMappingType.Move, listOf(rootPath)))
}

class Child(val parent: Expression, val index: Int) : Origin(parent.origin.path?.child(index)) {

    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Child(expression, index))

    override fun computePathMappings(rootPath: Path, children: List<Expression>): Sequence<PathMapping> {
        return parent.pathMappings(rootPath)
            .map { m -> PathMapping(m.fromPaths.map { it.child(index) }, m.type, m.toPaths) }
    }
}

class Move(private val from: Path) : Origin(from) {
    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Move(from.child(index)))

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        sequenceOf(PathMapping(listOf(from), PathMappingType.Move, listOf(rootPath)))
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

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        children.mapIndexed { i, child -> child.pathMappings(rootPath.child(i)) }.asSequence().flatten()
}

object Unknown : Origin() {

    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Unknown)

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        sequenceOf(PathMapping(emptyList(), PathMappingType.Introduce, listOf(rootPath)))
}

class Combine(val from: List<Path>) : Origin() {

    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Unknown)

    override fun computePathMappings(rootPath: Path, children: List<Expression>) = sequenceOf(
        PathMapping(
            from,
            if (from.size == 1) PathMappingType.Transform else PathMappingType.Combine,
            listOf(rootPath)
        )
    )
}

class Factor(val from: List<Path>) : Origin() {

    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Factor(from.map { it.child(index) }))

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        sequenceOf(PathMapping(from, PathMappingType.Factor, listOf(rootPath)))
}

class Distribute(val from: List<Path>) : Origin() {

    override fun computeChildOrigin(expression: Expression, index: Int) =
        expression.operands[index].withOrigin(Distribute(from.map { it.child(index) }))

    override fun computePathMappings(rootPath: Path, children: List<Expression>) =
        sequenceOf(PathMapping(from, PathMappingType.Distribute, listOf(rootPath)))
}
