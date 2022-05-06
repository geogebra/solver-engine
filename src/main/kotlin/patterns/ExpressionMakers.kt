package patterns

import expressions.*
import steps.PathMappingType

interface ExpressionMaker {
    fun makeExpression(match: Match): Expression
}

data class PathMappingAnnotator(val pathMappingType: PathMappingType, val pattern: Pattern) : ExpressionMaker {
    override fun makeExpression(match: Match): Expression {
        return match.getPathMappingExpr(pattern, pathMappingType)
    }
}

data class VanishingPathAnnotator(
    val pathMappingType: PathMappingType,
    val pattern: Pattern,
    val inExpression: ExpressionMaker
) : ExpressionMaker {
    override fun makeExpression(match: Match): Expression {
        return match.getVanishingPathMappingExpr(pattern, pathMappingType, inExpression.makeExpression(match))
    }
}

data class UnaryExpressionMaker(val operator: UnaryOperator, val expr: ExpressionMaker) : ExpressionMaker {
    override fun makeExpression(match: Match): Expression {
        return UnaryExpr(operator, expr.makeExpression(match))
    }
}

data class BinaryExpressionMaker(val operator: BinaryOperator, val left: ExpressionMaker, val right: ExpressionMaker) :
    ExpressionMaker {
    override fun makeExpression(match: Match): Expression {
        return BinaryExpr(operator, left.makeExpression(match), right.makeExpression(match))
    }
}

data class NaryExpressionMaker(val operator: NaryOperator, val operands: List<ExpressionMaker>) : ExpressionMaker {
    override fun makeExpression(match: Match): Expression {
        return NaryExpr(operator, operands.map { it.makeExpression(match) })
    }
}

data class RestExpressionMaker(val pattern: AssocNaryPattern) : ExpressionMaker {
    override fun makeExpression(match: Match): Expression {
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