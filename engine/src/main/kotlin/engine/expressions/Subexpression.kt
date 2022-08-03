package engine.expressions

import engine.expressionmakers.ExpressionMaker
import engine.patterns.Match

class Subexpression private constructor(
    val expr: Expression,
    val parent: Subexpression?,
    val path: Path
) : ExpressionMaker {
    constructor(root: Expression) : this(root, null, RootPath)

    override fun make(match: Match) = toMappedExpr()

    fun toMappedExpr(): MappedExpression {
        return MappedExpression(expr, PathMappingLeaf(listOf(path), PathMappingType.Move))
    }

    fun nthChild(index: Int): Subexpression {
        return Subexpression(expr.operands.elementAt(index), this, path.child(index))
    }

    fun children(): List<Subexpression> {
        return expr.operands.mapIndexed { i, child -> Subexpression(child, this, path.child(i)) }
    }

    fun index() = when (path) {
        is ChildPath -> path.index
        else -> 0
    }

    fun substitute(subPath: Path, mappedExpr: MappedExpression): Pair<MappedExpression, Subexpression> {
        val substitution = substitute(subPath, mappedExpr) { true }
        val sub = Subexpression(substitution.expr, parent, path)
        return Pair(substitution, sub)
    }

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
                }
            )
        }
    }
}
