@file:Suppress("TooManyFunctions")

package engine.expressions

import engine.operators.BinaryExpressionOperator
import engine.operators.DecimalOperator
import engine.operators.EquationOperator
import engine.operators.EquationSystemOperator
import engine.operators.IntegerOperator
import engine.operators.MixedNumberOperator
import engine.operators.NaryOperator
import engine.operators.Operator
import engine.operators.RecurringDecimalOperator
import engine.operators.UnaryExpressionOperator
import engine.operators.VariableOperator
import engine.utility.RecurringDecimal
import java.math.BigDecimal
import java.math.BigInteger

fun buildExpression(operator: Operator, operands: List<Expression>, decorators: List<Decorator> = emptyList()) =
    Expression(
        operator,
        operands.mapIndexed { index, operand -> operand.adjustBracketFor(operator, index) },
        decorators,
    )

fun xp(n: Int) = xp(n.toBigInteger())

fun xp(n: BigInteger): Expression {
    val posExpr = buildExpression(IntegerOperator(n.abs()), emptyList())
    return if (n.signum() >= 0) posExpr else negOf(posExpr)
}

fun xp(x: BigDecimal): Expression {
    val operator =
        if (x.scale() <= 0) IntegerOperator(x.abs().toBigInteger()) else DecimalOperator(x.abs())
    val posExpr = buildExpression(operator, emptyList())
    return if (x.signum() >= 0) posExpr else negOf(posExpr)
}

fun xp(x: RecurringDecimal): Expression {
    return buildExpression(RecurringDecimalOperator(x), emptyList())
}

fun xp(v: String) = buildExpression(VariableOperator(v), emptyList())

fun mixedNumber(integer: BigInteger, numerator: BigInteger, denominator: BigInteger) =
    buildExpression(MixedNumberOperator, listOf(xp(integer), xp(numerator), xp(denominator)))

fun mixedNumberOf(integer: Expression, numerator: Expression, denominator: Expression) =
    buildExpression(MixedNumberOperator, listOf(integer, numerator, denominator))

fun bracketOf(expr: Expression, decorator: Decorator? = null) = expr.decorate(decorator ?: Decorator.RoundBracket)
fun squareBracketOf(expr: Expression) = expr.decorate(Decorator.SquareBracket)
fun curlyBracketOf(expr: Expression) = expr.decorate(Decorator.CurlyBracket)
fun missingBracketOf(expr: Expression) = expr.decorate(Decorator.MissingBracket)

fun negOf(expr: Expression) = buildExpression(UnaryExpressionOperator.Minus, listOf(expr))
fun plusOf(expr: Expression) = buildExpression(UnaryExpressionOperator.Plus, listOf(expr))

fun fractionOf(numerator: Expression, denominator: Expression) =
    buildExpression(BinaryExpressionOperator.Fraction, listOf(numerator, denominator))

fun powerOf(base: Expression, exponent: Expression) =
    buildExpression(BinaryExpressionOperator.Power, listOf(base, exponent))

fun simplifiedPowerOf(base: Expression, exponent: Expression): Expression {
    return if (exponent == Constants.One) base
    else powerOf(base, exponent)
}

fun squareRootOf(radicand: Expression) = buildExpression(UnaryExpressionOperator.SquareRoot, listOf(radicand))

fun rawRootOf(radicand: Expression, order: Expression) =
    buildExpression(BinaryExpressionOperator.Root, listOf(radicand, order))

fun rootOf(radicand: Expression, order: Expression) =
    if (order == Constants.Two) squareRootOf(radicand) else rawRootOf(radicand, order)

fun sumOf(vararg operands: Expression) = sumOf(operands.asList())

fun sumOf(operands: List<Expression>): Expression = flattenedNaryExpression(
    NaryOperator.Sum, operands
)

fun getBaseOfPower(expr: Expression): Expression = when (expr.operator) {
    BinaryExpressionOperator.Power -> getBaseOfPower(expr.operands[0])
    else -> expr
}

private fun Expression.isNumbery(): Boolean = when (operator) {
    BinaryExpressionOperator.Power -> getBaseOfPower(this).isNumbery()
    BinaryExpressionOperator.Fraction -> true
    is IntegerOperator, is DecimalOperator, is RecurringDecimalOperator -> true
    UnaryExpressionOperator.Minus -> {
        val op = operands[0].operator
        op is IntegerOperator || op is DecimalOperator || op is RecurringDecimalOperator
    }
    else -> false
}

fun productSignRequired(left: Expression, right: Expression): Boolean = when {
    left.operator == UnaryExpressionOperator.DivideBy || right.operator == UnaryExpressionOperator.DivideBy -> true
    right.isNumbery() -> true
    left.hasBracket() || right.hasBracket() -> false
    else -> {
        val rightOp = getBaseOfPower(right).operator
        val leftOp = getBaseOfPower(left).operator

        val rightIsRootOrVariable = rightOp == UnaryExpressionOperator.SquareRoot ||
            rightOp == BinaryExpressionOperator.Root ||
            rightOp is VariableOperator
        val differentVariables = leftOp is VariableOperator && rightOp is VariableOperator &&
            leftOp.name != rightOp.name

        !(left.isNumbery() && rightIsRootOrVariable || differentVariables)
    }
}

fun productOf(operands: List<Expression>): Expression {
    if (operands.size == 1) {
        return operands[0]
    }
    val flattenedOperands = operands.flatMap { if (it.hasLabel()) listOf(it) else it.flattenedProductChildren() }

    var implicitFactors = mutableListOf<Expression>()
    val factors = mutableListOf<Expression>()
    for ((i, op) in flattenedOperands.withIndex()) {
        val op1 = op.adjustBracketFor(NaryOperator.ImplicitProduct, i)

        if (i > 0 && productSignRequired(implicitFactors.last(), op1)) {
            factors.add(implicitProductOf(implicitFactors))
            implicitFactors = mutableListOf()
        }
        implicitFactors.add(op1)
    }
    factors.add(implicitProductOf(implicitFactors))
    return explicitProductOf(factors)
}

fun explicitProductOf(operands: List<Expression>) =
    if (operands.size == 1) operands[0] else buildExpression(NaryOperator.Product, operands)

fun explicitProductOf(vararg operands: Expression) = explicitProductOf(operands.asList())

fun implicitProductOf(operands: List<Expression>) =
    if (operands.size == 1) operands[0] else buildExpression(NaryOperator.ImplicitProduct, operands)

fun implicitProductOf(vararg operands: Expression) = implicitProductOf(operands.asList())

fun productOf(vararg operands: Expression) = productOf(operands.asList())

fun simplifiedProductOf(vararg operands: Expression): Expression {
    val nonOneFactors = operands.filter { it != Constants.One }
    return when (nonOneFactors.size) {
        1 -> nonOneFactors[0]
        else -> productOf(nonOneFactors)
    }
}

fun divideBy(operand: Expression) = buildExpression(UnaryExpressionOperator.DivideBy, listOf(operand))

fun equationOf(lhs: Expression, rhs: Expression) = buildExpression(EquationOperator, listOf(lhs, rhs))

fun equationSystemOf(vararg equations: Expression) =
    buildExpression(EquationSystemOperator, equations.asList())

private fun flattenedNaryExpression(operator: NaryOperator, operands: List<Expression>): Expression {
    if (operands.size == 1) {
        return operands[0]
    }
    val ops = mutableListOf<Expression>()
    for (mappedExpr in operands) {
        if (mappedExpr.operator == operator) {
            ops.addAll(mappedExpr.children())
        } else {
            ops.add(mappedExpr)
        }
    }
    return buildExpression(operator, ops)
}
