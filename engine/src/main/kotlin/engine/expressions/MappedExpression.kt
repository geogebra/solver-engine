package engine.expressions

import engine.expressionmakers.ExpressionMaker
import engine.patterns.Match

data class MappedExpression(val expr: Expression, val mappings: PathMappingTree) : ExpressionMaker {
    override fun make(match: Match) = this

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
    if (operands.size == 1) {
        return operands[0]
    }
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

fun bracketOf(operand: MappedExpression) =
    mappedExpression(BracketOperator.Bracket, listOf(operand))

fun sumOf(vararg operands: MappedExpression) = sumOf(operands.asList())

fun sumOf(operands: List<MappedExpression>): MappedExpression = flattenedNaryMappedExpression(
    NaryOperator.Sum, operands
)

fun productOf(vararg operands: MappedExpression) = productOf(operands.asList())

fun productOf(operands: List<MappedExpression>): MappedExpression =
    flattenedNaryMappedExpression(NaryOperator.Product, operands)

fun simplifiedProductOf(vararg operands: MappedExpression): MappedExpression {
    val nonOneFactors = operands.filter { it.expr != Constants.One }
    return when (nonOneFactors.size) {
        1 -> nonOneFactors[0]
        else -> flattenedNaryMappedExpression(NaryOperator.Product, nonOneFactors)
    }
}

fun fractionOf(numerator: MappedExpression, denominator: MappedExpression) =
    mappedExpression(BinaryOperator.Fraction, listOf(numerator, denominator))

fun powerOf(base: MappedExpression, exponent: MappedExpression) =
    mappedExpression(BinaryOperator.Power, listOf(base, exponent))

fun simplifiedPowerOf(base: MappedExpression, exponent: MappedExpression): MappedExpression {
    return if (exponent.expr.equiv(Constants.One)) base
    else powerOf(base, exponent)
}

fun rootOf(radicand: MappedExpression, order: MappedExpression): MappedExpression {
    return if (order.expr.equiv(xp(2))) {
        squareRootOf(radicand)
    } else {
        mappedExpression(BinaryOperator.Root, listOf(radicand, order))
    }
}

fun squareRootOf(radicand: MappedExpression) =
    mappedExpression(UnaryOperator.SquareRoot, listOf(radicand))

fun negOf(operand: MappedExpression) = mappedExpression(UnaryOperator.Minus, listOf(operand))

fun divideBy(operand: MappedExpression) = mappedExpression(UnaryOperator.DivideBy, listOf(operand))

fun mixedNumberOf(integer: MappedExpression, numerator: MappedExpression, denominator: MappedExpression) =
    mappedExpression(MixedNumberOperator, listOf(integer, numerator, denominator))
