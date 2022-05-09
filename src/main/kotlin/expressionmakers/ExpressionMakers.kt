package expressionmakers

import expressions.*
import patterns.AssocNaryPattern
import patterns.IntegerPattern
import patterns.Match
import steps.PathMapping
import steps.PathMappingType
import steps.TypePathMapper
import java.math.BigInteger

interface ExpressionMaker {
    fun makeExpression(match: Match, currentPath: Path): Pair<Expression, List<PathMapping>> {
        val pathMappingsAccumulator = mutableListOf<PathMapping>()
        val result = makeExpressionAcc(match, currentPath, pathMappingsAccumulator)
        return Pair(result, pathMappingsAccumulator)
    }

    fun makeExpressionAcc(match: Match, currentPath: Path, acc: MutableList<PathMapping>): Expression
}

data class FixedExpressionMaker(val expression: Expression) : ExpressionMaker {

    override fun makeExpressionAcc(match: Match, currentPath: Path, acc: MutableList<PathMapping>): Expression {
        acc.add(PathMapping(IntroduceRootPath, PathMappingType.Introduce, currentPath))
        return expression
    }
}

data class SubexpressionMaker(val subexpression: Subexpression) : ExpressionMaker {

    override fun makeExpressionAcc(match: Match, currentPath: Path, acc: MutableList<PathMapping>): Expression {
        if (subexpression.path != currentPath) {
            acc.add(PathMapping(subexpression.path, PathMappingType.Move, currentPath))
        }
        return subexpression.expr
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
        val subexpressions = pattern.getRestSubexpressions(match);
        for ((i, subexpression) in subexpressions.withIndex()) {
            acc.add(PathMapping(subexpression.path, PathMappingType.Move, currentPath.child(i)))
        }

        return NaryExpr(pattern.operator, subexpressions.map { it.expr })
    }
}

data class SubstituteInExpressionMaker(val pattern: AssocNaryPattern, val newVal: ExpressionMaker) : ExpressionMaker {
    override fun makeExpressionAcc(match: Match, currentPath: Path, acc: MutableList<PathMapping>): Expression {
        val sub = match.getLastBinding(pattern)!!
        val matchIndexes = pattern.getMatchIndexes(match, sub.path)
        val firstIndex = matchIndexes.first()

        val restChildren = ArrayList<ExpressionMaker>()
        for (child in sub.children()) {
            if (child.index() == firstIndex) {
                restChildren.add(newVal)
            } else if (!matchIndexes.contains(child.index())) {
                restChildren.add(SubexpressionMaker(child))
            }
        }

        return NaryExpressionMaker(pattern.operator, restChildren).makeExpressionAcc(match, currentPath, acc)
    }
}

data class MixedNumberMaker(
    val integer: ExpressionMaker,
    val numerator: ExpressionMaker,
    val denominator: ExpressionMaker,
) : ExpressionMaker {

    override fun makeExpressionAcc(match: Match, currentPath: Path, acc: MutableList<PathMapping>): Expression {
        return MixedNumber(
            integer.makeExpressionAcc(match, currentPath.child(0), acc) as IntegerExpr,
            numerator.makeExpressionAcc(match, currentPath.child(1), acc) as IntegerExpr,
            denominator.makeExpressionAcc(match, currentPath.child(2), acc) as IntegerExpr,
        )
    }
}

data class NumericOp2(
    val num1: IntegerPattern,
    val num2: IntegerPattern,
    val operation: (BigInteger, BigInteger) -> BigInteger
) : ExpressionMaker {
    override fun makeExpressionAcc(match: Match, currentPath: Path, acc: MutableList<PathMapping>): Expression {
        TypePathMapper(match.getBoundPaths(num1), PathMappingType.Combine).accPathMappings(currentPath, acc)
        TypePathMapper(match.getBoundPaths(num2), PathMappingType.Combine).accPathMappings(currentPath, acc)

        return IntegerExpr(operation(num1.getBoundInt(match).value, num2.getBoundInt(match).value))
    }
}

fun makeFractionOf(numerator: ExpressionMaker, denominator: ExpressionMaker) =
    BinaryExpressionMaker(BinaryOperator.Fraction, numerator, denominator)

fun makeSumOf(vararg terms: ExpressionMaker) = NaryExpressionMaker(NaryOperator.Sum, terms.asList())

fun makeProductOf(vararg terms: ExpressionMaker) = NaryExpressionMaker(NaryOperator.Product, terms.asList())

fun restOf(pattern: AssocNaryPattern) = RestExpressionMaker(pattern)

fun makeMixedNumberOf(integer: ExpressionMaker, numerator: ExpressionMaker, denominator: ExpressionMaker) =
    MixedNumberMaker(integer, numerator, denominator)

fun makeNumericOp(
    ptn1: IntegerPattern,
    ptn2: IntegerPattern,
    operation: (BigInteger, BigInteger) -> BigInteger
): ExpressionMaker {
    return NumericOp2(ptn1, ptn2, operation)
}

fun substituteIn(pattern: AssocNaryPattern, newVal: ExpressionMaker) = SubstituteInExpressionMaker(pattern, newVal)