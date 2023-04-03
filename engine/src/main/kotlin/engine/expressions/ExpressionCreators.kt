@file:Suppress("TooManyFunctions")

package engine.expressions

import engine.operators.AddEquationsOperator
import engine.operators.BinaryExpressionOperator
import engine.operators.DecimalOperator
import engine.operators.EquationOperator
import engine.operators.EquationSystemOperator
import engine.operators.EquationUnionOperator
import engine.operators.InequalityOperators
import engine.operators.IntegerOperator
import engine.operators.IntervalOperator
import engine.operators.MixedNumberOperator
import engine.operators.MultiVariateSolutionOperator
import engine.operators.NaryOperator
import engine.operators.Operator
import engine.operators.RecurringDecimalOperator
import engine.operators.SetOperators
import engine.operators.SolutionOperator
import engine.operators.SubtractEquationsOperator
import engine.operators.TupleOperator
import engine.operators.UnaryExpressionOperator
import engine.operators.VariableListOperator
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

fun sumOf(vararg operands: Expression) = sumOf(operands.asList())

fun sumOf(operands: List<Expression>): Expression = flattenedNaryExpression(
    NaryOperator.Sum,
    operands,
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
    if (operands.size == 1) operands[0] else flattenedNaryExpression(NaryOperator.Product, operands)

fun explicitProductOf(vararg operands: Expression) = explicitProductOf(operands.asList())

fun implicitProductOf(operands: List<Expression>) =
    if (operands.size == 1) operands[0] else buildExpression(NaryOperator.ImplicitProduct, operands)

fun implicitProductOf(vararg operands: Expression) = implicitProductOf(operands.asList())

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

fun equationSystemOf(vararg equations: Expression) =
    buildExpression(EquationSystemOperator, equations.asList())

fun equationUnionOf(vararg equations: Expression) =
    buildExpression(EquationUnionOperator, equations.asList())

fun equationUnionOf(equations: List<Expression>) =
    buildExpression(EquationUnionOperator, equations)

fun solutionSetOf(elements: List<Expression>) = buildExpression(SetOperators.FiniteSet, elements)
fun solutionSetOf(vararg elements: Expression) = buildExpression(SetOperators.FiniteSet, elements.asList())

fun cartesianProductOf(sets: List<Expression>) = buildExpression(SetOperators.CartesianProduct, sets)
fun cartesianProductOf(vararg sets: Expression) = cartesianProductOf(sets.asList())

fun solutionOf(variable: Expression, solutionSet: Expression) =
    buildExpression(SolutionOperator, listOf(variable, solutionSet))

fun tupleOf(variables: List<Expression>) = buildExpression(TupleOperator, variables)

fun tupleOf(vararg variables: Expression) = tupleOf(variables.asList())

fun variableListOf(variables: List<String>) = buildExpression(VariableListOperator, variables.map { xp(it) })
fun variableListOf(vararg variables: String) = variableListOf(variables.asList())

fun variableListOf(vararg variables: Expression) = buildExpression(VariableListOperator, variables.asList())

fun identityOf(variables: Expression, expr: Expression) =
    buildExpression(MultiVariateSolutionOperator.Identity, listOf(variables, expr))

fun contradictionOf(variables: Expression, expr: Expression) =
    buildExpression(MultiVariateSolutionOperator.Contradiction, listOf(variables, expr))

fun implicitSolutionOf(variables: Expression, expr: Expression) =
    buildExpression(MultiVariateSolutionOperator.ImplicitSolution, listOf(variables, expr))

fun setSolutionOf(variables: Expression, set: Expression) =
    buildExpression(MultiVariateSolutionOperator.SetSolution, listOf(variables, set))

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
