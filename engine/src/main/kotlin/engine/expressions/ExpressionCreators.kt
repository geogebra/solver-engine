@file:Suppress("TooManyFunctions")

package engine.expressions

import engine.operators.AddEquationsOperator
import engine.operators.BinaryExpressionOperator
import engine.operators.Comparator
import engine.operators.ComparisonOperator
import engine.operators.DefaultProductOperator
import engine.operators.DefiniteIntegralOperator
import engine.operators.DerivativeOperator
import engine.operators.DoubleComparisonOperator
import engine.operators.EquationSystemOperator
import engine.operators.IndefiniteIntegralOperator
import engine.operators.InequalitySystemOperator
import engine.operators.IntervalOperator
import engine.operators.MatrixOperator
import engine.operators.Operator
import engine.operators.SetOperators
import engine.operators.SolutionOperator
import engine.operators.StatementUnionOperator
import engine.operators.SubtractEquationsOperator
import engine.operators.SumOperator
import engine.operators.TrigonometricFunctionOperator
import engine.operators.TupleOperator
import engine.operators.UnaryExpressionOperator
import engine.operators.VectorOperator
import engine.utility.RecurringDecimal
import java.math.BigDecimal
import java.math.BigInteger

internal fun buildExpression(operator: Operator, operands: List<Expression>) =
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

fun xp(v: String, subscript: String? = null) = Variable(v, subscript)

fun mixedNumber(integer: BigInteger, numerator: BigInteger, denominator: BigInteger) =
    MixedNumberExpression(IntegerExpression(integer), IntegerExpression(numerator), IntegerExpression(denominator))

fun mixedNumberOf(integer: IntegerExpression, numerator: IntegerExpression, denominator: IntegerExpression) =
    MixedNumberExpression(integer, numerator, denominator)

fun bracketOf(expr: Expression, decorator: Decorator? = null) = expr.decorate(decorator ?: Decorator.RoundBracket)
fun squareBracketOf(expr: Expression) = expr.decorate(Decorator.SquareBracket)
fun curlyBracketOf(expr: Expression) = expr.decorate(Decorator.CurlyBracket)
fun missingBracketOf(expr: Expression) = expr.decorate(Decorator.MissingBracket)

fun negOf(expr: Expression) = buildExpression(UnaryExpressionOperator.Minus, listOf(expr))

fun simplifiedNegOf(expr: Expression) = when (expr) {
    Constants.Zero -> expr
    is Minus -> expr.argument
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

fun naturalLogOf(argument: Expression) = buildExpression(UnaryExpressionOperator.NaturalLog, listOf(argument))
fun logBase10Of(argument: Expression) = buildExpression(UnaryExpressionOperator.LogBase10, listOf(argument))
fun logOf(base: Expression, argument: Expression) =
    buildExpression(BinaryExpressionOperator.Log, listOf(base, argument))

fun sinOf(argument: Expression) = buildExpression(TrigonometricFunctionOperator.Sin, listOf(argument))
fun arsinhOf(argument: Expression) = buildExpression(TrigonometricFunctionOperator.Arsinh, listOf(argument))

fun absoluteValueOf(argument: Expression) = buildExpression(UnaryExpressionOperator.AbsoluteValue, listOf(argument))

fun sumOf(vararg operands: Expression) = sumOf(operands.asList())

fun sumOf(operands: List<Expression>): Expression {
    return when (operands.size) {
        0 -> Constants.Zero
        1 -> operands[0]
        else -> {
            // We inline more products than sums -- something to look into in the future
            val flattenedOperands = operands
                .flatMap { if (!it.hasLabel() && !it.hasBracket() && it is Sum) it.children else listOf(it) }
                .mapIndexed { index, operand -> operand.adjustBracketFor(SumOperator, index) }
            Sum(flattenedOperands)
        }
    }
}

fun productOf(operands: List<Expression>): Expression {
    return when (operands.size) {
        0 -> Constants.One
        1 -> operands[0]
        else -> {
            val flattenedOperands = operands
                .flatMap { if (!it.hasLabel() && !it.isPartialProduct() && it is Product) it.children else listOf(it) }
                .mapIndexed { index, operand -> operand.adjustBracketFor(DefaultProductOperator, index) }
            Product(flattenedOperands)
        }
    }
}

fun explicitProductOf(operands: List<Expression>): Expression {
    return when (operands.size) {
        0 -> Constants.One
        1 -> operands[0]
        else -> {
            val flattenedOperands = operands
                .map { if (it is Product) it.decorate(Decorator.PartialBracket) else it }
                .mapIndexed { index, operand -> operand.adjustBracketFor(DefaultProductOperator, index) }
            val forcedSigns = (1 until flattenedOperands.size)
                .filter { !productSignRequired(flattenedOperands[it - 1], flattenedOperands[it]) }
            Product(flattenedOperands, forcedSigns)
        }
    }
}

fun explicitProductOf(vararg operands: Expression) = explicitProductOf(operands.asList())

fun productOf(vararg operands: Expression) = productOf(operands.asList())

/**
 * Move the negative sign of the first factor in front of the product and remove factors
 * equal to one
 */
fun simplifiedProductOf(firstFactor: Expression, secondFactor: Expression): Expression {
    if (secondFactor == Constants.One) return firstFactor

    return when (firstFactor) {
        Constants.One -> secondFactor
        Constants.MinusOne -> negOf(secondFactor)
        is Minus -> negOf(productOf(firstFactor.argument, secondFactor))
        else -> productOf(firstFactor, secondFactor)
    }
}

fun divideBy(operand: Expression) = buildExpression(UnaryExpressionOperator.DivideBy, listOf(operand))

fun percentageOf(operand: Expression) = buildExpression(UnaryExpressionOperator.Percentage, listOf(operand))

fun percentageOfOf(part: Expression, base: Expression) =
    buildExpression(BinaryExpressionOperator.PercentageOf, listOf(part, base))

fun derivativeOf(function: Expression, variable: Expression) =
    buildExpression(DerivativeOperator, listOf(Constants.One, function, variable))

fun derivativeOf(degree: Expression, function: Expression, vararg variables: Expression) =
    buildExpression(DerivativeOperator, listOf(degree, function) + variables)

fun indefiniteIntegralOf(function: Expression, variable: Expression) =
    buildExpression(IndefiniteIntegralOperator, listOf(function, variable))

fun definiteIntegralOf(lowerBound: Expression, upperBound: Expression, function: Expression, variable: Expression) =
    buildExpression(DefiniteIntegralOperator, listOf(lowerBound, upperBound, function, variable))

fun vectorOf(vararg elements: Expression) = buildExpression(VectorOperator, elements.asList())

fun matrixOf(matrixRows: List<List<Expression>>): Expression {
    val rows = matrixRows.size
    val cols = matrixRows[0].size
    assert(matrixRows.all { it.size == cols })

    return buildExpression(MatrixOperator(rows, cols), matrixRows.flatten())
}

fun matrixOf(vararg matrixRows: List<Expression>) = matrixOf(matrixRows.asList())

private fun buildComparison(lhs: Expression, comparator: Comparator, rhs: Expression) =
    buildExpression(ComparisonOperator(comparator), listOf(lhs, rhs))
fun equationOf(lhs: Expression, rhs: Expression) = buildComparison(lhs, Comparator.Equal, rhs)

fun addEquationsOf(eq1: Expression, eq2: Expression) =
    buildExpression(AddEquationsOperator, listOf(eq1, eq2))
fun subtractEquationsOf(eq1: Expression, eq2: Expression) =
    buildExpression(SubtractEquationsOperator, listOf(eq1, eq2))

fun lessThanOf(lhs: Expression, rhs: Expression) =
    buildComparison(lhs, Comparator.LessThan, rhs)

fun lessThanEqualOf(lhs: Expression, rhs: Expression) =
    buildComparison(lhs, Comparator.LessThanOrEqual, rhs)

fun greaterThanOf(lhs: Expression, rhs: Expression) =
    buildComparison(lhs, Comparator.GreaterThan, rhs)

fun greaterThanEqualOf(lhs: Expression, rhs: Expression) =
    buildComparison(lhs, Comparator.GreaterThanOrEqual, rhs)

fun inequationOf(lhs: Expression, rhs: Expression) =
    buildComparison(lhs, Comparator.NotEqual, rhs)

/**
 * compound inequality of the form: first < second < third
 */
fun openRangeOf(first: Expression, second: Expression, third: Expression) =
    buildExpression(
        DoubleComparisonOperator(Comparator.LessThan, Comparator.LessThan),
        listOf(first, second, third),
    )

/**
 * compound inequality of the form: first < second <= third
 */
fun openClosedRangeOf(first: Expression, second: Expression, third: Expression) =
    buildExpression(
        DoubleComparisonOperator(Comparator.LessThan, Comparator.LessThanOrEqual),
        listOf(first, second, third),
    )

/**
 * compound inequality of the form: first <= second < third
 */
fun closedOpenRangeOf(first: Expression, second: Expression, third: Expression) =
    buildExpression(
        DoubleComparisonOperator(Comparator.LessThanOrEqual, Comparator.LessThan),
        listOf(first, second, third),
    )

/**
 * compound inequality of the form: first <= second <= third
 */
fun closedRangeOf(first: Expression, second: Expression, third: Expression) =
    buildExpression(
        DoubleComparisonOperator(Comparator.LessThanOrEqual, Comparator.LessThanOrEqual),
        listOf(first, second, third),
    )

/**
 * compound inequality of the form: first > second > third
 */
fun reversedOpenRangeOf(first: Expression, second: Expression, third: Expression) =
    buildExpression(
        DoubleComparisonOperator(Comparator.GreaterThan, Comparator.GreaterThan),
        listOf(first, second, third),
    )

/**
 * compound inequality of the form: first > second >= third
 */
fun reversedOpenClosedRangeOf(first: Expression, second: Expression, third: Expression) =
    buildExpression(
        DoubleComparisonOperator(Comparator.GreaterThan, Comparator.GreaterThanOrEqual),
        listOf(first, second, third),
    )

/**
 * compound inequality of the form: first >= second > third
 */
fun reversedClosedOpenRangeOf(first: Expression, second: Expression, third: Expression) =
    buildExpression(
        DoubleComparisonOperator(Comparator.GreaterThanOrEqual, Comparator.GreaterThan),
        listOf(first, second, third),
    )

/**
 * compound inequality of the form: first >= second >= third
 */
fun reversedClosedRangeOf(first: Expression, second: Expression, third: Expression) =
    buildExpression(
        DoubleComparisonOperator(Comparator.GreaterThanOrEqual, Comparator.GreaterThanOrEqual),
        listOf(first, second, third),
    )

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

fun inequalitySystemOf(inequations: List<Expression>) = buildExpression(InequalitySystemOperator, inequations)

fun inequalitySystemOf(vararg inequations: Expression) = inequalitySystemOf(inequations.asList())

fun statementUnionOf(vararg equations: Expression) =
    buildExpression(StatementUnionOperator, equations.asList())

fun statementUnionOf(equations: List<Expression>) =
    buildExpression(StatementUnionOperator, equations)

fun finiteSetOf(elements: List<Expression>) = buildExpression(SetOperators.FiniteSet, elements)
fun finiteSetOf(vararg elements: Expression) = buildExpression(SetOperators.FiniteSet, elements.asList())

fun cartesianProductOf(sets: List<Expression>) = buildExpression(SetOperators.CartesianProduct, sets)
fun cartesianProductOf(vararg sets: Expression) = cartesianProductOf(sets.asList())

fun setUnionOf(sets: List<Expression>) = buildExpression(SetOperators.SetUnion, sets)
fun setUnionOf(vararg sets: Expression) = setUnionOf(sets.asList())

fun setDifferenceOf(left: Expression, right: Expression) =
    buildExpression(SetOperators.SetDifference, listOf(left, right))

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

fun nameXp(value: String) = Name(value)
