package engine.patterns

import engine.context.Context
import engine.expressions.Subexpression
import engine.operators.EquationOperator
import engine.operators.EquationSystemOperator

data class EquationPattern(
    val lhs: Pattern,
    val rhs: Pattern,
) : Pattern {
    override fun findMatches(context: Context, match: Match, subexpression: Subexpression): Sequence<Match> {
        if (!subexpression.expr.operator.equiv(EquationOperator) ||
            !checkPreviousMatch(subexpression.expr, match)
        ) {
            return emptySequence()
        }

        val matchedEquation = match.newChild(this, subexpression)
        return sequence {
            lhs.findMatches(context, matchedEquation, subexpression.nthChild(0)).forEach {
                yieldAll(rhs.findMatches(context, it, subexpression.nthChild(1)))
            }

            lhs.findMatches(context, matchedEquation, subexpression.nthChild(1)).forEach {
                yieldAll(rhs.findMatches(context, it, subexpression.nthChild(0)))
            }
        }
    }
}

fun equationOf(lhs: Pattern, rhs: Pattern) = EquationPattern(lhs, rhs)

fun equationSystemOf(eq1: Pattern, eq2: Pattern) = OperatorPattern(EquationSystemOperator, listOf(eq1, eq2))
