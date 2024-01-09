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

package methods.integerarithmetic

import engine.expressions.Constants
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.ConditionPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.divideBy
import engine.patterns.integerCondition
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.patterns.stickyOptionalNegOf
import engine.patterns.sumContaining
import engine.steps.metadata.metadata
import engine.steps.metadata.GmPathModifier as PM

private val MAX_POWER = 64.toBigInteger()

enum class IntegerArithmeticRules(override val runner: Rule) : RunnerMethod {
    EvaluateSignedIntegerAddition(
        rule {
            val term1 = SignedIntegerPattern()
            val term2 = SignedIntegerPattern()
            val sum = sumContaining(term1, term2)

            onPattern(sum) {
                val explanation = when {
                    !term1.isNeg() && term2.isNeg() ->
                        metadata(Explanation.EvaluateIntegerSubtraction, move(term1), move(term2.unsignedPattern))
                    else ->
                        metadata(Explanation.EvaluateIntegerAddition, move(term1), move(term2))
                }

                ruleResult(
                    toExpr = sum.substitute(integerOp(term1, term2) { n1, n2 -> n1 + n2 }),
                    gmAction = drag(term2, PM.Group, term1, PM.Group),
                    explanation = explanation,
                )
            }
        },
    ),

    EvaluateIntegerProductAndDivision(
        rule {
            val base = SignedIntegerPattern()
            val multiplier = SignedIntegerPattern()
            val divisor = SignedIntegerPattern()
            val product = productContaining(
                base,
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

                // if the result is 0, then no need for the sign
                val toExpr = if (res == Constants.Zero) {
                    product.substitute(res)
                } else {
                    copySign(optionalNegProduct, product.substitute(res))
                }

                val gmAction = if (isBound(multiplier)) {
                    drag(multiplier, PM.Group, base, PM.Group)
                } else {
                    drag(divisor, PM.Group, base, PM.Group)
                }

                val explanation = if (isBound(multiplier)) {
                    metadata(Explanation.EvaluateIntegerProduct, move(base), move(multiplier))
                } else {
                    metadata(Explanation.EvaluateIntegerDivision, move(base), move(divisor))
                }

                ruleResult(
                    toExpr = toExpr,
                    gmAction = gmAction,
                    explanation = explanation,
                )
            }
        },
    ),

    EvaluateIntegerPowerDirectly(
        rule {
            val base = SignedIntegerPattern()
            val exponent = integerCondition(UnsignedIntegerPattern()) { it <= MAX_POWER }
            val power = powerOf(base, exponent)

            onPattern(power) {
                ruleResult(
                    toExpr = integerOp(base, exponent) { n1, n2 -> n1.pow(n2.toInt()) },
                    gmAction = tap(exponent),
                    explanation = metadata(Explanation.EvaluateIntegerPowerDirectly, move(base), move(exponent)),
                )
            }
        },
    ),
}
