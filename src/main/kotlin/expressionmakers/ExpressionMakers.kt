package expressionmakers

import expressions.*
import patterns.*
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

data class OperatorExpressionMaker(val operator: Operator, val operands: List<ExpressionMaker>) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        return mappedExpression(operator, operands.map { it.makeMappedExpression(match) })
    }
}

data class FlattenedNaryExpressionMaker(val operator: NaryOperator, val operands: List<ExpressionMaker>) : ExpressionMaker {
    override fun makeMappedExpression(match: Match) =
        flattenedNaryMappedExpression(operator, operands.map { it.makeMappedExpression(match) })
}

data class RestExpressionMaker(val pattern: PartialNaryPattern) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        val subexpressions = pattern.getRestSubexpressions(match)
        return MappedExpression(
            Expression(pattern.operator, subexpressions.map { it.expr }),
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
            else -> FlattenedNaryExpressionMaker(pattern.operator, restChildren)
        }

        return exprMaker.makeMappedExpression(match)
    }
}

data class NumericOp2(
    val num1: IntegerProvider,
    val num2: IntegerProvider,
    val operation: (BigInteger, BigInteger) -> BigInteger
) : ExpressionMaker {
    override fun makeMappedExpression(match: Match): MappedExpression {
        val value = operation(num1.getBoundInt(match), num2.getBoundInt(match))

        val result = when {
            value.signum() >= 0 -> xp(value)
            else -> bracketOf(negOf(xp(-value)))
        }
        return MappedExpression(
            result,
            PathMappingLeaf(num1.getBoundPaths(match) + num2.getBoundPaths(match), PathMappingType.Combine),
        )
    }
}

fun makeFractionOf(numerator: ExpressionMaker, denominator: ExpressionMaker) =
    OperatorExpressionMaker(BinaryOperator.Fraction, listOf(numerator, denominator))

fun makeSumOf(vararg terms: ExpressionMaker) = FlattenedNaryExpressionMaker(NaryOperator.Sum, terms.asList())

fun makeProductOf(vararg terms: ExpressionMaker) = FlattenedNaryExpressionMaker(NaryOperator.Product, terms.asList())

fun makeNegOf(operand: ExpressionMaker) = OperatorExpressionMaker(UnaryOperator.Minus, listOf(operand))

fun restOf(pattern: PartialNaryPattern) = RestExpressionMaker(pattern)

fun makeMixedNumberOf(integer: ExpressionMaker, numerator: ExpressionMaker, denominator: ExpressionMaker) =
    OperatorExpressionMaker(MixedNumberOperator, listOf(integer, numerator, denominator))

fun makeNumericOp(
    ptn1: IntegerProvider,
    ptn2: IntegerProvider,
    operation: (BigInteger, BigInteger) -> BigInteger
): ExpressionMaker {
    return NumericOp2(ptn1, ptn2, operation)
}

fun substituteIn(pattern: NaryPatternBase, vararg newVals: ExpressionMaker) =
    SubstituteInExpressionMaker(pattern, newVals.asList())

class ExpressionMakerBuilder(private val match: Match) {
    fun isBound(pattern: Pattern): Boolean {
        return match.getLastBinding(pattern) != null
    }
}

class CustomExpressionMaker(val run: ExpressionMakerBuilder.() -> ExpressionMaker) : ExpressionMaker {

    override fun makeMappedExpression(match: Match): MappedExpression {
        val builder = ExpressionMakerBuilder(match)
        return builder.run().makeMappedExpression(match)
    }
}

fun custom(run: ExpressionMakerBuilder.() -> ExpressionMaker): ExpressionMaker {
    return CustomExpressionMaker(run)
}