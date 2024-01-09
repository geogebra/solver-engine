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

package methods.equations

import engine.expressions.Contradiction
import engine.expressions.Equation
import engine.expressions.Expression
import engine.expressions.FiniteSet
import engine.expressions.Identity
import engine.expressions.Inequality
import engine.expressions.SetExpression
import engine.expressions.SetSolution
import engine.expressions.Variable
import engine.expressions.equationOf
import engine.expressions.setSolutionOf
import engine.expressions.statementSystemOf
import engine.methods.TasksBuilder
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.AnyPattern
import engine.patterns.absoluteValueOf
import engine.patterns.condition
import engine.patterns.equationOf
import engine.patterns.inSolutionVariables
import engine.patterns.oneOf
import engine.patterns.statementSystemOf
import engine.patterns.withOptionalConstantCoefficient
import engine.steps.Task
import engine.steps.metadata.metadata
import methods.inequalities.InequalitiesPlans
import methods.inequalities.solveConstantInequalitySteps
import methods.solvable.expressionComparator

/**
 * Solves a system consisting of an equation and an inequality (constraint)
 *    e.g. [x ^ 2] = 3 AND x > 0
 */
internal val solveEquationWithInequalityConstraint = taskSet {
    explanation = Explanation.SolveEquation

    val equation = condition { it is Equation }
    val inequality = condition { it is Inequality }

    pattern = inSolutionVariables(
        oneOf(
            statementSystemOf(equation, inequality),
            statementSystemOf(inequality, equation),
        ),
    )

    val simplifyConstraintSteps = steps {
        firstOf {
            option(InequalitiesPlans.SolveLinearInequality)
            option(InequalitiesPlans.SimplifyInequality)
        }
    }

    tasks {
        val simplifyConstraint = task(
            startExpr = get(inequality),
            explanation = metadata(Explanation.SimplifyConstraint),
            stepsProducer = simplifyConstraintSteps,
        )

        val solveEquation = task(
            startExpr = get(equation),
            explanation = metadata(Explanation.SolveEquationWithoutConstraint),
            stepsProducer = EquationsPlans.SolveEquation,
        ) ?: return@tasks null

        val solution = solveEquation.result
        val constraint = simplifyConstraint?.result ?: get(inequality)

        checkSolutionsAgainstConstraint(solution, constraint) ?: return@tasks null

        allTasks()
    }
}

/**
 * This has been put in this file so [checkSolutionsAgainstConstraint] can be called.
 */
internal val solveEquationWithOneAbsoluteValueBySubstitution = taskSet {
    val signedLHS = AnyPattern()
    val absoluteValue = absoluteValueOf(signedLHS)
    val lhs = withOptionalConstantCoefficient(absoluteValue, positiveOnly = true)
    val rhs = AnyPattern()

    pattern = equationOf(lhs, rhs)
    explanation = Explanation.SolveEquationWithOneAbsoluteValueBySubstitution

    val solveWithoutConstraintSteps = steps {
        apply(EquationsRules.SeparateModulusEqualsExpressionWithoutConstraint)
        apply(EquationsPlans.SolveEquationUnion)
    }
    tasks {
        val solveWithoutConstraint = task(
            startExpr = expression,
            explanation = metadata(Explanation.SplitEquationWithAbsoluteValueAndSolve),
            stepsProducer = solveWithoutConstraintSteps,
        ) ?: return@tasks null

        checkSolutionsAgainstConstraint(solveWithoutConstraint.result, expression) ?: return@tasks null

        allTasks()
    }
}

/**
 * Creates some tasks that, if necessary, filter down the [solution] values to the ones which are valid according
 * to the [constraint].  Returns null if it fails to do it.
 */
fun TasksBuilder.checkSolutionsAgainstConstraint(solution: Expression, constraint: Expression): Task? {
    return when {
        constraint is Identity || solution is Contradiction -> {
            task(
                startExpr = solution,
                explanation = metadata(Explanation.GatherSolutionsAndConstraint),
            )
        }
        solution is Identity || constraint is Contradiction -> { // todo move contradiction up
            task(
                startExpr = constraint,
                explanation = metadata(Explanation.GatherSolutionsAndConstraint),
            )
        }
        solution is SetSolution -> {
            computeValidSetSolution(solution, constraint)?.let { reportValidSolutions(solution, constraint, it) }
        }
        else -> null
    }
}

/**
 * Given a [solution] which is a set, compute the valid solution (restricted by the [constraint] which can be any
 * expression) and return it.  If it cannot be computed, null is returned.
 *
 * This is only partially implemented but covered the currently needed cases.  It can be extended in the future.
 */
private fun TasksBuilder.computeValidSetSolution(solution: SetSolution, constraint: Expression): SetExpression? {
    return when (constraint) {
        is SetSolution -> {
            solution.solutionSet.intersect(constraint.solutionSet, expressionComparator)
        }
        is Inequality -> {
            computeValidSetSolutionForInequalityConstraint(solution, constraint)
        }
        is Equation -> {
            computeValidSetSolutionForEquationConstraint(solution, constraint)
        }
        else -> null
    }
}

/**
 * Given a [solution] which is a set, computes the valid solutions (restricted by the [constraint] which is an
 * inequality) by substituting them into the inequality and evaluating the inequality. If it cannot be computed, null is
 * returned.
 *
 * Currently, it only supports a solution which is a [FiniteSet].  This can be extended in the future although that
 * seems hard.
 */
private fun TasksBuilder.computeValidSetSolutionForInequalityConstraint(
    solution: SetSolution,
    constraint: Inequality,
): SetExpression? {
    return when (val solutionSet = solution.solutionSet) {
        is FiniteSet -> {
            val validSolutions = mutableListOf<Expression>()
            val variable = Variable(solution.solutionVariable)
            for (element in solutionSet.elements) {
                val constraintForElement = constraint.substituteAllOccurrences(variable, element)
                val simplifyConstraint = task(
                    startExpr = constraintForElement,
                    explanation = metadata(Explanation.CheckIfSolutionSatisfiesConstraint, element),
                    stepsProducer = solveConstantInequalitySteps,
                    context = context.copy(precision = 10, solutionVariables = emptyList()),
                ) ?: return null
                val constraintSatisfied = when (simplifyConstraint.result) {
                    is Contradiction -> false
                    is Identity -> true
                    else -> null
                } ?: return null
                if (constraintSatisfied) {
                    validSolutions.add(element)
                }
            }
            FiniteSet(validSolutions)
        }
        else -> null
    }
}

private fun TasksBuilder.computeValidSetSolutionForEquationConstraint(
    solution: SetSolution,
    constraint: Equation,
): SetExpression? {
    return when (val solutionSet = solution.solutionSet) {
        is FiniteSet -> {
            val validSolutions = mutableListOf<Expression>()
            val variable = Variable(solution.solutionVariable)
            for (element in solutionSet.elements) {
                val constraintForElement = constraint.substituteAllOccurrences(variable, element)
                val simplifyConstraint = task(
                    startExpr = constraintForElement,
                    explanation = metadata(
                        Explanation.CheckIfSolutionSatisfiesConstraint,
                        equationOf(variable, element),
                    ),
                    stepsProducer = solveConstantEquationSteps,
                    context = context.copy(solutionVariables = emptyList()),
                ) ?: return null
                val constraintSatisfied = when (simplifyConstraint.result) {
                    is Contradiction -> false
                    is Identity -> true
                    else -> null
                } ?: return null
                if (constraintSatisfied) {
                    validSolutions.add(element)
                }
            }
            FiniteSet(validSolutions)
        }
        else -> null
    }
}

/**
 * Creates a task to report the [validSolutions] inferred from [solution] and [constraint].  Returns null if that
 * cannot be done.
 */
private fun TasksBuilder.reportValidSolutions(
    solution: SetSolution,
    constraint: Expression,
    validSolutions: SetExpression,
): Task? {
    return when {
        validSolutions.isEmpty(expressionComparator) ?: return null -> {
            task(
                startExpr = Contradiction(solution.solutionVariables, statementSystemOf(solution, constraint)),
                explanation = metadata(Explanation.NoSolutionSatisfiesConstraint),
            )
        }
        solution.solutionSet == validSolutions -> {
            task(
                startExpr = solution,
                explanation = metadata(Explanation.AllSolutionsSatisfyConstraint),
            )
        }
        else -> {
            task(
                startExpr = setSolutionOf(solution.solutionVariables, validSolutions),
                explanation = metadata(Explanation.SomeSolutionsDoNotSatisfyConstraint),
            )
        }
    }
}
