package transformations

import expressions.Expression
import expressions.SumExpr
import patterns.Match
import patterns.Pattern

interface Rule {
    val pattern: Pattern

    fun apply(match: Match): Expression?

    fun apply(expr: Expression): Expression? {
        for (match in pattern.findMatches(expr)) {
            val result = apply(match);
            if (result != null) {
                return pattern.substitute(match, result);
            }
        }

        return null;
    }
}

object groupSumLiterals : Rule {
    override fun apply(expr: Expression): Expression? {
        return when (expr) {
            is SumExpr -> expr
            else -> null
        }
    }
}
