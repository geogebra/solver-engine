package expressionmakers

import expressions.*
import patterns.AssocNaryPattern
import patterns.IntegerPattern
import patterns.Match
import steps.PathMapping
import steps.PathMappingType
import java.math.BigInteger

interface PathMappingSet {
    fun childList(size: Int): List<PathMappingSet>

    fun pathMappings(root: Path): Sequence<PathMapping>
}

data class PathMappingLeaf(val paths: List<Path>, val type: PathMappingType) : PathMappingSet {

    override fun childList(size: Int) =
        (0 until size).map { i ->
            PathMappingLeaf(
                paths.map { it.child(i) },
                type.composeWith(PathMappingType.Move)
            )
        }

    override fun pathMappings(root: Path) = paths.map { PathMapping(it, type, root) }.asSequence()
}

data class PathMappingParent(val children: List<PathMappingSet>) : PathMappingSet {

    override fun childList(size: Int): List<PathMappingSet> {
        return children
    }

    override fun pathMappings(root: Path): Sequence<PathMapping> {
        return children.mapIndexed { i, child -> child.pathMappings(root.child(i)) }.asSequence().flatten()
    }
}

data class MappedExpression(val expr: Expression, val mappings: PathMappingSet)

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
        val mappedLeft = left.makeMappedExpression(match)
        val mappedRight = right.makeMappedExpression(match)
        return MappedExpression(
            BinaryExpr(operator, mappedLeft.expr, mappedRight.expr),
            PathMappingParent(listOf(mappedLeft.mappings, mappedRight.mappings)),
        )
    }
}

data class NaryExpressionMaker(val operator: NaryOperator, val operands: List<ExpressionMaker>) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        var ops = mutableListOf<Expression>()
        var mappingSets = mutableListOf<PathMappingSet>()
        for (operand in operands) {
            val mappedExpr = operand.makeMappedExpression(match)
            if (mappedExpr.expr is NaryExpr && mappedExpr.expr.operator == operator) {
                ops.addAll(mappedExpr.expr.operands)
                mappingSets.addAll(mappedExpr.mappings.childList(mappedExpr.expr.operands.size))
            } else {
                ops.add(mappedExpr.expr)
                mappingSets.add(mappedExpr.mappings)
            }
        }
        return MappedExpression(
            NaryExpr(operator, ops),
            PathMappingParent(mappingSets)
        )
    }
}

data class RestExpressionMaker(val pattern: AssocNaryPattern) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        val subexpressions = pattern.getRestSubexpressions(match)
        return MappedExpression(
            NaryExpr(pattern.operator, subexpressions.map { it.expr }),
            PathMappingParent(subexpressions.mapIndexed { i, sub ->
                PathMappingLeaf(listOf(sub.path), PathMappingType.Move)
            })
        )
    }
}

data class SubstituteInExpressionMaker(val pattern: AssocNaryPattern, val newVal: ExpressionMaker) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
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
        val mappedInteger = integer.makeMappedExpression(match)
        val mappedNumerator = numerator.makeMappedExpression(match)
        val mappedDenominator = denominator.makeMappedExpression(match)
        return MappedExpression(
            MixedNumber(
                mappedInteger.expr as IntegerExpr,
                mappedNumerator.expr as IntegerExpr,
                mappedDenominator.expr as IntegerExpr
            ),
            PathMappingParent(
                listOf(
                    mappedInteger.mappings,
                    mappedNumerator.mappings,
                    mappedDenominator.mappings,
                ),
            )
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