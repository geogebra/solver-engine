package engine.patterns

import engine.context.Context
import engine.expressions.Expression
import engine.operators.EquationOperator
import engine.operators.InequalityOperators

class SolvablePattern(
    val lhs: Pattern,
    val rhs: Pattern,
) : BasePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        if (subexpression.operator != EquationOperator && subexpression.operator !is InequalityOperators) {
            return emptySequence()
        }

        val matchedSolvable = match.newChild(this, subexpression)
        return lhs.findMatches(context, matchedSolvable, subexpression.firstChild).flatMap {
            rhs.findMatches(context, it, subexpression.secondChild)
        }
    }
}
