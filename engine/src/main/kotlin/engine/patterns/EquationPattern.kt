package engine.patterns

import engine.context.Context
import engine.expressions.Equation
import engine.expressions.Expression
import engine.operators.ExpressionWithConstraintOperator
import engine.operators.StatementSystemOperator
import engine.operators.StatementUnionOperator

data class EquationPattern(
    val lhs: Pattern,
    val rhs: Pattern,
) : BasePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        if (subexpression !is Equation) {
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

fun statementSystemOf(eq1: Pattern, eq2: Pattern) = OperatorPattern(StatementSystemOperator, listOf(eq1, eq2))

fun statementUnionOf(eq1: Pattern, eq2: Pattern) = OperatorPattern(StatementUnionOperator, listOf(eq1, eq2))

fun expressionWithConstraintOf(statement: Pattern, constraint: Pattern) =
    OperatorPattern(ExpressionWithConstraintOperator, listOf(statement, constraint))
