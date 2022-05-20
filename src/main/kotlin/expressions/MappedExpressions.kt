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

data class MappedExpression(val expr: Expression, val mappings: PathMappingSet)

fun Expression.copyWithMappedChildren(mappedChildren: List<MappedExpression>): MappedExpression {
    if (this is NaryExpr) {
        return bracketedNaryMappedExpression(operator, mappedChildren)
    }
    return MappedExpression(
        copyWithChildren(mappedChildren.map { it.expr }),
        PathMappingParent(mappedChildren.map { it.mappings })
    )
}

fun Subexpression.toMappedExpr(): MappedExpression {
    return MappedExpression(expr, PathMappingLeaf(listOf(path), PathMappingType.Move))
}

fun binaryMappedExpression(
    operator: BinaryOperator,
    left: MappedExpression,
    right: MappedExpression
): MappedExpression {
    return MappedExpression(
        BinaryExpr(operator, left.expr, right.expr),
        PathMappingParent(listOf(left.mappings, right.mappings))
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
            ops.add(mappedExpr.expr)
            mappingSets.add(mappedExpr.mappings)
        }
    }
    return MappedExpression(
        NaryExpr(operator, ops),
        PathMappingParent(mappingSets)
    )
}

fun bracketedNaryMappedExpression(operator: NaryOperator, operands: List<MappedExpression>): MappedExpression {
    val ops = mutableListOf<Expression>()
    val mappingSets = mutableListOf<PathMappingSet>()
    for (mappedExpr in operands) {
        if (mappedExpr.expr is NaryExpr && mappedExpr.expr.operator == operator) {
            ops.add(bracketOf(mappedExpr.expr))
        } else {
            ops.add(mappedExpr.expr)
        }
        mappingSets.add(mappedExpr.mappings)
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
