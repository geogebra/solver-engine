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

package methods.solvable

import engine.context.Context
import engine.context.emptyContext
import engine.expressions.Comparison
import engine.expressions.Constants
import engine.expressions.Contradiction
import engine.expressions.Expression
import engine.expressions.ExpressionComparator
import engine.expressions.ExpressionWithConstraint
import engine.expressions.Identity
import engine.expressions.RootOrigin
import engine.expressions.SetExpression
import engine.expressions.SetSolution
import engine.expressions.Sum
import engine.expressions.VariableList
import engine.expressions.expressionWithConstraintOf
import engine.expressions.negOf
import engine.expressions.setSolutionOf
import engine.expressions.statementSystemOf
import engine.expressions.sumOf
import engine.patterns.AnyPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.productContaining
import engine.patterns.sumContaining
import engine.sign.Sign
import methods.constantexpressions.ConstantExpressionsPlans

fun computeOverallUnionSolution(solutions: List<Expression>): Expression? {
    val (extractedSolutions, constraints) = solutions.map {
        if (it is ExpressionWithConstraint) {
            it.firstChild to it.secondChild
        } else {
            it to null
        }
    }.unzip()

    val solutionSets = extractedSolutions.mapNotNull { solution ->
        when (solution) {
            // If one of solutions is an identity, then the overall solution is also an identity
            is Identity -> return solution
            // Contradictions are irrelevant in a union
            is Contradiction -> null
            // Select SetSolutions
            is SetSolution -> solution.solutionSet
            // Otherwise we cannot merge the solutions
            else -> return null
        }
    }

    val filteredConstraints = constraints.filterNotNull()

    val mergedConstraint = if (filteredConstraints.isNotEmpty()) {
        computeOverallIntersectionSolution(filteredConstraints)
    } else {
        null
    }

    val retVal = computeUnionOfSets(solutionSets)?.let {
        val solution = setSolutionOf(extractedSolutions[0].firstChild as VariableList, it)

        when (mergedConstraint) {
            null -> solution
            else -> expressionWithConstraintOf(solution, mergedConstraint)
        }
    }

    return retVal
}

fun computeOverallIntersectionSolution(solutions: List<Expression>): Expression {
    val processedSolutions = mutableListOf(solutions[0])

    for (solution in solutions.drop(1)) {
        var intersected = false
        for ((index, processedSolution) in processedSolutions.withIndex()) {
            val intersectedSolution = intersect(processedSolution, solution)
            if (intersectedSolution != null) {
                processedSolutions[index] = intersectedSolution
                intersected = true
                break
            }
        }

        if (!intersected) {
            processedSolutions.add(solution)
        }
    }

    return if (processedSolutions.size == 1) {
        processedSolutions[0]
    } else {
        statementSystemOf(processedSolutions)
    }
}

fun intersect(solution1: Expression, solution2: Expression): Expression? {
    return when {
        solution1 is Contradiction -> solution1
        solution2 is Contradiction -> solution2
        solution1 is Identity -> solution2
        solution2 is Identity -> solution1
        solution1 is SetSolution && solution2 is SetSolution ->
            if (solution1.solutionVariables == solution2.solutionVariables) {
                solution1.solutionSet.intersect(solution2.solutionSet, expressionComparator)?.let {
                    setSolutionOf(solution1.solutionVariables, it)
                }
            } else {
                statementSystemOf(
                    solution1,
                    solution2,
                )
            }
        else -> null
    }
}

fun computeUnionOfSets(sets: List<SetExpression>): Expression? {
    return when (sets.size) {
        0 -> Constants.EmptySet
        else -> sets.reduce { acc, set ->
            acc.union(set, expressionComparator) ?: return null
        }
    }
}

fun findUnusedVariableLetter(expression: Expression): String {
    val usedVariables = expression.variables

    return ('k'..'z').map(Char::toString).first { !usedVariables.contains(it) }
}

val expressionComparator = ExpressionComparator { e1: Expression, e2: Expression ->
    when {
        e1 == Constants.Infinity -> Sign.POSITIVE
        e1 == Constants.NegativeInfinity -> Sign.NEGATIVE
        e2 == Constants.NegativeInfinity -> Sign.POSITIVE
        e2 == Constants.Infinity -> Sign.NEGATIVE
        else -> {
            val diff = sumOf(e1, negOf(e2)).withOrigin(RootOrigin())
            val result = ConstantExpressionsPlans.SimplifyConstantExpression.tryExecute(emptyContext, diff)
            val simplifiedDiff = result?.toExpr ?: diff
            val signOfDiff = simplifiedDiff.signOf()
            if (signOfDiff.isKnown()) {
                signOfDiff
            } else {
                val d = simplifiedDiff.doubleValue
                when {
                    d > 0 -> Sign.POSITIVE
                    d < 0 -> Sign.NEGATIVE
                    d.toInt() == 0 -> Sign.ZERO
                    d.isNaN() -> Sign.NONE
                    else -> Sign.UNKNOWN
                }
            }
        }
    }
}

private val nonConstantSum = condition(sumContaining()) { !it.isConstantIn(solutionVariables) }
private val fractionRequiringMultiplication = optionalNegOf(
    oneOf(
        fractionOf(nonConstantSum, UnsignedIntegerPattern()),
        productContaining(
            fractionOf(AnyPattern(), UnsignedIntegerPattern()),
            nonConstantSum,
        ),
    ),
)

private fun Expression.isSumContainingFractionRequiringMultiplication(ctx: Context) =
    fractionRequiringMultiplication.matches(ctx, this) ||
        this is Sum && this.terms.any { fractionRequiringMultiplication.matches(ctx, it) }

/**
 * YES: [2/3](x+1) = [1/6](x+2)
 * YES: [x + 2 / 4] + 5 = 3x
 * YES: [x + 2 / 4] = [2x - 3 / 6]
 * YES: [x + 2 / 4] = 3x <== but show a different explanation since only one denominator is involved
 * YES: [2/3](x+1) = x+2 <== but show a different explanation since only one denominator is involved
 * YES: [x + 2 / 4] = 1 <== but show a different explanation since only one denominator is involved
 * NO: [2 / 3](x-1) = 1
 * NO: [2/3]x = [4/5]
 * */
fun requiresMultiplicationByTheLCD(expr: Expression, ctx: Context): Boolean {
    if (expr !is Comparison) return false

    // sum containing fraction with sum numerator and integer denominator
    return expr.lhs.isSumContainingFractionRequiringMultiplication(ctx) ||
        expr.rhs.isSumContainingFractionRequiringMultiplication(ctx)

    // exclude the case "constantFraction * linearVariableExpr = constant"
}
