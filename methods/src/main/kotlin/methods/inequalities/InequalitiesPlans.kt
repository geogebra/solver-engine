package methods.inequalities

import engine.context.ResourceData
import engine.expressions.Constants
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.FormChecker
import engine.methods.stepsproducers.steps
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.RecurringDecimalPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.UnsignedNumberPattern
import engine.patterns.closedOpenIntervalOf
import engine.patterns.condition
import engine.patterns.fractionOf
import engine.patterns.inSolutionVariable
import engine.patterns.inequalityOf
import engine.patterns.oneOf
import engine.patterns.openClosedIntervalOf
import engine.patterns.openIntervalOf
import engine.patterns.optionalNegOf
import engine.patterns.solutionOf
import engine.patterns.sumContaining
import engine.patterns.withOptionalConstantCoefficient
import methods.polynomials.PolynomialsPlans
import methods.solvable.SolvableRules

enum class InequalitiesPlans(override val runner: CompositeMethod) : RunnerMethod {

    MoveConstantsToTheLeftAndSimplify(
        plan {
            explanation = Explanation.MoveConstantsToTheLeftAndSimplify

            steps {
                apply(SolvableRules.MoveConstantsToTheLeft)
                optionally(PolynomialsPlans.SimplifyAlgebraicExpressionInOneVariableWithoutNormalization)
            }
        },
    ),

    MoveConstantsToTheRightAndSimplify(
        plan {
            explanation = Explanation.MoveConstantsToTheRightAndSimplify

            steps {
                apply(SolvableRules.MoveConstantsToTheRight)
                optionally(PolynomialsPlans.SimplifyAlgebraicExpressionInOneVariableWithoutNormalization)
            }
        },
    ),

    MoveVariablesToTheLeftAndSimplify(
        plan {
            explanation = Explanation.MoveVariablesToTheLeftAndSimplify

            steps {
                apply(SolvableRules.MoveVariablesToTheLeft)
                optionally(PolynomialsPlans.SimplifyAlgebraicExpressionInOneVariableWithoutNormalization)
            }
        },
    ),

    MultiplyByInverseCoefficientOfVariableAndSimplify(
        plan {
            explanation = Explanation.MultiplyByInverseCoefficientOfVariableAndSimplify

            steps {
                apply(InequalitiesRules.MultiplyByInverseCoefficientOfVariable)
                optionally(PolynomialsPlans.SimplifyAlgebraicExpressionInOneVariableWithoutNormalization)
            }
        },
    ),

    MultiplyByLCDAndSimplify(
        plan {
            explanation = Explanation.MultiplyByLCDAndSimplify

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
                optionally(PolynomialsPlans.SimplifyAlgebraicExpressionInOneVariableWithoutNormalization)
            }
        },
    ),

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
                        option(MultiplyByLCDAndSimplify)
                        option(PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization)
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
                                inequalityOf(lhs, rhs)
                            }
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
}

private val inequalitySimplificationSteps = steps {
    whilePossible {
        firstOf {
            option(InequalitiesRules.ExtractSolutionFromConstantInequality)
            option(InequalitiesRules.ExtractSolutionFromConstantInequalityBasedOnSign)
            // normalize the inequality
            option(SolvableRules.CancelCommonTermsOnBothSides)
            option(PolynomialsPlans.SimplifyAlgebraicExpressionInOneVariableWithoutNormalization)
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
                openIntervalOf(FixedPattern(Constants.NegativeInfinity), acceptedSolutions),
                openClosedIntervalOf(FixedPattern(Constants.NegativeInfinity), acceptedSolutions),
                openIntervalOf(acceptedSolutions, FixedPattern(Constants.Infinity)),
                closedOpenIntervalOf(acceptedSolutions, FixedPattern(Constants.Infinity)),
            ),
        ),
    )
}

private fun inequalityInOneVariable() = inSolutionVariable(inequalityOf(AnyPattern(), AnyPattern()))
