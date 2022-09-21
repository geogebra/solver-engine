package engine.expressions

import engine.expressionmakers.ExpressionMaker
import engine.operators.BinaryExpressionOperator
import engine.operators.BracketOperator
import engine.operators.EquationOperator
import engine.operators.EquationSystemOperator
import engine.operators.MixedNumberOperator
import engine.operators.NaryOperator
import engine.operators.Operator
import engine.operators.UnaryExpressionOperator
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
    mappedExpression(BinaryExpressionOperator.Fraction, listOf(numerator, denominator))

fun powerOf(base: MappedExpression, exponent: MappedExpression) =
    mappedExpression(BinaryExpressionOperator.Power, listOf(base, exponent))

fun simplifiedPowerOf(base: MappedExpression, exponent: MappedExpression): MappedExpression {
    return if (exponent.expr.equiv(Constants.One)) base
    else powerOf(base, exponent)
}

fun rootOf(radicand: MappedExpression, order: MappedExpression): MappedExpression {
    return if (order.expr.equiv(xp(2))) {
        squareRootOf(radicand)
    } else {
        mappedExpression(BinaryExpressionOperator.Root, listOf(radicand, order))
    }
}

fun squareRootOf(radicand: MappedExpression) =
    mappedExpression(UnaryExpressionOperator.SquareRoot, listOf(radicand))

fun negOf(operand: MappedExpression) = mappedExpression(UnaryExpressionOperator.Minus, listOf(operand))

fun divideBy(operand: MappedExpression) = mappedExpression(UnaryExpressionOperator.DivideBy, listOf(operand))

fun mixedNumberOf(integer: MappedExpression, numerator: MappedExpression, denominator: MappedExpression) =
    mappedExpression(MixedNumberOperator, listOf(integer, numerator, denominator))

fun equationOf(lhs: MappedExpression, rhs: MappedExpression) = mappedExpression(EquationOperator, listOf(lhs, rhs))

fun equationSystemOf(vararg equations: MappedExpression) = mappedExpression(EquationSystemOperator, equations.asList())
