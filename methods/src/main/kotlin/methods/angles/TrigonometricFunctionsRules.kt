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
import engine.expressions.Constants.Pi
import engine.expressions.Constants.Two
import engine.expressions.Expression
import engine.expressions.Label
import engine.expressions.fractionOf
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.operators.TrigonometricFunctionType
import engine.patterns.AnyPattern
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.NaryPattern
import engine.patterns.OptionalWrappingPattern
import engine.patterns.Pattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.TrigonometricExpressionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.commutativeSumContaining
import engine.patterns.degreeOf
import engine.patterns.fractionOf
import engine.patterns.integerCondition
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.optional
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.productOf
import engine.patterns.sumContaining
import engine.patterns.sumOf
import engine.steps.Transformation
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.metadata
import engine.utility.isOdd
import java.math.BigInteger

enum class TrigonometricFunctionsRules(override val runner: Rule) : RunnerMethod {
    ApplyNegativeIdentityOfTrigFunction(applyNegativeIdentityOfTrigFunction),
    ApplyNegativeIdentityOfTrigFunctionInReverse(applyNegativeIdentityOfTrigFunctionInReverse),
    ApplyIdentityOfInverseTrigonometricFunction(applyIdentityOfInverseTrigonometricFunction),
    ApplyPythagoreanIdentity(applyPythagoreanIdentity),
    ApplyCosineSumIdentity(applyCosineSumIdentity),
    ApplySineSumIdentity(applySineSumIdentity),
    ApplyTangentSumIdentity(applyTangentSumIdentity),
    ExpressAs2xArgument(expressAs2xArgument),
    ApplyDoubleAngleIdentity(applyDoubleAngleIdentity),
    RearrangeAddendsInArgument(rearrangeAddendsInArgument),
    AddLabelToSumContainingDoubleAngle(addLabelToSumContainingDoubleAngle),
    SimplifyToDerivedFunction(simplifyToDerivedFunction),
}

private fun getNegativeIdentityExplanation(functionType: TrigonometricFunctionType): MetadataKey? =
    when (functionType) {
        TrigonometricFunctionType.Sin, TrigonometricFunctionType.Arcsin -> Explanation.ApplyOddSymmetryOfSine
        TrigonometricFunctionType.Cos, TrigonometricFunctionType.Arccos -> Explanation.ApplyEvenSymmetryOfCosine
        TrigonometricFunctionType.Sec, TrigonometricFunctionType.Arcsec -> Explanation.ApplyEvenSymmetryOfSecant
        TrigonometricFunctionType.Csc, TrigonometricFunctionType.Arccsc -> Explanation.ApplyOddSymmetryOfCosecant
        TrigonometricFunctionType.Tan, TrigonometricFunctionType.Arctan -> Explanation.ApplyOddSymmetryOfTangent
        TrigonometricFunctionType.Cot, TrigonometricFunctionType.Arccot -> Explanation.ApplyOddSymmetryOfCotangent
        else -> null
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
            TrigonometricFunctionType.Arcsin,
            TrigonometricFunctionType.Cos,
            TrigonometricFunctionType.Arccos,
            TrigonometricFunctionType.Tan,
            TrigonometricFunctionType.Arctan,
            TrigonometricFunctionType.Cot,
            TrigonometricFunctionType.Arccot,
            TrigonometricFunctionType.Sec,
            TrigonometricFunctionType.Arcsec,
            TrigonometricFunctionType.Csc,
            TrigonometricFunctionType.Arccsc,
        ),
    )

    onPattern(pattern) {
        val toExpr = when (getFunctionType(pattern)) {
            TrigonometricFunctionType.Sin,
            TrigonometricFunctionType.Arcsin,
            TrigonometricFunctionType.Csc,
            TrigonometricFunctionType.Arccsc,
            TrigonometricFunctionType.Tan,
            TrigonometricFunctionType.Arctan,
            TrigonometricFunctionType.Cot,
            TrigonometricFunctionType.Arccot,
            ->
                negOf(
                    wrapWithTrigonometricFunction(
                        pattern,
                        move(value),
                    ),
                )
            TrigonometricFunctionType.Cos,
            TrigonometricFunctionType.Arccos,
            TrigonometricFunctionType.Sec,
            TrigonometricFunctionType.Arcsec,
            ->
                wrapWithTrigonometricFunction(
                    pattern,
                    move(value),
                )

            else -> null
        }

        val explanation = getNegativeIdentityExplanation(getFunctionType(pattern))

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
 * Apply odd symmetry in case of sine, tangent, cotangent, cosecant:
 * -sin(x) --> sin(-x)
 */
private val applyNegativeIdentityOfTrigFunctionInReverse = rule {
    val value = AnyPattern()
    val trigExpression = TrigonometricExpressionPattern(
        value,
        listOf(
            TrigonometricFunctionType.Sin,
            TrigonometricFunctionType.Cos,
            TrigonometricFunctionType.Sec,
            TrigonometricFunctionType.Tan,
            TrigonometricFunctionType.Cot,
            TrigonometricFunctionType.Csc,
        ),
    )
    val pattern = negOf(trigExpression)

    onPattern(pattern) {
        val toExpr = when (getFunctionType(trigExpression)) {
            TrigonometricFunctionType.Sin,
            TrigonometricFunctionType.Csc,
            TrigonometricFunctionType.Tan,
            TrigonometricFunctionType.Cot,
            ->
                wrapWithTrigonometricFunction(
                    trigExpression,
                    negOf(
                        move(value),
                    ),
                )
            TrigonometricFunctionType.Cos,
            TrigonometricFunctionType.Sec,
            ->
                wrapWithTrigonometricFunction(
                    trigExpression,
                    sumOf(
                        introduce(Pi),
                        negOf(move(value)),
                    ),
                )
            else -> null
        }

        val explanation = getNegativeIdentityExplanation(getFunctionType(trigExpression))

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
 * arcsin(sin(x)) --> x
 */
private val applyIdentityOfInverseTrigonometricFunction = rule {
    val value = AnyPattern()
    val innerFunction = TrigonometricExpressionPattern(value)
    val outerFunction = TrigonometricExpressionPattern(innerFunction)

    onPattern(outerFunction) {
        val innerType = getFunctionType(innerFunction)
        val outerType = getFunctionType(outerFunction)

        if (innerType.getInv() != outerType) {
            return@onPattern null
        }

        ruleResult(
            toExpr = transform(outerFunction, get(value)),
            explanation = metadata(Explanation.ApplyIdentityOfInverseTrigonometricFunction),
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
private val applyCosineSumIdentity = rule {
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
private val applySineSumIdentity = rule {
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
private val applyTangentSumIdentity = rule {
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

// sin[kx] --> sin[2 * [k/2] * x]
private val expressAs2xArgument = rule {
    val constantTerm = UnsignedIntegerPattern()
    val variableTerm = ArbitraryVariablePattern()
    val degreeTerm = degreeOf(constantTerm)

    val argument = oneOf(
        constantTerm,
        productOf(constantTerm, variableTerm),
        degreeTerm,
    )

    onPattern(argument) {
        val constant = getValue(constantTerm)

        if (constant.isOdd() || (constant == BigInteger.TWO && !isBound(variableTerm))) {
            return@onPattern null
        }

        val restConstant = constant.div(BigInteger.TWO)

        val terms = buildList {
            add(Two)
            if (isBound(degreeTerm)) {
                add(engine.expressions.degreeOf(xp(restConstant)))
            } else if (restConstant != BigInteger.ONE) {
                add(xp(restConstant))
            }
            if (isBound(variableTerm)) {
                add(get(variableTerm))
            }
        }

        ruleResult(
            toExpr = transform(
                argument,
                productOf(terms),
            ),
            explanation = metadata(Explanation.ExtractTwoFromArgument),
        )
    }
}

// Use double angle identity to simplify
private val applyDoubleAngleIdentity = rule {
    val twoTerm = FixedPattern(Two)
    val constantTerm = optional(SignedIntegerPattern(), ::degreeOf)
    val variable = ArbitraryVariablePattern()
    val argument = oneOf(
        productOf(twoTerm, constantTerm),
        productOf(twoTerm, variable),
        productOf(twoTerm, constantTerm, variable),
    )

    val trigFunction = TrigonometricExpressionPattern(
        argument,
        listOf(
            TrigonometricFunctionType.Sin,
            TrigonometricFunctionType.Cos,
            TrigonometricFunctionType.Tan,
            TrigonometricFunctionType.Cot,
        ),
    )

    onPattern(trigFunction) {
        val angle = distribute(
            buildList {
                if (isBound(constantTerm)) add(constantTerm)
                if (isBound(variable)) add(variable)
            },
        ).let {
            when (it.size) {
                1 -> it[0]
                else -> productOf(it)
            }
        }

        val two = distribute(twoTerm)

        val (toExpr, explanation) = when (getFunctionType(trigFunction)) {
            TrigonometricFunctionType.Sin ->
                productOf(
                    two,
                    wrapWithTrigonometricFunction(
                        trigFunction,
                        angle,
                        TrigonometricFunctionType.Sin,
                    ),
                    wrapWithTrigonometricFunction(
                        trigFunction,
                        angle,
                        TrigonometricFunctionType.Cos,
                    ),
                ) to Explanation.ApplySineDoubleAngleIdentity
            TrigonometricFunctionType.Cos ->
                sumOf(
                    powerOf(
                        wrapWithTrigonometricFunction(
                            trigFunction,
                            angle,
                            TrigonometricFunctionType.Cos,
                        ),
                        two,
                    ),
                    negOf(
                        powerOf(
                            wrapWithTrigonometricFunction(
                                trigFunction,
                                angle,
                                TrigonometricFunctionType.Sin,
                            ),
                            two,
                        ),
                    ),
                ) to Explanation.ApplyCosineDoubleAngleIdentity
            TrigonometricFunctionType.Tan -> fractionOf(
                productOf(
                    two,
                    wrapWithTrigonometricFunction(
                        trigFunction,
                        angle,
                    ),
                ),
                sumOf(
                    One,
                    negOf(
                        powerOf(
                            wrapWithTrigonometricFunction(
                                trigFunction,
                                angle,
                            ),
                            Two,
                        ),
                    ),
                ),
            ) to Explanation.ApplyTangentDoubleAngleIdentity
            else -> null to null
        }

        if (toExpr == null || explanation == null) {
            return@onPattern null
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

// Add a label to the expression containing the double angle in a sum, so it can be easily identified in follow-up steps
private val addLabelToSumContainingDoubleAngle = rule {
    val constant1 = UnsignedIntegerPattern()
    val constant2 = UnsignedIntegerPattern()

    val degree1 = degreeOf(constant1)
    val degree2 = degreeOf(constant2)

    val variable1 = ArbitraryVariablePattern()
    val variable2 = ArbitraryVariablePattern()

    val trigFunctionConst1 = TrigonometricExpressionPattern(constant1)
    val trigFunctionConst2 = TrigonometricExpressionPattern(constant2)

    val trigFunctionDegree1 = TrigonometricExpressionPattern(degree1)
    val trigFunctionDegree2 = TrigonometricExpressionPattern(degree2)

    val trigFunctionVarKX = TrigonometricExpressionPattern(productOf(constant1, variable1))
    val trigFunctionVar2KX = TrigonometricExpressionPattern(productOf(constant2, variable2))

    val (sumWithOnlyConstant, sumWithDegree, sumWithVariableAndConstant) = listOf(
        trigFunctionConst1 to trigFunctionConst2,
        trigFunctionDegree1 to trigFunctionDegree2,
        trigFunctionVarKX to trigFunctionVar2KX,
    ).map {
        commutativeSumContaining(
            optionalPatternContaining(it.first),
            optionalPatternContaining(it.second),
        )
    }

    val trigFunctionVarX = TrigonometricExpressionPattern(variable1)
    val trigFunctionVar2X = TrigonometricExpressionPattern(
        productOf(FixedPattern(Two), variable2),
    )

    val sumWithConstant = oneOf(
        sumWithOnlyConstant,
        sumWithDegree,
        sumWithVariableAndConstant,
    )

    val condition = integerCondition(
        constant1,
        constant2,
    ) { c1, c2 ->
        c1 * BigInteger.TWO == c2
    }

    val sumWithCondition = ConditionPattern(
        sumWithConstant,
        condition,
    )

    val sumWithoutConstant = commutativeSumContaining(
        optionalPatternContaining(trigFunctionVar2X),
        optionalPatternContaining(trigFunctionVarX),
    )

    val pattern = oneOf(
        sumWithoutConstant,
        sumWithCondition,
    )

    onPattern(pattern) {
        if (isBound(variable1) && get(variable1) != get(variable2)) {
            return@onPattern null
        }

        fun addLabelToExpr(parent: NaryPattern, expr: Expression) =
            get(parent).substitute(
                expr,
                expr.withLabel(Label.A),
            )

        val toExpr = when {
            isBound(sumWithOnlyConstant) -> addLabelToExpr(
                sumWithOnlyConstant,
                get(trigFunctionConst2),
            )
            isBound(sumWithVariableAndConstant) -> addLabelToExpr(
                sumWithVariableAndConstant,
                get(trigFunctionVar2KX),
            )
            isBound(sumWithoutConstant) -> addLabelToExpr(
                sumWithoutConstant,
                get(trigFunctionVar2X),
            )
            isBound(sumWithDegree) -> addLabelToExpr(
                sumWithDegree,
                get(trigFunctionDegree2),
            )
            else -> return@onPattern null
        }

        ruleResult(
            toExpr,
            explanation = metadata(Explanation.AddLabelToSumContainingDoubleAngle),
            tags = listOf(Transformation.Tag.InvisibleChange),
        )
    }
}

/**
 * Given a fraction containing trigonometric functions, simplify it to a derived function if possible
 *
 * e.g. sin(x) / cos(x) -> tan(x)
 */
private val simplifyToDerivedFunction = rule {
    val argument = AnyPattern()

    val sine = TrigonometricExpressionPattern.sin(argument)
    val cosine = TrigonometricExpressionPattern.cos(argument)

    val tanFraction = fractionOf(
        sine,
        cosine,
    )

    val cotFraction = fractionOf(
        cosine,
        sine,
    )

    val secFraction = fractionOf(
        FixedPattern(One),
        cosine,
    )

    val cscFraction = fractionOf(
        FixedPattern(One),
        sine,
    )

    val pattern = oneOf(tanFraction, cotFraction, secFraction, cscFraction)

    onPattern(pattern) {
        val argument = get(argument)
        val toExpression = transform(
            when {
                isBound(tanFraction) -> wrapWithTrigonometricFunction(
                    sine,
                    argument,
                    TrigonometricFunctionType.Tan,
                )
                isBound(cotFraction) -> wrapWithTrigonometricFunction(
                    cosine,
                    argument,
                    TrigonometricFunctionType.Cot,
                )
                isBound(secFraction) -> wrapWithTrigonometricFunction(
                    cosine,
                    argument,
                    TrigonometricFunctionType.Sec,
                )
                isBound(cscFraction) -> wrapWithTrigonometricFunction(
                    sine,
                    argument,
                    TrigonometricFunctionType.Csc,
                )
                else -> return@onPattern null
            },
        )

        ruleResult(
            toExpr = toExpression,
            explanation = metadata(Explanation.DeriveTrigonometricFunction),
        )
    }
}

// This is used only for double angles, instead of the FindPattern so that we don't go
// unnecessarily deep
fun optionalPatternContaining(pattern: Pattern) =
    OptionalWrappingPattern(pattern) {
        powerOf(pattern, AnyPattern())
    }.let { power ->
        optionalNegOf(
            OptionalWrappingPattern(
                oneOf(
                    power,
                    sumContaining(power),
                    productContaining(power),
                ),
            ) {
                fractionOf(it, AnyPattern())
            },
        )
    }
