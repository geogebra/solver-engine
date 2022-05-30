package expressions

data class MappedExpression(val expr: Expression, val mappings: PathMappingTree) {
    fun wrapInBracketsUnless(cond: (Operator) -> Boolean): MappedExpression = when {
        cond(expr.operator) -> this
        else -> MappedExpression(bracketOf(expr), PathMappingParent(listOf(mappings)))
    }
}

fun Expression.copyWithMappedChildren(mappedChildren: List<MappedExpression>): MappedExpression {
    return MappedExpression(
        Expression(operator, mappedChildren.map { it.expr }),
        PathMappingParent(mappedChildren.map { it.mappings }),
    )
}

fun Subexpression.toMappedExpr(): MappedExpression {
    return MappedExpression(expr, PathMappingLeaf(listOf(path), PathMappingType.Move))
}

fun mappedExpression(operator: Operator, mappedOperands: List<MappedExpression>): MappedExpression {
    val wrappedOperands = mappedOperands.mapIndexed { i, operand ->
        operand.wrapInBracketsUnless {
            operator.nthChildAllowed(
                i,
                operand.expr.operator
            )
        }
    }
    return MappedExpression(
        Expression(operator, wrappedOperands.map { it.expr }),
        PathMappingParent(wrappedOperands.map { it.mappings })
    )
}

fun flattenedNaryMappedExpression(operator: NaryOperator, operands: List<MappedExpression>): MappedExpression {
    val ops = mutableListOf<Expression>()
    val mappingSets = mutableListOf<PathMappingTree>()
    for (mappedExpr in operands) {
        if (mappedExpr.expr.operator == operator) {
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
        Expression(operator, ops),
        PathMappingParent(mappingSets)
    )
}

