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

package methods.integerrationalexponents

import engine.expressions.Constants
import engine.expressions.Fraction
import engine.expressions.Power
import engine.expressions.fractionOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.simplifiedFractionOf
import engine.expressions.simplifiedNegOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.simplifiedProductOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.IntegerFractionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.expressionWithFactor
import engine.patterns.fractionOf
import engine.patterns.integerCondition
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.sign.Sign
import engine.steps.metadata.Skill
import engine.steps.metadata.metadata
import engine.utility.divides
import engine.utility.isFactorizableUnderRationalExponent
import engine.utility.isPrime
import engine.utility.primeFactorDecomposition
import java.math.BigInteger

enum class IntegerRationalExponentsRules(override val runner: Rule) : RunnerMethod {
    EvaluateNegativeToRationalExponentAsUndefined(evaluateNegativeToRationalExponentAsUndefined),

    FactorizeIntegerUnderRationalExponent(factorizeIntegerUnderRationalExponent()),
    FactorizeIntegerUnderRationalExponentAlways(factorizeIntegerUnderRationalExponent(alwaysFactorize = true)),

    /**
     * brings the "integers" or exponents with integral powers to the front
     * e.g. [2 ^ [2 / 5]] * [3 ^ 2] * 5 --> [3 ^ 2] * 5 * [2 ^ [2 / 5]]
     */
    NormaliseProductWithRationalExponents(normaliseProductWithRationalExponents),

    FindCommonDenominatorOfRationalExponents(findCommonDenominatorOfRationalExponents),

    FactorDenominatorOfRationalExponents(factorDenominatorOfRationalExponents),

    ApplyReciprocalPowerRule(applyReciprocalPowerRule),
}

private val evaluateNegativeToRationalExponentAsUndefined = rule {
    val base = AnyPattern()

    val exponent = IntegerFractionPattern()
    val pattern = powerOf(
        condition(base) { it.signOf() == Sign.NEGATIVE },
        ConditionPattern(
            optionalNegOf(exponent),
            integerCondition(exponent.numerator, exponent.denominator) { n, d -> !d.divides(n) },
        ),
    )

    onPattern(pattern) {
        ruleResult(
            toExpr = transformTo(pattern, Constants.Undefined),
            explanation = metadata(Explanation.EvaluateNegativeToRationalExponentAsUndefined),
        )
    }
}

/**
 * [1 / a^n] -> a^(-n)
 */
private val applyReciprocalPowerRule = rule {
    val numerator = AnyPattern()

    val base = UnsignedIntegerPattern()
    val exp = optionalNegOf(IntegerFractionPattern())
    val power = powerOf(base, exp)
    val denominator = expressionWithFactor(power)

    val fraction = fractionOf(numerator, denominator)

    onPattern(fraction) {
        // only highlight the power as a transformation
        val newPower = transform(power, powerOf(get(base), simplifiedNegOf(get(exp))))
        val newNumerator = simplifiedProductOf(get(numerator), newPower)
        val result = simplifiedFractionOf(newNumerator, restOf(denominator))
        ruleResult(
            toExpr = result,
            explanation = metadata(Explanation.ApplyReciprocalPowerRule),
        )
    }
}

private fun factorizeIntegerUnderRationalExponent(alwaysFactorize: Boolean = false) =
    rule {
        val integer = integerCondition(UnsignedIntegerPattern()) { !it.isPrime() }
        val exp = IntegerFractionPattern()
        val signedExp = optionalNegOf(exp)
        val power = powerOf(integer, signedExp)

        onPattern(power) {
            val integerValue = getValue(integer)
            val expNum = getValue(exp.numerator)
            val expDen = getValue(exp.denominator)
            if (alwaysFactorize || integerValue.isFactorizableUnderRationalExponent(expNum, expDen)) {
                val primeFactorization = getValue(integer).primeFactorDecomposition()
                val factorized = primeFactorization
                    .map { (f, n) -> introduce(if (n == BigInteger.ONE) xp(f) else powerOf(xp(f), xp(n))) }

                ruleResult(
                    toExpr = powerOf(productOf(factorized), move(signedExp)),
                    explanation = metadata(Explanation.FactorizeIntegerUnderRationalExponent),
                    skills = listOf(metadata(Skill.FactorInteger, move(integer))),
                )
            } else {
                null
            }
        }
    }

private val normaliseProductWithRationalExponents = rule {
    val notRationalExponent = condition { it !is Power || it.exponent !is Fraction }
    val product = productContaining(
        powerOf(UnsignedIntegerPattern(), fractionOf(UnsignedIntegerPattern(), UnsignedIntegerPattern())),
        notRationalExponent,
    )
    onPattern(product) {
        val (rationalExponents, nonRationalExponents) = get(product).children
            .partition {
                it is Power && it.exponent is Fraction
            }
        ruleResult(
            toExpr = productOf(
                productOf(nonRationalExponents.map { move(it) }),
                productOf(rationalExponents.map { move(it) }),
            ),
            explanation = metadata(Explanation.NormaliseProductWithRationalExponents),
        )
    }
}

private val findCommonDenominatorOfRationalExponents = rule {
    val base1 = AnyPattern()
    val base2 = AnyPattern()

    val exponent1 = IntegerFractionPattern()
    val exponent2 = IntegerFractionPattern()

    val power1 = powerOf(base1, exponent1)
    val power2 = powerOf(base2, exponent2)

    val product = productContaining(power1, power2)
    val fraction = fractionOf(power1, power2)

    onPattern(oneOf(product, fraction)) {
        if (getValue(exponent1.denominator) == getValue(exponent2.denominator)) {
            null
        } else {
            val expandingTerm1 =
                integerOp(exponent1.denominator, exponent2.denominator) { n1, n2 -> n2 / n1.gcd(n2) }
            val expandingTerm2 =
                integerOp(exponent1.denominator, exponent2.denominator) { n1, n2 -> n1 / n1.gcd(n2) }

            val fraction1 = when (expandingTerm1) {
                Constants.One -> get(exponent1)
                else -> fractionOf(
                    productOf(get(exponent1.numerator), expandingTerm1),
                    productOf(get(exponent1.denominator), expandingTerm1),
                )
            }

            val fraction2 = when (expandingTerm2) {
                Constants.One -> get(exponent2)
                else -> fractionOf(
                    productOf(get(exponent2.numerator), expandingTerm2),
                    productOf(get(exponent2.denominator), expandingTerm2),
                )
            }

            val result = when {
                isBound(product) -> product.substitute(
                    powerOf(move(base1), fraction1),
                    powerOf(move(base2), fraction2),
                )

                else -> fractionOf(
                    powerOf(move(base1), fraction1),
                    powerOf(move(base2), fraction2),
                )
            }

            ruleResult(
                toExpr = result,
                explanation = metadata(Explanation.FindCommonDenominatorOfRationalExponents),
            )
        }
    }
}

private val factorDenominatorOfRationalExponents = rule {
    val base1 = AnyPattern()
    val base2 = AnyPattern()

    val numerator1 = UnsignedIntegerPattern()
    val numerator2 = UnsignedIntegerPattern()

    val denominator = UnsignedIntegerPattern()

    val exponent1 = fractionOf(numerator1, denominator)
    val exponent2 = fractionOf(numerator2, denominator)

    val power1 = powerOf(base1, exponent1)
    val power2 = powerOf(base2, exponent2)

    val product = productContaining(power1, power2)
    val fraction = fractionOf(power1, power2)

    onPattern(oneOf(product, fraction)) {
        val newPower1 = simplifiedPowerOf(move(base1), move(numerator1))
        val newPower2 = simplifiedPowerOf(move(base2), move(numerator2))
        val newExponent = fractionOf(introduce(Constants.One), factor(denominator))

        val result = when {
            isBound(product) -> product.substitute(
                powerOf(
                    productOf(newPower1, newPower2),
                    newExponent,
                ),
            )
            else -> powerOf(fractionOf(newPower1, newPower2), newExponent)
        }

        ruleResult(
            toExpr = result,
            explanation = metadata(Explanation.FactorDenominatorOfRationalExponents),
        )
    }
}
