package engine.patterns

import engine.expressions.Subexpression
import engine.operators.BinaryExpressionOperator
import engine.operators.MixedNumberOperator
import engine.operators.NaryOperator
import engine.operators.Operator
import engine.operators.UnaryExpressionOperator

/**
 * Produces a `Pattern` having a list of child patterns
 * `childPattern`'s connected by an operator `operator`.
 */
data class OperatorPattern(val operator: Operator, val childPatterns: List<Pattern>) : Pattern {
    init {
        require(childPatterns.size >= operator.minChildCount())
        require(childPatterns.size <= operator.maxChildCount())
    }

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!subexpression.expr.operator.equiv(operator) ||
            subexpression.expr.operands.size != childPatterns.size ||
            !checkPreviousMatch(subexpression.expr, match)
        ) {
            return emptySequence()
        }

        var matches = sequenceOf(match.newChild(this, subexpression))
        for ((index, op) in childPatterns.withIndex()) {
            matches = matches.flatMap { op.findMatches(subexpression.nthChild(index), it) }
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

fun rootOf(radicand: Pattern, degree: Pattern) =
    OperatorPattern(BinaryExpressionOperator.Root, listOf(radicand, degree))

fun mixedNumberOf(
    integer: UnsignedIntegerPattern = UnsignedIntegerPattern(),
    numerator: UnsignedIntegerPattern = UnsignedIntegerPattern(),
    denominator: UnsignedIntegerPattern = UnsignedIntegerPattern(),
) = OperatorPattern(MixedNumberOperator, listOf(integer, numerator, denominator))

fun sumOf(vararg terms: Pattern) = OperatorPattern(NaryOperator.Sum, terms.asList())

fun plusOf(operand: Pattern) = OperatorPattern(UnaryExpressionOperator.Plus, listOf(operand))
fun negOf(operand: Pattern) = OperatorPattern(UnaryExpressionOperator.Minus, listOf(operand))

fun productOf(vararg factors: Pattern) = OperatorPattern(NaryOperator.Product, factors.asList())
