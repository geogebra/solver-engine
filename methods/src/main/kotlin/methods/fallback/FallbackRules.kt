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

package methods.fallback

import engine.expressions.VoidExpression
import engine.methods.Rule
import engine.methods.RunnerMethod
import engine.methods.rule
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.ConstantPattern
import engine.patterns.condition
import engine.patterns.monomialPattern
import engine.patterns.oneOf
import engine.patterns.sumContaining
import engine.steps.metadata.metadata

enum class FallbackRules(override val runner: Rule) : RunnerMethod {
    ExpressionIsFullySimplified(
        rule {
            val variablePattern = ArbitraryVariablePattern()
            val fullySimplifiedPtn = oneOf(
                // Any constant expression, if SimplifyConstantExpression didn't apply then it means it's simplify
                ConstantPattern(),
                // A monomial with a constant coefficient, the coefficient must be simplified otherwise
                // SimplifyAlgebraicExpressionInOneVariable would have applied.
                monomialPattern(variablePattern),
                // A sum of constant expressions and monomials of degree 1 in the same variable
                condition(sumContaining(monomialPattern(variablePattern))) {
                    it.children.all { child ->
                        child.isConstant() || monomialPattern(variablePattern).matches(this, child)
                    }
                },
            )
            onPattern(fullySimplifiedPtn) {
                ruleResult(
                    toExpr = VoidExpression(),
                    explanation = metadata(Explanation.ExpressionIsFullySimplified),
                )
            }
        },
    ),
}
