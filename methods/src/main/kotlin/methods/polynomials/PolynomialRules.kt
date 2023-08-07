package methods.polynomials

import engine.expressions.Decorator
import engine.expressions.Expression
import engine.expressions.Product
import engine.expressions.negOf
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.expressions.variablePowerBase
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.condition
import engine.patterns.monomialPattern
import engine.patterns.optionalNegOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.Transformation
import engine.steps.metadata.metadata
import java.math.BigInteger

enum class PolynomialRules(override val runner: Rule) : RunnerMethod {
    RearrangeProductOfMonomials(rearrangeProductOfMonomials),
    NormalizePolynomial(normalizePolynomial),
}

private val rearrangeProductOfMonomials = rule {
    val product = productContaining()
    val monomial = optionalNegOf(product)

    onPattern(monomial) {
        val coefficients = mutableListOf<Expression>()
        val variablePowers = sortedMapOf<String, List<Expression>>()

        for (factor in get(product).children) {
            if (factor.isConstant()) {
                coefficients.add(move(factor))
            } else {
                val variablePowerBase = factor.variablePowerBase() ?: return@onPattern null
                variablePowers.merge(variablePowerBase.variableName, listOf(move(factor))) { a, b -> a + b }
            }
        }

        // If there's at most one variable power, then it is not a product of monomials, so return
        if (variablePowers.values.sumOf { it.size } <= 1) return@onPattern null

        var negCopied = false
        if (monomial.isNeg() && coefficients.size > 0) {
            coefficients[0] = negOf(coefficients[0])
            negCopied = true
        }

        val normalized = bracketedProductOf(listOf(coefficients) + variablePowers.values)
        if (normalized == expression) return@onPattern null

        ruleResult(
            toExpr = if (negCopied) normalized else copySign(monomial, normalized),
            explanation = metadata(Explanation.RearrangeProductOfMonomials),
        )
    }
}

private val normalizePolynomial = rule {
    val commonVariable = ArbitraryVariablePattern()
    val sum = condition(sumContaining(monomialPattern(commonVariable))) { it.variables.size == 1 }

    onPattern(sum) {
        val terms = get(sum).children
        val monomialPattern = monomialPattern(commonVariable)

        // Find the degree of each term so we can decide whether the sum is normalized already.
        // If we find a non-monomial non-constant term, we don't have a polynomial, so we cannot
        // normalize it
        val termsWithDegree = terms.map { term ->
            val termOrder = when (val match = matchPattern(monomialPattern, term)) {
                null -> if (term.isConstant()) BigInteger.ZERO else return@onPattern null
                else -> monomialPattern.exponent.getBoundInt(match)
            }
            Pair(term, termOrder)
        }

        // It's normalized if the degrees are in non-increasing order
        val isNormalized = termsWithDegree.zipWithNext { t1, t2 -> t1.second >= t2.second }.all { it }

        when {
            isNormalized -> null
            else -> {
                val termsInDescendingOrder = termsWithDegree.sortedByDescending { it.second }.map { it.first }
                ruleResult(
                    toExpr = sumOf(termsInDescendingOrder),
                    explanation = metadata(Explanation.NormalizePolynomial),
                    tags = listOf(Transformation.Tag.Rearrangement),
                )
            }
        }
    }
}

private fun bracketedProductOf(factors: List<List<Expression>>): Expression {
    val bracketedFactors = factors.mapNotNull {
        when (it.size) {
            0 -> null
            1 -> it[0]
            else -> productOf(it).decorate(Decorator.RoundBracket)
        }
    }

    return if (bracketedFactors.size == 1) {
        bracketedFactors[0]
    } else {
        Product(bracketedFactors)
    }
}
