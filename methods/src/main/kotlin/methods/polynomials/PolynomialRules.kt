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
import engine.patterns.defaultPolynomialSpecification
import engine.patterns.monomialPattern
import engine.patterns.optionalNegOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.Transformation
import engine.steps.metadata.DragTargetPosition
import engine.steps.metadata.metadata
import java.math.BigInteger
import engine.steps.metadata.GmPathModifier as PM

enum class PolynomialRules(override val runner: Rule) : RunnerMethod {
    RearrangeProductOfMonomials(rearrangeProductOfMonomials),
    NormalizePolynomial(normalizePolynomial),
    NormalizePolynomialOneStep(normalizePolynomialOneStep),
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
    val sum = sumContaining()

    onPattern(sum) {

        val spec = defaultPolynomialSpecification(context, expression) ?: return@onPattern null
        val terms = get(sum).children
        val monomialPattern = monomialPattern(spec)

        // Find the degree of each term so we can decide whether the sum is normalized already.
        // If we find a non-monomial non-constant term, we don't have a polynomial, so we cannot
        // normalize it
        val termsWithDegree = terms.map { term ->
            val termOrder = when (val match = matchPattern(monomialPattern, term)) {
                null -> if (spec.isConstant(context, term)) BigInteger.ZERO else return@onPattern null
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

// Like normalizePolynomial, but only does one step.
// It will find the biggest monomial term that's not in the right place and move it there.
private val normalizePolynomialOneStep = rule {
    val sum = sumContaining()

    onPattern(sum) {

        val spec = defaultPolynomialSpecification(context, expression) ?: return@onPattern null
        val terms = get(sum).children
        val monomialPattern = monomialPattern(spec)

        // Find the degree of each term so we can decide whether the sum is normalized already.
        // If we find a non-monomial non-constant term, we don't have a polynomial, so we cannot
        // normalize it
        val termsWithDegree = terms.map { term ->
            val termOrder = when (val match = matchPattern(monomialPattern, term)) {
                null -> if (spec.isConstant(context, term)) BigInteger.ZERO else return@onPattern null
                else -> monomialPattern.exponent.getBoundInt(match)
            }
            Pair(term, termOrder)
        }

        val termsInDescendingOrder = termsWithDegree.sortedByDescending { it.second }.map { it.first }

        val mismatchedTerm = termsInDescendingOrder.withIndex().find { it.value != termsWithDegree[it.index].first }
            ?: return@onPattern null

        ruleResult(
            toExpr = sumOf(
                terms.take(mismatchedTerm.index) +
                    move(mismatchedTerm.value) +
                    terms.drop(mismatchedTerm.index).filter { it != mismatchedTerm.value },
            ),
            gmAction = drag(
                mismatchedTerm.value,
                PM.Group,
                termsWithDegree[mismatchedTerm.index].first,
                null,
                DragTargetPosition.LeftOf,
            ),
            explanation = metadata(Explanation.NormalizePolynomial),
            tags = listOf(Transformation.Tag.Rearrangement),
        )
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
