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

package methods.units

import engine.expressions.Constants
import engine.expressions.fractionOf
import engine.expressions.productOf
import engine.expressions.simplifiedProductOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnitExpressionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.UnsignedNumberPattern
import engine.patterns.commutativeProductContaining
import engine.patterns.divideBy
import engine.patterns.fractionOf
import engine.patterns.integerCondition
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.productContaining
import engine.patterns.stickyOptionalNegOf
import engine.patterns.sumContaining
import engine.steps.metadata.metadata
import engine.utility.divides
import java.math.BigInteger
import engine.steps.metadata.GmPathModifier as PM

enum class UnitsRules(override val runner: Rule) : RunnerMethod {
    EvaluateSignedIntegerWithUnitAddition(evaluateSignedIntegerWithUnitAddition),
    EvaluateUnitProductAndDivision(evaluateUnitProductAndDivision),
    SimplifyFractionOfUnits(simplifyFractionOfUnits),
    SimplifyFractionOfUnitAndConstantToInteger(simplifyFractionOfUnitAndConstantToInteger),
    FindCommonIntegerFactorInFractionOfUnitAndConstant(findCommonIntegerFactorInFractionOfUnitAndConstant),
    ConvertTerminatingDecimalWithUnitToFraction(convertTerminatingDecimalWithUnitToFraction),
}

/**
 * unit[ x ] * y --> unit[ (x*y) ]
 * unit[ x ] : y --> unit[ (x:y) ]
 */
private val evaluateUnitProductAndDivision = rule {
    val base = SignedIntegerPattern()
    val baseWrapper = UnitExpressionPattern(base)
    val multiplier = SignedIntegerPattern()
    val divisor = SignedIntegerPattern()
    val product = commutativeProductContaining(
        baseWrapper,
        oneOf(
            multiplier,
            ConditionPattern(
                divideBy(divisor),
                integerCondition(base, divisor) { n1, n2 -> n2.signum() != 0 && (n1 % n2).signum() == 0 },
            ),
        ),
    )

    val optionalNegProduct = stickyOptionalNegOf(product, initialPositionOnly = true)

    onPattern(optionalNegProduct) {
        val res = if (isBound(multiplier)) {
            integerOp(base, multiplier) { n1, n2 -> n1 * n2 }
        } else {
            integerOp(base, divisor) { n1, n2 -> n1 / n2 }
        }

        val wrappedRes = addUnit(baseWrapper, res)

        // if the result is 0, then no need for the sign
        val toExpr = if (res == Constants.Zero) {
            product.substitute(wrappedRes)
        } else {
            copySign(optionalNegProduct, product.substitute(wrappedRes))
        }

        val gmAction = if (isBound(multiplier)) {
            drag(multiplier, PM.Group, base, PM.Group)
        } else {
            drag(divisor, PM.Group, base, PM.Group)
        }

        val explanation = if (isBound(multiplier)) {
            metadata(Explanation.EvaluateIntegerUnitProduct, move(baseWrapper), move(multiplier))
        } else {
            metadata(Explanation.EvaluateIntegerUnitDivision, move(baseWrapper), move(divisor))
        }

        ruleResult(
            toExpr = toExpr,
            gmAction = gmAction,
            explanation = explanation,
        )
    }
}

/**
 * [unit[ x ] / unit [ y ]] -> [x / y]
 */
private val simplifyFractionOfUnits = rule {
    val numeratorVal = AnyPattern()
    val denominatorVal = AnyPattern()

    val numeratorUnit = UnitExpressionPattern(numeratorVal)
    val negNumerator = optionalNegOf(numeratorUnit)
    val denominatorUnit = UnitExpressionPattern(denominatorVal)
    val negDenominator = optionalNegOf(denominatorUnit)

    val productNumerator = commutativeProductContaining(negNumerator)
    val productDenominator = commutativeProductContaining(negDenominator)

    val numerator = oneOf(productNumerator, negNumerator)
    val denominator = oneOf(productDenominator, negDenominator)

    val pattern = fractionOf(numerator, denominator)

    onPattern(pattern) {
        if (getUnitType(numeratorUnit) != getUnitType(denominatorUnit)) {
            return@onPattern null
        }

        ruleResult(
            toExpr = fractionOf(
                if (isBound(productNumerator)) {
                    productNumerator.substitute(copySign(negNumerator, move(numeratorVal)))
                } else {
                    move(numeratorVal)
                },
                if (isBound(productDenominator)) {
                    productDenominator.substitute(copySign(negDenominator, move(denominatorVal)))
                } else {
                    move(denominatorVal)
                },
            ),
            explanation = metadata(Explanation.CancelOutUnitInFractionOfUnits),
        )
    }
}

/**
 * [unit[ k * c ] / c] -> unit[ k ]
 */
private val simplifyFractionOfUnitAndConstantToInteger = rule {
    val numerator = UnsignedIntegerPattern()
    val numeratorWrapper = UnitExpressionPattern(numerator)
    val denominator = UnsignedIntegerPattern()

    val frac = fractionOf(numeratorWrapper, denominator)

    onPattern(
        ConditionPattern(
            frac,
            integerCondition(numerator, denominator) { n, d -> d.divides(n) },
        ),
    ) {
        ruleResult(
            toExpr = combineTo(
                numerator,
                denominator,
                addUnit(numeratorWrapper, integerOp(numerator, denominator) { n, d -> n / d }),
            ),
            gmAction = tap(frac, PM.FractionBar),
            explanation = metadata(Explanation.SimplifyFractionOfUnitAndConstantToInteger),
        )
    }
}

/**
 * [ unit[ 30 ] / 14 ] -> [ 2 * unit[ 15 ] / 2 * 7 ]
 */
private val findCommonIntegerFactorInFractionOfUnitAndConstant = rule {
    val factorNumerator = UnsignedIntegerPattern()
    val factorNumeratorWrapper = UnitExpressionPattern(factorNumerator)
    val factorDenominator = UnsignedIntegerPattern()

    val productNumerator = productContaining(factorNumeratorWrapper)
    val productDenominator = productContaining(factorDenominator)

    val numerator = oneOf(factorNumeratorWrapper, productNumerator)
    val denominator = oneOf(factorDenominator, productDenominator)

    val frac = fractionOf(numerator, denominator)

    onPattern(
        ConditionPattern(
            frac,
            integerCondition(factorNumerator, factorDenominator) { n, d ->
                d != n && n.gcd(d) != BigInteger.ONE
            },
        ),
    ) {
        val gcd = integerOp(factorNumerator, factorDenominator) { n, d -> n.gcd(d) }
        val numeratorOverGcd = addUnit(
            factorNumeratorWrapper,
            integerOp(factorNumerator, factorDenominator) { n, d -> n / n.gcd(d) },
        )
        val denominatorOverGcd = integerOp(factorNumerator, factorDenominator) { n, d -> d / n.gcd(d) }

        ruleResult(
            toExpr = fractionOf(
                if (isBound(productNumerator)) {
                    productNumerator.substitute(simplifiedProductOf(gcd, numeratorOverGcd))
                } else {
                    productOf(gcd, numeratorOverGcd)
                },
                if (isBound(productDenominator)) {
                    productDenominator.substitute(simplifiedProductOf(gcd, denominatorOverGcd))
                } else {
                    productOf(gcd, denominatorOverGcd)
                },
            ),
            //
            gmAction = drag(factorDenominator, PM.Group, factorNumerator, PM.Group),
            explanation = metadata(Explanation.FindCommonFactorInFractionWithUnitAndConstant),
        )
    }
}

/**
 * unit[1.2] -> unit[12] / 10
 */
private val convertTerminatingDecimalWithUnitToFraction = rule {
    val decimal = UnsignedNumberPattern()
    val unitPattern = UnitExpressionPattern(decimal)

    onPattern(unitPattern) {
        val decimalValue = getValue(decimal)

        val scale = decimalValue.scale()
        when {
            scale > 0 -> {
                // E.g. if decimalValue = 6.75 then entireNumber = 675
                val entireNumber = decimalValue.scaleByPowerOfTen(scale).toBigInteger()
                val denominator = BigInteger.TEN.pow(scale)

                ruleResult(
                    toExpr = fractionOf(
                        addUnit(unitPattern, introduce(xp(entireNumber))),
                        introduce(xp(denominator)),
                    ),
                    gmAction = edit(decimal),
                    explanation = metadata(Explanation.ConvertTerminatingDecimalWithUnitToFraction),
                )
            }

            else -> null
        }
    }
}

private val evaluateSignedIntegerWithUnitAddition = rule {
    val term1 = UnsignedIntegerPattern()
    val term2 = UnsignedIntegerPattern()

    val unit1 = UnitExpressionPattern(term1)
    val unit2 = UnitExpressionPattern(term2)

    val signedWrapper1 = optionalNegOf(unit1)
    val signedWrapper2 = optionalNegOf(unit2)

    val sum = sumContaining(signedWrapper1, signedWrapper2)

    onPattern(sum) {
        if (getUnitType(unit1) != getUnitType(unit2)) {
            return@onPattern null
        }

        val explanation = when {
            !signedWrapper1.isWrapping() && signedWrapper2.isWrapping() ->
                metadata(
                    Explanation.EvaluateIntegerUnitSubtraction,
                    move(unit1),
                    addUnit(
                        unit1,
                        move(term2),
                    ),
                )
            else ->
                metadata(Explanation.EvaluateIntegerUnitAddition, move(unit1), move(unit2))
        }

        ruleResult(
            toExpr = sum.substitute(
                addUnit(
                    unit1,
                    integerOp(term1, term2) { n1, n2 ->
                        n1.let { if (signedWrapper1.isWrapping()) -it else it } +
                            n2.let { if (signedWrapper2.isWrapping()) -it else it }
                    },
                ),
            ),
            gmAction = drag(term2, PM.Group, term1, PM.Group),
            explanation = explanation,
        )
    }
}
