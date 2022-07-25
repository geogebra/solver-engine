package engine.patterns

import engine.expressions.BinaryOperator
import engine.expressions.BracketOperator
import engine.expressions.MixedNumberOperator
import engine.expressions.NaryOperator
import engine.expressions.Operator
import engine.expressions.Subexpression
import engine.expressions.UnaryOperator

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
    OperatorPattern(BinaryOperator.Fraction, listOf(numerator, denominator))

fun divideBy(divisor: Pattern) =
    OperatorPattern(UnaryOperator.DivideBy, listOf(divisor))

fun powerOf(base: Pattern, exponent: Pattern) =
    OperatorPattern(BinaryOperator.Power, listOf(base, exponent))

fun squareRootOf(radicand: Pattern) =
    OperatorPattern(UnaryOperator.SquareRoot, listOf(radicand))

fun rootOf(radicand: Pattern, degree: Pattern) =
    OperatorPattern(BinaryOperator.Root, listOf(radicand, degree))

fun bracketOf(expr: Pattern) = OperatorPattern(BracketOperator.Bracket, listOf(expr))

fun mixedNumberOf(
    integer: UnsignedIntegerPattern = UnsignedIntegerPattern(),
    numerator: UnsignedIntegerPattern = UnsignedIntegerPattern(),
    denominator: UnsignedIntegerPattern = UnsignedIntegerPattern(),
) = OperatorPattern(MixedNumberOperator, listOf(integer, numerator, denominator))

fun sumOf(vararg terms: Pattern) = OperatorPattern(NaryOperator.Sum, terms.asList())

fun negOf(operand: Pattern) = OperatorPattern(UnaryOperator.Minus, listOf(operand))

fun productOf(vararg factors: Pattern) = OperatorPattern(NaryOperator.Product, factors.asList())
