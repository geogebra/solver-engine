/*
 * Copyright (c) 2025 GeoGebra GmbH, office@geogebra.org
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

package methods.angles

import engine.expressions.Constants.One
import engine.expressions.Constants.Two
import engine.expressions.fractionOf
import engine.expressions.negOf
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.operators.TrigonometricFunctionType
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.TrigonometricExpressionPattern
import engine.patterns.commutativeSumContaining
import engine.patterns.negOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.sumOf
import engine.steps.metadata.metadata

enum class TrigonometricFunctionsRules(override val runner: Rule) : RunnerMethod {
    ApplyNegativeIdentityOfTrigFunction(applyNegativeIdentityOfTrigFunction),
    ApplyPythagoreanIdentity(applyPythagoreanIdentity),
    ApplyCosineIdentity(applyCosineIdentity),
    ApplySineIdentity(applySineIdentity),
    ApplyTangentIdentity(applyTangentIdentity),
    RearrangeAddendsInArgument(rearrangeAddendsInArgument),
}

/**
 * Apply odd symmetry in case of sine, tangent, cotangent, cosecant:
 * sin(-x) --> -sin(x)
 * Apply even symmetry in case of cosine, sec:
 * cos(-x) --> cos(x)
 */
private val applyNegativeIdentityOfTrigFunction = rule {
    val value = AnyPattern()
    val negValue = negOf(value)
    val pattern = TrigonometricExpressionPattern(
        negValue,
        listOf(
            TrigonometricFunctionType.Sin,
            TrigonometricFunctionType.Cos,
            TrigonometricFunctionType.Tan,
            TrigonometricFunctionType.Cot,
            TrigonometricFunctionType.Sec,
            TrigonometricFunctionType.Csc,
        ),
    )

    onPattern(pattern) {
        val (toExpr, explanation) = when (getFunctionType(pattern)) {
            TrigonometricFunctionType.Sin, TrigonometricFunctionType.Csc ->
                negOf(
                    wrapWithTrigonometricFunction(
                        pattern,
                        move(value),
                    ),
                ) to Explanation.ApplyOddSymmetryOfSine
            TrigonometricFunctionType.Cos, TrigonometricFunctionType.Sec ->
                wrapWithTrigonometricFunction(
                    pattern,
                    move(value),
                ) to Explanation.ApplyEvenSymmetryOfCosine
            TrigonometricFunctionType.Tan, TrigonometricFunctionType.Cot ->
                negOf(
                    wrapWithTrigonometricFunction(
                        pattern,
                        move(value),
                    ),
                ) to Explanation.ApplyOddSymmetryOfTangent
            else -> null to null
        }

        if (toExpr == null || explanation == null) {
            return@onPattern null
        }

        ruleResult(
            toExpr = toExpr,
            explanation = metadata(explanation),
        )
    }
}

/**
 * sin x + cos x --> 1
 */
private val applyPythagoreanIdentity = rule {
    val argument = AnyPattern()
    val exponent = FixedPattern(Two)
    val sine = powerOf(TrigonometricExpressionPattern(argument, listOf(TrigonometricFunctionType.Sin)), exponent)
    val cosine = powerOf(TrigonometricExpressionPattern(argument, listOf(TrigonometricFunctionType.Cos)), exponent)

    val sum = commutativeSumContaining(sine, cosine)

    onPattern(sum) {
        ruleResult(
            toExpr = sum.substitute(One),
            explanation = metadata(Explanation.ApplyPythagoreanIdentity),
        )
    }
}

/**
 * cos(x+y) --> cos x * cos y - sin x * sin y
 * cos(x-y) --> cos x * cos y + sin x * sin y
 */
private val applyCosineIdentity = rule {
    val term1 = AnyPattern()
    val term2 = AnyPattern()
    val wrappedTerm2 = optionalNegOf(term2)

    val sum = sumOf(term1, wrappedTerm2)

    val pattern = TrigonometricExpressionPattern(
        sum,
        listOf(TrigonometricFunctionType.Cos),
    )

    onPattern(pattern) {
        val distributedCosines = productOf(
            wrapWithTrigonometricFunction(pattern, distribute(term1)),
            wrapWithTrigonometricFunction(pattern, distribute(term2)),
        )

        val distributedSines = productOf(
            wrapWithTrigonometricFunction(pattern, distribute(term1), TrigonometricFunctionType.Sin),
            wrapWithTrigonometricFunction(pattern, distribute(term2), TrigonometricFunctionType.Sin),
        )

        val toExpr = sumOf(distributedCosines, copyFlippedSign(wrappedTerm2, distributedSines))

        val explanation = if (wrappedTerm2.isNeg()) {
            AnglesExplanation.ApplyCosineDifferenceIdentity
        } else {
            AnglesExplanation.ApplyCosineSumIdentity
        }

        ruleResult(
            toExpr,
            metadata(explanation),
        )
    }
}

/**
 * sin (x-y) --> sin x * cos y - cos x * sin y
 * sin (x+y) --> sin x * cos y + cos x * sin y
 */
private val applySineIdentity = rule {
    val term1 = AnyPattern()
    val term2 = AnyPattern()
    val wrappedTerm2 = optionalNegOf(term2)

    val sum = sumOf(term1, wrappedTerm2)

    val pattern = TrigonometricExpressionPattern(
        sum,
        listOf(TrigonometricFunctionType.Sin),
    )

    onPattern(pattern) {
        val leftOperand = productOf(
            wrapWithTrigonometricFunction(pattern, distribute(term1)),
            wrapWithTrigonometricFunction(pattern, distribute(term2), TrigonometricFunctionType.Cos),
        )

        val rightOperand = productOf(
            wrapWithTrigonometricFunction(pattern, distribute(term1), TrigonometricFunctionType.Cos),
            wrapWithTrigonometricFunction(pattern, distribute(term2)),
        )

        val toExpr = sumOf(leftOperand, copySign(wrappedTerm2, rightOperand))

        val explanation = if (wrappedTerm2.isNeg()) {
            AnglesExplanation.ApplySineDifferenceIdentity
        } else {
            AnglesExplanation.ApplySineSumIdentity
        }

        ruleResult(
            toExpr,
            metadata(explanation),
        )
    }
}

/**
 * tan (x - y) --> [ tan x - tan y / 1 + tan x * tan y ]
 * tan (x + y) --> [ tan x + tan y / 1 - tan x * tan y ]
 */
private val applyTangentIdentity = rule {
    val term1 = AnyPattern()
    val term2 = AnyPattern()
    val wrappedTerm2 = optionalNegOf(term2)

    val sum = sumOf(term1, wrappedTerm2)

    val pattern = TrigonometricExpressionPattern(
        sum,
        listOf(TrigonometricFunctionType.Tan),
    )

    onPattern(pattern) {
        val numerator = sumOf(
            wrapWithTrigonometricFunction(pattern, distribute(term1)),
            copySign(wrappedTerm2, wrapWithTrigonometricFunction(pattern, distribute(term2))),
        )

        val denominator = sumOf(
            introduce(One),
            copyFlippedSign(
                wrappedTerm2,
                productOf(
                    wrapWithTrigonometricFunction(pattern, distribute(term1)),
                    wrapWithTrigonometricFunction(pattern, distribute(term2)),
                ),
            ),
        )

        val toExpr = fractionOf(numerator, denominator)

        val explanation = if (wrappedTerm2.isNeg()) {
            AnglesExplanation.ApplyTangentDifferenceIdentity
        } else {
            AnglesExplanation.ApplyTangentSumIdentity
        }

        ruleResult(
            toExpr,
            metadata(explanation),
        )
    }
}

/**
 * sin(-x + y) --> sin(y - x)
 */
private val rearrangeAddendsInArgument = rule {
    val term1 = optionalNegOf(AnyPattern())
    val term2 = optionalNegOf(AnyPattern())

    val pattern = TrigonometricExpressionPattern(sumOf(term1, term2))

    onPattern(pattern) {
        if (term1.isNeg() && !term2.isNeg()) {
            ruleResult(
                toExpr = wrapWithTrigonometricFunction(pattern, sumOf(move(term2), move(term1))),
                explanation = metadata(Explanation.RearrangeAddendsInArgument),
            )
        } else {
            null
        }
    }
}
