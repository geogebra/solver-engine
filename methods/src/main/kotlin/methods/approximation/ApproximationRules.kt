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

package methods.approximation

import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.RecurringDecimalPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.UnsignedNumberPattern
import engine.patterns.divideBy
import engine.patterns.integerCondition
import engine.patterns.numericCondition
import engine.patterns.oneOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.steps.metadata.metadata
import engine.steps.metadata.GmPathModifier as PM

private val MAX_POWER = 64.toBigInteger()

enum class ApproximationRules(override val runner: Rule) : RunnerMethod {
    RoundTerminatingDecimal(
        rule {
            val decimal = UnsignedNumberPattern()

            onPattern(decimal) {
                val value = getValue(decimal)
                when {
                    value.scale() <= context.effectivePrecision -> null
                    else -> ruleResult(
                        toExpr = numericOp(decimal) { round(it) },
                        gmAction = edit(decimal),
                        explanation = metadata(
                            Explanation.RoundTerminatingDecimal,
                            move(decimal),
                            introduce(xp(context.effectivePrecision)),
                        ),
                    )
                }
            }
        },
    ),

    ExpandRecurringDecimal(
        rule {
            val recurringDecimal = RecurringDecimalPattern()

            onPattern(recurringDecimal) {
                val value = getValue(recurringDecimal)
                when {
                    // we need at least one more decimal digit than what the precision requires
                    value.decimalDigits > context.effectivePrecision -> null
                    else -> ruleResult(
                        toExpr = transformTo(recurringDecimal, xp(value.expand(context.effectivePrecision + 1))),
                        gmAction = edit(recurringDecimal),
                        explanation = metadata(
                            Explanation.ExpandRecurringDecimal,
                            move(recurringDecimal),
                            introduce(xp(context.effectivePrecision)),
                        ),
                    )
                }
            }
        },
    ),

    RoundRecurringDecimal(
        rule {
            val recurringDecimal = RecurringDecimalPattern()

            onPattern(recurringDecimal) {
                val value = getValue(recurringDecimal)
                when {
                    // we need at least one more decimal digit than what the precision requires
                    value.decimalDigits <= context.effectivePrecision -> null
                    else -> ruleResult(
                        toExpr = transformTo(
                            recurringDecimal,
                            xp(round(value.nonRepeatingValue)),
                        ),
                        gmAction = edit(recurringDecimal),
                        explanation = metadata(
                            Explanation.RoundRecurringDecimal,
                            move(recurringDecimal),
                            introduce(xp(context.effectivePrecision)),
                        ),
                    )
                }
            }
        },
    ),

    ApproximateDecimalProductAndDivision(
        rule {
            val base = SignedNumberPattern()
            val multiplier = SignedNumberPattern()
            val divisor = SignedNumberPattern()
            val product = productContaining(
                base,
                oneOf(
                    multiplier,
                    divideBy(
                        numericCondition(divisor) { it.signum() != 0 },
                    ),
                ),
            )

            onPattern(product) {
                when {
                    isBound(multiplier) -> ruleResult(
                        toExpr = product.substitute(
                            numericOp(base, multiplier) { n1, n2 -> round(n1 * n2) },
                        ),
                        gmAction = drag(multiplier, PM.Group, base, PM.Group),
                        explanation = metadata(
                            Explanation.ApproximateDecimalProduct,
                            move(base),
                            move(multiplier),
                            introduce(xp(context.effectivePrecision)),
                        ),
                    )

                    else -> ruleResult(
                        toExpr = product.substitute(
                            numericOp(base, divisor) { n1, n2 -> round(n1) / n2 },
                        ),
                        gmAction = drag(divisor, PM.Group, base, PM.Group),
                        explanation = metadata(
                            Explanation.ApproximateDecimalDivision,
                            move(base),
                            move(divisor),
                            introduce(xp(context.effectivePrecision)),
                        ),
                    )
                }
            }
        },
    ),

    ApproximateDecimalPower(
        rule {
            val base = SignedNumberPattern()
            val exponent = integerCondition(UnsignedIntegerPattern()) { it <= MAX_POWER }
            val power = powerOf(base, exponent)

            onPattern(power) {
                ruleResult(
                    toExpr = numericOp(base, exponent) { n1, n2 -> round(n1.pow(n2.toInt())) },
                    gmAction = tap(exponent),
                    explanation = metadata(
                        Explanation.ApproximateDecimalPower,
                        move(base),
                        move(exponent),
                        introduce(xp(context.effectivePrecision)),
                    ),
                )
            }
        },
    ),
}
