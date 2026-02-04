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

package engine.expressions

import engine.operators.SetOperators
import engine.operators.SolutionOperator

interface Solution {
    val solutionVariables: VariableList
    val solutionVariable get() = solutionVariables.variableExpressions.single().variableName
}

class Identity(
    solutionVariables: VariableList,
    identityExpression: Expression,
    meta: NodeMeta = BasicMeta(),
) : Solution, Expression(SolutionOperator.Identity, listOf(solutionVariables, identityExpression), meta) {
    override val solutionVariables = firstChild as VariableList
    val identityExpression = secondChild
}

class Contradiction(
    solutionVariables: VariableList,
    contradictionExpression: Expression,
    meta: NodeMeta = BasicMeta(),
) : Solution, Expression(SolutionOperator.Contradiction, listOf(solutionVariables, contradictionExpression), meta) {
    override val solutionVariables = firstChild as VariableList
    val contradictionExpression = secondChild
}

class ImplicitSolution(
    solutionVariables: VariableList,
    implicitRelation: Expression,
    meta: NodeMeta = BasicMeta(),
) : Solution, Expression(SolutionOperator.ImplicitSolution, listOf(solutionVariables, implicitRelation), meta) {
    override val solutionVariables = firstChild as VariableList
    val implicitRelation = secondChild
}

class SetSolution(
    solutionVariables: VariableList,
    solutionSet: Expression,
    meta: NodeMeta = BasicMeta(),
) : Solution, Expression(SolutionOperator.SetSolution, listOf(solutionVariables, solutionSet), meta) {
    override val solutionVariables = firstChild as VariableList

    val solutionSet = secondChild as SetExpression
    val solution get() = secondChild.children.single()

    /**
     * Turn a unique solution into an equation x = k
     */
    fun asEquation(): Expression? {
        val variable = when (firstChild.childCount) {
            1 -> firstChild.firstChild
            else -> return null
        }

        return when (secondChild.operator) {
            SetOperators.FiniteSet -> {
                if (secondChild.childCount == 1) equationOf(variable, secondChild.firstChild) else null
            }
            else -> null
        }
    }

    fun asEquationList(): List<Expression>? {
        val variable = when (firstChild.childCount) {
            1 -> firstChild.firstChild
            else -> return null
        }

        return when (secondChild.operator) {
            SetOperators.FiniteSet -> secondChild.children.map {
                equationOf(variable, it)
            }
            else -> null
        }
    }
}
