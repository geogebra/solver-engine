package expressions

data class Subexpression(val path: Path, val expr: Expression) {
    fun nthChild(index: Int): Subexpression {
        return Subexpression(path.child(index), expr.operands.elementAt(index))
    }

    fun children(): List<Subexpression> {
        return expr.operands.mapIndexed { i, child -> Subexpression(path.child(i), child) }
    }

    fun index() = when (path) {
        is ChildPath -> path.index
        else -> 0
    }

    fun substitute(subPath: Path, mappedExpr: MappedExpression): MappedExpression =
        substitute(subPath, mappedExpr) { true }

    private fun substitute(
        subPath: Path,
        mappedExpr: MappedExpression,
        childAllowed: (Operator) -> Boolean
    ): MappedExpression {
        return when {
            path == subPath -> mappedExpr.wrapInBracketsUnless(childAllowed)
            !subPath.hasAncestor(path) -> toMappedExpr()
            else -> expr.copyWithMappedChildren(
                children().mapIndexed { index, child ->
                    child.substitute(
                        subPath,
                        mappedExpr,
                    ) { childOp -> expr.operator.nthChildAllowed(index, childOp) }
                })
        }
    }
}