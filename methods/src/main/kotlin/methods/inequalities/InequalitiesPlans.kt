package methods.inequalities

import engine.context.ResourceData
import engine.expressions.Constants
import engine.expressions.DoubleInequality
import engine.expressions.Solution
import engine.expressions.StatementUnion
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.FormChecker
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.RecurringDecimalPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.UnsignedNumberPattern
import engine.patterns.closedOpenIntervalOf
import engine.patterns.condition
import engine.patterns.contradictionOf
import engine.patterns.fractionOf
import engine.patterns.identityOf
import engine.patterns.inSolutionVariables
import engine.patterns.inequalityOf
import engine.patterns.oneOf
import engine.patterns.openClosedIntervalOf
import engine.patterns.openIntervalOf
import engine.patterns.optionalNegOf
import engine.patterns.setSolutionOf
import engine.patterns.variableListOf
import engine.steps.metadata.metadata
import methods.constantexpressions.simpleTidyUpSteps
import methods.equations.EquationsPlans
import methods.general.NormalizationPlans
import methods.inequations.InequationsPlans
import methods.polynomials.PolynomialsPlans
import methods.polynomials.algebraicSimplificationSteps
import methods.solvable.SolvablePlans
import methods.solvable.SolvableRules
import methods.solvable.computeOverallIntersectionSolution
import methods.solvable.computeOverallUnionSolution

enum class InequalitiesPlans(override val runner: CompositeMethod) : RunnerMethod {

    SimplifyInequality(
        plan {
            explanation = Explanation.SimplifyInequality

            steps {
                whilePossible { deeply(simpleTidyUpSteps) }
                optionally(NormalizationPlans.NormalizeExpression)
                whilePossible(SolvableRules.CancelCommonTermsOnBothSides)
                whilePossible(algebraicSimplificationSteps)
            }
        },
    ),

    IsolateAbsoluteValue(
        plan {
            explanation = Explanation.IsolateAbsoluteValue
            pattern = inequalityInOneVariable()

            steps {
                firstOf {
                    option(SolvableRules.MoveTermsNotContainingModulusToTheRight)
                    option(SolvableRules.MoveTermsNotContainingModulusToTheLeft)
                }
                apply(SimplifyInequality)
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
            pattern = inequalityInOneVariable()

            val solvablePlansForInequalities = SolvablePlans(SimplifyInequality)

            steps {
                whilePossible {
                    firstOf {
                        option(NormalizationPlans.NormalizeExpression)
                        // check for contradiction or identity
                        option(InequalitiesRules.ExtractSolutionFromConstantInequality)
                        option(InequalitiesRules.ExtractSolutionFromConstantInequalityBasedOnSign)
                        // normalize the equation
                        option(SimplifyInequality)

                        option(solvablePlansForInequalities.removeConstantDenominatorsSteps)
                        option(PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization)
                    }
                }

                optionally(solvablePlansForInequalities.solvableRearrangementSteps)
                optionally(solvablePlansForInequalities.coefficientRemovalSteps)
                optionally(InequalitiesRules.ExtractSolutionFromInequalityInSolvedForm)

                contextSensitive {
                    default(
                        ResourceData(preferDecimals = false),
                        FormChecker(condition { it is Solution }),
                    )
                    alternative(
                        ResourceData(preferDecimals = true),
                        decimalSolutionFormChecker,
                    )
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
                        apply(InequationsPlans.SolveInequationInOneVariable)
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
                        apply(EquationsPlans.SolveEquationInOneVariable)
                    }

                    option(InequalitiesRules.ExtractSolutionFromModulusGreaterThanEqualToNonPositiveConstant)
                    option(InequalitiesRules.ExtractSolutionFromModulusGreaterThanNegativeConstant)
                }
            }
        },
    ),
}

internal val inequalitySimplificationSteps = steps {
    whilePossible {
        firstOf {
            option(InequalitiesRules.ExtractSolutionFromConstantInequality)
            option(InequalitiesRules.ExtractSolutionFromConstantInequalityBasedOnSign)
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

private fun inequalityInOneVariable() = inSolutionVariables(inequalityOf(AnyPattern(), AnyPattern()))
