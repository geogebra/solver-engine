package expressionmakers

import expressions.Expression
import expressions.Path
import patterns.Match
import patterns.Pattern
import steps.PathMapping
import steps.PathMappingType
import steps.TypePathMapper
import steps.VanishingPathMapper

data class PathMappingAnnotator(val pathMappingType: PathMappingType, val pattern: Pattern) : ExpressionMaker {
    override fun makeExpressionAcc(match: Match, currentPath: Path, acc: MutableList<PathMapping>): Expression {
        val paths = match.getBoundPaths(pattern)
        TypePathMapper(paths, pathMappingType).accPathMappings(currentPath, acc)
        return match.getBoundExpr(pattern)!!
    }

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
    override fun makeExpressionAcc(match: Match, currentPath: Path, acc: MutableList<PathMapping>): Expression {
        val paths = match.getBoundPaths(pattern)
        VanishingPathMapper(paths, pathMappingType).accPathMappings(currentPath, acc)
        return inExpression.makeExpressionAcc(match, currentPath, acc)
    }

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