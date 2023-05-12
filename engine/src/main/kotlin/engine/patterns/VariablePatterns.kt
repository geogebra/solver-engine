package engine.patterns

import engine.context.Context
import engine.expressions.Expression
import engine.expressions.Variable

abstract class VariablePattern : BasePattern() {
    fun getBoundSymbol(m: Match): String {
        return when (val expression = m.getBoundExpr(this)) {
            is Variable -> expression.variableName
            else -> throw InvalidMatch("Variable pattern matched to $expression")
        }
    }
}

/**
 * A pattern to match with any variable (i.e. symbol)
 */
class ArbitraryVariablePattern : VariablePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return when (subexpression) {
            is Variable -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

class SolutionVariablePattern : VariablePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return if (subexpression is Variable && subexpression.variableName in context.solutionVariables) {
            sequenceOf(match.newChild(this, subexpression))
        } else {
            emptySequence()
        }
    }
}
