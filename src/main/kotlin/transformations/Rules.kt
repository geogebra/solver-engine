package transformations

import expressionmakers.ExpressionMaker
import expressions.Subexpression
import patterns.Match
import plans.Plan
import steps.Transformation

interface Rule : Plan {
    val resultMaker: ExpressionMaker

    override fun execute(match: Match, sub: Subexpression): Transformation? {
        val (result, pathMappings) = resultMaker.makeExpression(match, sub.path)
        return Transformation(sub.path, sub.expr, result, pathMappings)
    }
}
