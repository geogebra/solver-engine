package engine.patterns

import engine.expressions.Subexpression
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
