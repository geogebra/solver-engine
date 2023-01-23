package methods.polynomials

import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.Label
import engine.expressions.implicitProductOf
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.methods.ruleResult
import engine.operators.UnaryExpressionOperator
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.IntegerProvider
import engine.patterns.IntegerProviderWithDefault
import engine.patterns.KeyedPattern
import engine.patterns.Match
import engine.patterns.Pattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.patterns.withOptionalConstantCoefficient
import engine.steps.metadata.metadata
import java.math.BigInteger

enum class PolynomialRules(override val runner: Rule) : RunnerMethod {
    CollectLikeTerms(
        rule {
            val common = oneOf(
                ArbitraryVariablePattern(),
                powerOf(ArbitraryVariablePattern(), UnsignedIntegerPattern())
            )

            val commonTerm1 = withOptionalConstantCoefficient(common)
            val commonTerm2 = withOptionalConstantCoefficient(common)
            val sum = sumContaining(commonTerm1, commonTerm2)

            onPattern(sum) {
                ruleResult(
                    toExpr = collectLikeTermsInSum(
                        get(sum)!!,
                        withOptionalConstantCoefficient(common),
                        Label.A
                    ),
                    explanation = metadata(Explanation.CollectLikeTerms)
                )
            }
        }
    ),
    CollectUnitaryMonomialsInProduct(collectUnitaryMonomialsInProduct),
    NormalizeMonomial(normalizeMonomial),
    DistributeMonomialToIntegerPower(distributeMonomialToIntegerPower),
    DistributeProductToIntegerPower(distributeProductToIntegerPower),
    NormalizePolynomial(normalizePolynomial)
}

private val collectUnitaryMonomialsInProduct = rule {
    val commonVariable = ArbitraryVariablePattern()
    val product = productContaining(MonomialPattern(commonVariable), MonomialPattern(commonVariable))
    val negProduct = optionalNegOf(product)

    onPattern(negProduct) {
        val factors = get(product)!!.flattenedProductChildren()
        val monomialFactorPattern = MonomialPattern(commonVariable)
        val monomialFactors = mutableListOf<Expression>()
        val constantFactors = mutableListOf<Expression>()
        val otherFactors = mutableListOf<Expression>()
        for (factor in factors) {
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
            val hasNegativeConstantFactor = constantFactors.any { it.operator == UnaryExpressionOperator.Minus }
            if (hasNegativeConstantFactor) {
                constantFactors[0] = copySign(negProduct, constantFactors[0])
                signCopied = true
            }
            implicitProductOf(
                productOf(constantFactors).withLabel(Label.A),
                productOf(monomialFactors).withLabel(Label.B)
            )
        }

        val result = if (otherFactors.isEmpty()) {
            monomialProduct
        } else {
            productOf(monomialProduct, productOf(otherFactors))
        }

        ruleResult(
            toExpr = if (signCopied) result else copySign(negProduct, result),
            explanation = metadata(Explanation.CollectUnitaryMonomialsInProduct, move(commonVariable))
        )
    }
}

private val normalizeMonomial = rule {
    val monomial = MonomialPattern(ArbitraryVariablePattern(), positiveOnly = true)

    onPattern(monomial) {
        val before = get(monomial.key)
        val coeff = get(monomial::coefficient)!!
        val normalized = when {
            coeff == Constants.Zero -> move(coeff)
            coeff == Constants.One -> move(monomial.powerPattern)
            coeff.operator != UnaryExpressionOperator.Minus ->
                productOf(
                    coeff,
                    move(monomial.powerPattern)
                )
            coeff.firstChild == Constants.One -> negOf(move(monomial.powerPattern))
            else -> negOf(productOf(coeff.firstChild, move(monomial.powerPattern)))
        }
        if (normalized == before) null
        else ruleResult(
            toExpr = normalized,
            explanation = metadata(Explanation.NormalizeMonomial)
        )
    }
}

private val distributeMonomialToIntegerPower = rule {
    val monomial = MonomialPattern(ArbitraryVariablePattern())
    val exponent = UnsignedIntegerPattern()
    val power = powerOf(monomial, exponent)

    onPattern(power) {
        val coeff = get(monomial::coefficient)!!
        if (coeff != Constants.One) {
            ruleResult(
                toExpr = productOf(
                    powerOf(coeff, move(exponent)).withLabel(Label.A),
                    powerOf(move(monomial.powerPattern), move(exponent))
                ),
                explanation = metadata(Explanation.DistributeProductToIntegerPower)
            )
        } else {
            null
        }
    }
}

private val distributeProductToIntegerPower = rule {
    val testMonomialFactor = MonomialPattern(ArbitraryVariablePattern())
    val product = productContaining(testMonomialFactor)
    val exponent = UnsignedIntegerPattern()
    val power = powerOf(product, exponent)

    onPattern(power) {
        val factors = get(product)!!.flattenedProductChildren()
        val (constantFactors, otherFactors) = factors.partition { it.isConstant() }
        val factorPowers = mutableListOf<Expression>()
        if (constantFactors.isNotEmpty()) {
            factorPowers.add(powerOf(productOf(constantFactors), move(exponent)).withLabel(Label.A))
        }
        factorPowers.addAll(otherFactors.map { powerOf(it, move(exponent)) })
        ruleResult(
            toExpr = productOf(factorPowers),
            explanation = metadata(Explanation.DistributeProductToIntegerPower)
        )
    }
}

private val normalizePolynomial = rule {
    val commonVariable = ArbitraryVariablePattern()
    val sum = sumContaining(MonomialPattern(commonVariable))

    onPattern(sum) {
        val terms = get(sum)!!.children()
        val monomialPattern = MonomialPattern(commonVariable)

        // Find the degree of each term so we can decide whether the sum is normalized already.
        val termsWithDegree = terms.map { term ->
            val termOrder = when (val match = matchPattern(monomialPattern, term)) {
                null -> BigInteger.ZERO
                else -> monomialPattern.exponent.getBoundInt(match)
            }
            Pair(term, termOrder)
        }

        // It's normalized if the degrees are in non-increasing order
        val isNormalized = termsWithDegree.windowed(2).all { (t1, t2) -> t1.second >= t2.second }

        when {
            isNormalized -> null
            else -> {
                val termsInDescendingOrder = termsWithDegree.sortedByDescending { it.second }.map { it.first }
                ruleResult(
                    toExpr = sumOf(termsInDescendingOrder),
                    explanation = metadata(Explanation.NormalizePolynomial)
                )
            }
        }
    }
}

/**
 * Monomial pattern, i.e. [x ^ n] for n non-negative integer or x where x is the [base], possibly with a constant
 * coefficient.
 *
 * This pattern may be better off in the engine module if it proves widely useful.
 */
private class MonomialPattern(val base: Pattern, positiveOnly: Boolean = false) : KeyedPattern() {
    private val exponentPattern = UnsignedIntegerPattern()

    val powerPattern = oneOf(base, powerOf(base, exponentPattern))

    private val ptn = withOptionalConstantCoefficient(powerPattern, positiveOnly)

    val exponent: IntegerProvider = IntegerProviderWithDefault(exponentPattern, BigInteger.ONE)

    override val key = ptn.key

    fun getPower(match: Match) = powerPattern.getBoundExpr(match)

    fun coefficient(match: Match) = ptn.coefficient(match)
}
