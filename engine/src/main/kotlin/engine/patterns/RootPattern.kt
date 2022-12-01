package engine.patterns

import engine.context.Context
import engine.expressions.Constants
import engine.expressions.Expression
import engine.operators.BinaryExpressionOperator
import engine.operators.UnaryExpressionOperator

/**
 * A pattern that matches either `rootOf(radicand, order)` or `squareRootOf(radicand)` by matching
 * order to a newly introduced expression with a value of 2.
 * It allows treating both in a uniform way in rules.
 */
class RootPattern<T : Pattern>(val radicand: Pattern, val order: T) : BasePattern() {

    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return when (subexpression.operator) {
            BinaryExpressionOperator.Root -> {
                val m = match.newChild(this, subexpression)
                radicand.findMatches(context, m, subexpression.nthChild(0))
                    .flatMap { order.findMatches(context, it, subexpression.nthChild(1)) }
            }
            UnaryExpressionOperator.SquareRoot -> {
                val m = match.newChild(this, subexpression)
                radicand.findMatches(context, m, subexpression.nthChild(0))
                    .flatMap { order.findMatches(context, it, Constants.Two) }
            }
            else -> emptySequence()
        }
    }
}

fun rootOf(radicand: Pattern, index: Pattern = AnyPattern()) = RootPattern(radicand, index)
fun integerOrderRootOf(radicand: Pattern) = RootPattern(radicand, UnsignedIntegerPattern())
