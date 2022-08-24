package engine.expressionmakers

import engine.expressions.BinaryOperator
import engine.expressions.BracketOperator
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.MappedExpression
import engine.expressions.MixedNumberOperator
import engine.expressions.NaryOperator
import engine.expressions.Operator
import engine.expressions.PathMappingLeaf
import engine.expressions.PathMappingType
import engine.expressions.UnaryOperator
import engine.expressions.flattenedNaryMappedExpression
import engine.expressions.mappedExpression
import engine.expressions.xp
import engine.patterns.Maker
import engine.patterns.Match
import engine.patterns.NaryPatternBase
import engine.patterns.OptionalDivideBy
import engine.patterns.PartialNaryPattern

typealias ExpressionMaker = Maker<MappedExpression>

data class FixedExpressionMaker(val expression: Expression) : ExpressionMaker {
    override fun make(match: Match): MappedExpression {
        return MappedExpression(
            expression,
            PathMappingLeaf(listOf(), PathMappingType.Introduce),
        )
    }
}

data class OperatorExpressionMaker(val operator: Operator, val operands: List<ExpressionMaker>) : ExpressionMaker {
    override fun make(match: Match): MappedExpression {
        return mappedExpression(operator, operands.map { it.make(match) })
    }
}

data class FlattenedNaryExpressionMaker(val operator: NaryOperator, val operands: List<ExpressionMaker>) :
    ExpressionMaker {
    override fun make(match: Match): MappedExpression {
        // If there is only one operand, it makes no sense to wrap it in an nary expression
        return when (operands.size) {
            1 -> operands[0].make(match)
            else -> flattenedNaryMappedExpression(operator, operands.map { it.make(match) })
        }
    }
}

data class SimplifiedProductMaker(val operands: List<ExpressionMaker>) : ExpressionMaker {
    override fun make(match: Match): MappedExpression {
        val madeOperands = operands.map { it.make(match) }.filter { it.expr != Constants.One }
        return when (madeOperands.size) {
            1 -> madeOperands[0]
            else -> flattenedNaryMappedExpression(NaryOperator.Product, madeOperands)
        }
    }
}

data class SimplifiedPowerMaker(val base: ExpressionMaker, val exponent: ExpressionMaker) : ExpressionMaker {
    override fun make(match: Match): MappedExpression {
        val madeBase = base.make(match)
        val madeExponent = exponent.make(match)

        return if (madeExponent.expr.equiv(Constants.One)) madeBase
        else mappedExpression(BinaryOperator.Power, listOf(madeBase, madeExponent))
    }
}

data class SubstituteInExpressionMaker(val pattern: NaryPatternBase, val newVals: List<ExpressionMaker>) :
    ExpressionMaker {
    override fun make(match: Match): MappedExpression {
        val sub = match.getLastBinding(pattern)!!
        val matchIndexes = pattern.getMatchIndexes(match, sub.path)

        val restChildren = ArrayList<ExpressionMaker>()
        for (child in sub.children()) {
            val newValIndex = matchIndexes.indexOf(child.index())
            when {
                newValIndex == -1 -> restChildren.add(child)
                newValIndex < newVals.size -> restChildren.add(newVals[newValIndex])
            }
        }

        return FlattenedNaryExpressionMaker(pattern.operator, restChildren).make(match)
    }
}

data class RestExpressionMaker(val pattern: PartialNaryPattern) :
    ExpressionMaker by SubstituteInExpressionMaker(pattern, listOf())

data class RootExpressionMaker(val radicand: ExpressionMaker, val order: ExpressionMaker) : ExpressionMaker {
    override fun make(match: Match): MappedExpression {
        val orderExpr = order.make(match)
        return if (orderExpr.expr.equiv(xp(2))) {
            makeSquareRootOf(radicand).make(match)
        } else {
            OperatorExpressionMaker(BinaryOperator.Root, listOf(radicand, order)).make(match)
        }
    }
}

fun makeBracketOf(operand: ExpressionMaker) =
    OperatorExpressionMaker(BracketOperator.Bracket, listOf(operand))

fun makeInvisibleBracketOf(operand: ExpressionMaker) =
    OperatorExpressionMaker(UnaryOperator.InvisibleBracket, listOf(operand))

fun makeFractionOf(numerator: ExpressionMaker, denominator: ExpressionMaker) =
    OperatorExpressionMaker(BinaryOperator.Fraction, listOf(numerator, denominator))

fun makeSumOf(terms: List<ExpressionMaker>) =
    FlattenedNaryExpressionMaker(NaryOperator.Sum, terms)

fun makeSumOf(vararg terms: ExpressionMaker) =
    FlattenedNaryExpressionMaker(NaryOperator.Sum, terms.asList())

fun makeProductOf(terms: List<ExpressionMaker>) =
    FlattenedNaryExpressionMaker(NaryOperator.Product, terms)

fun makeProductOf(vararg terms: ExpressionMaker) =
    FlattenedNaryExpressionMaker(NaryOperator.Product, terms.asList())

fun makeSimplifiedProductOf(vararg terms: ExpressionMaker) =
    SimplifiedProductMaker(terms.asList())

fun makePowerOf(base: ExpressionMaker, exponent: ExpressionMaker) =
    OperatorExpressionMaker(BinaryOperator.Power, listOf(base, exponent))

fun makeSimplifiedPowerOf(base: ExpressionMaker, exponent: ExpressionMaker) =
    SimplifiedPowerMaker(base, exponent)

fun makeNegOf(operand: ExpressionMaker) = OperatorExpressionMaker(UnaryOperator.Minus, listOf(operand))

fun makeDivideBy(operand: ExpressionMaker) = OperatorExpressionMaker(UnaryOperator.DivideBy, listOf(operand))

fun makeSquareRootOf(radicand: ExpressionMaker) = OperatorExpressionMaker(UnaryOperator.SquareRoot, listOf(radicand))

fun makeRootOf(radicand: ExpressionMaker, order: ExpressionMaker) = RootExpressionMaker(radicand, order)

fun restOf(pattern: PartialNaryPattern) = RestExpressionMaker(pattern)

fun makeMixedNumberOf(integer: ExpressionMaker, numerator: ExpressionMaker, denominator: ExpressionMaker) =
    OperatorExpressionMaker(MixedNumberOperator, listOf(integer, numerator, denominator))

fun substituteIn(pattern: NaryPatternBase, vararg newVals: ExpressionMaker) =
    SubstituteInExpressionMaker(pattern, newVals.asList())

data class OptionalDivideByExpressionMaker(
    val divPattern: OptionalDivideBy,
    val operand: ExpressionMaker,
) : ExpressionMaker {
    override fun make(match: Match): MappedExpression {
        val maker = if (divPattern.isDivide(match)) makeDivideBy(operand) else operand
        return maker.make(match)
    }
}

fun makeOptionalDivideBy(divPattern: OptionalDivideBy, operand: ExpressionMaker) =
    OptionalDivideByExpressionMaker(divPattern, operand)
