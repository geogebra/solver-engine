@file:Suppress("TooManyFunctions")

package engine.expressions

import engine.operators.AddEquationsOperator
import engine.operators.BinaryExpressionOperator
import engine.operators.DefaultProductOperator
import engine.operators.EquationOperator
import engine.operators.EquationSystemOperator
import engine.operators.EquationUnionOperator
import engine.operators.InequalityOperators
import engine.operators.IntervalOperator
import engine.operators.NameOperator
import engine.operators.NaryOperator
import engine.operators.Operator
import engine.operators.SetOperators
import engine.operators.SolutionOperator
import engine.operators.SubtractEquationsOperator
import engine.operators.SumOperator
import engine.operators.TupleOperator
import engine.operators.UnaryExpressionOperator
import engine.utility.RecurringDecimal
import java.math.BigDecimal
import java.math.BigInteger

fun buildExpression(operator: Operator, operands: List<Expression>) =
    expressionOf(
        operator,
        operands.mapIndexed { index, operand -> operand.adjustBracketFor(operator, index) },
    )

fun xp(n: Int) = xp(n.toBigInteger())

fun xp(n: BigInteger): Expression {
    val posExpr = IntegerExpression(n.abs())
    return if (n.signum() >= 0) posExpr else negOf(posExpr)
}

fun xp(x: Double) = xp(x.toBigDecimal())

fun xp(x: BigDecimal): Expression {
    if (x.scale() <= 0) {
        return xp(x.toBigInteger())
    }
    val posExpr = DecimalExpression(x.abs())
    return if (x.signum() >= 0) posExpr else negOf(posExpr)
}

fun xp(x: RecurringDecimal): Expression {
    return RecurringDecimalExpression(x)
}

fun xp(v: String) = Variable(v)

fun mixedNumber(integer: BigInteger, numerator: BigInteger, denominator: BigInteger) =
    MixedNumberExpression(IntegerExpression(integer), IntegerExpression(numerator), IntegerExpression(denominator))

fun mixedNumberOf(integer: IntegerExpression, numerator: IntegerExpression, denominator: IntegerExpression) =
    MixedNumberExpression(integer, numerator, denominator)

fun bracketOf(expr: Expression, decorator: Decorator? = null) = expr.decorate(decorator ?: Decorator.RoundBracket)
fun squareBracketOf(expr: Expression) = expr.decorate(Decorator.SquareBracket)
fun curlyBracketOf(expr: Expression) = expr.decorate(Decorator.CurlyBracket)
fun missingBracketOf(expr: Expression) = expr.decorate(Decorator.MissingBracket)

fun negOf(expr: Expression) = buildExpression(UnaryExpressionOperator.Minus, listOf(expr))

fun simplifiedNegOf(expr: Expression) = when (expr.operator) {
    UnaryExpressionOperator.Minus -> expr.firstChild
    else -> negOf(expr)
}

fun plusOf(expr: Expression) = buildExpression(UnaryExpressionOperator.Plus, listOf(expr))

fun plusMinusOf(expr: Expression) = buildExpression(UnaryExpressionOperator.PlusMinus, listOf(expr))

fun fractionOf(numerator: Expression, denominator: Expression) =
    buildExpression(BinaryExpressionOperator.Fraction, listOf(numerator, denominator))

fun simplifiedFractionOf(numerator: Expression, denominator: Expression) = when {
    denominator == Constants.One -> numerator
    else -> fractionOf(numerator, denominator)
}

fun powerOf(base: Expression, exponent: Expression) =
    buildExpression(BinaryExpressionOperator.Power, listOf(base, exponent))

fun simplifiedPowerOf(base: Expression, exponent: Expression): Expression {
    return if (exponent == Constants.One) {
        base
    } else {
        powerOf(base, exponent)
    }
}

fun squareRootOf(radicand: Expression) = buildExpression(UnaryExpressionOperator.SquareRoot, listOf(radicand))

fun rawRootOf(radicand: Expression, order: Expression) =
    buildExpression(BinaryExpressionOperator.Root, listOf(radicand, order))

fun rootOf(radicand: Expression, order: Expression) =
    if (order == Constants.Two) squareRootOf(radicand) else rawRootOf(radicand, order)

fun absoluteValueOf(argument: Expression) = buildExpression(UnaryExpressionOperator.AbsoluteValue, listOf(argument))

fun sumOf(vararg operands: Expression) = sumOf(operands.asList())

fun sumOf(operands: List<Expression>): Expression = flattenedNaryExpression(
    SumOperator,
    operands,
)

fun productOf(operands: List<Expression>): Expression {
    if (operands.size == 1) {
        return operands[0]
    }

    val flattenedOperands = operands
        .flatMap { if (!it.hasLabel() && !it.isPartialProduct() && it is Product) it.children else listOf(it) }
        .mapIndexed { index, operand -> operand.adjustBracketFor(DefaultProductOperator, index) }
    return Product(flattenedOperands)
}

fun explicitProductOf(operands: List<Expression>): Expression {
    if (operands.size == 1) {
        return operands[0]
    }
    val flattenedOperands = operands
        .map { if (it is Product) it.decorate(Decorator.PartialBracket) else it }
        .mapIndexed { index, operand -> operand.adjustBracketFor(DefaultProductOperator, index) }
    val forcedSigns = (1 until flattenedOperands.size)
        .filter { !productSignRequired(flattenedOperands[it - 1], flattenedOperands[it]) }
    return Product(flattenedOperands, forcedSigns)
}

fun explicitProductOf(vararg operands: Expression) = explicitProductOf(operands.asList())

fun productOf(vararg operands: Expression) = productOf(operands.asList())

fun simplifiedProductOf(vararg operands: Expression): Expression {
    val nonOneFactors = operands.filter { it != Constants.One }
    return when (nonOneFactors.size) {
        0 -> operands[0]
        1 -> nonOneFactors[0]
        else -> productOf(nonOneFactors)
    }
}

fun divideBy(operand: Expression) = buildExpression(UnaryExpressionOperator.DivideBy, listOf(operand))

fun equationOf(lhs: Expression, rhs: Expression) = buildExpression(EquationOperator, listOf(lhs, rhs))

fun addEquationsOf(eq1: Expression, eq2: Expression) =
    buildExpression(AddEquationsOperator, listOf(eq1, eq2))
fun subtractEquationsOf(eq1: Expression, eq2: Expression) =
    buildExpression(SubtractEquationsOperator, listOf(eq1, eq2))

fun lessThanOf(lhs: Expression, rhs: Expression) =
    buildExpression(InequalityOperators.LessThan, listOf(lhs, rhs))

fun lessThanEqualOf(lhs: Expression, rhs: Expression) =
    buildExpression(InequalityOperators.LessThanEqual, listOf(lhs, rhs))

fun greaterThanOf(lhs: Expression, rhs: Expression) =
    buildExpression(InequalityOperators.GreaterThan, listOf(lhs, rhs))

fun greaterThanEqualOf(lhs: Expression, rhs: Expression) =
    buildExpression(InequalityOperators.GreaterThanEqual, listOf(lhs, rhs))

fun openIntervalOf(lhs: Expression, rhs: Expression) =
    buildExpression(IntervalOperator(closedLeft = false, closedRight = false), listOf(lhs, rhs))

fun openClosedIntervalOf(lhs: Expression, rhs: Expression) =
    buildExpression(IntervalOperator(closedLeft = false, closedRight = true), listOf(lhs, rhs))

fun closedOpenIntervalOf(lhs: Expression, rhs: Expression) =
    buildExpression(IntervalOperator(closedLeft = true, closedRight = false), listOf(lhs, rhs))

fun closedIntervalOf(lhs: Expression, rhs: Expression) =
    buildExpression(IntervalOperator(closedLeft = true, closedRight = true), listOf(lhs, rhs))

fun equationSystemOf(equations: List<Expression>) = buildExpression(EquationSystemOperator, equations)
fun equationSystemOf(vararg equations: Expression) = equationSystemOf(equations.asList())

fun equationUnionOf(vararg equations: Expression) =
    buildExpression(EquationUnionOperator, equations.asList())

fun equationUnionOf(equations: List<Expression>) =
    buildExpression(EquationUnionOperator, equations)

fun solutionSetOf(elements: List<Expression>) = buildExpression(SetOperators.FiniteSet, elements)
fun solutionSetOf(vararg elements: Expression) = buildExpression(SetOperators.FiniteSet, elements.asList())

fun cartesianProductOf(sets: List<Expression>) = buildExpression(SetOperators.CartesianProduct, sets)
fun cartesianProductOf(vararg sets: Expression) = cartesianProductOf(sets.asList())

fun tupleOf(variables: List<Expression>) = buildExpression(TupleOperator, variables)

fun tupleOf(vararg variables: Expression) = tupleOf(variables.asList())

fun variableListOf(variables: List<String>) = VariableList(variables.map { xp(it) })
fun variableListOf(vararg variables: String) = variableListOf(variables.asList())
fun variableListOf(vararg variables: Variable) = VariableList(variables.asList())

fun identityOf(variables: VariableList, expr: Expression) =
    buildExpression(SolutionOperator.Identity, listOf(variables, expr))

fun contradictionOf(variables: VariableList, expr: Expression) =
    buildExpression(SolutionOperator.Contradiction, listOf(variables, expr))

fun implicitSolutionOf(variables: VariableList, expr: Expression) =
    buildExpression(SolutionOperator.ImplicitSolution, listOf(variables, expr))

fun setSolutionOf(variables: VariableList, set: Expression) =
    buildExpression(SolutionOperator.SetSolution, listOf(variables, set))

private fun flattenedNaryExpression(operator: NaryOperator, operands: List<Expression>): Expression {
    if (operands.size == 1) {
        return operands[0]
    }
    val ops = mutableListOf<Expression>()
    for (mappedExpr in operands) {
        if (!mappedExpr.hasBracket() && mappedExpr.operator == operator) {
            ops.addAll(mappedExpr.children)
        } else {
            ops.add(mappedExpr)
        }
    }
    return buildExpression(operator, ops)
}

fun nameXp(value: String) = buildExpression(NameOperator(value), emptyList())
