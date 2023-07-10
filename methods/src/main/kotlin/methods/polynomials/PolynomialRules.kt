package methods.polynomials

import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.Label
import engine.expressions.Minus
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.monomialPattern
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.steps.Transformation
import engine.steps.metadata.metadata
import java.math.BigInteger

enum class PolynomialRules(override val runner: Rule) : RunnerMethod {
    CollectUnitaryMonomialsInProduct(collectUnitaryMonomialsInProduct),
    NormalizeMonomial(normalizeMonomial),
    DistributeMonomialToIntegerPower(distributeMonomialToIntegerPower),
    DistributeProductToIntegerPower(distributeProductToIntegerPower),
    NormalizePolynomial(normalizePolynomial),
}

private val collectUnitaryMonomialsInProduct = rule {
    val commonVariable = ArbitraryVariablePattern()
    val product = productContaining(monomialPattern(commonVariable), monomialPattern(commonVariable))
    val negProduct = optionalNegOf(product)

    onPattern(negProduct) {
        val monomialFactorPattern = monomialPattern(commonVariable)
        val monomialFactors = mutableListOf<Expression>()
        val constantFactors = mutableListOf<Expression>()
        val otherFactors = mutableListOf<Expression>()
        for (factor in get(product).children) {
            if (factor.isConstant()) {
                constantFactors.add(factor)
                continue
            }
            val match = matchPattern(monomialFactorPattern, factor)
            if (match == null) {
                otherFactors.add(factor)
            } else {
                monomialFactors.add(monomialFactorPattern.getPower(match)!!)
                val coeff = monomialFactorPattern.coefficient(match)

                // It would be better to just know when there is no coefficient
                if (coeff != Constants.One) {
                    constantFactors.add(coeff)
                }
            }
        }

        if (constantFactors.isEmpty() && otherFactors.isEmpty()) {
            return@onPattern null
        }
        var signCopied = false

        val monomialProduct = if (constantFactors.isEmpty()) {
            productOf(monomialFactors).withLabel(Label.B)
        } else {
            val hasNegativeConstantFactor = constantFactors.any { it is Minus }
            if (hasNegativeConstantFactor) {
                constantFactors[0] = copySign(negProduct, constantFactors[0])
                signCopied = true
            }
            productOf(
                productOf(constantFactors).withLabel(Label.A),
                productOf(monomialFactors).withLabel(Label.B),
            )
        }

        val result = if (otherFactors.isEmpty()) {
            monomialProduct
        } else {
            productOf(monomialProduct, productOf(otherFactors))
        }

        ruleResult(
            toExpr = if (signCopied) result else copySign(negProduct, result),
            explanation = metadata(Explanation.CollectUnitaryMonomialsInProduct, move(commonVariable)),
        )
    }
}

private val normalizeMonomial = rule {
    val monomial = monomialPattern(ArbitraryVariablePattern(), positiveOnly = true)

    onPattern(monomial) {
        val before = get(monomial.key)
        val coeff = get(monomial::coefficient)!!
        val normalized = when {
            coeff == Constants.Zero -> move(coeff)
            coeff == Constants.One -> move(monomial.powerPattern)
            coeff !is Minus ->
                productOf(
                    coeff,
                    move(monomial.powerPattern),
                )
            coeff.firstChild == Constants.One -> negOf(move(monomial.powerPattern))
            else -> negOf(productOf(coeff.firstChild, move(monomial.powerPattern)))
        }
        if (normalized == before) {
            null
        } else {
            ruleResult(
                toExpr = normalized,
                explanation = metadata(Explanation.NormalizeMonomial),
            )
        }
    }
}

private val distributeMonomialToIntegerPower = rule {
    val monomial = monomialPattern(ArbitraryVariablePattern())
    val exponent = UnsignedIntegerPattern()
    val power = powerOf(monomial, exponent)

    onPattern(power) {
        val coeff = get(monomial::coefficient)!!
        if (coeff != Constants.One) {
            ruleResult(
                toExpr = productOf(
                    powerOf(coeff, move(exponent)).withLabel(Label.A),
                    powerOf(move(monomial.powerPattern), move(exponent)),
                ),
                gmAction = drag(exponent, monomial),
                explanation = metadata(Explanation.DistributeProductToIntegerPower),
            )
        } else {
            null
        }
    }
}

private val distributeProductToIntegerPower = rule {
    val testMonomialFactor = monomialPattern(ArbitraryVariablePattern())
    val product = productContaining(testMonomialFactor)
    val exponent = UnsignedIntegerPattern()
    val power = powerOf(product, exponent)

    onPattern(power) {
        val (constantFactors, otherFactors) = get(product).children.partition { it.isConstant() }
        val factorPowers = mutableListOf<Expression>()
        if (constantFactors.isNotEmpty()) {
            factorPowers.add(powerOf(productOf(constantFactors), move(exponent)).withLabel(Label.A))
        }
        factorPowers.addAll(otherFactors.map { powerOf(it, move(exponent)) })
        ruleResult(
            toExpr = productOf(factorPowers),
            gmAction = drag(exponent, product),
            explanation = metadata(Explanation.DistributeProductToIntegerPower),
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
