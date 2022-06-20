package engine.expressionmakers

import engine.expressions.MappedExpression
import engine.expressions.PathMappingLeaf
import engine.expressions.PathMappingType
import engine.patterns.Match
import engine.patterns.PathProvider

private data class UnaryPathMappingAnnotator(
    val pathMappingType: PathMappingType,
    val pattern: PathProvider,
) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        val paths = pattern.getBoundPaths(match)
        return MappedExpression(pattern.getBoundExpr(match)!!, PathMappingLeaf(paths, pathMappingType))
    }
}

private data class BinaryPathMappingAnnotator(
    val pathMappingType: PathMappingType,
    val pattern: PathProvider,
    val expressionMaker: ExpressionMaker,
) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        return expressionMaker.makeMappedExpression(match)
    }
}

fun move(pattern: PathProvider): ExpressionMaker =
    UnaryPathMappingAnnotator(PathMappingType.Move, pattern)

fun factor(pattern: PathProvider): ExpressionMaker =
    UnaryPathMappingAnnotator(PathMappingType.Factor, pattern)

fun transform(pattern: PathProvider): ExpressionMaker =
    UnaryPathMappingAnnotator(PathMappingType.Transform, pattern)

fun transform(pattern: PathProvider, toExpression: ExpressionMaker): ExpressionMaker =
    BinaryPathMappingAnnotator(PathMappingType.Transform, pattern, toExpression)

fun cancel(pattern: PathProvider, inExpression: ExpressionMaker): ExpressionMaker =
    BinaryPathMappingAnnotator(PathMappingType.Cancel, pattern, inExpression)
