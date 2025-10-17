/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package methods.collecting

import engine.context.Context
import engine.expressions.CancellableView
import engine.expressions.Expression
import engine.expressions.Factor
import engine.expressions.Fraction
import engine.expressions.IntegerExpression
import engine.expressions.Label
import engine.expressions.PiExpression
import engine.expressions.Power
import engine.expressions.Root
import engine.expressions.SquareRoot
import engine.expressions.Sum
import engine.expressions.TermView
import engine.expressions.TrigonometricExpression
import engine.expressions.isSigned
import engine.expressions.negOf
import engine.expressions.productOf
import engine.expressions.simplifiedProductOf
import engine.expressions.sumOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.IntegerFractionPattern
import engine.patterns.Pattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.rootOf
import engine.patterns.sumContaining
import engine.patterns.withOptionalIntegerCoefficient
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.metadata
import engine.steps.metadata.GmPathModifier as PM

enum class CollectingRules(override val runner: Rule) : RunnerMethod {
    CollectLikeRoots(
        CollectLikeTermsRule(
            factorSelector = { it is SquareRoot || it is Root },
            coefficientCondition = { it.isSigned<IntegerExpression>() || it.isSigned<Fraction>() },
            explanationKey = Explanation.CollectLikeRoots,
        ).rule,
    ),

    CollectLikeRationalPowers(
        CollectLikeTermsRule(
            factorSelector = { it is Power && it.base is IntegerExpression && it.exponent.isSigned<Fraction>() },
            coefficientCondition = { it.isSigned<IntegerExpression>() || it.isSigned<Fraction>() },
            explanationKey = Explanation.CollectLikeRationalPowers,
        ).rule,
    ),

    CollectLikeTerms(
        CollectLikeTermsRule(
            factorSelector = { !it.isConstant() },
            coefficientCondition = { it.isConstant() },
            explanationKey = Explanation.CollectLikeTerms,
        ).rule,
    ),

    CollectLikeTermsWithPi(
        CollectLikeTermsRule(
            factorSelector = { it is PiExpression },
            coefficientCondition = { it.isConstant() && it !is PiExpression },
            explanationKey = Explanation.CollectLikeTerms,
        ).rule,
    ),

    CollectLikeTermsWithTrigonometricFunctions(
        CollectLikeTermsRule(
            factorSelector = { it is TrigonometricExpression },
            coefficientCondition = { it.isConstant() && it !is TrigonometricExpression },
            explanationKey = Explanation.CollectLikeTerms,
        ).rule,
    ),

    CollectLikeTermsInSolutionVariables(
        CollectLikeTermsRule(
            factorSelector = { !it.isConstantIn(solutionVariables) },
            coefficientCondition = { it.isConstantIn(solutionVariables) },
            explanationKey = Explanation.CollectLikeTerms,
        ).rule,
    ),

    CombineTwoSimpleLikeRoots(
        createCombineSimpleLikeTermsRule(
            commonPattern = rootOf(UnsignedIntegerPattern()),
            explanationKey = Explanation.CombineTwoSimpleLikeRoots,
        ),
    ),

    CombineTwoSimpleLikeRationalPowers(
        createCombineSimpleLikeTermsRule(
            commonPattern = powerOf(UnsignedIntegerPattern(), IntegerFractionPattern()),
            explanationKey = Explanation.CombineTwoSimpleLikeRationalPowers,
        ),
    ),

    CombineTwoSimpleLikeTerms(
        createCombineSimpleLikeTermsRule(
            commonPattern = oneOf(
                ArbitraryVariablePattern(),
                powerOf(ArbitraryVariablePattern(), UnsignedIntegerPattern()),
            ),
            explanationKey = Explanation.CombineTwoSimpleLikeTerms,
        ),
    ),
}

/**
 * Create a rule which collects all the like terms, meaning terms in which the factors
 * selected by [factorSelector] match. There is an extra condition imposed on the coefficients
 * of the like terms by [coefficientCondition]. This is done so `sqrt[2]` and `sqrt[2] x` are
 * not collected.
 */
private class CollectLikeTermsRule(
    private val factorSelector: Context.(Expression) -> Boolean,
    private val coefficientCondition: Context.(Expression) -> Boolean,
    private val explanationKey: MetadataKey,
) {
    private class SplitTerm(
        val term: Expression,
        val selectedFactors: List<Expression>,
        val coefficient: Expression,
    ) {
        private val sortedFactors = selectedFactors.sortedWith(compareBy({ it.hashCode() }, { it.toString() }))

        fun hasSameFactorsAs(other: SplitTerm?) = other != null && sortedFactors == other.sortedFactors

        fun findLikeFactor(factor: Expression) = selectedFactors.singleOrNull { it == factor }
    }

    private fun Context.splitTerm(term: Expression): SplitTerm? {
        val termView = TermView(term) { CancellableView(it) }
        val selectedFactors = mutableListOf<Expression>()
        for (factor in termView.factors) {
            if (factorSelector(factor.original)) {
                selectedFactors.add(factor.original)
                factor.cancel()
            }
        }

        if (selectedFactors.isEmpty()) return null

        val coefficient = termView.recombine()
        if (!coefficientCondition(coefficient)) return null

        return SplitTerm(term, selectedFactors, coefficient)
    }

    val rule = rule {
        onPattern(sumContaining()) {
            val terms = (expression as Sum).terms
            val splitTerms = terms.map { context.splitTerm(it) }

            // Find a split term which has the same selected factors as at least one more term
            // (then we have a like term)
            val firstLikeTerm = splitTerms.firstOrNull { splitTerm ->
                splitTerm != null && splitTerms.count { splitTerm.hasSameFactorsAs(it) } > 1
            } ?: return@onPattern null

            // Now find the like terms in the sum
            val likeTerms = splitTerms.mapNotNull { if (firstLikeTerm.hasSameFactorsAs(it)) it else null }

            // Do not collect like terms when all like terms are fractions
            if (likeTerms.all { it.term is Fraction }) return@onPattern null

            // Compute the common factors of the sum with correct origin
            val commonFactorsWithOrigins = firstLikeTerm.selectedFactors.map { commonFactor ->
                val sameFactors = likeTerms.map { it.findLikeFactor(commonFactor) ?: return@onPattern null }
                commonFactor.withOrigin(Factor(sameFactors))
            }

            // Construct the factored term
            val factoredLikeTerms = productOf(
                sumOf(likeTerms.map { it.coefficient }),
                productOf(commonFactorsWithOrigins),
            ).withLabel(Label.A)

            // Then substitute that back into the sum
            val result = sumOf(
                splitTerms.mapIndexedNotNull { index, splitTerm ->
                    when (splitTerm) {
                        !in likeTerms -> terms[index]
                        likeTerms[0] -> factoredLikeTerms
                        else -> null
                    }
                },
            )

            ruleResult(
                toExpr = result,
                explanation = metadata(explanationKey),
            )
        }
    }
}

/**
 * Create a rule which combines two occurrences of the common pattern
 * with integer coefficients.
 * E.g. when the common pattern matches roots:
 *  3 sqrt[2] + sqrt[3] + 5 sqrt[2] -> 8 sqrt[2] + sqrt[3]
 */
private fun createCombineSimpleLikeTermsRule(commonPattern: Pattern, explanationKey: MetadataKey): Rule {
    return rule {
        val t1 = withOptionalIntegerCoefficient(commonPattern, false)
        val t2 = withOptionalIntegerCoefficient(commonPattern, false)
        val sum = sumContaining(t1, t2)

        onPattern(sum) {
            val newCoef =
                integerOp(t1.integerCoefficient, t2.integerCoefficient) { n1, n2 -> (n1 + n2).abs() }
            val newCoefValue = getValue(t1.integerCoefficient) + getValue(t2.integerCoefficient)
            val newTermAbs = simplifiedProductOf(newCoef, factor(commonPattern))
            val newTerm = if (newCoefValue < java.math.BigInteger.ZERO) negOf(newTermAbs) else newTermAbs

            ruleResult(
                toExpr = sum.substitute(newTerm),
                gmAction = drag(t2, PM.Group, t1, PM.Group),
                explanation = metadata(explanationKey, move(t1), move(t2)),
            )
        }
    }
}
