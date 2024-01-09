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

package engine.patterns

import engine.context.Context
import engine.expressions.Expression
import engine.expressions.Variable

abstract class VariablePattern : BasePattern() {
    fun getBoundSymbol(m: Match): String {
        return when (val expression = m.getBoundExpr(this)) {
            is Variable -> expression.variableName
            else -> throw InvalidMatch("Variable pattern matched to $expression")
        }
    }
}

/**
 * A pattern to match with any variable (i.e. symbol)
 */
class ArbitraryVariablePattern : VariablePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return when (subexpression) {
            is Variable -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

class SolutionVariablePattern : VariablePattern() {
    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        return if (subexpression is Variable && subexpression.variableName in context.solutionVariables) {
            sequenceOf(match.newChild(this, subexpression))
        } else {
            emptySequence()
        }
    }
}
