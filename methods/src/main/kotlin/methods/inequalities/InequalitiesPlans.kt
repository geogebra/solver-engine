package methods.inequalities

import engine.context.ResourceData
import engine.expressions.Constants
import engine.expressions.DoubleInequality
import engine.expressions.Solution
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.FormChecker
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.operators.DoubleInequalityOperator
import engine.operators.StatementUnionOperator
import engine.patterns.AnyPattern
import engine.patterns.BinaryIntegerCondition
import engine.patterns.ConditionPattern
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
import engine.patterns.sumContaining
import engine.patterns.variableListOf
import engine.patterns.withOptionalConstantCoefficient
import engine.patterns.withOptionalIntegerCoefficient
import engine.steps.metadata.metadata
import methods.constantexpressions.ConstantExpressionsPlans
import methods.constantexpressions.simpleTidyUpSteps
import methods.equations.EquationsPlans
import methods.general.NormalizationPlans
import methods.polynomials.PolynomialsPlans
import methods.polynomials.algebraicSimplificationSteps
import methods.solvable.ApplySolvableRuleAndSimplify
import methods.solvable.DenominatorExtractor.extractDenominator
import methods.solvable.SolvableKey
import methods.solvable.SolvableRules
import methods.solvable.computeOverallIntersectionSolution
import methods.solvable.computeOverallUnionSolution
import methods.solvable.extractSumTermsFromSolvable
import methods.solvable.fractionRequiringMultiplication

enum class InequalitiesPlans(override val runner: CompositeMethod) : RunnerMethod {

    MoveConstantsToTheLeftAndSimplify(
        applySolvableRuleAndSimplify(SolvableKey.MoveConstantsToTheLeft, SolvableRules.MoveConstantsToTheLeft),
    ),

    MoveConstantsToTheRightAndSimplify(
        applySolvableRuleAndSimplify(SolvableKey.MoveConstantsToTheRight, SolvableRules.MoveConstantsToTheRight),
    ),

    MoveVariablesToTheLeftAndSimplify(
        applySolvableRuleAndSimplify(SolvableKey.MoveVariablesToTheLeft, SolvableRules.MoveVariablesToTheLeft),
    ),

    MoveVariablesToTheRightAndSimplify(
        applySolvableRuleAndSimplify(SolvableKey.MoveVariablesToTheRight, SolvableRules.MoveVariablesToTheRight),
    ),

    MultiplyByInverseCoefficientOfVariableAndSimplify(
        plan {
            explanation = Explanation.MultiplyByInverseCoefficientOfVariableAndSimplify

            steps {
                apply(InequalitiesRules.MultiplyByInverseCoefficientOfVariable)
                optionally(inequalitySimplificationSteps)
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
                apply(simplifyInequality)
            }
        },
    ),

    MultiplyByLCDAndSimplify(
        plan {
            explanation = methods.solvable.InequalitiesExplanation.MultiplyByLCDAndSimplify

            steps {
                apply(SolvableRules.MultiplySolvableByLCD)
                whilePossible(PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization)
            }
        },
    ),

    DivideByCoefficientOfVariableAndSimplify(
        plan {
            explanation = Explanation.DivideByCoefficientOfVariableAndSimplify

            steps {
                apply(InequalitiesRules.DivideByCoefficientOfVariable)
                optionally(inequalitySimplificationSteps)
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

            steps {
                whilePossible {
                    firstOf {
                        option(inequalitySimplificationSteps)
                        option {
                            check {
                                val sumTerms = extractSumTermsFromSolvable(it)
                                val denominators = sumTerms.mapNotNull { term -> extractDenominator(term) }

                                denominators.size >= 2 || sumTerms.any { term ->
                                    fractionRequiringMultiplication.matches(this, term)
                                }
                            }
                            apply(MultiplyByLCDAndSimplify)
                        }
                        option(PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization)
                    }
                }

                optionally {
                    firstOf {
                        // three ways to reorganize the inequality into ax </> b form
                        option {
                            // if the inequality is in the form `a </> bx + c` with `b` non-negative, then
                            // we move `c` to the left hand side and flip the equation
                            checkForm {
                                val lhs = condition { it.isConstant() }
                                val variableWithCoefficient = withOptionalConstantCoefficient(
                                    SolutionVariablePattern(),
                                    positiveOnly = true,
                                )
                                val rhs = oneOf(variableWithCoefficient, sumContaining(variableWithCoefficient))
                                inequalityOf(lhs, rhs)
                            }
                            optionally(MoveConstantsToTheLeftAndSimplify)
                            apply(InequalitiesRules.FlipInequality)
                        }
                        option {
                            // if the inequality is in the form ax + b = cx + d with a an integer and c a
                            // positive integer such that c > a we move ax to the right hand side, d to
                            // the left hand side and flip the inequality
                            checkForm {
                                val variable = SolutionVariablePattern()
                                val lhsVariable = withOptionalIntegerCoefficient(variable, false)
                                val rhsVariable = withOptionalIntegerCoefficient(variable, true)

                                val lhs = oneOf(lhsVariable, sumContaining(lhsVariable))
                                val rhs = oneOf(rhsVariable, sumContaining(rhsVariable))

                                ConditionPattern(
                                    inequalityOf(lhs, rhs),
                                    BinaryIntegerCondition(
                                        lhsVariable.integerCoefficient,
                                        rhsVariable.integerCoefficient,
                                    ) { n1, n2 -> n2 > n1 },
                                )
                            }

                            apply(MoveVariablesToTheRightAndSimplify)
                            optionally(MoveConstantsToTheLeftAndSimplify)
                            apply(InequalitiesRules.FlipInequality)
                        }
                        option {
                            // otherwise we first move variables to the left and then constants
                            // to the right
                            optionally(MoveVariablesToTheLeftAndSimplify)
                            optionally(MoveConstantsToTheRightAndSimplify)
                        }
                    }
                }

                optionally {
                    firstOf {
                        // get rid of the coefficient of the variable
                        option(InequalitiesRules.NegateBothSides)
                        option(MultiplyByInverseCoefficientOfVariableAndSimplify)
                        option(DivideByCoefficientOfVariableAndSimplify)
                    }
                }

                optionally {
                    firstOf {
                        option(InequalitiesRules.ExtractSolutionFromInequalityInSolvedForm)
                        option(InequalitiesRules.ExtractSolutionFromConstantInequality)
                        option(InequalitiesRules.ExtractSolutionFromConstantInequalityBasedOnSign)
                    }
                }

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
                    apply(InequalitiesRules.FlipInequality)
                }

                optionally(InequalitiesRules.NegateBothSides)

                optionally {
                    firstOf {
                        option {
                            apply(InequalitiesRules.SeparateModulusGreaterThanPositiveConstant)
                            apply(SolveInequalityUnion)
                        }
                        option {
                            apply(InequalitiesRules.ConvertModulusGreaterThanZero)
                            apply(SolveLinearInequality)
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
                            apply(EquationsPlans.SolveLinearEquation)
                        }

                        option(InequalitiesRules.ExtractSolutionFromModulusGreaterThanEqualToNonPositiveConstant)
                        option(InequalitiesRules.ExtractSolutionFromModulusGreaterThanNegativeConstant)
                    }
                }
            }
        },
    ),
}

internal val simplifyInequality = plan {
    explanation = Explanation.SimplifyInequality
    pattern = inequalityOf(AnyPattern(), AnyPattern())

    specificPlans(ConstantExpressionsPlans.SimplifyConstantExpression)

    steps {
        whilePossible { deeply(simpleTidyUpSteps) }
        optionally(NormalizationPlans.NormalizeExpression)
        whilePossible(SolvableRules.CancelCommonTermsOnBothSides)
        whilePossible(algebraicSimplificationSteps)
    }
}

internal val inequalitySimplificationSteps = steps {
    whilePossible {
        firstOf {
            option(InequalitiesRules.ExtractSolutionFromConstantInequality)
            option(InequalitiesRules.ExtractSolutionFromConstantInequalityBasedOnSign)
            // normalize the inequality
            option(simplifyInequality)
        }
    }
}

private val applySolvableRuleAndSimplify = ApplySolvableRuleAndSimplify(inequalitySimplificationSteps)::getPlan

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
    val inequalityUnion = condition { it.operator == StatementUnionOperator }
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
    val doubleInequalityPattern = condition { it.operator is DoubleInequalityOperator }
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
