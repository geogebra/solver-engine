package engine.conditions

import engine.context.emptyContext
import engine.expressions.Expression
import engine.operators.BinaryExpressionOperator
import engine.operators.NaryOperator
import engine.operators.UnaryExpressionOperator
import engine.patterns.RationalPattern
import engine.patterns.RootMatch
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.integerOrderRootOf
import engine.patterns.oneOf
import engine.patterns.withOptionalRationalCoefficient
import engine.utility.hasFactorOfDegree
import java.math.BigInteger

/**
 * Returns true if the expression is definitely known to be non-zero, according to some heuristics
 */
fun Expression.isDefinitelyNotZero(): Boolean = when (signOf()) {
    Sign.POSITIVE, Sign.NEGATIVE -> true
    Sign.ZERO -> false
    else -> isNotZeroNotBasedOnSign()
}

@Suppress("ComplexMethod")
fun Expression.isNotZeroNotBasedOnSign(): Boolean = when (operator) {
    is UnaryExpressionOperator -> when (operator) {
        UnaryExpressionOperator.DivideBy -> operands[0].isDefinitelyNotZero()
        UnaryExpressionOperator.Plus -> operands[0].isDefinitelyNotZero()
        UnaryExpressionOperator.Minus -> operands[0].isDefinitelyNotZero()
        else -> false
    }
    is BinaryExpressionOperator -> when (operator) {
        BinaryExpressionOperator.Fraction -> operands[0].isDefinitelyNotZero() && operands[1].isDefinitelyNotZero()
        else -> false
    }
    is NaryOperator -> when (operator) {
        NaryOperator.Sum -> sumTermsAreIncommensurable(operands)
        NaryOperator.Product, NaryOperator.ImplicitProduct -> operands.all { it.isDefinitelyNotZero() }
    }
    else -> false
}

/**
 * Returns true if the [terms] are rational or integer roots (of any order) and are all incommensurable
 */
fun sumTermsAreIncommensurable(terms: List<Expression>): Boolean {

    // Define the pattern for acceptable terms
    val radicandPtn = UnsignedIntegerPattern()
    val rootPtn = integerOrderRootOf(radicandPtn)
    val rootTermPtn = withOptionalRationalCoefficient(rootPtn)
    val fractionTermPtn = RationalPattern()
    val termPtn = oneOf(fractionTermPtn, rootTermPtn)

    // We record the pair (a, n) for each term of the form x * root[a, n] where x is rational.  For rational
    // coefficients, we use the pair (1, 0).  For invalid terms we use the pair (0, 0). If the same pair is not
    // encountered twice then all subexpressions are incommensurable so we can return true.

    val invalidKey = Pair(BigInteger.ZERO, BigInteger.ZERO)
    val rationalKey = Pair(BigInteger.ONE, BigInteger.ONE)
    val visited = mutableSetOf(invalidKey)

    for (operand in terms) {
        val match = termPtn.findMatches(emptyContext, RootMatch, operand).firstOrNull()
        val fraction = match?.getBoundExpr(fractionTermPtn)
        val key = when {
            match == null -> invalidKey
            fraction != null -> if (fraction.isDefinitelyNotZero()) rationalKey else invalidKey
            else -> {
                val coeff = rootTermPtn.coefficient(match)
                val radicand = radicandPtn.getBoundInt(match)
                val order = rootPtn.order.getBoundInt(match)
                if (coeff.isDefinitelyNotZero() && order >= BigInteger.TWO && rootIsSimplified(radicand, order))
                    Pair(radicand, order) else invalidKey
            }
        }
        if (key in visited) {
            return false
        }
        visited.add(key)
    }
    return true
}

private fun rootIsSimplified(radicand: BigInteger, order: BigInteger): Boolean =
    !radicand.hasFactorOfDegree(order.toInt())
