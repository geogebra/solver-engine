package engine.expressionmakers

import engine.expressions.MappedExpression
import engine.expressions.PathMappingLeaf
import engine.expressions.PathMappingType
import engine.patterns.Match
import engine.patterns.PathProvider

data class PathMappingAnnotator(val pathMappingType: PathMappingType, val pattern: PathProvider) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        val paths = pattern.getBoundPaths(match)
        return MappedExpression(pattern.getBoundExpr(match)!!, PathMappingLeaf(paths, pathMappingType))
    }
}

data class VanishingPathAnnotator(
    val pathMappingType: PathMappingType,
    val pattern: PathProvider,
    val inExpression: ExpressionMaker
) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        // TODO
        return inExpression.makeMappedExpression(match)
    }
}

fun move(pattern: PathProvider) = PathMappingAnnotator(PathMappingType.Move, pattern)

fun factor(pattern: PathProvider) = PathMappingAnnotator(PathMappingType.Factor, pattern)

fun transform(pattern: PathProvider) = PathMappingAnnotator(PathMappingType.Transform, pattern)

fun cancel(pattern: PathProvider, inExpression: ExpressionMaker) =
    VanishingPathAnnotator(PathMappingType.Cancel, pattern, inExpression)
