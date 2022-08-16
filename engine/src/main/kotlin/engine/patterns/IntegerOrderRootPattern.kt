package engine.patterns

import engine.expressions.Expression
import engine.expressions.Path
import engine.expressions.Subexpression
import engine.expressions.xp
import java.math.BigInteger

/**
 * A pattern that matches either `rootOf(radicand, UnsingedIntegerPattern())` or `squareRootOf(radicand)`.  It allows
 * treating both in a uniform way in rules.  The order of the root is always available via the `order` member of the
 * pattern, regardless of the pattern matched.
 */
data class IntegerOrderRootPattern(val radicand: Pattern) : Pattern {

    private val orderPtn = UnsignedIntegerPattern()
    private val ptn = OneOfPattern(listOf(rootOf(radicand, orderPtn), squareRootOf(radicand)))

    /**
     * This is either the orderPtn or just a path provider that returns 2 if the pattern was a square root.
     */
    val order: IntegerProvider = IntegerProviderWithDefault(orderPtn, BigInteger.TWO)

    override val key = ptn

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        return ptn.findMatches(subexpression, match)
    }
}

fun integerOrderRootOf(radicand: Pattern) = IntegerOrderRootPattern(radicand)

/**
 * This wraps a PathProvider so that it is given a default value if did not match.  It could be useful in other place
 * but for now only used here so kept private.
 */
private class IntegerProviderWithDefault(
    private val integerProvider: IntegerProvider,
    private val default: BigInteger
) : IntegerProvider {

    override fun getBoundInt(m: Match): BigInteger {
        return if (integerProvider.getBoundExpr(m) != null) {
            integerProvider.getBoundInt(m)
        } else {
            default
        }
    }

    override fun getBoundPaths(m: Match): List<Path> {
        return integerProvider.getBoundPaths(m)
    }

    override fun getBoundExpr(m: Match): Expression {
        return integerProvider.getBoundExpr(m) ?: xp(default)
    }
}
