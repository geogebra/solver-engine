package methods.equations

import engine.context.StrategySelectionMode
import engine.expressions.Equation
import engine.expressions.Expression
import engine.expressions.hasSingleValue
import engine.methods.PublicStrategy
import engine.methods.Strategy
import engine.methods.StrategyFamily
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.optionalSteps
import engine.methods.stepsproducers.steps
import engine.methods.stepsproducers.whileStrategiesAvailableFirstOf
import methods.constantexpressions.ConstantExpressionsPlans
import methods.equationsystems.EquationSystemsPlans
import methods.factor.FactorPlans
import methods.polynomials.PolynomialsPlans
import methods.polynomials.normalizePolynomialSteps
import methods.rationalexpressions.RationalExpressionsPlans
import methods.simplify.SimplifyPlans
import methods.solvable.DenominatorExtractor.extractFraction
import methods.solvable.SolvableRules
import methods.solvable.countAbsoluteValues
import methods.solvable.extractSumTermsFromSolvable

enum class EquationSolvingStrategy(
    override val family: Family,
    override val priority: Int,
    override val explanation: Explanation,
    override val steps: StepsProducer,
) : Strategy {

    LinearEquation(
        family = Family.LINEAR,
        priority = 100,
        explanation = EquationsExplanation.SolveLinearEquation,
        steps = EquationsRules.ExtractSolutionFromEquationInSolvedForm,
    ),

    /**
     * Solve an equation in one variable in the form p(x)^n = k
     * by taking the nth root from both sides
     */
    @PublicStrategy
    RootsMethod(
        family = Family.POLYNOMIAL,
        priority = 10,
        explanation = EquationsExplanation.SolveEquationUsingRootsMethod,
        steps = steps {
            firstOf {
                // x^2k = something negative
                option(EquationsRules.ExtractSolutionFromEvenPowerEqualsNegative)
                option {
                    firstOf {
                        // x^n = 0
                        option(EquationsRules.TakeRootOfBothSidesRHSIsZero)
                        // x^2k = something positive
                        // x^(2k+1) = something nonzero
                        option(solvablePlansForEquations.takeRootOfBothSidesAndSimplify)
                    }

                    apply(equationSolvingSteps)
                }
            }
        },
    ),

    /**
     * Solve an equation by completing the square.
     * The equation can be of higher order than 2 as long as completing the square is possible.
     */
    @PublicStrategy
    CompletingTheSquare(
        family = Family.POLYNOMIAL,
        priority = 3,
        explanation = EquationsExplanation.SolveByCompletingTheSquare,
        steps = steps {
            optionally(solvablePlansForEquations.moveVariablesToTheLeftAndSimplify)

            // Complete the square
            firstOf {
                option {
                    // See if we can complete the square straight away
                    optionally(EquationsPlans.MultiplyByInverseOfLeadingCoefficientAndSimplify)
                    optionally {
                        applyTo(normalizePolynomialSteps) { it.firstChild }
                    }
                    applyTo(FactorPlans.FactorSquareOfBinomial) { it.firstChild }
                }
                option {
                    // Else rearrange to put constants on the right and complete the square
                    optionally(solvablePlansForEquations.moveConstantsToTheRightAndSimplify)
                    optionally(EquationsPlans.MultiplyByInverseOfLeadingCoefficientAndSimplify)
                    optionally {
                        applyTo(normalizePolynomialSteps) { it.firstChild }
                    }
                    apply(EquationsPlans.RewriteToXPLusASquareEqualsBForm)
                }
            }

            // take root of both sides and solve
            apply(RootsMethod.steps)
        },
    ),

    /**
     * Solve an equation by writing it as a product of factors equal to 0 and solving
     * each equation.
     */
    @PublicStrategy
    Factoring(
        family = Family.POLYNOMIAL,
        priority = 8,
        explanation = EquationsExplanation.SolveEquationByFactoring,
        steps = steps {
            optionally(solvablePlansForEquations.moveEverythingToTheLeftAndSimplify)
            applyTo(FactorPlans.FactorPolynomialInOneVariable) { it.firstChild }
            firstOf {

                // If the equation factored into at least two distinct factors
                option(separateFactoredEquationSteps)

                // If the equation factored into a single power P(x)^n = 0
                option(RootsMethod.steps)
            }
        },
    ),

    /**
     * Solve a quadratic equation using the quadratic formula.
     */
    @PublicStrategy
    QuadraticFormula(
        family = Family.POLYNOMIAL,
        priority = 5,
        explanation = EquationsExplanation.SolveQuadraticEquationUsingQuadraticFormula,
        steps = quadraticFormulaSteps,
    ),

    SolveEquationWithOneAbsoluteValue(
        family = Family.ABSOLUTE_VALUE,
        priority = 0,
        explanation = EquationsExplanation.SolveEquationWithOneAbsoluteValue,
        steps = steps {
            check { it.countAbsoluteValues(solutionVariables) == 1 }

            optionally(EquationsPlans.IsolateAbsoluteValue)
            optionally {
                check { it is Equation && it.rhs.countAbsoluteValues(solutionVariables) > 0 }
                apply(SolvableRules.FlipSolvable)
            }
            optionally(SolvableRules.NegateBothSides)

            firstOf {
                // Cases where the RHS is a constant value

                // RHS > 0
                option {
                    apply(EquationsRules.SeparateModulusEqualsPositiveConstant)
                    apply(EquationsPlans.SolveEquationUnion)
                }

                // RHS = 0
                option {
                    apply(EquationsRules.ResolveModulusEqualsZero)
                    apply(EquationsPlans.SolveEquation)
                }

                // RHS < 0
                option(EquationsRules.ExtractSolutionFromModulusEqualsNegativeConstant)

                // Cases where the RHS is not constant

                // The solution without domain computation doesn't always work, so we try it first
                option {
                    check { isSet(engine.context.Setting.SolveEquationsWithoutComputingTheDomain) }
                    apply(EquationsPlans.SolveEquationWithOneAbsoluteValueBySubstitution)
                }

                option {
                    apply(EquationsRules.SeparateModulusEqualsExpression)
                    apply(EquationsPlans.SolveEquationUnion)
                }
            }
        },
    ),

    SolveEquationWithTwoAbsoluteValues(
        family = Family.ABSOLUTE_VALUE,
        priority = 0,
        explanation = EquationsExplanation.SolveEquationWithTwoAbsoluteValues,
        steps = solveEquationWithTwoAbsoluteValues,
    ),

    SeparateFactoredEquation(
        family = Family.SEPARABLE,
        priority = -1,
        explanation = EquationsExplanation.SolveEquation,
        steps = separateFactoredEquationSteps,
    ),

    ResolvePlusminus(
        family = Family.PLUSMINUS,
        priority = -1,
        explanation = EquationsExplanation.SolveEquation,
        steps = resolvePlusminusSteps,
    ),

    ConstantEquation(
        family = Family.CONSTANT,
        priority = -1,
        explanation = EquationsExplanation.SolveEquation,
        steps = steps {
            check { it.isConstant() }

            shortcut(EquationsRules.ExtractSolutionFromConstantEquation)

            optionally(EquationsPlans.SimplifyEquation)
            shortcut(EquationsRules.ExtractSolutionFromConstantEquation)

            apply(solvablePlansForEquations.moveEverythingToTheLeftAndSimplify)
            apply(EquationsRules.ExtractSolutionFromConstantEquation)
        },
    ),

    RationalEquation(
        family = Family.RATIONAL,
        priority = -1,
        explanation = EquationsExplanation.SolveRationalEquation,
        steps = steps {
            check { it.containsVariableDenominator(solutionVariables) }
            firstOf {
                option {
                    /**
                     * First we check if we can get rid of the denominators by simplifying and expanding.
                     */
                    optionally(EquationsPlans.SimplifyEquation)
                    whilePossible {
                        check { it.containsVariableDenominator(solutionVariables) }
                        apply(PolynomialsPlans.ExpandMostComplexSubterm)
                    }
                    check { !it.containsVariableDenominator(solutionVariables) }
                }
                option {
                    /**
                     * An equation where all the denominators are the same is called a
                     * rational equation with a trivial LCD. Here we don't want to cancel
                     * the fractions (because the cancellation would have to be undone)
                     */
                    apply(EquationsRules.MultiplyBothSidesOfRationalEquationWithTrivialLCD)
                    apply(EquationsPlans.SimplifyEquation)
                }
                option {
                    optionally(EquationsPlans.SimplifyEquation)
                    optionally(factorDenominatorsOfFractions)
                    apply(EquationsRules.MultiplyBothSidesOfRationalEquation)
                    optionally(EquationsPlans.SimplifyEquation)
                }
            }
            apply(equationSolvingSteps)
        },
    ),

    Undefined(
        family = Family.UNDEFINED,
        priority = -1,
        explanation = EquationsExplanation.SolveEquation,
        steps = EquationsRules.UndefinedEquationCannotBeSolved,
    ),

    Fallback(
        family = Family.FALLBACK,
        priority = -1,
        explanation = EquationsExplanation.ReduceEquation,
        steps = optionalSteps {
            optionally(solvablePlansForEquations.moveEverythingToTheLeftAndSimplify)
            optionally {
                applyTo(normalizePolynomialSteps) { it.firstChild }
            }
        },
    ),

    ;

    enum class Family : StrategyFamily {
        LINEAR,
        POLYNOMIAL,
        ABSOLUTE_VALUE,
        SEPARABLE,
        PLUSMINUS,
        RATIONAL,
        CONSTANT,
        UNDEFINED,
        FALLBACK,
    }

    override fun isIncompatibleWith(other: Strategy): Boolean {
        return family != other.family ||
            this == RootsMethod && other == CompletingTheSquare ||
            this == CompletingTheSquare && other == RootsMethod
    }
}

private val solveEquationWithTwoAbsoluteValues = steps {
    check { it.countAbsoluteValues(solutionVariables) == 2 }

    optionally(EquationsPlans.MoveOneModulusToOtherSideAndSimplify)

    optionally(SolvableRules.NegateBothSides)

    firstOf {
        option {
            apply(EquationsRules.SeparateModulusEqualsModulus)
            apply(EquationsPlans.SolveEquationUnion)
        }
        option {
            apply(EquationsRules.ResolveModulusEqualsNegativeModulus)
            apply(EquationSystemsPlans.SolveEquationSystemInOneVariable)
        }
    }
}

private val separateFactoredEquationSteps = steps {
    apply(EquationsRules.SeparateFactoredEquation)
    apply(EquationsPlans.SolveEquationUnion)
}

private val resolvePlusminusSteps = steps {
    check { !it.hasSingleValue() }
    optionally(solvablePlansForEquations.solvableRearrangementSteps)
    firstOf {
        option {
            apply(EquationsRules.SeparateEquationInPlusMinusForm)
            apply(EquationsPlans.SolveEquationUnion)
        }
        shortOption(EquationsRules.ExtractSolutionFromEquationInPlusMinusForm)
    }
}

fun Expression.containsVariableDenominator(solutionVariables: List<String>): Boolean {
    return extractSumTermsFromSolvable(this).mapNotNull { extractFraction(it) }
        .count { !it.denominator.isConstantIn(solutionVariables) } > 0
}

private val factorDenominatorsOfFractions = plan {
    explanation = Explanation.FactorDenominatorOfFraction
    steps {
        whilePossible {
            deeply(RationalExpressionsPlans.FactorDenominatorOfFraction)
        }
    }
}

internal val solveEquation = lazy {
    // All strategies excepting special ones
    val regularStrategies = EquationSolvingStrategy.entries.filter { it.priority >= 0 }

    whileStrategiesAvailableFirstOf(EquationSolvingStrategy.entries) {
        // before we simplify we always have to check for an identity / trivial contradiction
        option(EquationSolvingStrategy.ConstantEquation)
        option(EquationSolvingStrategy.Undefined)

        option(EquationSolvingStrategy.RationalEquation)

        // simplify the equation
        option(EquationsPlans.SimplifyEquation)

        // Split up equations containing +/- and solve them
        option(EquationSolvingStrategy.ResolvePlusminus)

        // Remove constant coefficient but only for linear equations.  This is so the next options doesn't do something
        // more complicated with the coefficients.
        option(solvablePlansForEquations.linearCoefficientRemovalSteps)

        // Remove constant denominators when it makes the equation simpler
        option(solvablePlansForEquations.removeConstantDenominatorsSteps)

        // Try all regular strategies
        for (strategy in regularStrategies) {
            option(strategy)
        }

        option(solvablePlansForEquations.solvableRearrangementSteps)

        option(EquationSolvingStrategy.SeparateFactoredEquation)

        option(PolynomialsPlans.ExpandSingleBracketWithIntegerCoefficient)

        option(solvablePlansForEquations.coefficientRemovalSteps)

        option(PolynomialsPlans.ExpandMostComplexSubterm)

        fallback(EquationSolvingStrategy.Fallback)
    }
}

private val equationSolvingSteps = StepsProducer { ctx, sub ->
    solveEquation.value
        .run(ctx.copy(strategySelectionMode = StrategySelectionMode.HIGHEST_PRIORITY), sub)
        ?.steps
}

private val quadraticFormulaSteps = steps {
    optionally(solvablePlansForEquations.multiplyByLCDAndSimplify)
    optionally(solvablePlansForEquations.moveEverythingToTheLeftAndSimplify)

    // rearrange LHS to the form: a[x^2] + bx + c
    optionally {
        applyTo(normalizePolynomialSteps) { it.firstChild }
    }

    // normalize to the form: a[x^2] + bx + c = 0, where a > 0
    optionally(EquationsPlans.SimplifyByFactoringNegativeSignOfLeadingCoefficient)
    // normalize to the form: a[x^2] + bx + c = 0, where gcd(a,b,c) = 1
    optionally(EquationsPlans.SimplifyByDividingByGcfOfCoefficients)

    apply(EquationsRules.ApplyQuadraticFormula)

    optionally {
        applyToConstraint(constraintSimplificationPlan)
    }

    optionally {
        firstOf {
            // Keep this option to make the tests pass.  Perhaps it's ok to remove it.
            option {
                applyTo(ConstantExpressionsPlans.SimplifyConstantExpression) { it.secondChild }
            }
            // This option is for parametric equations.  It is ad-hoc for now, we need a generic plan for simplifying
            // expressions.
            option {
                optionally {
                    applyTo(PolynomialsPlans.ExpandPolynomialExpression) { it.secondChild }
                }
                optionally {
                    applyTo(SimplifyPlans.SimplifyAlgebraicExpression) { it.secondChild }
                }
            }
        }
    }

    optionally {
        firstOf {
            // Δ < 0
            option(EquationsRules.ExtractSolutionFromNegativeUnderSquareRootInRealDomain)
            // Δ > 0
            option {
                check { it.secondChild.isConstant() }
                apply(resolvePlusminusSteps)
            }
            // Δ = 0
            option(EquationsRules.ExtractSolutionFromEquationInSolvedForm)
        }
    }
}
