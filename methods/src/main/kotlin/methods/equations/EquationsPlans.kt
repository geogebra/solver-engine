package methods.equations

import engine.context.ResourceData
import engine.expressions.Constants
import engine.expressions.equationOf
import engine.expressions.solutionOf
import engine.expressions.solutionSetOf
import engine.expressions.splitPlusMinus
import engine.expressions.xp
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.FormChecker
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.AnyPattern
import engine.patterns.ConstantPattern
import engine.patterns.FixedPattern
import engine.patterns.RecurringDecimalPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.UnsignedNumberPattern
import engine.patterns.condition
import engine.patterns.equationOf
import engine.patterns.equationUnionOf
import engine.patterns.fractionOf
import engine.patterns.inSolutionVariable
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.productContaining
import engine.patterns.solutionOf
import engine.patterns.solutionSetOf
import engine.patterns.sumContaining
import engine.patterns.withOptionalConstantCoefficient
import engine.steps.metadata.metadata
import methods.constantexpressions.ConstantExpressionsPlans
import methods.constantexpressions.simpleTidyUpSteps
import methods.equations.EquationsRules.FactorNegativeSignOfLeadingCoefficient
import methods.general.GeneralRules
import methods.general.NormalizationPlans
import methods.polynomials.PolynomialsPlans
import methods.polynomials.PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization
import methods.polynomials.PolynomialsPlans.SimplifyAlgebraicExpressionInOneVariable
import methods.polynomials.algebraicSimplificationSteps
import methods.solvable.SolvableRules

enum class EquationsPlans(override val runner: CompositeMethod) : RunnerMethod {

    SimplifyByFactoringNegativeSignOfLeadingCoefficient(
        plan {
            explanation = Explanation.SimplifyByFactoringNegativeSignOfLeadingCoefficient

            steps {
                apply(FactorNegativeSignOfLeadingCoefficient)
                apply(EquationsRules.EliminateConstantFactorOfLhsWithZeroRhs)
            }
        },
    ),

    SimplifyByDividingByGcfOfCoefficients(
        plan {
            explanation = Explanation.SimplifyByDividingByGcfOfCoefficients

            steps {
                applyTo(PolynomialsPlans.FactorGreatestCommonFactor) { it.firstChild }
                apply(EquationsRules.EliminateConstantFactorOfLhsWithZeroRhs)
            }
        },
    ),

    MoveConstantsToTheLeftAndSimplify(
        plan {
            explanation = Explanation.MoveConstantsToTheLeftAndSimplify

            steps {
                apply(SolvableRules.MoveConstantsToTheLeft)
                optionally(simplifyEquation)
            }
        },
    ),

    MoveConstantsToTheRightAndSimplify(
        plan {
            explanation = Explanation.MoveConstantsToTheRightAndSimplify

            steps {
                apply(SolvableRules.MoveConstantsToTheRight)
                optionally(simplifyEquation)
            }
        },
    ),

    MoveVariablesToTheLeftAndSimplify(
        plan {
            explanation = Explanation.MoveVariablesToTheLeftAndSimplify

            steps {
                apply(SolvableRules.MoveVariablesToTheLeft)
                optionally(simplifyEquation)
            }
        },
    ),

    MoveEverythingToTheLeftAndSimplify(
        plan {
            explanation = Explanation.MoveEverythingToTheLeftAndSimplify

            steps {
                apply(EquationsRules.MoveEverythingToTheLeft)
                optionally(simplifyEquation)
            }
        },
    ),

    MultiplyByInverseCoefficientOfVariableAndSimplify(
        plan {
            explanation = Explanation.MultiplyByInverseCoefficientOfVariableAndSimplify

            steps {
                apply(EquationsRules.MultiplyByInverseCoefficientOfVariable)
                optionally(simplifyEquation)
            }
        },
    ),

    MultiplyByLCDAndSimplify(
        plan {
            explanation = Explanation.MultiplyByLCDAndSimplify

            steps {
                apply(SolvableRules.MultiplySolvableByLCD)
                whilePossible(ExpandPolynomialExpressionInOneVariableWithoutNormalization)
            }
        },
    ),

    DivideByCoefficientOfVariableAndSimplify(
        plan {
            explanation = Explanation.DivideByCoefficientOfVariableAndSimplify

            steps {
                apply(EquationsRules.DivideByCoefficientOfVariable)
                optionally(simplifyEquation)
            }
        },
    ),

    CompleteTheSquareAndSimplify(
        plan {
            explanation = Explanation.CompleteTheSquareAndSimplify

            steps {
                apply(EquationsRules.CompleteTheSquare)
                optionally(SimplifyAlgebraicExpressionInOneVariable)
            }
        },
    ),

    MultiplyByInverseOfLeadingCoefficientAndSimplify(
        plan {
            explanation = Explanation.MultiplyByInverseOfLeadingCoefficientAndSimplify

            steps {
                apply(EquationsRules.MultiplyByInverseOfLeadingCoefficient)
                apply(PolynomialsPlans.ExpandPolynomialExpressionInOneVariable)
            }
        },
    ),

    ExtractSolutionAndSimplifyFromEquationInPlusMinusForm(extractSolutionAndSimplifyFromEquationInPlusMinusForm),

    SolveFactorisedEquation(
        taskSet {
            val product = productContaining()
            val zero = FixedPattern(Constants.Zero)
            pattern = equationOf(product, zero)
            explanation = Explanation.SolveFactorisedEquation

            tasks {
                val tasks = get(product).children.mapNotNull {
                    if (!it.isConstantIn(context.solutionVariable)) {
                        task(
                            startExpr = equationOf(it, get(zero)),
                            explanation = metadata(Explanation.SolveFactorOfEquation, it),
                            stepsProducer = optimalEquationSolvingSteps,
                        ) ?: return@tasks null
                    } else {
                        null
                    }
                }

                val solutions = solutionSetOf(tasks.flatMap { it.result.secondChild.children })

                task(
                    startExpr = solutionOf(xp(context.solutionVariable!!), solutions),
                    explanation = metadata(Explanation.CollectSolutions),
                    dependsOn = tasks,
                )
                allTasks()
            }
        },
    ),

    /**
     * Solve a linear equation in one variable
     */
    @PublicMethod
    SolveLinearEquation(
        plan {
            explanation = Explanation.SolveLinearEquation
            pattern = equationInOneVariable()

            steps {
                whilePossible {
                    firstOf {
                        option(equationSimplificationSteps)
                        option(MultiplyByLCDAndSimplify)
                        option(ExpandPolynomialExpressionInOneVariableWithoutNormalization)
                    }
                }

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
                        FormChecker(solutionOf(SolutionVariablePattern(), AnyPattern())),
                    )
                    alternative(
                        ResourceData(preferDecimals = false, gmFriendly = true),
                        FormChecker(equationOf(SolutionVariablePattern(), AnyPattern())),
                    )
                    alternative(
                        ResourceData(preferDecimals = true),
                        decimalSolutionFormChecker,
                    )
                }
            }
        },
    ),

    /**
     * Solve an equation in one variable with no linear term by writing it in the form
     *
     *     x^n = k
     *
     * and taking the nth root from both sides
     */
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

    ExtractSolutionAndSimplifyFromEquationInUnionForm(extractSolutionAndSimplifyFromEquationInUnionForm),

    /**
     * Solve a quadratic equation by completing the square.
     */
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
                        applyTo(PolynomialsPlans.FactorTrinomialToSquareAndSimplify) { it.firstChild }
                    }
                    option {
                        // Else rearrange to put constants on the right and complete the square
                        optionally(MoveConstantsToTheRightAndSimplify)
                        apply(CompleteTheSquareAndSimplify)
                        applyTo(PolynomialsPlans.FactorTrinomialToSquareAndSimplify) { it.firstChild }
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

    /**
     * Solve an equation by writing it as a product of factors equal to 0 and solving
     * each equation.
     */
    @PublicMethod
    SolveEquationByFactoring(
        plan {
            explanation = Explanation.SolveEquationByFactoring

            steps {
                optionally(MoveEverythingToTheLeftAndSimplify)
                optionally {
                    applyTo(PolynomialsPlans.FactorPolynomialInOneVariable) { it.firstChild }
                }
                apply(SolveFactorisedEquation)
            }
        },
    ),

    /**
     * Solve a quadratic equation using the quadratic formula.
     */
    @PublicMethod
    SolveQuadraticEquationUsingQuadraticFormula(
        plan {
            explanation = Explanation.SolveQuadraticEquationUsingQuadraticFormula
            pattern = equationOf(AnyPattern(), AnyPattern())

            steps {
                optionally(equationSimplificationSteps)

                optionally {
                    applyTo(ExpandPolynomialExpressionInOneVariableWithoutNormalization) { it.firstChild }
                }
                optionally {
                    applyTo(ExpandPolynomialExpressionInOneVariableWithoutNormalization) { it.secondChild }
                }
                optionally(MultiplyByLCDAndSimplify)
                optionally(MoveVariablesToTheLeftAndSimplify)
                optionally(MoveConstantsToTheLeftAndSimplify)

                // normalize to the form: a[x^2] + bx + c = 0, where a > 0
                optionally(SimplifyByFactoringNegativeSignOfLeadingCoefficient)

                // normalize to the form: a[x^2] + bx + c = 0, where gcd(a,b,c) = 1
                optionally(SimplifyByDividingByGcfOfCoefficients)

                apply(EquationsRules.ApplyQuadraticFormula)
                optionally {
                    applyTo(SimplifyAlgebraicExpressionInOneVariable) { it.secondChild }
                }

                // Δ
                firstOf {
                    // Δ = 0
                    option {
                        // x = [4 +/- 0 / 2] --> x = [4 / 2]
                        apply { deeply(GeneralRules.EliminatePlusMinusZeroInSum) }
                        // [4 / 2] --> 2
                        optionally {
                            applyTo(SimplifyAlgebraicExpressionInOneVariable) { it.secondChild }
                        }
                        apply(EquationsRules.ExtractSolutionFromEquationInSolvedForm)
                    }
                    // Δ < 0
                    option { deeply(EquationsRules.ExtractSolutionFromNegativeUnderSquareRootInRealDomain) }
                    // Δ > 0
                    option {
                        apply(EquationsRules.SeparatePlusMinusQuadraticSolutions)
                        firstOf {
                            option(ExtractSolutionAndSimplifyFromEquationInUnionForm)
                            option(EquationsRules.ExtractSolutionFromEquationInUnionForm)
                        }
                    }
                }
            }
        },
    ),
}

private val extractSolutionAndSimplifyFromEquationInUnionForm = taskSet {
    val lhs = SolutionVariablePattern()
    val rhs1 = ConstantPattern()
    val rhs2 = ConstantPattern()
    val eq1 = equationOf(lhs, rhs1)
    val eq2 = equationOf(lhs, rhs2)

    pattern = equationUnionOf(eq1, eq2)
    explanation = Explanation.ExtractSolutionAndSimplifyFromEquationInUnionForm

    tasks {
        val task1 = task(
            startExpr = get(eq1),
            explanation = metadata(Explanation.SimplifyExtractedSolution),
            stepsProducer = SimplifyAlgebraicExpressionInOneVariable,
        )
            ?: return@tasks null
        val task2 = task(
            startExpr = get(eq2),
            explanation = metadata(Explanation.SimplifyExtractedSolution),
            stepsProducer = SimplifyAlgebraicExpressionInOneVariable,
        )
            ?: return@tasks null
        val solution1 = task1.result.secondChild
        val solution2 = task2.result.secondChild
        task(
            startExpr = solutionOf(task1.result.firstChild, solutionSetOf(solution1, solution2)),
            explanation = metadata(Explanation.CollectSolutions),
            dependsOn = listOf(task1, task2),
        )
        allTasks()
    }
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

private val optimalEquationSolvingSteps = steps {
    firstOf {
        option(EquationsPlans.SolveLinearEquation)
        option(EquationsPlans.SolveEquationUsingRootsMethod)
        option(EquationsPlans.SolveQuadraticEquationByCompletingTheSquare)
    }
}

private val simplifyEquation = plan {
    explanation = Explanation.SimplifyEquation
    specificPlans(ConstantExpressionsPlans.SimplifyConstantExpression)

    steps {
        whilePossible { deeply(simpleTidyUpSteps) }
        optionally(NormalizationPlans.NormalizeExpression)
        whilePossible(SolvableRules.CancelCommonTermsOnBothSides)
        whilePossible(algebraicSimplificationSteps)
    }
}

private val equationSimplificationSteps = steps {
    whilePossible {
        firstOf {
            // before we cancel we always have to check for an identity
            option(EquationsRules.ExtractSolutionFromIdentity)
            // normalize the equation
            option(simplifyEquation)
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
