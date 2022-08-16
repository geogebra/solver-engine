package engine.expressionmakers

import engine.expressions.MappedExpression
import engine.expressions.PathMappingLeaf
import engine.expressions.PathMappingType
import engine.expressions.negOf
import engine.expressions.xp
import engine.patterns.IntegerProvider
import engine.patterns.Match
import java.math.BigInteger

data class NumericOp(
    val num: IntegerProvider,
    val operation: (BigInteger) -> BigInteger
) : ExpressionMaker {
    override fun make(match: Match): MappedExpression {
        val value = operation(num.getBoundInt(match))

        val result = when {
            value.signum() >= 0 -> xp(value)
            else -> negOf(xp(-value))
        }
        return MappedExpression(
            result,
            PathMappingLeaf(num.getBoundPaths(match), PathMappingType.Transform),
        )
    }
}

data class NumericOp2(
    val num1: IntegerProvider,
    val num2: IntegerProvider,
    val operation: (BigInteger, BigInteger) -> BigInteger
) : ExpressionMaker {
    override fun make(match: Match): MappedExpression {
        val value = operation(num1.getBoundInt(match), num2.getBoundInt(match))

        val result = when {
            value.signum() >= 0 -> xp(value)
            else -> negOf(xp(-value))
        }
        return MappedExpression(
            result,
            PathMappingLeaf(num1.getBoundPaths(match) + num2.getBoundPaths(match), PathMappingType.Combine),
        )
    }
}

fun makeNumericOp(
    ptn: IntegerProvider,
    operation: (BigInteger) -> BigInteger
): ExpressionMaker {
    return NumericOp(ptn, operation)
}

fun makeNumericOp(
    ptn1: IntegerProvider,
    ptn2: IntegerProvider,
    operation: (BigInteger, BigInteger) -> BigInteger
): ExpressionMaker {
    return NumericOp2(ptn1, ptn2, operation)
}
