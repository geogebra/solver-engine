package engine.patterns

import engine.expressions.Subexpression
import engine.operators.EquationOperator
import engine.operators.EquationSystemOperator

data class EquationPattern(
    val lhs: Pattern,
    val rhs: Pattern,
) : Pattern {
    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!subexpression.expr.operator.equiv(EquationOperator) ||
            !checkPreviousMatch(subexpression.expr, match)
        ) {
            return emptySequence()
        }

        val matchedEquation = match.newChild(this, subexpression)
        return sequence {
            lhs.findMatches(subexpression.nthChild(0), matchedEquation).forEach {
                yieldAll(rhs.findMatches(subexpression.nthChild(1), it))
            }

            lhs.findMatches(subexpression.nthChild(1), matchedEquation).forEach {
                yieldAll(rhs.findMatches(subexpression.nthChild(0), it))
            }
        }
    }
}

fun equationOf(lhs: Pattern, rhs: Pattern) = EquationPattern(lhs, rhs)

fun equationSystemOf(eq1: Pattern, eq2: Pattern) = OperatorPattern(EquationSystemOperator, listOf(eq1, eq2))
