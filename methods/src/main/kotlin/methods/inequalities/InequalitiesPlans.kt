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

package methods.inequalities

import engine.conditions.isDefinitelyNegative
import engine.context.BooleanSetting
import engine.context.Setting
import engine.expressions.Constants
import engine.expressions.Contradiction
import engine.expressions.DoubleInequality
import engine.expressions.Expression
import engine.expressions.FiniteSet
import engine.expressions.Identity
import engine.expressions.Inequality
import engine.expressions.Interval
import engine.expressions.SetSolution
import engine.expressions.SetUnion
import engine.expressions.Solution
import engine.expressions.StatementUnion
import engine.expressions.VariableList
import engine.expressions.closedIntervalOf
import engine.expressions.closedOpenIntervalOf
import engine.expressions.contradictionOf
import engine.expressions.equationOf
import engine.expressions.finiteSetOf
import engine.expressions.identityOf
import engine.expressions.openClosedIntervalOf
import engine.expressions.openIntervalOf
import engine.expressions.setDifferenceOf
import engine.expressions.setSolutionOf
import engine.expressions.setUnionOf
import engine.expressions.solutionVariableConstantChecker
import engine.expressions.variableListOf
import engine.expressions.xp
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.FormChecker
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.operators.Comparator
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.FixedPattern
import engine.patterns.QuadraticPolynomialPattern
import engine.patterns.RecurringDecimalPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.UnsignedNumberPattern
import engine.patterns.closedOpenIntervalOf
import engine.patterns.condition
import engine.patterns.contradictionOf
import engine.patterns.fractionOf
import engine.patterns.identityOf
import engine.patterns.inequalityOf
import engine.patterns.oneOf
import engine.patterns.openClosedIntervalOf
import engine.patterns.openIntervalOf
import engine.patterns.optionalNegOf
import engine.patterns.setSolutionOf
import engine.patterns.variableListOf
import engine.steps.metadata.Metadata
import engine.steps.metadata.metadata
import engine.utility.pickValueInInterval
import methods.constantexpressions.constantSimplificationSteps
import methods.constantexpressions.simpleTidyUpSteps
import methods.equations.EquationsPlans
import methods.general.NormalizationPlans
import methods.inequations.InequationsPlans
import methods.polynomials.PolynomialRules
import methods.polynomials.PolynomialsPlans
import methods.simplify.algebraicSimplificationStepsWithoutFractionAddition
import methods.solvable.SolvablePlans
import methods.solvable.SolvableRules
import methods.solvable.computeOverallIntersectionSolution
import methods.solvable.computeOverallUnionSolution
import methods.solvable.evaluateBothSidesNumerically

enum class InequalitiesPlans(override val runner: CompositeMethod) : RunnerMethod {
    SimplifyInequality(
        plan {
            explanation = Explanation.SimplifyInequality

            steps {
                whilePossible { deeply(simpleTidyUpSteps) }
                optionally(NormalizationPlans.NormalizeExpression)
                whilePossible(SolvableRules.CancelCommonTermsOnBothSides)
                optionally(algebraicSimplificationStepsWithoutFractionAddition)
            }
        },
    ),

    IsolateAbsoluteValue(
        plan {
            explanation = Explanation.IsolateAbsoluteValue
            pattern = inequalityInOneVariable()

            val innerSteps = engine.methods.stepsproducers.steps {
                firstOf {
                    option(SolvableRules.MoveTermsNotContainingModulusToTheRight)
                    option(SolvableRules.MoveTermsNotContainingModulusToTheLeft)
                }
                apply(SimplifyInequality)
            }

            steps {
                branchOn(Setting.MoveTermsOneByOne) {
                    case(BooleanSetting.True) { whilePossible(innerSteps) }
                    case(BooleanSetting.False) { apply(innerSteps) }
                }
            }
        },
    ),

    SolveInequalityUnion(solveInequalityUnion),

    SolveDoubleInequality(solveDoubleInequality),

    /**
     * Solve a linear inequality in one variable
     */
    @PublicMethod
    SolveLinearInequality(
        plan {
            explanation = Explanation.SolveLinearInequality
            pattern = condition {
                it is Inequality && solutionVariables.size == 1 &&
                    it.variables.contains(solutionVariables[0])
            }

            steps {
                whilePossible {
                    firstOf {
                        option(NormalizationPlans.NormalizeExpression)
                        // check for contradiction or identity
                        option(InequalitiesRules.ExtractSolutionFromConstantInequality)
                        // normalize the equation
                        option(SimplifyInequality)

                        option(solvablePlansForInequalities.removeConstantDenominatorsSteps)
                        option(PolynomialsPlans.ExpandPolynomialExpressionWithoutNormalization)
                    }
                }

                optionally(solvablePlansForInequalities.solvableRearrangementSteps)
                optionally(solvablePlansForInequalities.coefficientRemovalSteps)
                optionally(InequalitiesRules.ExtractSolutionFromInequalityInSolvedForm)

                branchOn(Setting.PreferDecimals) {
                    case(BooleanSetting.True, decimalSolutionFormChecker)
                    case(BooleanSetting.False, FormChecker(condition { it is Solution }))
                }
            }
        },
    ),

    @PublicMethod
    SolveInequalityWithVariablesInOneAbsoluteValue(
        plan {
            explanation = Explanation.SolveInequalityWithVariablesInOneAbsoluteValue
            pattern = inequalityInOneVariable()
            resultPattern = condition { it is Solution }

            steps {
                optionally(inequalitySimplificationSteps)
                optionally(IsolateAbsoluteValue)
                optionally {
                    check { it.firstChild.isConstant() }
                    apply(SolvableRules.FlipSolvable)
                }

                optionally(SolvableRules.NegateBothSides)

                firstOf {
                    option {
                        apply(InequalitiesRules.SeparateModulusGreaterThanPositiveConstant)
                        apply(SolveInequalityUnion)
                    }
                    option {
                        apply(InequalitiesRules.ConvertModulusGreaterThanZero)
                        apply(InequationsPlans.SolveInequation)
                    }
                    option {
                        apply(InequalitiesRules.ConvertModulusLessThanPositiveConstant)
                        apply(SolveDoubleInequality)
                    }
                    option {
                        apply(InequalitiesRules.ConvertModulusLessThanEqualToPositiveConstant)
                        apply(SolveDoubleInequality)
                    }
                    option {
                        apply(InequalitiesRules.SeparateModulusGreaterThanEqualToPositiveConstant)
                        apply(SolveInequalityUnion)
                    }
                    option(InequalitiesRules.ExtractSolutionFromModulusLessThanNonPositiveConstant)

                    // after this we need to solve an equation
                    option {
                        apply(InequalitiesRules.ReduceModulusLessThanEqualToZeroInequalityToEquation)
                        apply(EquationsPlans.SolveEquation)
                    }

                    option(InequalitiesRules.ExtractSolutionFromModulusGreaterThanEqualToNonPositiveConstant)
                    option(InequalitiesRules.ExtractSolutionFromModulusGreaterThanNegativeConstant)
                }
            }
        },
    ),

    @PublicMethod
    SolveConstantInequality(
        plan {
            explanation = Explanation.SolveConstantInequality

            steps {
                apply(solveConstantInequalitySteps)
            }
        },
    ),

    SolveQuadraticInequalityInCanonicalForm(solveQuadraticInequalityInCanonicalForm),

    @PublicMethod
    SolveQuadraticInequality(solveQuadraticInequality),
}

internal val inequalitySimplificationSteps = steps {
    whilePossible {
        firstOf {
            option(InequalitiesRules.ExtractSolutionFromConstantInequality)
            // normalize the inequality
            option(InequalitiesPlans.SimplifyInequality)
        }
    }
}

private val decimalSolutionFormChecker = run {
    val acceptedSolutions = oneOf(
        SignedNumberPattern(),
        optionalNegOf(RecurringDecimalPattern()),
        optionalNegOf(fractionOf(UnsignedNumberPattern(), UnsignedNumberPattern())),
    )

    FormChecker(
        oneOf(
            identityOf(variableListOf(SolutionVariablePattern())),
            contradictionOf(variableListOf(SolutionVariablePattern())),
            setSolutionOf(
                variableListOf(SolutionVariablePattern()),
                oneOf(
                    openIntervalOf(FixedPattern(Constants.NegativeInfinity), acceptedSolutions),
                    openClosedIntervalOf(FixedPattern(Constants.NegativeInfinity), acceptedSolutions),
                    openIntervalOf(acceptedSolutions, FixedPattern(Constants.Infinity)),
                    closedOpenIntervalOf(acceptedSolutions, FixedPattern(Constants.Infinity)),
                ),
            ),
        ),
    )
}

private val solveInequalityUnion = taskSet {
    val inequalityUnion = condition { it is StatementUnion }
    pattern = inequalityUnion
    explanation = Explanation.SolveInequalityUnion

    tasks {
        // Create a task for each to simplify it
        val splitTasks = get(inequalityUnion).children.map {
            task(
                startExpr = it,
                explanation = metadata(Explanation.SolveInequalityInInequalityUnion),
                stepsProducer = InequalitiesPlans.SolveLinearInequality,
            ) ?: return@tasks null
        }

        // Else combine the solutions together
        val overallSolution = computeOverallUnionSolution(splitTasks.map { it.result }) ?: return@tasks null
        task(
            startExpr = overallSolution,
            explanation = metadata(Explanation.CollectUnionSolutions),
        )
        allTasks()
    }
}

private val solveDoubleInequality = taskSet {
    val doubleInequalityPattern = condition { it is DoubleInequality }
    pattern = doubleInequalityPattern
    explanation = Explanation.SolveDoubleInequality

    tasks {
        // Create a task for both the inequalities to simplify it
        val doubleInequality = get(doubleInequalityPattern) as DoubleInequality
        val leftInequality = doubleInequality.getLeftInequality()
        val rightInequality = doubleInequality.getRightInequality()

        val task1 = task(
            startExpr = leftInequality,
            explanation = metadata(Explanation.SolveLeftInequalityInDoubleInequality),
            stepsProducer = InequalitiesPlans.SolveLinearInequality,
        ) ?: return@tasks null

        val task2 = task(
            startExpr = rightInequality,
            explanation = metadata(Explanation.SolveRightInequalityInDoubleInequality),
            stepsProducer = InequalitiesPlans.SolveLinearInequality,
        ) ?: return@tasks null

        val splitTasks = listOf(task1, task2)

        // Else combine the solutions together
        val overallSolution = computeOverallIntersectionSolution(splitTasks.map { it.result }) ?: return@tasks null
        task(
            startExpr = overallSolution,
            explanation = metadata(Explanation.CollectIntersectionSolutions),
        )
        allTasks()
    }
}

private fun inequalityInOneVariable() =
    condition(inequalityOf(AnyPattern(), AnyPattern())) {
        it.variables.size == 1 && solutionVariables.size == 1 && it.variables.contains(solutionVariables[0])
    }

val solveConstantInequalitySteps = steps {
    check { it is Inequality && it.isConstant() }
    optionally {
        plan {
            explanation = Explanation.SimplifyInequality

            steps {
                whilePossible(constantSimplificationSteps)
            }
        }
    }
    shortcut(InequalitiesRules.ExtractSolutionFromConstantInequality)

    optionally(InequalitiesPlans.SimplifyInequality)
    shortcut(InequalitiesRules.ExtractSolutionFromConstantInequality)

    optionally(solvablePlansForInequalities.moveEverythingToTheLeftAndSimplify)
    shortcut(InequalitiesRules.ExtractSolutionFromConstantInequality)

    inContext(contextFactory = { copy(precision = 10) }) {
        apply(evaluateBothSidesNumerically)
    }
    apply(InequalitiesRules.ExtractSolutionFromConstantInequality)
}

val solvablePlansForInequalities = SolvablePlans(InequalitiesPlans.SimplifyInequality)

private val ensureLeadCoefficientOfLHSIsPositive = plan {
    explanation = Explanation.EnsureLeadCoefficientOfLHSIsPositive

    val lhs = QuadraticPolynomialPattern(
        variable = SolutionVariablePattern(),
        constantChecker = solutionVariableConstantChecker,
    )

    // We only reverse the sign when the inequality is of the form ax^2 + bx + c ** 0 where a < 0
    pattern = inequalityOf(
        ConditionPattern(lhs) { _, match, _ -> lhs.quadraticCoefficient(match).isDefinitelyNegative() },
        FixedPattern(Constants.Zero),
    )

    steps {
        apply(SolvableRules.NegateBothSidesUnconditionally)
        optionally(InequalitiesPlans.SimplifyInequality)
    }
}

private fun extractSolutions(solutionExpr: Expression): List<Expression>? {
    return when (solutionExpr) {
        is Contradiction -> emptyList()
        is SetSolution -> {
            val set = solutionExpr.solutionSet
            if (set is FiniteSet) {
                set.elements
            } else {
                null
            }
        }
        else -> null
    }
}

private data class TaskParameters(val explanation: Metadata, val startExpr: Expression)

@Suppress("CyclomaticComplexMethod", "LongMethod")
private fun getDeduceInequalitySolutionParameters(
    variables: VariableList,
    expression: Inequality,
    solutions: List<Expression>,
): TaskParameters? {
    return when (expression.comparator) {
        // We just apply the appropriate method depending on
        // - the comparator (< , <=, >, >=)
        // - the number of solutions (0, 1, 2)
        // If not any of these, we return null, meaning we don't know what to do.
        Comparator.GreaterThan -> when (solutions.size) {
            0 -> TaskParameters(
                explanation = metadata(Explanation.DeduceInequalitySolutionForGreaterThanAndNoSolution),
                startExpr = identityOf(variables, expression),
            )
            1 -> TaskParameters(
                explanation = metadata(Explanation.DeduceInequalitySolutionForGreaterThanAndOneSolution),
                startExpr = setSolutionOf(
                    variables,
                    setDifferenceOf(Constants.Reals, finiteSetOf(solutions)),
                ),
            )
            2 -> TaskParameters(
                explanation = metadata(Explanation.DeduceInequalitySolutionForGreaterThanAndTwoSolutions),
                startExpr = setSolutionOf(
                    variables,
                    setUnionOf(
                        openIntervalOf(Constants.NegativeInfinity, solutions[0]),
                        openIntervalOf(solutions[1], Constants.Infinity),
                    ),
                ),
            )
            else -> null
        }
        Comparator.GreaterThanOrEqual -> when (solutions.size) {
            0 -> TaskParameters(
                explanation = metadata(Explanation.DeduceInequalitySolutionForGreaterThanOrEqualAndNoSolution),
                startExpr = identityOf(variables, expression),
            )
            1 -> TaskParameters(
                explanation = metadata(Explanation.DeduceInequalitySolutionForGreaterThanOrEqualAndOneSolution),
                startExpr = identityOf(variables, expression),
            )
            2 -> TaskParameters(
                explanation = metadata(Explanation.DeduceInequalitySolutionForGreaterThanOrEqualAndTwoSolutions),
                startExpr = setSolutionOf(
                    variables,
                    setUnionOf(
                        openClosedIntervalOf(Constants.NegativeInfinity, solutions[0]),
                        closedOpenIntervalOf(solutions[1], Constants.Infinity),
                    ),
                ),
            )
            else -> null
        }
        Comparator.LessThan -> when (solutions.size) {
            0 -> TaskParameters(
                explanation = metadata(Explanation.DeduceInequalitySolutionForLessThanAndNoSolution),
                startExpr = contradictionOf(variables, expression),
            )
            1 -> TaskParameters(
                explanation = metadata(Explanation.DeduceInequalitySolutionForLessThanAndOneSolution),
                startExpr = contradictionOf(variables, expression),
            )
            2 -> TaskParameters(
                explanation = metadata(Explanation.DeduceInequalitySolutionForLessThanAndTwoSolutions),
                startExpr = setSolutionOf(
                    variables,
                    openIntervalOf(solutions[0], solutions[1]),
                ),
            )
            else -> null
        }
        Comparator.LessThanOrEqual -> when (solutions.size) {
            0 -> TaskParameters(
                explanation = metadata(Explanation.DeduceInequalitySolutionForLessThanOrEqualAndNoSolution),
                startExpr = contradictionOf(variables, expression),
            )
            1 -> TaskParameters(
                explanation = metadata(Explanation.DeduceInequalitySolutionForLessThanOrEqualAndOneSolution),
                startExpr = setSolutionOf(
                    variables,
                    finiteSetOf(solutions),
                ),
            )
            2 -> TaskParameters(
                explanation = metadata(Explanation.DeduceInequalitySolutionForLessThanOrEqualAndTwoSolutions),
                startExpr = setSolutionOf(
                    variables,
                    closedIntervalOf(solutions[0], solutions[1]),
                ),
            )
            else -> null
        }
        else -> null
    }
}

private fun getSolutionIntervals(comparator: Comparator, solutions: List<Expression>): List<Interval>? {
    val isClosed = when (comparator) {
        Comparator.LessThanOrEqual, Comparator.GreaterThanOrEqual -> true
        Comparator.LessThan, Comparator.GreaterThan -> false
        else -> return null
    }

    return when (solutions.size) {
        0 -> listOf(
            Interval(Constants.NegativeInfinity, Constants.Infinity, closedLeft = false, closedRight = false),
        )
        1 -> if (isClosed) {
            listOf(
                Interval(Constants.NegativeInfinity, Constants.Infinity, closedLeft = false, closedRight = false),
            )
        } else {
            listOf(
                Interval(Constants.NegativeInfinity, solutions[0], closedLeft = false, closedRight = false),
                Interval(solutions[0], Constants.Infinity, closedLeft = false, closedRight = false),
            )
        }
        2 -> {
            listOf(
                Interval(Constants.NegativeInfinity, solutions[0], closedLeft = false, closedRight = isClosed),
                Interval(solutions[0], solutions[1], closedLeft = isClosed, closedRight = isClosed),
                Interval(solutions[1], Constants.Infinity, closedLeft = isClosed, closedRight = false),
            )
        }
        else -> null
    }
}

private val solveQuadraticInequalityInCanonicalForm = taskSet {
    explanation = Explanation.SolveQuadraticInequalityInCanonicalForm
    pattern = inequalityOf(
        QuadraticPolynomialPattern(
            variable = SolutionVariablePattern(),
            constantChecker = solutionVariableConstantChecker,
        ),
        FixedPattern(Constants.Zero),
    )

    tasks {
        val inequality = expression as Inequality

        // First solve the equation ax^2 + bx + c = 0
        val solveEquation = task(
            startExpr = equationOf(expression.firstChild, expression.secondChild),
            explanation = metadata(Explanation.SolveCorrespondingQuadraticEquation),
            stepsProducer = EquationsPlans.SolveEquation,
        ) ?: return@tasks null

        val solutions = extractSolutions(solveEquation.result) ?: return@tasks null

        if (context.isSet(Setting.SolveInequalitiesUsingTestPoints)) {
            val solutionIntervals = getSolutionIntervals(inequality.comparator, solutions) ?: return@tasks null
            val validSolutionIntervals = mutableListOf<Interval>()
            for (solutionInterval in solutionIntervals) {
                val testValue = xp(
                    pickValueInInterval(
                        solutionInterval.leftBound.doubleValue,
                        solutionInterval.rightBound.doubleValue,
                    ),
                )
                val testInequality = expression.substituteAllOccurrences(
                    xp(context.solutionVariables[0]),
                    testValue,
                )
                val checkIntervalTask = task(
                    startExpr = testInequality,
                    explanation = metadata(
                        Explanation.CheckSolutionIntervalUsingTestPoint,
                        solutionInterval,
                        testValue,
                    ),
                    stepsProducer = solveConstantInequalitySteps,
                    context = context.copy(solutionVariables = emptyList()),
                ) ?: return@tasks null
                val checkSuccessful = when (checkIntervalTask.result) {
                    is Contradiction -> false
                    is Identity -> true
                    else -> null
                } ?: return@tasks null
                if (checkSuccessful) {
                    validSolutionIntervals.add(solutionInterval)
                }
            }
            task(
                startExpr = when (validSolutionIntervals.size) {
                    0 -> contradictionOf(variableListOf(context.solutionVariables), expression)
                    1 -> setSolutionOf(variableListOf(context.solutionVariables), validSolutionIntervals[0])
                    else -> setSolutionOf(variableListOf(context.solutionVariables), SetUnion(validSolutionIntervals))
                },
                explanation = metadata(Explanation.CollectUnionSolutions),
            )
        } else {
            // Then find the method for solving the inequality, depending on the type of inequality and number of
            // solutions
            val (explanation, startExpr) = getDeduceInequalitySolutionParameters(
                variables = variableListOf(context.solutionVariables),
                expression = inequality,
                solutions = solutions,
            ) ?: return@tasks null

            task(explanation = explanation, startExpr = startExpr)
        }
        allTasks()
    }
}

private val solveQuadraticInequality = plan {
    explanation = Explanation.SolveQuadraticInequality
    pattern = condition {
        it is Inequality && solutionVariables.size == 1 &&
            it.variables.contains(solutionVariables[0])
    }

    steps {
        // First tidy up and move all terms to the LSH
        whilePossible {
            firstOf {
                option(NormalizationPlans.NormalizeExpression)
                option(InequalitiesPlans.SimplifyInequality)

                option(solvablePlansForInequalities.removeConstantDenominatorsSteps)
                option(PolynomialsPlans.ExpandPolynomialExpressionWithoutNormalization)
                option(solvablePlansForInequalities.moveEverythingToTheLeftAndSimplify)
            }
        }

        // Then make sure the leading coefficient is negative
        applyToChildren(PolynomialRules.NormalizePolynomial)
        optionally(ensureLeadCoefficientOfLHSIsPositive)

        // Now the inequality is of the form ax^2 + bx + c (> | >= | < | <=) 0, solve it!
        apply(InequalitiesPlans.SolveQuadraticInequalityInCanonicalForm)
    }
}
