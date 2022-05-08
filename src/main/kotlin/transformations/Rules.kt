package transformations

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

    fun apply(sub: Subexpression): Step? {
        for (match in pattern.findMatches(RootMatch, sub)) {
            val endResultMaker = pattern.substitute(match, resultMaker)
            val (endResult, pathMappings) = endResultMaker.makeExpression(match, sub.path)
            return Step(sub.expr, endResult, pathMappings)
        }

        return null
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
