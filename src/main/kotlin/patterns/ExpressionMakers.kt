package patterns

import expressions.*
import steps.PathMapping
import steps.PathMappingType
import steps.TypePathMapper
import steps.VanishingPathMapper

interface ExpressionMaker {
    fun makeExpression(match: Match, currentPath: Path): Pair<Expression, List<PathMapping>> {
        val pathMappingsAccumulator = mutableListOf<PathMapping>()
        val result = makeExpressionAcc(match, currentPath, pathMappingsAccumulator)
        return Pair(result, pathMappingsAccumulator)
    }

    fun makeExpressionAcc(match: Match, currentPath: Path, acc: MutableList<PathMapping>): Expression
}

data class PathMappingAnnotator(val pathMappingType: PathMappingType, val pattern: Pattern) : ExpressionMaker {
    override fun makeExpressionAcc(match: Match, currentPath: Path, acc: MutableList<PathMapping>): Expression {
        val paths = match.getPaths(pattern)
        TypePathMapper(paths, pathMappingType).accPathMappings(currentPath, acc)
        return match.getBinding(pattern)!!.expr
    }
}

data class VanishingPathAnnotator(
    val pathMappingType: PathMappingType,
    val pattern: Pattern,
    val inExpression: ExpressionMaker
) : ExpressionMaker {
    override fun makeExpressionAcc(match: Match, currentPath: Path, acc: MutableList<PathMapping>): Expression {
        val paths = match.getPaths(pattern)
        VanishingPathMapper(paths, pathMappingType).accPathMappings(currentPath, acc)
        return inExpression.makeExpressionAcc(match, currentPath, acc)
    }
}

data class UnaryExpressionMaker(val operator: UnaryOperator, val expr: ExpressionMaker) : ExpressionMaker {
    override fun makeExpressionAcc(
        match: Match,
        currentPath: Path,
        acc: MutableList<PathMapping>
    ): Expression {
        return UnaryExpr(operator, expr.makeExpressionAcc(match, currentPath.child(0), acc))
    }
}

data class BinaryExpressionMaker(val operator: BinaryOperator, val left: ExpressionMaker, val right: ExpressionMaker) :
    ExpressionMaker {
    override fun makeExpressionAcc(
        match: Match,
        currentPath: Path,
        acc: MutableList<PathMapping>
    ): Expression {
        return BinaryExpr(
            operator,
            left.makeExpressionAcc(match, currentPath.child(0), acc),
            right.makeExpressionAcc(match, currentPath.child(1), acc)
        )
    }
}

data class NaryExpressionMaker(val operator: NaryOperator, val operands: List<ExpressionMaker>) : ExpressionMaker {
    override fun makeExpressionAcc(
        match: Match,
        currentPath: Path,
        acc: MutableList<PathMapping>
    ): Expression {
        return NaryExpr(
            operator,
            operands.mapIndexed { index, operand -> operand.makeExpressionAcc(match, currentPath.child(index), acc) }
        )
    }
}

data class RestExpressionMaker(val pattern: AssocNaryPattern) : ExpressionMaker {

    override fun makeExpressionAcc(match: Match, currentPath: Path, acc: MutableList<PathMapping>): Expression {
        return pattern.getRest(match)
    }
}

fun move(pattern: Pattern) = PathMappingAnnotator(PathMappingType.Move, pattern)
fun factor(pattern: Pattern) = PathMappingAnnotator(PathMappingType.Factor, pattern)

fun cancel(pattern: Pattern, inExpression: ExpressionMaker) =
    VanishingPathAnnotator(PathMappingType.Cancel, pattern, inExpression)

fun makeFractionOf(numerator: ExpressionMaker, denominator: ExpressionMaker) =
    BinaryExpressionMaker(BinaryOperator.Fraction, numerator, denominator)

fun makeSumOf(vararg terms: ExpressionMaker) = NaryExpressionMaker(NaryOperator.Sum, terms.asList())

fun restOf(pattern: AssocNaryPattern) = RestExpressionMaker(pattern)