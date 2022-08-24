package engine.expressionmakers

import engine.expressions.MappedExpression
import engine.patterns.Match
import engine.patterns.OptionalNegPattern

data class CopySignExpressionMaker(val from: OptionalNegPattern, val to: ExpressionMaker) :
    ExpressionMaker {

    override fun make(match: Match): MappedExpression {
        val maker = if (from.isNeg(match)) makeNegOf(to) else to
        return maker.make(match)
    }
}

fun copySign(from: OptionalNegPattern, to: ExpressionMaker) =
    CopySignExpressionMaker(from, to)

data class CopyFlippedSignExpressionMaker(val from: OptionalNegPattern, val to: ExpressionMaker) :
    ExpressionMaker {

    override fun make(match: Match): MappedExpression {
        val maker = if (from.isNeg(match)) to else makeNegOf(to)
        return maker.make(match)
    }
}

fun copyFlippedSign(from: OptionalNegPattern, to: ExpressionMaker) =
    CopyFlippedSignExpressionMaker(from, to)
