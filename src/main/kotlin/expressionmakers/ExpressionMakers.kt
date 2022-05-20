package expressionmakers

import expressions.*
import patterns.IntegerPattern
import patterns.Match
import patterns.NaryPatternBase
import patterns.PartialNaryPattern
import java.math.BigInteger

interface ExpressionMaker {
    fun makeExpression(match: Match, currentPath: Path = RootPath): Pair<Expression, List<PathMapping>> {
        val mappedExpr = makeMappedExpression(match)
        return Pair(mappedExpr.expr, mappedExpr.mappings.pathMappings(currentPath).toList())
    }

    fun makeMappedExpression(match: Match): MappedExpression
}

data class FixedExpressionMaker(val expression: Expression) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        return MappedExpression(
            expression,
            PathMappingLeaf(listOf(IntroduceRootPath), PathMappingType.Introduce),
        )
    }
}

data class SubexpressionMaker(val subexpression: Subexpression) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        return MappedExpression(
            subexpression.expr,
            PathMappingLeaf(listOf(subexpression.path), PathMappingType.Move),
        )
    }
}

data class UnaryExpressionMaker(val operator: UnaryOperator, val expr: ExpressionMaker) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        val mappedExpr = expr.makeMappedExpression(match)
        return MappedExpression(
            UnaryExpr(operator, mappedExpr.expr),
            PathMappingParent(listOf(mappedExpr.mappings)),
        )
    }
}

data class BinaryExpressionMaker(val operator: BinaryOperator, val left: ExpressionMaker, val right: ExpressionMaker) :
    ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        return binaryMappedExpression(operator, left.makeMappedExpression(match), right.makeMappedExpression(match))
    }
}

data class NaryExpressionMaker(val operator: NaryOperator, val operands: List<ExpressionMaker>) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        return naryMappedExpression(operator, operands.map { it.makeMappedExpression(match) })
    }
}

data class RestExpressionMaker(val pattern: PartialNaryPattern) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        val subexpressions = pattern.getRestSubexpressions(match)
        return MappedExpression(
            NaryExpr(pattern.operator, subexpressions.map { it.expr }),
            PathMappingParent(subexpressions.map { PathMappingLeaf(listOf(it.path), PathMappingType.Move) })
        )
    }
}

data class SubstituteInExpressionMaker(val pattern: NaryPatternBase, val newVals: List<ExpressionMaker>) :
    ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        val sub = match.getLastBinding(pattern)!!
        val matchIndexes = pattern.getMatchIndexes(match, sub.path)

        val restChildren = ArrayList<ExpressionMaker>()
        for (child in sub.children()) {
            val newValIndex = matchIndexes.indexOf(child.index())
            when {
                newValIndex == -1 -> restChildren.add(SubexpressionMaker(child))
                newValIndex < newVals.size -> restChildren.add(newVals[newValIndex])
            }
        }

        // If there is only one operand, it makes no sense to wrap it in an nary expression
        val exprMaker = when (restChildren.size) {
            1 -> restChildren[0]
            else -> NaryExpressionMaker(pattern.operator, restChildren)
        }

        return exprMaker.makeMappedExpression(match)
    }
}

data class MixedNumberMaker(
    val integer: ExpressionMaker,
    val numerator: ExpressionMaker,
    val denominator: ExpressionMaker,
) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        return mixedNumberMappedExpression(
            integer.makeMappedExpression(match),
            numerator.makeMappedExpression(match),
            denominator.makeMappedExpression(match)
        )
    }
}

data class NumericOp2(
    val num1: IntegerPattern,
    val num2: IntegerPattern,
    val operation: (BigInteger, BigInteger) -> BigInteger
) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        return MappedExpression(
            IntegerExpr(operation(num1.getBoundInt(match).value, num2.getBoundInt(match).value)),
            PathMappingLeaf(match.getBoundPaths(num1) + match.getBoundPaths(num2), PathMappingType.Combine),
        )
    }
}

fun makeFractionOf(numerator: ExpressionMaker, denominator: ExpressionMaker) =
    BinaryExpressionMaker(BinaryOperator.Fraction, numerator, denominator)

fun makeSumOf(vararg terms: ExpressionMaker) = NaryExpressionMaker(NaryOperator.Sum, terms.asList())

fun makeProductOf(vararg terms: ExpressionMaker) = NaryExpressionMaker(NaryOperator.Product, terms.asList())

fun restOf(pattern: PartialNaryPattern) = RestExpressionMaker(pattern)

fun makeMixedNumberOf(integer: ExpressionMaker, numerator: ExpressionMaker, denominator: ExpressionMaker) =
    MixedNumberMaker(integer, numerator, denominator)

fun makeNumericOp(
    ptn1: IntegerPattern,
    ptn2: IntegerPattern,
    operation: (BigInteger, BigInteger) -> BigInteger
): ExpressionMaker {
    return NumericOp2(ptn1, ptn2, operation)
}

fun substituteIn(pattern: NaryPatternBase, vararg newVals: ExpressionMaker) =
    SubstituteInExpressionMaker(pattern, newVals.asList())