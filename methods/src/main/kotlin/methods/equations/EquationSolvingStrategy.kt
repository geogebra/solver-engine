/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package methods.equations

import engine.context.BooleanSetting
import engine.context.Setting
import engine.context.StrategySelectionMode
import engine.expressions.Constants
import engine.expressions.Equation
import engine.expressions.Expression
import engine.expressions.TrigonometricExpression
import engine.expressions.containsTrigExpression
import engine.expressions.hasSingleValue
import engine.methods.PublicStrategy
import engine.methods.Strategy
import engine.methods.StrategyFamily
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.branchOn
import engine.methods.stepsproducers.optionalSteps
import engine.methods.stepsproducers.steps
import engine.methods.stepsproducers.whileStrategiesAvailableFirstOf
import engine.operators.TrigonometricFunctionType
import engine.patterns.AnyPattern
import engine.patterns.FixedPattern
import engine.patterns.TrigonometricExpressionPattern
import engine.patterns.equationOf
import engine.patterns.optionalNegOf
import methods.angles.TrigonometricFunctionsRules
import methods.angles.createEvaluateInverseTrigonometricFunctionExactlyPlan
import methods.constantexpressions.ConstantExpressionsPlans
import methods.equationsystems.EquationSystemsPlans
import methods.factor.FactorPlans
import methods.polynomials.PolynomialsPlans
import methods.polynomials.normalizePolynomialSteps
import methods.rationalexpressions.RationalExpressionsPlans
import methods.simplify.SimplifyPlans
import methods.simplify.algebraicSimplificationSteps
import methods.simplify.algebraicSimplificationStepsForEquations
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
        steps = branchOn(Setting.DontExtractSetSolution) {
            case(BooleanSetting.False) {
                apply(EquationsRules.ExtractSolutionFromEquationInSolvedForm)
            }
        },
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

            optionally(EquationsPlans.MergeTrigonometricEquationSolutionsTask)
        },
    ),

    LogsMethod(
        family = Family.EXPONENTIAL,
        priority = 10,
        explanation = EquationsExplanation.SolveExponentialEquation,
        steps = steps {
            firstOf {
                option(solvablePlansForEquations.takeLogOfRHSAndSimplify)
                option(solvablePlansForEquations.takeLogOfBothSidesAndSimplify)
            }
            apply(equationSolvingSteps)
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
            // We add some optional simplification here so that steps are more linear
            optionally(solvablePlansForEquations.moveEverythingToTheLeftAndSimplify)

            // We try to expand before factoring.
            optionally(algebraicSimplificationSteps)

            applyTo(FactorPlans.FactorPolynomial) { it.firstChild }
            // We want to get rid of the constant factors
            optionally(EquationsPlans.SimplifyEquation)
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

    IncompatibleSigns(
        family = Family.INCOMPATIBLE_SIGNS,
        priority = 0,
        explanation = EquationsExplanation.SolveEquation,
        steps = EquationsRules.SolveEquationWithIncompatibleSigns,
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

    /**
     * - IF possible Balance equations to form sin(x) = sin(y)
     * - IF both sides are negative, negate both of them
     * - IF any side still contains a negative expression, apply negative identity
     * - Apply inverse function to both sides
     * - Try to simplify equation
     */
    TrigonometricEquation(
        family = Family.LINEAR,
        priority = 5,
        explanation = EquationsExplanation.SolveTrigonometricEquation,
        steps = steps {
            check { it.containsTrigExpression() }

            optionally(solvablePlansForEquations.solvableRearrangementSteps)
            optionally(EquationsPlans.SimplifyEquation)
            optionally(EquationsRules.BalanceEquationWithTrigonometricExpressions)
            optionally(SolvableRules.NegateBothSidesIfBothNegative)
            applyToChildren(TrigonometricFunctionsRules.ApplyNegativeIdentityOfTrigFunctionInReverse)

            firstOf {
                option(EquationsRules.ExtractSolutionFromImpossibleSineLikeEquation)

                option(sineLikeEquationSteps)

                option(tanEquationSteps)

                // Upcoming equation types
            }

            optionally(EquationsPlans.MergeTrigonometricEquationSolutionsTask)
        },
    ),

    QuadraticTrigonometricEquation(
        family = Family.POLYNOMIAL,
        priority = 5,
        explanation = EquationsExplanation.SolveQuadraticTrigonometricEquations,
        steps = steps {
            optionally(quadraticPreprocessingSteps)
            optionally(EquationsRules.ReorderQuadraticEquationWithTrigonometricFunctions)

            // a [sin ^ 2][x] + b sin x + c -->
            // ┌ a * [t ^ 2] + b * t + c = 0
            // └ t = sin[x]
            apply(EquationsRules.SubstituteTrigFunctionInQuadraticEquation)

            // solve a * [t^2] + b * t + c = 0
            inContext({ copy(solutionVariables = it.secondChild.firstChild.variables.toList()) }) {
                applyTo(EquationsPlans.SolveEquation) {
                    it.firstChild
                }
            }

            // solve equation system by substituting
            firstOf {
                option(
                    EquationsRules.ExtractSolutionFromImpossibleQuadraticEquationWithTrigonometricExpressions,
                )
                option(EquationsPlans.SubstituteOriginalExpressionIntoQuadraticTrigEquation)
            }

            optionally(EquationsPlans.MergeTrigonometricEquationSolutionsTask)
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
            branchOn(Setting.DontExtractSetSolution) {
                case(BooleanSetting.False) {
                    optionally(solvablePlansForEquations.moveEverythingToTheLeftAndSimplify)
                    optionally {
                        applyTo(normalizePolynomialSteps) { it.firstChild }
                    }
                }
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
        EXPONENTIAL,
        INCOMPATIBLE_SIGNS,
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

        // Check if LHS and RHS have incompatible signs (e.g. [2^x] = -1
        // option(EquationSolvingStrategy.IncompatibleSigns)

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

private val quadraticPreprocessingSteps = steps {
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
}

private val quadraticFormulaSteps = steps {
    optionally(quadraticPreprocessingSteps)

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

private fun expressionOnlyContainsTrigFunctionType(
    expr: Expression,
    trigFunctionType: TrigonometricFunctionType,
): Boolean =
    if (expr is TrigonometricExpression) {
        expr.functionType == trigFunctionType
    } else {
        expr.children.isEmpty() ||
            expr.children.all { expressionOnlyContainsTrigFunctionType(it, trigFunctionType) }
    }

private val evaluateInverseTrigonometricFunctions =
    createEvaluateInverseTrigonometricFunctionExactlyPlan(algebraicSimplificationStepsForEquations)

private val sineLikeEquationSteps = steps {
    check {
        expressionOnlyContainsTrigFunctionType(it, TrigonometricFunctionType.Sin) ||
            expressionOnlyContainsTrigFunctionType(it, TrigonometricFunctionType.Cos)
    }

    apply(EquationsRules.ApplyInverseSineFunctionToBothSides)

    applyTo(
        stepsProducer = steps {
            firstOf {
                option(TrigonometricFunctionsRules.ApplyIdentityOfInverseTrigonometricFunction)
                option(evaluateInverseTrigonometricFunctions)
            }
        },
        extractor = { it.firstChild },
    )

    firstOf {
        option {
            checkForm {
                equationOf(
                    AnyPattern(),
                    TrigonometricExpressionPattern(
                        optionalNegOf(FixedPattern(Constants.One)),
                        listOf(TrigonometricFunctionType.Arcsin, TrigonometricFunctionType.Arccos),
                    ),
                )
            }
            firstOf {
                option {
                    check {
                        expressionOnlyContainsTrigFunctionType(it, TrigonometricFunctionType.Arcsin)
                    }
                    apply(extractedSineEquationSolvingSteps)
                }
                option {
                    check {
                        expressionOnlyContainsTrigFunctionType(it, TrigonometricFunctionType.Arccos)
                    }
                    apply(extractedCosineEquationSolvingSteps)
                }
            }
        }
        option {
            apply(EquationsRules.ExtractSolutionFromEquationWithInverseSineOfZero)

            optionally(
                trigonometricEquationSolvingSteps,
            )
        }
        option {
            apply(EquationsRules.ExtractSolutionFromEquationWithInverseCosineOfZero)

            optionally(
                trigonometricEquationSolvingSteps,
            )
        }
        option {
            check {
                expressionOnlyContainsTrigFunctionType(it, TrigonometricFunctionType.Arcsin)
            }
            apply(EquationsPlans.SineEquationSolutionExtractionTask)
        }
        option {
            check {
                expressionOnlyContainsTrigFunctionType(it, TrigonometricFunctionType.Arccos)
            }
            apply(EquationsPlans.CosineEquationSolutionExtractionTask)
        }
    }
}

private val tanEquationSteps = steps {
    check {
        expressionOnlyContainsTrigFunctionType(it, TrigonometricFunctionType.Tan)
    }

    apply(EquationsRules.ApplyInverseSineFunctionToBothSides)

    applyTo(TrigonometricFunctionsRules.ApplyIdentityOfInverseTrigonometricFunction) {
        it.firstChild
    }

    apply(extractedTanContainingEquationSolvingSteps)
}

val extractedSineEquationSolvingSteps =
    createExtractedTrigonometricEquationSolvingSteps(EquationsRules.AddPeriodicityOfSine)

val extractedCosineEquationSolvingSteps =
    createExtractedTrigonometricEquationSolvingSteps(EquationsRules.AddPeriodicityOfCosine)

val extractedTanContainingEquationSolvingSteps =
    createExtractedTrigonometricEquationSolvingSteps(EquationsRules.AddPeriodicityOfTanLike)

private fun createExtractedTrigonometricEquationSolvingSteps(addPeriodicityRule: StepsProducer) =
    steps {
        optionally {
            applyTo(extractor = { it.secondChild }) {
                firstOf {
                    option {
                        deeply(evaluateInverseTrigonometricFunctions)
                    }
                    option {
                        deeply(TrigonometricFunctionsRules.ApplyIdentityOfInverseTrigonometricFunction)
                    }
                    option {
                        deeply(TrigonometricFunctionsRules.ApplyNegativeIdentityOfTrigFunction)
                    }
                }
            }
        }

        apply(addPeriodicityRule)

        optionally(trigonometricEquationSolvingSteps)
    }

private val trigonometricEquationSolvingSteps = steps {
    // This may seem a bit redundant, but the extractor removes the constraint already :)
    applyTo(extractor = { it }) {
        optionally {
            inContext(
                contextFactory = {
                    copy(settings = settings + Pair(Setting.DontExtractSetSolution, BooleanSetting.True))
                },
            ) {
                apply(EquationsPlans.SolveEquation)
            }
        }

        optionally(algebraicSimplificationSteps)
        optionally(EquationsPlans.NormalizePeriod)

        firstOf {
            option {
                applyTo(extractor = { it }) {
                    optionally(EquationsRules.ExtractSolutionWithoutPeriod)
                    apply(EquationsRules.ExtractSolutionFromConstantEquation)
                }
            }
            option(EquationsRules.ExtractSolutionFromEquationInSolvedForm)
            option(EquationsRules.ExtractSolutionFromConstantEquation)
        }
    }
}
