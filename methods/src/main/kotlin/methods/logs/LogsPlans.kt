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

import engine.context.Context
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.ExpressionWithConstraint
import engine.expressions.Fraction
import engine.expressions.Logarithm
import engine.expressions.Minus
import engine.expressions.Product
import engine.expressions.Sum
import engine.expressions.asDecimal
import engine.expressions.asInteger
import engine.methods.CompositeMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.patterns.AnyPattern
import engine.patterns.logOf
import engine.steps.Transformation
import engine.steps.metadata.metadata
import methods.general.GeneralRules
import java.math.BigDecimal

enum class LogsPlans(override val runner: CompositeMethod) : RunnerMethod {
    SimplifyLogOfKnownPower(simplifyLogOfKnownPower),
    SimplifyLogWithMatchingPowers(simplifyLogWithMatchingPowers),
    ExpandLogNotMatchingBase(expandLogNotMatchingBase),
}

private val simplifyLogOfKnownPower = plan {
    explanation = Explanation.SimplifyLogOfKnownPower

    steps {
        apply(LogsRules.RewriteLogOfKnownPower)
        apply(LogsRules.TakePowerOutOfLog)
    }
}

private val simplifyLogWithMatchingPowers = plan {
    explanation = Explanation.SimplifyLogWithMatchingPowers

    steps {
        apply(LogsRules.RewriteLogWithMatchingPowers)
        apply(LogsRules.SimplifyLogWithCommonExponents)
    }
}

private val expandLogNotMatchingBase = plan {
    explanation = Explanation.ExpandLogNotMatchingBase

    pattern = logOf(AnyPattern())

    steps {
        applyTo(GeneralRules.FactorizeInteger) {
            it.extractLogarithmArgument()
        }

        apply(LogsRules.SplitLogOfProduct)
    }
}

private fun Expression.extractLogarithmArgument(): Expression? =
    if (this !is Logarithm) {
        null
    } else if (childCount == 2) {
        secondChild
    } else {
        // In case of natural log or log base 10 we have special log types (log [] and ln []) which don't have the
        // base as a child
        firstChild
    }

/**
 * Check the bases of logarithms in a sum, if they are different, reduce to the smallest
 */
fun createSwitchLogsToSmallestBase(simplificationSteps: StepsProducer) =
    object : CompositeMethod() {
        // I think having more return statements is nicer here than trying to force a lower number
        @Suppress("ReturnCount")
        override fun run(ctx: Context, sub: Expression): Transformation? {
            val expression = if (sub is ExpressionWithConstraint) sub.expression else sub
            val sum = expression as? Sum ?: return null
            val terms = sum.terms
            val logTerms = terms.mapNotNull { it.extractLogTerm() }
            if (logTerms.map { it.base }.distinct().size < 2) return null

            val targetBase = smallestLogBase(logTerms) ?: return null

            val switchRule = switchLogBaseRule(targetBase)

            val switchSteps = plan {
                explanation = Explanation.SwitchBaseOfLogarithmAndSimplify

                explanationParameters {
                    listOf(targetBase)
                }

                steps {
                    deeply(switchRule)

                    whilePossible(simplificationSteps)
                }
            }

            val steps = mutableListOf<Transformation>()
            var currentExpression = expression
            for (term in terms) {
                val logTerm = term.extractLogTerm()
                if (logTerm?.base == targetBase) continue

                switchSteps.run(ctx, term)?.let {
                    steps.add(it)
                    currentExpression = currentExpression.substituteAllOccurrences(term, it.toExpr)
                }
            }

            if (steps.isEmpty()) return null

            return Transformation(
                type = Transformation.Type.Plan,
                fromExpr = expression,
                toExpr = currentExpression,
                steps = steps,
                explanation = metadata(Explanation.BringLogsToCommonBase, targetBase),
            )
        }
    }

private fun smallestLogBase(logTerms: List<LogOccurrence>): Expression? {
    val bases = logTerms.map { it.base }.distinct()
    val comparableBases = bases.mapNotNull { base -> base.sortKey()?.let { it to base } }
    if (comparableBases.size != bases.size) return null

    return comparableBases.minBy { it.first }.second
}

private data class LogOccurrence(val term: Expression, val log: Logarithm) {
    val base get() = log.base
}

private fun Expression.extractLogTerm(): LogOccurrence? {
    return when (this) {
        is Logarithm -> LogOccurrence(this, this)
        is Product -> {
            val logFactor = factors().singleOrNull { it is Logarithm } as? Logarithm ?: return null
            LogOccurrence(this, logFactor)
        }
        is Fraction -> {
            val logChildren = children.mapNotNull { it.extractLogTerm() }
            logChildren.singleOrNull()
        }
        is Minus -> firstChild.extractLogTerm()
        else -> null
    }
}

private fun Expression.sortKey(): BigDecimal? =
    when {
        this == Constants.E -> BigDecimal.valueOf(Math.E)
        this == Constants.Pi -> BigDecimal.valueOf(Math.PI)
        else -> asDecimal() ?: asInteger()?.toBigDecimal()
    }
