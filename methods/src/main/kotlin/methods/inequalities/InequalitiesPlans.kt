package methods.inequalities

import engine.context.BooleanSetting
import engine.context.Setting
import engine.expressions.Constants
import engine.expressions.DoubleInequality
import engine.expressions.Inequality
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
import engine.patterns.inequalityOf
import engine.patterns.oneOf
import engine.patterns.openClosedIntervalOf
import engine.patterns.openIntervalOf
import engine.patterns.optionalNegOf
import engine.patterns.setSolutionOf
import engine.patterns.variableListOf
import engine.steps.metadata.metadata
import methods.constantexpressions.constantSimplificationSteps
import methods.constantexpressions.simpleTidyUpSteps
import methods.equations.EquationsPlans
import methods.general.NormalizationPlans
import methods.inequations.InequationsPlans
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
