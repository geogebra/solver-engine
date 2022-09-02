package engine.expressionmakers

import engine.expressions.MappedExpression
import engine.expressions.PathMappingLeaf
import engine.expressions.PathMappingType
import engine.patterns.Match
import engine.patterns.PathProvider

data class UnaryPathMappingAnnotator(
    val pathMappingType: PathMappingType,
    val pattern: PathProvider,
) : ExpressionMaker {
    override fun make(match: Match): MappedExpression {
        val paths = pattern.getBoundPaths(match)
        return MappedExpression(pattern.getBoundExpr(match)!!, PathMappingLeaf(paths, pathMappingType))
    }
}

data class BinaryPathMappingAnnotator(
    val pathMappingType: PathMappingType,
    val pattern: PathProvider,
    val expressionMaker: ExpressionMaker,
) : ExpressionMaker {
    override fun make(match: Match): MappedExpression {
        return expressionMaker.make(match)
    }
}

fun move(pattern: PathProvider): ExpressionMaker =
    UnaryPathMappingAnnotator(PathMappingType.Move, pattern)

fun factor(pattern: PathProvider): ExpressionMaker =
    UnaryPathMappingAnnotator(PathMappingType.Factor, pattern)
