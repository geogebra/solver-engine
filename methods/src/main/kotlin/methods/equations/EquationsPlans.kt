package methods.equations

import engine.context.ResourceData
import engine.expressions.Contradiction
import engine.expressions.SetSolution
import engine.expressions.VariableList
import engine.expressions.setSolutionOf
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
import engine.patterns.BinaryIntegerCondition
import engine.patterns.ConditionPattern
import engine.patterns.Pattern
import engine.patterns.RecurringDecimalPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.UnsignedNumberPattern
import engine.patterns.condition
import engine.patterns.contradictionOf
import engine.patterns.equationOf
import engine.patterns.fractionOf
import engine.patterns.identityOf
import engine.patterns.inSolutionVariables
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.powerOf
import engine.patterns.setSolutionOf
import engine.patterns.solutionSetOf
import engine.patterns.sumContaining
import engine.patterns.variableListOf
import engine.patterns.withOptionalConstantCoefficient
import engine.patterns.withOptionalIntegerCoefficient
import engine.steps.metadata.metadata
import methods.constantexpressions.ConstantExpressionsPlans
import methods.constantexpressions.simpleTidyUpSteps
import methods.equations.EquationsRules.FactorNegativeSignOfLeadingCoefficient
import methods.general.NormalizationPlans
import methods.polynomials.PolynomialRules
import methods.polynomials.PolynomialsPlans
import methods.polynomials.PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization
import methods.polynomials.PolynomialsPlans.SimplifyAlgebraicExpressionInOneVariable
import methods.polynomials.algebraicSimplificationSteps
import methods.solvable.ApplySolvableRuleAndSimplify
import methods.solvable.DenominatorExtractor.extractDenominator
import methods.solvable.SolvableKey
import methods.solvable.SolvableRules
import methods.solvable.extractSumTermsFromSolvable
import methods.solvable.fractionRequiringMultiplication

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
        applySolvableRuleAndSimplify(SolvableKey.MoveConstantsToTheLeft, SolvableRules.MoveConstantsToTheLeft),
    ),

    CollectLikeTermsToTheLeftAndSimplify(
        plan {
            explanation = Explanation.CollectLikeTermsToTheLeftAndSimplify

            steps {
                apply(EquationsRules.CollectLikeTermsToTheLeft)
                optionally(simplifyEquation)
            }
        },
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

    /**
     * rewrite a simplified quadratic equation to [(x + a)^2] = b form
     */
    RewriteToXPLusASquareEqualsBForm(
        plan {
            explanation = Explanation.RewriteToXPLusASquareEqualsBForm
            pattern = equationInOneVariable()

            steps {
                apply(CompleteTheSquareAndSimplify)
                applyTo(PolynomialsPlans.FactorTrinomialToSquareAndSimplify) { it.firstChild }
            }
        },
    ),

    MultiplyByLCDAndSimplify(
        plan {
            explanation = methods.solvable.EquationsExplanation.MultiplyByLCDAndSimplify

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
                apply(ExpandPolynomialExpressionInOneVariableWithoutNormalization)
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
                optionally(rearrangeLinearEquationSteps)

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
                        FormChecker(solutionPattern()),
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
            resultPattern = setSolutionOf(variableListOf(SolutionVariablePattern()), AnyPattern())

            steps {
                optionally(equationSimplificationSteps)
                optionally(MoveVariablesToTheLeftAndSimplify)
                optionally(MoveConstantsToTheRightAndSimplify)

                optionally {
                    firstOf {
                        // get rid of the coefficient of the variable
                        option {
                            checkForm {
                                equationOf(negOf(powerOf(SolutionVariablePattern(), AnyPattern())), AnyPattern())
                            }
                            apply(EquationsRules.NegateBothSides)
                        }
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
            resultPattern = setSolutionOf(variableListOf(SolutionVariablePattern()), AnyPattern())

            steps {

                // Simplify the equation and move variables to the left
                optionally(equationSimplificationSteps)
                optionally {
                    applyTo(ExpandPolynomialExpressionInOneVariableWithoutNormalization) { it.firstChild }
                }
                optionally {
                    applyTo(ExpandPolynomialExpressionInOneVariableWithoutNormalization) { it.secondChild }
                }
                optionally(MoveVariablesToTheLeftAndSimplify)

                // Complete the square
                firstOf {
                    option {
                        // See if we can complete the square straight away
                        optionally(MultiplyByInverseOfLeadingCoefficientAndSimplify)
                        optionally {
                            applyTo(PolynomialRules.NormalizePolynomial) { it.firstChild }
                        }
                        applyTo(PolynomialsPlans.FactorTrinomialToSquareAndSimplify) { it.firstChild }
                    }
                    option {
                        // Else rearrange to put constants on the right and complete the square
                        optionally(MoveConstantsToTheRightAndSimplify)
                        optionally(MultiplyByInverseOfLeadingCoefficientAndSimplify)
                        optionally {
                            applyTo(PolynomialRules.NormalizePolynomial) { it.firstChild }
                        }
                        apply(RewriteToXPLusASquareEqualsBForm)
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

                // rearrange LHS to the form: a[x^2] + bx + c
                optionally {
                    applyTo(PolynomialRules.NormalizePolynomial) { it.firstChild }
                }
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
    SolveEquationWithVariablesInOneAbsoluteValue(
        plan {
            explanation = Explanation.SolveEquationWithVariablesInOneAbsoluteValue
            pattern = equationInOneVariable()

            steps {
                optionally(equationSimplificationSteps)

                optionally {
                    check { it.firstChild.isConstant() }
                    apply(EquationsRules.FlipEquation)
                }

                optionally(MoveConstantsToTheRightAndSimplify)
                optionally(EquationsRules.NegateBothSides)

                firstOf {
                    option {
                        apply(EquationsRules.SeparateModulusEqualsPositiveConstant)
                        apply(solveEquationUnion)
                    }
                    option {
                        apply(EquationsRules.ResolveModulusEqualsZero)
                        apply(SolveEquationInOneVariable)
                    }
                    option(EquationsRules.ExtractSolutionFromModulusEqualsNegativeConstant)
                }
            }
        },
    ),

    @PublicMethod
    SolveEquationInOneVariable(
        plan {
            explanation = Explanation.SolveEquationInOneVariable
            pattern = inSolutionVariables(equationOf(AnyPattern(), AnyPattern()))

            specificPlans(
                SolveLinearEquation,
                SolveEquationUsingRootsMethod,
                SolveEquationByFactoring,
                SolveQuadraticEquationUsingQuadraticFormula,
                SolveByCompletingTheSquare,
                SolveEquationWithVariablesInOneAbsoluteValue,
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
            apply(EquationsPlans.SolveEquationUnion)
        }
        shortOption(EquationsRules.ExtractSolutionFromEquationInPlusMinusForm)
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
        val solutions = splitTasks.flatMap {
            when (val result = it.result) {
                is SetSolution -> result.solutionSet.children
                is Contradiction -> listOf()
                else -> return@tasks null
            }
        }
        task(
            startExpr = setSolutionOf(
                splitTasks[0].result.firstChild as VariableList,
                solutionSetOf(solutions.sortedBy { it.doubleValue }),
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
        option(EquationsPlans.SolveEquationWithVariablesInOneAbsoluteValue)
    }
}

val simplifyEquation = plan {
    explanation = Explanation.SimplifyEquation

    steps {
        whilePossible { deeply(simpleTidyUpSteps) }
        optionally(NormalizationPlans.NormalizeExpression)
        whilePossible(SolvableRules.CancelCommonTermsOnBothSides)
        whilePossible(algebraicSimplificationSteps)
    }
}

private val applySolvableRuleAndSimplify = ApplySolvableRuleAndSimplify(simplifyEquation)::getPlan

val equationSimplificationSteps = steps {
    whilePossible {
        firstOf {
            option(NormalizationPlans.NormalizeExpression)
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

    FormChecker(solutionPattern(acceptedSolutions))
}

val rearrangeLinearEquationSteps = steps {
    whilePossible {
        firstOf {
            option(equationSimplificationSteps)
            option {
                check {
                    val sumTerms = extractSumTermsFromSolvable(it)
                    val denominators = sumTerms.mapNotNull { term -> extractDenominator(term) }

                    denominators.size >= 2 || sumTerms.any { term ->
                        fractionRequiringMultiplication.matches(this, term)
                    }
                }
                apply(EquationsPlans.MultiplyByLCDAndSimplify)
            }
            option(ExpandPolynomialExpressionInOneVariableWithoutNormalization)
        }
    }

    optionally {
        firstOf {
            // three ways to reorganize the equation into ax = b form
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
                optionally(EquationsPlans.MoveConstantsToTheLeftAndSimplify)
                apply(EquationsRules.FlipEquation)
            }
            option {
                // if the equation is in the form `ax + b = cx + d` with an integer and `c` a
                // positive integer such that `c > a`, we move `ax` to the right hand side, `d` to
                // the left hand side and flip the equation
                checkForm {
                    val variable = SolutionVariablePattern()
                    val lhsVariable = withOptionalIntegerCoefficient(variable, false)
                    val rhsVariable = withOptionalIntegerCoefficient(variable, true)

                    val lhs = oneOf(lhsVariable, sumContaining(lhsVariable))
                    val rhs = oneOf(rhsVariable, sumContaining(rhsVariable))

                    ConditionPattern(
                        equationOf(lhs, rhs),
                        BinaryIntegerCondition(
                            lhsVariable.integerCoefficient,
                            rhsVariable.integerCoefficient,
                        ) { n1, n2 -> n2 > n1 },
                    )
                }

                apply(EquationsPlans.MoveVariablesToTheRightAndSimplify)
                optionally(EquationsPlans.MoveConstantsToTheLeftAndSimplify)
                apply(EquationsRules.FlipEquation)
            }
            option {
                // otherwise we first move variables to the left and then constants
                // to the right
                optionally(EquationsPlans.MoveVariablesToTheLeftAndSimplify)
                optionally(EquationsPlans.MoveConstantsToTheRightAndSimplify)
            }
        }
    }

    optionally {
        firstOf {
            // get rid of the coefficient of the variable
            option {
                checkForm {
                    equationOf(negOf(SolutionVariablePattern()), AnyPattern())
                }
                apply(EquationsRules.NegateBothSides)
            }
            option(EquationsPlans.MultiplyByInverseCoefficientOfVariableAndSimplify)
            option(EquationsPlans.DivideByCoefficientOfVariableAndSimplify)
        }
    }
}

private fun equationInOneVariable() = inSolutionVariables(equationOf(AnyPattern(), AnyPattern()))

private fun solutionPattern(solutionValuePattern: Pattern = AnyPattern()) = oneOf(
    setSolutionOf(variableListOf(SolutionVariablePattern()), solutionSetOf(solutionValuePattern)),
    contradictionOf(variableListOf(SolutionVariablePattern())),
    identityOf(variableListOf(SolutionVariablePattern())),
)
