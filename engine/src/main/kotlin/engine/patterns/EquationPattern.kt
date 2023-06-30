package engine.patterns

import engine.context.Context
import engine.expressions.Expression
import engine.operators.EquationOperator
import engine.operators.EquationSystemOperator
import engine.operators.InequalitySystemOperator
import engine.operators.StatementUnionOperator
import engine.operators.StatementWithConstraintOperator

data class EquationPattern(
    val lhs: Pattern,
    val rhs: Pattern,
) : BasePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        if (subexpression.operator != EquationOperator) {
            return emptySequence()
        }

        val matchedEquation = match.newChild(this, subexpression)
        return sequence {
            lhs.findMatches(context, matchedEquation, subexpression.firstChild).forEach {
                yieldAll(rhs.findMatches(context, it, subexpression.secondChild))
            }

            lhs.findMatches(context, matchedEquation, subexpression.secondChild).forEach {
                yieldAll(rhs.findMatches(context, it, subexpression.firstChild))
            }
        }
    }
}

fun commutativeEquationOf(lhs: Pattern, rhs: Pattern) = EquationPattern(lhs, rhs)

fun equationSystemOf(eq1: Pattern, eq2: Pattern) = OperatorPattern(EquationSystemOperator, listOf(eq1, eq2))

fun inequalitySystemOf(eq1: Pattern, eq2: Pattern) = OperatorPattern(InequalitySystemOperator, listOf(eq1, eq2))

fun equationUnionOf(eq1: Pattern, eq2: Pattern) = OperatorPattern(StatementUnionOperator, listOf(eq1, eq2))

fun statementWithConstraintOf(statement: Pattern, constraint: Pattern) =
    OperatorPattern(StatementWithConstraintOperator, listOf(statement, constraint))
