package methods.equations

import engine.context.ResourceData
import engine.expressions.Constants
import engine.expressions.equationOf
import engine.expressions.solutionOf
import engine.expressions.solutionSetOf
import engine.expressions.splitPlusMinus
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.FormChecker
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.AnyPattern
import engine.patterns.ConstantPattern
import engine.patterns.FindPattern
import engine.patterns.FixedPattern
import engine.patterns.RecurringDecimalPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.UnsignedNumberPattern
import engine.patterns.condition
import engine.patterns.equationOf
import engine.patterns.fractionOf
import engine.patterns.inSolutionVariable
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.productContaining
import engine.patterns.productOf
import engine.patterns.solutionOf
import engine.patterns.solutionSetOf
import engine.patterns.sumContaining
import engine.patterns.withOptionalConstantCoefficient
import engine.steps.metadata.CategorisedMetadataKey
import engine.steps.metadata.metadata
import methods.constantexpressions.ConstantExpressionsPlans
import methods.polynomials.PolynomialPlans
import methods.solvable.SolvableRules

enum class EquationsPlans(override val runner: CompositeMethod) : RunnerMethod {

    MoveConstantsToTheLeftAndSimplify(
        plan {
            explanation = Explanation.MoveConstantsToTheLeftAndSimplify

            steps {
                apply(SolvableRules.MoveConstantsToTheLeft)
                optionally(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariableWithoutNormalization)
            }
        },
    ),

    MoveConstantsToTheRightAndSimplify(
        plan {
            explanation = Explanation.MoveConstantsToTheRightAndSimplify

            steps {
                apply(SolvableRules.MoveConstantsToTheRight)
                optionally(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariableWithoutNormalization)
            }
        },
    ),

    MoveVariablesToTheLeftAndSimplify(
        plan {
            explanation = Explanation.MoveVariablesToTheLeftAndSimplify

            steps {
                apply(SolvableRules.MoveVariablesToTheLeft)
                optionally(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariableWithoutNormalization)
            }
        },
    ),

    MultiplyByInverseCoefficientOfVariableAndSimplify(
        plan {
            explanation = Explanation.MultiplyByInverseCoefficientOfVariableAndSimplify

            steps {
                apply(EquationsRules.MultiplyByInverseCoefficientOfVariable)
                optionally(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariableWithoutNormalization)
            }
        },
    ),

    MultiplyByLCDAndSimplify(
        plan {
            explanation = Explanation.MultiplyByLCDAndSimplify

            steps {
                apply(SolvableRules.MultiplySolvableByLCD)
                whilePossible(PolynomialPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization)
            }
        },
    ),

    DivideByCoefficientOfVariableAndSimplify(
        plan {
            explanation = Explanation.DivideByCoefficientOfVariableAndSimplify

            steps {
                apply(EquationsRules.DivideByCoefficientOfVariable)
                optionally(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariableWithoutNormalization)
            }
        },
    ),

    CompleteTheSquareAndSimplify(
        plan {
            explanation = Explanation.CompleteTheSquareAndSimplify

            steps {
                apply(EquationsRules.CompleteTheSquare)
                optionally(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable)
            }
        },
    ),

    MultiplyByInverseOfLeadingCoefficientAndSimplify(
        plan {
            explanation = Explanation.MultiplyByInverseOfLeadingCoefficientAndSimplify

            steps {
                apply(EquationsRules.MultiplyByInverseOfLeadingCoefficient)
                apply(PolynomialPlans.ExpandPolynomialExpressionInOneVariable)
            }
        },
    ),

    ExtractSolutionAndSimplifyFromEquationInPlusMinusForm(extractSolutionAndSimplifyFromEquationInPlusMinusForm),

    @PublicMethod
    SolveLinearEquation(
        plan {
            explanation = Explanation.SolveLinearEquation
            pattern = equationInOneVariable()

            steps {
                optionally(equationSimplificationSteps)

                optionally {
                    // multiply through with the LCD if the equation contains one fraction with a sum numerator
                    // or a fraction multiplied by a sum
                    checkForm {
                        val nonConstantSum = condition(sumContaining()) { !it.isConstant() }
                        oneOf(
                            FindPattern(fractionOf(nonConstantSum, UnsignedIntegerPattern())),
                            FindPattern(
                                productContaining(
                                    fractionOf(AnyPattern(), UnsignedIntegerPattern()),
                                    nonConstantSum,
                                ),
                            ),
                        )
                    }
                    apply(MultiplyByLCDAndSimplify)
                }

                optionally(PolynomialPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization)
                optionally(equationSimplificationSteps)

                optionally {
                    firstOf {
                        // two ways to reorganize the equation into ax = b form
                        option {
                            // if the equation is in the form `a = bx + c` with `b` non-negative, then
                            // we move `c` to the left hand side and flip the equation
                            checkForm {
                                val lhs = condition(AnyPattern()) { it.isConstant() }
                                val variableWithCoefficient = withOptionalConstantCoefficient(
                                    SolutionVariablePattern(),
                                    positiveOnly = true,
                                )
                                val rhs = oneOf(variableWithCoefficient, sumContaining(variableWithCoefficient))
                                equationOf(lhs, rhs)
                            }
                            optionally(MoveConstantsToTheLeftAndSimplify)
                            apply(EquationsRules.FlipEquation)
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
                        option(EquationsRules.NegateBothSides)
                        option(MultiplyByInverseCoefficientOfVariableAndSimplify)
                        option(DivideByCoefficientOfVariableAndSimplify)
                    }
                }

                optionally {
                    firstOf {
                        // check if the equation is in one of the possible solved forms
                        option(EquationsRules.ExtractSolutionFromContradiction)
                        option(EquationsRules.ExtractSolutionFromIdentity)
                        option(EquationsRules.ExtractSolutionFromEquationInSolvedForm)
                    }
                }

                contextSensitive {
                    default(
                        ResourceData(preferDecimals = false),
                        FormChecker(
                            solutionOf(SolutionVariablePattern(), AnyPattern()),
                        ),
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
    SolveEquationUsingRootsMethod(
        plan {
            explanation = Explanation.SolveEquationUsingRootsMethod
            pattern = equationInOneVariable()
            resultPattern = solutionOf(SolutionVariablePattern(), AnyPattern())

            steps {
                optionally(equationSimplificationSteps)

                optionally(MoveVariablesToTheLeftAndSimplify)
                optionally(MoveConstantsToTheRightAndSimplify)

                optionally {
                    firstOf {
                        // get rid of the coefficient of the variable
                        option(EquationsRules.NegateBothSides)
                        option(MultiplyByInverseCoefficientOfVariableAndSimplify)
                        option(DivideByCoefficientOfVariableAndSimplify)
                    }
                }

                firstOf {
                    // x^2n = something negative
                    option(EquationsRules.ExtractSolutionFromEvenPowerEqualsNegative)

                    option {
                        apply(EquationsRules.TakeRootOfBothSidesRHSIsZero)
                        apply(EquationsRules.ExtractSolutionFromEquationInSolvedForm)
                    }

                    option {
                        apply(EquationsRules.TakeRootOfBothSides)
                        optionally {
                            applyTo(ConstantExpressionsPlans.SimplifyConstantExpression) { it.secondChild }
                        }
                        firstOf {
                            option(EquationsRules.ExtractSolutionFromEquationInPlusMinusForm)
                            option(EquationsRules.ExtractSolutionFromEquationInSolvedForm)
                        }
                    }
                }
            }
        },
    ),

    @PublicMethod
    SolveQuadraticEquationByCompletingTheSquare(
        plan {
            explanation = Explanation.SolveQuadraticEquationByCompletingTheSquare
            pattern = equationOf(AnyPattern(), AnyPattern())
            resultPattern = solutionOf(SolutionVariablePattern(), AnyPattern())

            steps {

                // Simplify the equation and move variables to the left
                optionally(equationSimplificationSteps)
                optionally(MoveVariablesToTheLeftAndSimplify)
                optionally(MultiplyByInverseOfLeadingCoefficientAndSimplify)

                // Complete the square
                firstOf {
                    option {
                        // See if we can complete the square straight away
                        applyTo(PolynomialPlans.FactorTrinomialToSquareAndSimplify) { it.firstChild }
                    }
                    option {
                        // Else rearrange to put constants on the right and complete the square
                        optionally(MoveConstantsToTheRightAndSimplify)
                        apply(CompleteTheSquareAndSimplify)
                        applyTo(PolynomialPlans.FactorTrinomialToSquareAndSimplify) { it.firstChild }
                    }
                }

                // Solve the equation
                firstOf {
                    // (...)^2 = something negative
                    option(EquationsRules.ExtractSolutionFromEvenPowerEqualsNegative)

                    // (...)^2 = 0
                    option {
                        apply(EquationsRules.TakeRootOfBothSidesRHSIsZero)
                        apply(MoveConstantsToTheRightAndSimplify)
                        apply(EquationsRules.ExtractSolutionFromEquationInSolvedForm)
                    }

                    // (...)^2 = something positive
                    option {
                        apply(EquationsRules.TakeRootOfBothSides)
                        optionally {
                            applyTo(ConstantExpressionsPlans.SimplifyConstantExpression) { it.secondChild }
                        }
                        apply(MoveConstantsToTheRightAndSimplify)
                        firstOf {
                            option(ExtractSolutionAndSimplifyFromEquationInPlusMinusForm)
                            option(EquationsRules.ExtractSolutionFromEquationInPlusMinusForm)
                        }
                    }
                }
            }
        },
    ),

    @PublicMethod
    SolveFactorisedQuadraticEquation(solveFactorisedQuadratic),
}

private val extractSolutionAndSimplifyFromEquationInPlusMinusForm = taskSet {
    val equation = equationOf(SolutionVariablePattern(), ConstantPattern())
    pattern = equation
    explanation = Explanation.ExtractSolutionAndSimplifyFromEquationInPlusMinusForm

    tasks {
        // Get all the equations with +/- expanded
        val splitEquations = get(equation).splitPlusMinus()
        if (splitEquations.size <= 1) {
            return@tasks null
        }

        // Create a task for each to simplify it
        val splitTasks = splitEquations.map {
            task(
                startExpr = it,
                explanation = metadata(Explanation.SimplifyExtractedSolution),
                stepsProducer = steps {
                    applyTo(ConstantExpressionsPlans.SimplifyConstantExpression) { it.secondChild }
                },
            ) ?: return@tasks null
        }

        // Gather all solutions together in a single solution set.
        task(
            startExpr = solutionOf(
                splitTasks[0].result.firstChild,
                solutionSetOf(splitTasks.map { it.result.secondChild }),
            ),
            explanation = metadata(Explanation.CollectSolutions),
        )
        allTasks()
    }
}
//
// Below is an example of using a task set to perform a list of tasks (here solving a factorised quadratic equation).
// This will need to be revisited when doing quadratic equations.
//

enum class ExperimentalExplanation : CategorisedMetadataKey {

    SolveFactorisedQuadratic,
    SolveFactorOfQuadratic,
    CollectSolutions,
    ;

    override val category = "Experimental"
}

private val solveFactorisedQuadratic = taskSet {
    val factor1 = AnyPattern()
    val factor2 = AnyPattern()
    val zero = FixedPattern(Constants.Zero)
    pattern = equationOf(productOf(factor1, factor2), zero)
    explanation = ExperimentalExplanation.SolveFactorisedQuadratic

    tasks {
        val task1 = task(
            startExpr = equationOf(get(factor1), get(zero)),
            explanation = metadata(ExperimentalExplanation.SolveFactorOfQuadratic, get(factor1)),
            stepsProducer = EquationsPlans.SolveLinearEquation,
        )
            ?: return@tasks null
        val task2 = task(
            startExpr = equationOf(get(factor2), get(zero)),
            explanation = metadata(ExperimentalExplanation.SolveFactorOfQuadratic, get(factor2)),
            stepsProducer = EquationsPlans.SolveLinearEquation,
        )
            ?: return@tasks null
        val solution1 = task1.result.secondChild.firstChild
        val solution2 = task2.result.secondChild.firstChild
        task(
            startExpr = solutionOf(task1.result.firstChild, solutionSetOf(solution1, solution2)),
            explanation = metadata(ExperimentalExplanation.CollectSolutions),
            dependsOn = listOf(task1, task2),
        )
        allTasks()
    }
}

private val equationSimplificationSteps = steps {
    whilePossible {
        firstOf {
            // before we cancel we always have to check for an identity
            option(EquationsRules.ExtractSolutionFromIdentity)
            // normalize the equation
            option(SolvableRules.CancelCommonTermsOnBothSides)
            option(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariableWithoutNormalization)
            // after cancelling we have to check for contradiction
            option(EquationsRules.ExtractSolutionFromContradiction)
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
        solutionOf(
            SolutionVariablePattern(),
            oneOf(
                FixedPattern(Constants.EmptySet),
                FixedPattern(Constants.Reals),
                solutionSetOf(acceptedSolutions),
            ),
        ),
    )
}

private fun equationInOneVariable() = inSolutionVariable(equationOf(AnyPattern(), AnyPattern()))
