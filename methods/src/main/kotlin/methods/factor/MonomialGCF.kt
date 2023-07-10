package methods.factor

import engine.context.emptyContext
import engine.expressions.Distribute
import engine.expressions.Expression
import engine.expressions.Minus
import engine.expressions.negOf
import engine.expressions.productOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.xp
import engine.patterns.FixedPattern
import engine.patterns.Match
import engine.patterns.Pattern
import engine.patterns.RootMatch
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.utility.gcd
import java.math.BigInteger

object MonomialGCF {

    /**
     * Returns the factors in a (possibly negated) product
     * E.g.
     *   3x*2 -> [3, x, 2]
     *   -4xy -> [4, x, y]
     *   -x -> [x]
     */
    private fun extractFactors(exp: Expression): MutableList<Expression> {
        val product = if (exp is Minus) {
            exp.firstChild
        } else {
            exp
        }
        return product.factors().toMutableList()
    }

    /**
     * Given a list of factors find which one matches the [pattern]. If zero or more than
     * one factor matches the pattern, then return null.
     *
     * E.g. if `p` is a SignedIntegerPattern, then
     * [-1, x] -> (0, match[p -> -1])
     * [x, y] -> null
     * [3, x, 4] -> null
     */
    private fun findMatchingFactor(pattern: Pattern, factors: List<Expression>): Pair<Int, Match>? {
        var matchWithIndex: Pair<Int, Match>? = null
        for ((index, term) in factors.withIndex()) {
            val currentMatch = pattern.findMatches(emptyContext, RootMatch, term).firstOrNull()
            if (currentMatch != null) {
                if (matchWithIndex == null) {
                    matchWithIndex = Pair(index, currentMatch)
                } else {
                    // more than one match among the factors -- monomial not normalized
                    return null
                }
            }
        }

        return matchWithIndex
    }

    /**
     * Puts the new factors back into the polynomials, negating each term as necessary to agree with the original
     * polynomial
     */
    private fun recombineNewFactors(originalExps: List<Expression>, newFactors: List<List<Expression>>):
        List<Expression> {
        return newFactors.mapIndexed { index, factors ->
            if (originalExps[index] is Minus) {
                negOf(productOf(factors))
            } else {
                productOf(factors)
            }
        }
    }

    /**
     * Splits each monomial of a polynomial so that the common integer factor is explicit
     * E.g. 6 + 14x + 22x^2 -> 2 * 3 + 2 * 7x + 2 * 11x^2
     *
     * Non-integer constant factors are supported - they remain unchanged
     * E.g. 9 sqrt[2] x^2 + 12 sqrt[3] x^3 -> 3 * 3 sqrt[2] x^2 + 3 * 4 sqrt[3] x^3
     */
    fun splitIntegersInMonomials(exps: List<Expression>): List<Expression>? {
        val integerPattern = SignedIntegerPattern()
        val monomialFactors = exps.map { extractFactors(it) }

        val matches = monomialFactors.map { findMatchingFactor(integerPattern, it) ?: return null }

        val gcd = matches.map { (_, match) -> integerPattern.getBoundInt(match) }.gcd()
        if (gcd == BigInteger.ONE) return null

        matches.forEachIndexed { index, (matchIndex, match) ->
            val integerValue = integerPattern.getBoundInt(match)
            if (integerValue != gcd) {
                val terms = monomialFactors[index]

                val toReplace = terms[matchIndex]
                val gcdExpression = xp(gcd).withOrigin(Distribute(toReplace))
                terms[matchIndex] = gcdExpression

                val remainder = integerValue / gcd
                val remainderExpression = xp(remainder).withOrigin(Distribute(toReplace))
                terms.add(matchIndex + 1, remainderExpression)
            }
        }

        return recombineNewFactors(exps, monomialFactors)
    }

    /**
     * Splits each monomial of a polynomial so that the common variable power is explicit
     * E.g. 14 x^2 + 22 x^5 -> 14 x^2 + 22 x^2 * x^3
     *
     * Non-integer constant factors are supported - they remain unchanged
     * E.g. 9 sqrt[2] x^2 + 12 sqrt[3] x^3 -> 9 sqrt[2] x^2 + 12 sqrt[3] x^2 * x
     */
    fun splitVariablePowersInMonomials(exps: List<Expression>, variable: String): List<Expression>? {
        val basePattern = FixedPattern(xp(variable))
        val exponentPattern = UnsignedIntegerPattern()
        val variablePowerPattern = oneOf(basePattern, powerOf(basePattern, exponentPattern))

        val monomialFactors = exps.map { extractFactors(it) }

        val matches = monomialFactors.map { findMatchingFactor(variablePowerPattern, it) ?: return null }

        val minExponent = matches.minOf { (_, match) -> exponentPattern.getBoundInt(match, BigInteger.ONE) }
        if (minExponent == BigInteger.ZERO) return null

        matches.forEachIndexed { index, (matchIndex, match) ->
            val exponentValue = exponentPattern.getBoundInt(match, default = BigInteger.ONE)
            if (exponentValue != minExponent) {
                val terms = monomialFactors[index]

                val base = xp(variable).withOrigin(Distribute(basePattern.getBoundExprs(match)))
                val exponent = xp(minExponent).withOrigin(Distribute(exponentPattern.getBoundExprs(match)))

                val gcf = simplifiedPowerOf(base, exponent)
                terms[matchIndex] = gcf

                val remainingExponent = xp(exponentValue - minExponent)
                    .withOrigin(Distribute(exponentPattern.getBoundExprs(match)))
                terms.add(matchIndex + 1, simplifiedPowerOf(base, remainingExponent))
            }
        }

        return recombineNewFactors(exps, monomialFactors)
    }
}
