package expressions

fun Sum(vararg expression: Expression): Expression = NaryExpr(NaryOperator.Sum, expression.asList())

//
//fun Expression.getAt(path: Path?): Expression {
//    if (path == null) {
//        return this
//    }
//    return getAt(path.parent).children().elementAt(path.index)
//}

/*

fun Expression.replace(path: Path?, newExpression: Expression): Expression {
    if (path == null) {
        return newExpression
    }
    return copy
}

fun SumExpr.groupLiterals(): SumExpr {
    val literals = operands.filterIsInstance<Literal>()
    if (literals.isEmpty() || literals.size == operands.size) {
        return this
    }
    val nonLiterals = operands.filter { it !is Literal }
    return sumOf(SumExpr(nonLiterals), SumExpr(literals))
}

 */