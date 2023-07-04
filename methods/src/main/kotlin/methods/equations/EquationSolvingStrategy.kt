package methods.equations

import engine.context.Context
import engine.context.Curriculum
import engine.context.StrategySelectionMode
import engine.expressions.Equation
import engine.expressions.Expression
import engine.expressions.hasSingleValue
import engine.methods.PublicStrategy
import engine.methods.Strategy
import engine.methods.StrategyFamily
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.steps
import engine.methods.stepsproducers.whileStrategiesAvailableFirstOf
import methods.constantexpressions.ConstantExpressionsPlans
import methods.equationsystems.EquationSystemsPlans
import methods.factor.FactorPlans
import methods.polynomials.PolynomialRules
import methods.polynomials.PolynomialsPlans
import methods.solvable.countAbsoluteValues

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
                        option(EquationsRules.TakeRootOfBothSides)
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
            optionally(EquationsPlans.MoveVariablesToTheLeftAndSimplify)

            // Complete the square
            firstOf {
                option {
                    // See if we can complete the square straight away
                    optionally(EquationsPlans.MultiplyByInverseOfLeadingCoefficientAndSimplify)
                    optionally {
                        applyTo(PolynomialRules.NormalizePolynomial) { it.firstChild }
                    }
                    applyTo(FactorPlans.FactorSquareOfBinomial) { it.firstChild }
                }
                option {
                    // Else rearrange to put constants on the right and complete the square
                    optionally(EquationsPlans.MoveConstantsToTheRightAndSimplify)
                    optionally(EquationsPlans.MultiplyByInverseOfLeadingCoefficientAndSimplify)
                    optionally {
                        applyTo(PolynomialRules.NormalizePolynomial) { it.firstChild }
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
            optionally(EquationsPlans.MoveEverythingToTheLeftAndSimplify)
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
        steps = steps {
            optionally(EquationsPlans.MultiplyByLCDAndSimplify)
            optionally(EquationsPlans.MoveEverythingToTheLeftAndSimplify)
            optionally {
                applyTo(PolynomialsPlans.ExpandPolynomialExpressionInOneVariable) { it.firstChild }
            }

            // rearrange LHS to the form: a[x^2] + bx + c
            optionally {
                applyTo(PolynomialRules.NormalizePolynomial) { it.firstChild }
            }
            // normalize to the form: a[x^2] + bx + c = 0, where a > 0
            optionally(EquationsPlans.SimplifyByFactoringNegativeSignOfLeadingCoefficient)
            // normalize to the form: a[x^2] + bx + c = 0, where gcd(a,b,c) = 1
            optionally(EquationsPlans.SimplifyByDividingByGcfOfCoefficients)

            apply(EquationsRules.ApplyQuadraticFormula)

            optionally {
                applyTo(ConstantExpressionsPlans.SimplifyConstantExpression) { it.secondChild }
            }

            firstOf {
                // Δ < 0
                option(EquationsRules.ExtractSolutionFromNegativeUnderSquareRootInRealDomain)
                // Δ > 0
                option(resolvePlusminusSteps)
                // Δ = 0
                option(EquationsRules.ExtractSolutionFromEquationInSolvedForm)
            }
        },
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
                apply(EquationsRules.FlipEquation)
            }
            optionally(EquationsRules.NegateBothSides)

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
                    apply(EquationsPlans.SolveEquationInOneVariable)
                }

                // RHS < 0
                option(EquationsRules.ExtractSolutionFromModulusEqualsNegativeConstant)

                // Cases where the RHS is not constant

                // The US method doesn't use equations with constraints but doesn't always work, so we
                // try it first if the curriculum is US
                option {
                    check { curriculum == Curriculum.US }
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
        explanation = EquationsExplanation.SolveEquationInOneVariable,
        steps = separateFactoredEquationSteps,
    ),

    ResolvePlusminus(
        family = Family.PLUSMINUS,
        priority = -1,
        explanation = EquationsExplanation.SolveEquationInOneVariable,
        steps = resolvePlusminusSteps,
    ),

    ExtractSolutionFromConstantEquation(
        family = Family.CONSTANT,
        priority = -1,
        explanation = EquationsExplanation.SolveEquationInOneVariable,
        steps = EquationsRules.ExtractSolutionFromConstantEquation,
    ),

    ;

    enum class Family : StrategyFamily {
        LINEAR,
        POLYNOMIAL,
        ABSOLUTE_VALUE,
        SEPARABLE,
        PLUSMINUS,
        CONSTANT,
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

    optionally(EquationsRules.NegateBothSides)

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
    optionally(equationRearrangementSteps)
    firstOf {
        option {
            apply(EquationsRules.SeparateEquationInPlusMinusForm)
            apply(EquationsPlans.SolveEquationUnion)
        }
        shortOption(EquationsRules.ExtractSolutionFromEquationInPlusMinusForm)
    }
}

internal val solveEquationInOneVariable = lazy {
    // All strategies excepting special ones
    val regularStrategies = EquationSolvingStrategy.values().filter { it.priority >= 0 }

    whileStrategiesAvailableFirstOf(EquationSolvingStrategy.values()) {

        // before we simplify we always have to check for an identity
        option(EquationSolvingStrategy.ExtractSolutionFromConstantEquation)

        // simplify the equation
        option(simplifyEquation)

        // Split up equations containing +/- and solve them
        option(EquationSolvingStrategy.ResolvePlusminus)

        // Remove constant denominators when it makes the equation simpler
        option(removeConstantDenominatorsSteps)

        // Try all regular strategies
        for (strategy in regularStrategies) {
            option(strategy)
        }

        option(equationRearrangementSteps)

        option(EquationSolvingStrategy.SeparateFactoredEquation)

        option(PolynomialsPlans.ExpandSingleBracketWithIntegerCoefficient)

        option(coefficientRemovalSteps)

        option(PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization)
    }
}

private val equationSolvingSteps = object : StepsProducer {
    override fun produceSteps(ctx: Context, sub: Expression) =
        solveEquationInOneVariable.value
            .run(ctx.copy(strategySelectionMode = StrategySelectionMode.HIGHEST_PRIORITY), sub)
            ?.steps
}
