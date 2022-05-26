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

fun unaryMappedExpression(operator: UnaryOperator, operand: MappedExpression): MappedExpression {
    val wrappedOperand = operand.wrapInBracketsUnless(operator::childAllowed)
    return MappedExpression(
        Expression(operator, listOf(wrappedOperand.expr)),
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
        Expression(operator, listOf(wrappedLeft.expr, wrappedRight.expr)),
        PathMappingParent(listOf(wrappedLeft.mappings, wrappedRight.mappings))
    )
}

fun naryMappedExpression(operator: NaryOperator, operands: List<MappedExpression>): MappedExpression {
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

fun mixedNumberMappedExpression(
    integer: MappedExpression,
    numerator: MappedExpression,
    denominator: MappedExpression
) =
    MappedExpression(
        Expression(
            MixedNumberOperator(
                (integer.expr.operator as IntegerOperator).value,
                (numerator.expr.operator as IntegerOperator).value,
                (denominator.expr.operator as IntegerOperator).value,
            ),
            emptyList()
        ),
        combinePathMappingTrees(
            listOf(
                integer.mappings,
                numerator.mappings,
                denominator.mappings
            )
        )
    )
