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

package methods.algebra

import engine.expressions.Constants
import engine.expressions.Contradiction
import engine.expressions.DivideBy
import engine.expressions.Expression
import engine.expressions.ExpressionWithConstraint
import engine.expressions.Fraction
import engine.expressions.Identity
import engine.expressions.ListExpression
import engine.expressions.Logarithm
import engine.expressions.Product
import engine.expressions.SetSolution
import engine.expressions.ValueExpression
import engine.expressions.greaterThanOf
import engine.expressions.inequationOf
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.condition
import engine.steps.metadata.metadata
import methods.inequalities.InequalitiesPlans
import methods.inequations.InequationsPlans
import methods.simplify.SimplifyExplanation
import methods.simplify.simplifyAlgebraicExpressionSteps
import methods.solvable.computeOverallIntersectionSolution

enum class AlgebraPlans(override val runner: CompositeMethod) : RunnerMethod {
    /**
     * Compute the domain of an algebraic expression then simplify it.
     */
    @PublicMethod
    ComputeDomainAndSimplifyAlgebraicExpression(
        taskSet {
            explanation = Explanation.ComputeDomainAndSimplifyAlgebraicExpression
            pattern = condition { it is ValueExpression }

            tasks {
                val domainComputationTask = task(
                    startExpr = expression,
                    explanation = metadata(Explanation.ComputeDomainOfAlgebraicExpression),
                    stepsProducer = ComputeDomainOfAlgebraicExpression,
                ) ?: return@tasks null

                // In case the domain is a contradiction we don't need to solve it, and can fall back to
                // the ComputeDomainOfAlgebraicExpression method
                if (
                    domainComputationTask.result == Constants.Reals ||
                    domainComputationTask.result is Contradiction
                ) {
                    return@tasks allTasks()
                }

                val simplificationTask = task(
                    startExpr = expression,
                    explanation = metadata(SimplifyExplanation.SimplifyAlgebraicExpression),
                    stepsProducer = simplifyAlgebraicExpressionSteps,
                ) ?: return@tasks null

                task(
                    startExpr = ExpressionWithConstraint(simplificationTask.result, domainComputationTask.result),
                    explanation = metadata(Explanation.CombineSimplifiedExpressionWithConstraint),
                )

                allTasks()
            }
        },
    ),

    @PublicMethod
    ComputeDomainOfAlgebraicExpression(
        taskSet {
            explanation = Explanation.ComputeDomainOfAlgebraicExpression
            specificPlans(ComputeDomainAndSimplifyAlgebraicExpression)

            val solveInequationDomainConstraintSteps = steps {
                inContext({ copy(solutionVariables = it.variables.toList()) }) {
                    apply(InequationsPlans.SolveInequation)
                }
            }

            val solveInequalityDomainConstraintSteps = steps {
                inContext({ copy(solutionVariables = it.variables.toList()) }) {
                    firstOf {
                        option(InequalitiesPlans.SolveLinearInequality)
                        option(InequalitiesPlans.SolveQuadraticInequality)
                    }
                }
            }

            tasks {
                val denominatorsAndDivisors = findDenominatorsAndDivisors(expression)
                    .filter { (denominatorOrDivisor, _) -> !denominatorOrDivisor.isConstant() }
                    .groupBy(
                        keySelector = { (denominatorOrDivisor, _) -> denominatorOrDivisor.removeBrackets() },
                        valueTransform = { (_, inExpression) -> inExpression },
                    )

                val logConstraints = findLogarithmDomainConstraints(expression)
                    .filter { (domainExpression, _) -> !domainExpression.isConstant() }
                    .groupBy(
                        keySelector = { (domainExpression, kind) -> Pair(domainExpression.removeBrackets(), kind) },
                        valueTransform = { (_, _, inExpression) -> inExpression },
                    )

                if (denominatorsAndDivisors.isEmpty() && logConstraints.isEmpty()) return@tasks null

                val constraintTasks = denominatorsAndDivisors.map { (denominatorOrDivisor, inExpressions) ->
                    val explanation = if (inExpressions.size == 1) {
                        metadata(
                            Explanation.ExpressionMustNotBeZero,
                            denominatorOrDivisor,
                            inExpressions[0],
                        )
                    } else {
                        metadata(
                            Explanation.ExpressionMustNotBeZeroPlural,
                            denominatorOrDivisor,
                            ListExpression(inExpressions),
                        )
                    }

                    taskWithOptionalSteps(
                        startExpr = inequationOf(denominatorOrDivisor, Constants.Zero),
                        stepsProducer = solveInequationDomainConstraintSteps,
                        explanation = explanation,
                    )
                } + logConstraints.map { entry ->
                    val (key, inExpressions) = entry
                    val (domainExpression, kind) = key

                    val explanation = when (kind) {
                        LogDomainConstraintKind.Argument -> if (inExpressions.size == 1) {
                            metadata(
                                Explanation.LogArgumentMustBePositive,
                                domainExpression,
                                inExpressions[0],
                            )
                        } else {
                            metadata(
                                Explanation.LogArgumentMustBePositivePlural,
                                domainExpression,
                                ListExpression(inExpressions),
                            )
                        }

                        LogDomainConstraintKind.BasePositive -> if (inExpressions.size == 1) {
                            metadata(
                                Explanation.LogBaseMustBePositive,
                                domainExpression,
                                inExpressions[0],
                            )
                        } else {
                            metadata(
                                Explanation.LogBaseMustBePositivePlural,
                                domainExpression,
                                ListExpression(inExpressions),
                            )
                        }

                        LogDomainConstraintKind.BaseNotOne -> if (inExpressions.size == 1) {
                            metadata(
                                Explanation.LogBaseMustNotEqualOne,
                                domainExpression,
                                inExpressions[0],
                            )
                        } else {
                            metadata(
                                Explanation.LogBaseMustNotEqualOnePlural,
                                domainExpression,
                                ListExpression(inExpressions),
                            )
                        }
                    }

                    taskWithOptionalSteps(
                        startExpr = when (kind) {
                            LogDomainConstraintKind.Argument, LogDomainConstraintKind.BasePositive ->
                                greaterThanOf(domainExpression, Constants.Zero)

                            LogDomainConstraintKind.BaseNotOne -> inequationOf(domainExpression, Constants.One)
                        },
                        stepsProducer = when (kind) {
                            LogDomainConstraintKind.Argument, LogDomainConstraintKind.BasePositive ->
                                solveInequalityDomainConstraintSteps

                            LogDomainConstraintKind.BaseNotOne -> solveInequationDomainConstraintSteps
                        },
                        explanation = explanation,
                    )
                }

                val overallSolution = computeOverallIntersectionSolution(constraintTasks.map { it.result })

                val explanation = when {
                    overallSolution is Identity ->
                        metadata(Explanation.ExpressionIsDefinedEverywhere, overallSolution.solutionVariables)
                    overallSolution is SetSolution && overallSolution.solutionSet == Constants.Reals ->
                        metadata(Explanation.ExpressionIsDefinedEverywhere, overallSolution.solutionVariables)
                    overallSolution is Contradiction -> metadata(
                        Explanation.ExpressionIsUndefinedEverywhere,
                        overallSolution.solutionVariables,
                    )
                    else -> metadata(Explanation.CollectDomainRestrictions)
                }

                task(
                    startExpr = overallSolution,
                    explanation = explanation,
                )

                allTasks()
            }
        },
    ),
}

fun findDenominatorsAndDivisors(expr: Expression): Sequence<Pair<Expression, Expression>> =
    sequence {
        for (child in expr.children) {
            yieldAll(findDenominatorsAndDivisors(child))
        }

        when (expr) {
            is Fraction -> {
                yield(Pair(expr.denominator, expr))
            }
            is Product -> {
                for (factor in expr.children) {
                    if (factor is DivideBy) {
                        yield(Pair(factor.divisor, expr))
                    }
                }
            }
        }
    }

private enum class LogDomainConstraintKind {
    Argument,
    BasePositive,
    BaseNotOne,
}

private data class LogDomainConstraint(
    val expression: Expression,
    val kind: LogDomainConstraintKind,
    val inExpression: Expression,
)

private fun findLogarithmDomainConstraints(expr: Expression): Sequence<LogDomainConstraint> =
    sequence {
        for (child in expr.children) {
            yieldAll(findLogarithmDomainConstraints(child))
        }

        if (expr is Logarithm) {
            yield(LogDomainConstraint(expr.argument, LogDomainConstraintKind.Argument, expr))
            yield(LogDomainConstraint(expr.base, LogDomainConstraintKind.BasePositive, expr))
            yield(LogDomainConstraint(expr.base, LogDomainConstraintKind.BaseNotOne, expr))
        }
    }
