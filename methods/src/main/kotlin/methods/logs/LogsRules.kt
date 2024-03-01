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
import engine.expressions.Logarithm
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
import engine.utility.asKnownPower

enum class LogsRules(override val runner: Rule) : RunnerMethod {
    TakePowerOutOfLog(takePowerOutOfLog),
    EvaluateLogOfBase(evaluateLogOfBase),
    EvaluateLogOfOne(evaluateLogOfOne),
    EvaluateLogOfNonPositiveAsUndefined(evaluateLogOfNonPositiveAsUndefined),
    SimplifyLogOfReciprocal(simplifyLogOfReciprocal),
    RewriteLogOfKnownPower(rewriteLogOfKnownPower),
    SplitLogOfProduct(splitLogOfProduct),
    SplitLogOfFraction(splitLogOfFraction),
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
    val expr = logOf(arg)

    onPattern(expr) {
        val (a, n) = getValue(arg).asKnownPower() ?: return@onPattern null
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
