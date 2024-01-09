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

package engine.methods

import engine.context.Context
import engine.expressionbuilder.MappedExpressionBuilder
import engine.expressions.Expression
import engine.expressions.ExpressionWithConstraint
import engine.patterns.Match
import engine.patterns.Pattern
import engine.patterns.RootMatch
import engine.patterns.equationOf
import engine.steps.Task
import engine.steps.Transformation
import engine.steps.metadata.GmAction
import engine.steps.metadata.Metadata

class Rule(
    val pattern: Pattern,
    val transformation: RuleResultBuilder.() -> Transformation?,
) : Runner {
    override fun run(ctx: Context, sub: Expression): Transformation? {
        val expression = if (sub is ExpressionWithConstraint) sub.expression else sub

        for (match in pattern.findMatches(ctx, RootMatch, expression)) {
            val builder = RuleResultBuilder(ctx, expression, match)
            builder.transformation()?.let {
                return it
            }
        }
        return null
    }

    override val minDepth = pattern.minDepth
}

class RuleBuilder {
    fun onPattern(pattern: Pattern, result: RuleResultBuilder.() -> Transformation?): Rule = Rule(pattern, result)

    fun onEquation(lhs: Pattern, rhs: Pattern, result: RuleResultBuilder.() -> Transformation?): Rule =
        Rule(
            equationOf(lhs, rhs),
            result,
        )
}

class RuleResultBuilder(ctx: Context, expression: Expression, match: Match) :
    MappedExpressionBuilder(ctx, expression, match) {
    @Suppress("LongParameterList")
    fun ruleResult(
        toExpr: Expression,
        explanation: Metadata,
        formula: Expression? = null,
        steps: List<Transformation>? = null,
        tasks: List<Task>? = null,
        skills: List<Metadata>? = null,
        gmAction: GmAction? = null,
        tags: List<Transformation.Tag>? = null,
    ) = Transformation(
        type = Transformation.Type.Rule,
        tags = tags,
        fromExpr = expression,
        toExpr = toExpr,
        steps = steps,
        tasks = tasks,
        explanation = explanation,
        formula = formula,
        skills = skills,
        gmAction = gmAction,
    )
}

fun rule(init: RuleBuilder.() -> Rule): Rule {
    val builder = RuleBuilder()
    return builder.init()
}
