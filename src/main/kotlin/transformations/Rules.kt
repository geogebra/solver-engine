package transformations

import expressions.Expression
import expressions.RootPath
import expressions.Subexpression
import patterns.ExpressionMaker
import patterns.Match
import patterns.Pattern
import patterns.RootMatch
import steps.Skill
import steps.Step

interface Rule {
    val pattern: Pattern
    val resultMaker: ExpressionMaker

    fun apply(expr: Expression): Step? {
        for (match in pattern.findMatches(RootMatch, Subexpression(RootPath, expr))) {
            val result = resultMaker.makeExpression(match);
            val (endResult, pathMappings) = pattern.substitute(match, result).extractPathMappings()
            return Step(expr, endResult, pathMappings)
        }

        return null;
    }

    //    fun getTransformation(match: Match): Transformation
    fun getSkills(match: Match): Sequence<Skill> = emptySequence()
//    fun getPathMappings(match: Match): List<PathMapping>
}

/*
object groupSumLiterals : Rule {
    override fun apply(expr: Expression): Expression? {
        return when (expr) {
            is SumExpr -> expr
            else -> null
        }
    }
}
 */
