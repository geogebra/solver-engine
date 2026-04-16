/*
 * Copyright (c) 2024 GeoGebra GmbH, office@geogebra.org
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

package methods.logs

import engine.conditions.isDefinitelyNotPositive
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.IntegerExpression
import engine.expressions.Logarithm
import engine.expressions.logBase10Of
import engine.expressions.naturalLogOf
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.logOf
import engine.patterns.powerOf
import engine.patterns.productContaining
import engine.steps.metadata.metadata
import engine.utility.Power
import engine.utility.asKnownPower
import engine.utility.asPower
import engine.utility.asPowerOf

enum class LogsRules(override val runner: Rule) : RunnerMethod {
    TakePowerOutOfLog(takePowerOutOfLog),
    EvaluateLogOfBase(evaluateLogOfBase),
    EvaluateLogOfOne(evaluateLogOfOne),
    EvaluateLogOfNonPositiveAsUndefined(evaluateLogOfNonPositiveAsUndefined),
    EvaluateLogWithNonPositiveBaseAsUndefined(evaluateLogWithNonPositiveBaseAsUndefined),
    EvaluateLogWithBaseOneAsUndefined(evaluateLogWithBaseOneAsUndefined),
    SimplifyLogOfReciprocal(simplifyLogOfReciprocal),
    RewriteLogOfKnownPower(rewriteLogOfKnownPower),
    RewriteLogWithMatchingPowers(rewriteLogWithMatchingPowers),
    SplitLogOfProduct(splitLogOfProduct),
    SplitLogOfFraction(splitLogOfFraction),
    SimplifyLogWithCommonExponents(simplifyLogWithCommonExponents),
}

private val takePowerOutOfLog = rule {
    val base = AnyPattern()
    val exponent = AnyPattern()
    val expr = logOf(powerOf(base, exponent))

    onPattern(expr) {
        val logExpr = expression as Logarithm
        ruleResult(
            toExpr = productOf(move(exponent), logExpr.withArgument(move(base))),
            explanation = metadata(Explanation.TakePowerOutOfLog),
        )
    }
}

private val evaluateLogOfBase = rule {
    val base = AnyPattern()
    val expr = logOf(base, base)

    onPattern(expr) {
        ruleResult(
            toExpr = transform(expr, Constants.One),
            explanation = metadata(Explanation.EvaluateLogOfBase),
        )
    }
}

private val evaluateLogOfOne = rule {
    val expr = logOf(FixedPattern(Constants.One))

    onPattern(expr) {
        ruleResult(
            toExpr = transform(expr, Constants.Zero),
            explanation = metadata(Explanation.EvaluateLogOfOne),
        )
    }
}

private val evaluateLogOfNonPositiveAsUndefined = rule {
    val arg = condition { it.isDefinitelyNotPositive() }
    val expr = logOf(arg)

    onPattern(expr) {
        ruleResult(
            toExpr = transform(expr, Constants.Undefined),
            explanation = metadata(Explanation.EvaluateLogOfNonPositiveAsUndefined),
        )
    }
}

private val evaluateLogWithNonPositiveBaseAsUndefined = rule {
    val base = condition { it.isDefinitelyNotPositive() }
    val expr = logOf(AnyPattern(), base)

    onPattern(expr) {
        ruleResult(
            toExpr = transform(expr, Constants.Undefined),
            explanation = metadata(Explanation.EvaluateLogWithNonPositiveBaseAsUndefined),
        )
    }
}

private val evaluateLogWithBaseOneAsUndefined = rule {
    val base = FixedPattern(Constants.One)
    val expr = logOf(AnyPattern(), base)

    onPattern(expr) {
        ruleResult(
            toExpr = transform(expr, Constants.Undefined),
            explanation = metadata(Explanation.EvaluateLogWithBaseOne),
        )
    }
}

private val simplifyLogOfReciprocal = rule {
    val denominator = AnyPattern()
    val expr = logOf(fractionOf(FixedPattern(Constants.One), denominator))
    onPattern(expr) {
        val logExpr = expression as Logarithm
        ruleResult(
            toExpr = negOf(logExpr.withArgument(move(denominator))),
            explanation = metadata(Explanation.SimplifyLogOfReciprocal),
        )
    }
}

private val rewriteLogOfKnownPower = rule {
    val arg = UnsignedIntegerPattern()
    val base = AnyPattern()
    val expr = logOf(arg, base)

    onPattern(expr) {
        val argument = getValue(arg)

        val (a, n) = get(base).let {
            if (it is IntegerExpression) {
                val baseValue = it.value
                val exponentOfBase = argument.asPowerOf(baseValue)
                if (exponentOfBase != null) {
                    return@let Power(baseValue, exponentOfBase)
                }
            }
            getValue(arg).asKnownPower() ?: return@onPattern null
        }

        val logExpr = expression as Logarithm

        ruleResult(
            toExpr = logExpr.withArgument(transform(arg, powerOf(xp(a), xp(n)))),
            explanation = metadata(Explanation.RewriteLogOfKnownPower),
        )
    }
}

private val splitLogOfProduct = rule {
    val product = productContaining()
    val expr = logOf(product)

    onPattern(expr) {
        val logExpr = expression as Logarithm
        ruleResult(
            toExpr = sumOf(get(product).factors().map { logExpr.withArgument(it) }),
            explanation = metadata(Explanation.SplitLogOfProduct),
        )
    }
}

private val splitLogOfFraction = rule {
    val numerator = AnyPattern()
    val denominator = AnyPattern()
    val expr = logOf(fractionOf(numerator, denominator))

    onPattern(expr) {
        val logExpr = expression as Logarithm
        ruleResult(
            toExpr = sumOf(
                logExpr.withArgument(move(numerator)),
                negOf(logExpr.withArgument(move(denominator))),
            ),
            explanation = metadata(Explanation.SplitLogOfFraction),
        )
    }
}

private val simplifyLogWithCommonExponents = rule {
    val exponent = AnyPattern()
    val base1 = AnyPattern()
    val base2 = AnyPattern()

    val power1 = powerOf(base1, exponent)
    val power2 = powerOf(base2, exponent)

    val logarithm = logOf(
        power2,
        power1,
    )

    onPattern(logarithm) {
        ruleResult(
            toExpr = createLog(
                transform(power1, get(base1)),
                transform(power2, get(base2)),
            ),
            explanation = metadata(Explanation.SimplifyLogWithCommonExponents),
        )
    }
}

private val rewriteLogWithMatchingPowers =
    rule {
        val base = UnsignedIntegerPattern()
        val argument = UnsignedIntegerPattern()

        val expr = logOf(argument, base)

        onPattern(expr) {
            val basePowers = getValue(base).asPower()
            val argumentPowers = getValue(argument).asPower()

            val matchingPower = basePowers.map { it.exponent }
                .intersect(
                    argumentPowers.map { it.exponent }.toSet(),
                ).maxOrNull()

            if (matchingPower == null) {
                return@onPattern null
            }

            val baseBase = basePowers.find { it.exponent == matchingPower }?.base
            val argumentBase = argumentPowers.find { it.exponent == matchingPower }?.base

            if (baseBase == null || argumentBase == null) {
                return@onPattern null
            }

            ruleResult(
                toExpr = createLog(
                    transform(base, powerOf(xp(baseBase), xp(matchingPower))),
                    transform(argument, powerOf(xp(argumentBase), xp(matchingPower))),
                ),
                explanation = metadata(Explanation.RewriteLogUsingMatchingPowers),
            )
        }
    }

private fun switchLogBase(targetBase: Expression) =
    rule {
        val base = AnyPattern()
        val argument = AnyPattern()
        val expr = logOf(argument, base)

        onPattern(expr) {
            ruleResult(
                toExpr = engine.expressions.fractionOf(
                    createLog(targetBase, move(argument)),
                    createLog(targetBase, move(base)),
                ),
                explanation = metadata(
                    Explanation.SwitchBaseOfLogarithm,
                ),
            )
        }
    }

fun switchLogBaseRule(targetBase: Expression): RunnerMethod =
    object : RunnerMethod {
        override val name = "SwitchBaseOfLogarithm_$targetBase"

        override val runner = switchLogBase(targetBase)
    }

fun createLog(base: Expression, argument: Expression) =
    when (base) {
        Constants.E -> naturalLogOf(
            argument,
        )
        Constants.Ten -> logBase10Of(
            argument,
        )
        else -> engine.expressions.logOf(
            base,
            argument,
        )
    }
