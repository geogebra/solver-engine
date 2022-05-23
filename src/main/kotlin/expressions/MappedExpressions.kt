package expressions


interface PathMappingSet {
    fun childList(size: Int): List<PathMappingSet>

    fun pathMappings(root: Path): Sequence<PathMapping>
}

data class PathMappingLeaf(val paths: List<Path>, val type: PathMappingType) : PathMappingSet {

    override fun childList(size: Int) =
        (0 until size).map { i ->
            PathMappingLeaf(
                paths.map { it.child(i) },
                type
            )
        }

    override fun pathMappings(root: Path) = sequenceOf(PathMapping(paths, type, root))
}

data class PathMappingParent(val children: List<PathMappingSet>) : PathMappingSet {

    override fun childList(size: Int): List<PathMappingSet> {
        return children
    }

    override fun pathMappings(root: Path): Sequence<PathMapping> {
        return children.mapIndexed { i, child -> child.pathMappings(root.child(i)) }.asSequence().flatten()
    }
}

data class MappedExpression(val expr: Expression, val mappings: PathMappingSet) {
    fun wrapInBracketsUnless(cond: (Operator) -> Boolean): MappedExpression = when {
        cond(expr.operator) -> this
        else -> MappedExpression(bracketOf(expr), PathMappingParent(listOf(mappings)))
    }
}

fun Expression.copyWithMappedChildren(mappedChildren: List<MappedExpression>): MappedExpression {
    return MappedExpression(
        copyWithChildren(mappedChildren.map { it.expr }),
        PathMappingParent(mappedChildren.map { it.mappings }),
    )
}

fun Subexpression.toMappedExpr(): MappedExpression {
    return MappedExpression(expr, PathMappingLeaf(listOf(path), PathMappingType.Move))
}

fun unaryMappedExpression(operator: UnaryOperator, operand: MappedExpression): MappedExpression {
    val wrappedOperand = operand.wrapInBracketsUnless(operator::childAllowed)
    return MappedExpression(
        UnaryExpr(operator, wrappedOperand.expr),
        PathMappingParent(listOf(wrappedOperand.mappings))
    )
}

fun binaryMappedExpression(
    operator: BinaryOperator,
    left: MappedExpression,
    right: MappedExpression
): MappedExpression {
    val wrappedLeft = left.wrapInBracketsUnless(operator::leftChildAllowed)
    val wrappedRight = right.wrapInBracketsUnless(operator::rightChildAllowed)
    return MappedExpression(
        BinaryExpr(operator, wrappedLeft.expr, wrappedRight.expr),
        PathMappingParent(listOf(wrappedLeft.mappings, wrappedRight.mappings))
    )
}

fun naryMappedExpression(operator: NaryOperator, operands: List<MappedExpression>): MappedExpression {
    val ops = mutableListOf<Expression>()
    val mappingSets = mutableListOf<PathMappingSet>()
    for (mappedExpr in operands) {
        if (mappedExpr.expr is NaryExpr && mappedExpr.expr.operator == operator) {
            ops.addAll(mappedExpr.expr.operands)
            mappingSets.addAll(mappedExpr.mappings.childList(mappedExpr.expr.operands.size))
        } else {
            val wrappedExpr = mappedExpr
                .wrapInBracketsUnless { operator.nthChildAllowed(ops.size, mappedExpr.expr.operator) }
            ops.add(wrappedExpr.expr)
            mappingSets.add(wrappedExpr.mappings)
        }
    }
    return MappedExpression(
        NaryExpr(operator, ops),
        PathMappingParent(mappingSets)
    )
}

fun mixedNumberMappedExpression(
    integer: MappedExpression,
    numerator: MappedExpression,
    denominator: MappedExpression
) =
    MappedExpression(
        MixedNumber(
            integer.expr as IntegerExpr,
            numerator.expr as IntegerExpr,
            denominator.expr as IntegerExpr,
        ),
        PathMappingParent(
            listOf(
                integer.mappings,
                numerator.mappings,
                denominator.mappings,
            )
        )
    )
