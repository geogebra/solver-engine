package expressionmakers

import expressions.MappedExpression
import expressions.PathMappingLeaf
import expressions.PathMappingType
import patterns.Match
import patterns.Pattern

data class PathMappingAnnotator(val pathMappingType: PathMappingType, val pattern: Pattern) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        val paths = match.getBoundPaths(pattern)
        return MappedExpression(match.getBoundExpr(pattern)!!, PathMappingLeaf(paths, pathMappingType))
    }
}

data class VanishingPathAnnotator(
    val pathMappingType: PathMappingType,
    val pattern: Pattern,
    val inExpression: ExpressionMaker
) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        // TODO
        return inExpression.makeMappedExpression(match)
    }
}

fun move(pattern: Pattern) = PathMappingAnnotator(PathMappingType.Move, pattern)
fun factor(pattern: Pattern) = PathMappingAnnotator(PathMappingType.Factor, pattern)
fun cancel(pattern: Pattern, inExpression: ExpressionMaker) =
    VanishingPathAnnotator(PathMappingType.Cancel, pattern, inExpression)

fun introduce(pattern: Pattern) = PathMappingAnnotator(PathMappingType.Introduce, pattern)