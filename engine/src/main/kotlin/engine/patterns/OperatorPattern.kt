package engine.patterns

import engine.context.Context
import engine.expressions.Expression
import engine.operators.BinaryExpressionOperator
import engine.operators.EquationOperator
import engine.operators.IntervalOperator
import engine.operators.MixedNumberOperator
import engine.operators.NaryOperator
import engine.operators.Operator
import engine.operators.SetOperators
import engine.operators.SolutionOperator
import engine.operators.UnaryExpressionOperator

/**
 * Produces a `Pattern` having a list of child patterns
 * `childPattern`'s connected by an operator `operator`.
 */
data class OperatorPattern(val operator: Operator, val childPatterns: List<Pattern>) : BasePattern() {
    init {
        require(childPatterns.size >= operator.minChildCount())
        require(childPatterns.size <= operator.maxChildCount())
    }

    override fun toString() = operator.readableString(childPatterns.map { it.toString() })

    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        if (!subexpression.operator.equiv(operator) || subexpression.operands.size != childPatterns.size) {
            return emptySequence()
        }

        var matches = sequenceOf(match.newChild(this, subexpression))
        for ((index, op) in childPatterns.withIndex()) {
            matches = matches.flatMap { op.findMatches(context, it, subexpression.nthChild(index)) }
        }
        return matches
    }
}

fun fractionOf(numerator: Pattern, denominator: Pattern) =
    OperatorPattern(BinaryExpressionOperator.Fraction, listOf(numerator, denominator))

fun divideBy(divisor: Pattern) =
    OperatorPattern(UnaryExpressionOperator.DivideBy, listOf(divisor))

fun powerOf(base: Pattern, exponent: Pattern) =
    OperatorPattern(BinaryExpressionOperator.Power, listOf(base, exponent))

fun squareRootOf(radicand: Pattern) =
    OperatorPattern(UnaryExpressionOperator.SquareRoot, listOf(radicand))

fun mixedNumberOf(
    integer: UnsignedIntegerPattern = UnsignedIntegerPattern(),
    numerator: UnsignedIntegerPattern = UnsignedIntegerPattern(),
    denominator: UnsignedIntegerPattern = UnsignedIntegerPattern(),
) = OperatorPattern(MixedNumberOperator, listOf(integer, numerator, denominator))

fun plusOf(operand: Pattern) = OperatorPattern(UnaryExpressionOperator.Plus, listOf(operand))
fun negOf(operand: Pattern) = OperatorPattern(UnaryExpressionOperator.Minus, listOf(operand))
fun plusMinusOf(operand: Pattern) = OperatorPattern(UnaryExpressionOperator.PlusMinus, listOf(operand))

fun explicitProductOf(vararg factors: Pattern) = OperatorPattern(NaryOperator.Product, factors.asList())
fun implicitProductOf(vararg factors: Pattern) = OperatorPattern(NaryOperator.ImplicitProduct, factors.asList())

fun equationOf(lhs: Pattern, rhs: Pattern) = OperatorPattern(EquationOperator, listOf(lhs, rhs))

fun solutionOf(variable: Pattern, solution: Pattern) = OperatorPattern(SolutionOperator, listOf(variable, solution))

fun solutionSetOf(vararg elements: Pattern) = OperatorPattern(SetOperators.FiniteSet, elements.asList())

fun openIntervalOf(lhs: Pattern, rhs: Pattern) =
    OperatorPattern(IntervalOperator(closedLeft = false, closedRight = false), listOf(lhs, rhs))
fun openClosedIntervalOf(lhs: Pattern, rhs: Pattern) =
    OperatorPattern(IntervalOperator(closedLeft = false, closedRight = true), listOf(lhs, rhs))
fun closedOpenIntervalOf(lhs: Pattern, rhs: Pattern) =
    OperatorPattern(IntervalOperator(closedLeft = true, closedRight = false), listOf(lhs, rhs))
fun closedIntervalOf(lhs: Pattern, rhs: Pattern) =
    OperatorPattern(IntervalOperator(closedLeft = true, closedRight = true), listOf(lhs, rhs))
