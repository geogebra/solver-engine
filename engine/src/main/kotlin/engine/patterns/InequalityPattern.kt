package engine.patterns

import engine.context.Context
import engine.expressions.Expression
import engine.operators.InequalityOperators

class InequalityPattern(
    val lhs: Pattern,
    val rhs: Pattern,
    private val inequalityType: InequalityOperators? = null,
) : BasePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        if (subexpression.operator !is InequalityOperators) {
            return emptySequence()
        }

        if (inequalityType != null && (inequalityType != subexpression.operator)) {
            return emptySequence()
        }

        val matchedSolvable = match.newChild(this, subexpression)
        return lhs.findMatches(context, matchedSolvable, subexpression.firstChild).flatMap {
            rhs.findMatches(context, it, subexpression.secondChild)
        }
    }
}

fun inequalityOf(lhs: Pattern, rhs: Pattern) = InequalityPattern(lhs, rhs)

fun lessThanOf(lhs: Pattern, rhs: Pattern) = InequalityPattern(lhs, rhs, InequalityOperators.LessThan)

fun lessThanEqualOf(lhs: Pattern, rhs: Pattern) = InequalityPattern(lhs, rhs, InequalityOperators.LessThanEqual)

fun greaterThanOf(lhs: Pattern, rhs: Pattern) = InequalityPattern(lhs, rhs, InequalityOperators.GreaterThan)

fun greaterThanEqualOf(lhs: Pattern, rhs: Pattern) = InequalityPattern(lhs, rhs, InequalityOperators.GreaterThanEqual)

fun notEqualOf(lhs: Pattern, rhs: Pattern) = InequalityPattern(lhs, rhs, InequalityOperators.NotEqual)
