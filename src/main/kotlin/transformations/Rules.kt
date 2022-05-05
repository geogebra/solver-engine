package transformations

import expressions.Expression
import expressions.RootPath
import expressions.Subexpression
import patterns.Match
import patterns.Pattern
import patterns.RootMatch
import steps.Skill
import steps.Step

interface Rule {
    val pattern: Pattern

    fun apply(match: Match): Expression?

    fun apply(expr: Expression): Step? {
        for (match in pattern.findMatches(RootMatch, Subexpression(RootPath, expr))) {
            val result = apply(match);
            if (result != null) {
                val (endResult, pathMappings) = pattern.substitute(match, result).extractPathMappings()
                return Step(expr, endResult, pathMappings)
            }
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
