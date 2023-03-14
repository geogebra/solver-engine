package methods.equations

import engine.context.ResourceData
import engine.expressions.Constants
import engine.expressions.solutionOf
import engine.expressions.solutionSetOf
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.FormChecker
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.operators.EquationUnionOperator
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.RecurringDecimalPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.UnsignedNumberPattern
import engine.patterns.condition
import engine.patterns.equationOf
import engine.patterns.fractionOf
import engine.patterns.inSolutionVariable
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.solutionOf
import engine.patterns.solutionSetOf
import engine.patterns.sumContaining
import engine.patterns.withOptionalConstantCoefficient
import engine.steps.metadata.metadata
import methods.constantexpressions.ConstantExpressionsPlans
import methods.constantexpressions.simpleTidyUpSteps
import methods.equations.EquationsRules.FactorNegativeSignOfLeadingCoefficient
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

    SolveEquationUnion(solveEquationUnion),

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

    /**
     * Solve an equation by completing the square.  The equation can by of higher order than 2 as long
     * as completing the square is possible.
     */
    @PublicMethod
    SolveByCompletingTheSquare(
        plan {
            explanation = Explanation.SolveByCompletingTheSquare
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
                        apply(extractSolutionFromEquationPossiblyInPlusMinusForm)
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
                apply(EquationsRules.SeparateFactoredEquation)
                apply(solveEquationUnion)
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
                    applyTo(ConstantExpressionsPlans.SimplifyConstantExpression) { it.secondChild }
                }

                // Δ
                firstOf {
                    // Δ < 0
                    option { deeply(EquationsRules.ExtractSolutionFromNegativeUnderSquareRootInRealDomain) }
                    // Δ >= 0
                    option(extractSolutionFromEquationPossiblyInPlusMinusForm)
                }
            }
        },
    ),

    @PublicMethod
    SolveEquationInOneVariable(
        plan {
            explanation = Explanation.SolveEquationInOneVariable
            pattern = inSolutionVariable(equationOf(AnyPattern(), AnyPattern()))

            specificPlans(
                SolveLinearEquation,
                SolveEquationUsingRootsMethod,
                SolveEquationByFactoring,
                SolveQuadraticEquationUsingQuadraticFormula,
                SolveByCompletingTheSquare,
            )
            steps {
                apply(optimalEquationSolvingSteps)
            }
        },
    ),
}

private val extractSolutionFromEquationPossiblyInPlusMinusForm = steps {
    firstOf {
        option {
            apply(EquationsRules.SeparateEquationInPlusMinusForm)
            firstOf {
                option(EquationsPlans.SolveEquationUnion)
            }
        }
        option(EquationsRules.ExtractSolutionFromEquationInPlusMinusForm)
        option(EquationsRules.ExtractSolutionFromEquationInSolvedForm)
    }
}

private val solveEquationUnion = taskSet {
    val equationUnion = condition(AnyPattern()) { it.operator == EquationUnionOperator }
    pattern = equationUnion
    explanation = Explanation.SolveEquationUnion

    tasks {

        // Create a task for each to simplify it
        val splitTasks = get(equationUnion).children.map {
            task(
                startExpr = it,
                explanation = metadata(Explanation.SolveEquationInEquationUnion),
                stepsProducer = optimalEquationSolvingSteps,
            ) ?: return@tasks null
        }

        // Gather all solutions together in a single solution set.
        task(
            startExpr = solutionOf(
                splitTasks[0].result.firstChild,
                solutionSetOf(splitTasks.flatMap { it.result.secondChild.children }.sortedBy { it.doubleValue }),
            ),
            explanation = metadata(Explanation.CollectSolutions),
        )
        allTasks()
    }
}

private val optimalEquationSolvingSteps = steps {
    firstOf {
        option {
            optionally(simplifyEquation)
            apply(EquationsRules.ExtractSolutionFromEquationInSolvedForm)
        }
        option(EquationsPlans.SolveLinearEquation)
        option(EquationsPlans.SolveEquationUsingRootsMethod)
        option(EquationsPlans.SolveEquationByFactoring)
        option(EquationsPlans.SolveQuadraticEquationUsingQuadraticFormula)
        option(EquationsPlans.SolveByCompletingTheSquare)
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
