package engine.patterns

import engine.context.Context
import engine.expressions.Expression
import engine.operators.InequalityOperators

class InequalityPattern(
    val lhs: Pattern,
    val rhs: Pattern,
) : BasePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        if (subexpression.operator !is InequalityOperators) {
            return emptySequence()
        }

        val matchedSolvable = match.newChild(this, subexpression)
        return lhs.findMatches(context, matchedSolvable, subexpression.firstChild).flatMap {
            rhs.findMatches(context, it, subexpression.secondChild)
        }
    }
}

fun inequalityOf(lhs: Pattern, rhs: Pattern) = InequalityPattern(lhs, rhs)
