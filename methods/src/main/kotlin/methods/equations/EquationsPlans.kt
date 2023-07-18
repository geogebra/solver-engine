package methods.equations

import engine.context.Context
import engine.expressions.Constants
import engine.expressions.Equation
import engine.expressions.Expression
import engine.expressions.StatementUnion
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.RecurringDecimalPattern
import engine.patterns.SignedNumberPattern
import engine.patterns.SolutionVariablePattern
import engine.patterns.UnsignedNumberPattern
import engine.patterns.condition
import engine.patterns.contradictionOf
import engine.patterns.fractionOf
import engine.patterns.identityOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
import engine.patterns.setSolutionOf
import engine.patterns.solutionSetOf
import engine.patterns.variableListOf
import engine.steps.Transformation
import engine.steps.metadata.metadata
import methods.constantexpressions.constantSimplificationSteps
import methods.constantexpressions.simpleTidyUpSteps
import methods.factor.FactorPlans
import methods.factor.FactorRules
import methods.general.NormalizationPlans
import methods.polynomials.PolynomialsPlans
import methods.polynomials.algebraicSimplificationSteps
import methods.solvable.SolvablePlans
import methods.solvable.SolvableRules
import methods.solvable.computeOverallUnionSolution
import methods.solvable.evaluateBothSidesNumerically

enum class EquationsPlans(override val runner: CompositeMethod) : RunnerMethod {

    SimplifyEquation(
        plan {
            explanation = Explanation.SimplifyEquation

            steps {
                whilePossible { deeply(simpleTidyUpSteps) }
                optionally(NormalizationPlans.NormalizeExpression)
                whilePossible(EquationsRules.EliminateConstantFactorOfLhsWithZeroRhs)
                whilePossible(SolvableRules.CancelCommonTermsOnBothSides)
                whilePossible(algebraicSimplificationSteps)
            }
        },
    ),

    CollectLikeTermsToTheLeftAndSimplify(
        plan {
            explanation = Explanation.CollectLikeTermsToTheLeftAndSimplify

            steps {
                apply(EquationsRules.CollectLikeTermsToTheLeft)
                optionally(SimplifyEquation)
            }
        },
    ),

    SimplifyByFactoringNegativeSignOfLeadingCoefficient(
        plan {
            explanation = Explanation.SimplifyByFactoringNegativeSignOfLeadingCoefficient

            steps {
                check { it.secondChild == Constants.Zero }
                applyTo(FactorRules.FactorNegativeSignOfLeadingCoefficient) { it.firstChild }
                apply(SolvableRules.NegateBothSides)
            }
        },
    ),

    SimplifyByDividingByGcfOfCoefficients(
        plan {
            explanation = Explanation.SimplifyByDividingByGcfOfCoefficients

            steps {
                applyTo(FactorPlans.FactorGreatestCommonIntegerFactor) { it.firstChild }
                apply(EquationsRules.EliminateConstantFactorOfLhsWithZeroRhs)
            }
        },
    ),

    /**
     * rewrite a simplified quadratic equation to [(x + a)^2] = b form
     */
    RewriteToXPLusASquareEqualsBForm(
        plan {
            explanation = Explanation.RewriteToXPLusASquareEqualsBForm

            steps {
                apply(CompleteTheSquareAndSimplify)
                applyTo(FactorPlans.FactorSquareOfBinomial) { it.firstChild }
            }
        },
    ),

    CompleteTheSquareAndSimplify(
        plan {
            explanation = Explanation.CompleteTheSquareAndSimplify

            steps {
                apply(EquationsRules.CompleteTheSquare)
                optionally(PolynomialsPlans.SimplifyAlgebraicExpressionInOneVariable)
            }
        },
    ),

    MultiplyByInverseOfLeadingCoefficientAndSimplify(
        plan {
            explanation = Explanation.MultiplyByInverseOfLeadingCoefficientAndSimplify

            steps {
                apply(EquationsRules.MultiplyByInverseOfLeadingCoefficient)
                apply(PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization)
            }
        },
    ),

    IsolateAbsoluteValue(
        plan {
            explanation = Explanation.IsolateAbsoluteValue

            steps {
                firstOf {
                    option(SolvableRules.MoveTermsNotContainingModulusToTheRight)
                    option(SolvableRules.MoveTermsNotContainingModulusToTheLeft)
                }
                apply(SimplifyEquation)
            }
        },
    ),

    MoveOneModulusToOtherSideAndSimplify(
        plan {
            explanation = Explanation.MoveOneModulusToOtherSideAndSimplify
            steps {
                firstOf {
                    option(EquationsRules.MoveSecondModulusToRhs)
                    option(EquationsRules.MoveSecondModulusToLhs)
                }
                optionally(SimplifyEquation)
            }
        },
    ),

    SolveEquationUnion(
        taskSet {
            val equationUnion = condition { it is StatementUnion }
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

                // Else combine the solutions together
                val overallSolution = computeOverallUnionSolution(splitTasks.map { it.result }) ?: return@tasks null
                task(
                    startExpr = overallSolution,
                    explanation = metadata(Explanation.CollectSolutions),
                )
                allTasks()
            }
        },
    ),

    SolveEquationWithOneAbsoluteValueBySubstitution(solveEquationWithOneAbsoluteValueBySubstitution),

    @PublicMethod
    SolveDecimalLinearEquation(
        plan {
            explanation = Explanation.SolveDecimalLinearEquation

            val acceptedSolutions = oneOf(
                SignedNumberPattern(),
                optionalNegOf(RecurringDecimalPattern()),
                optionalNegOf(fractionOf(UnsignedNumberPattern(), UnsignedNumberPattern())),
            )

            resultPattern = oneOf(
                setSolutionOf(variableListOf(SolutionVariablePattern()), solutionSetOf(acceptedSolutions)),
                contradictionOf(variableListOf(SolutionVariablePattern())),
                identityOf(variableListOf(SolutionVariablePattern())),
            )

            steps {
                inContext(contextFactory = { copy(preferDecimals = true) }) {
                    apply(rearrangeLinearEquationSteps)
                    optionally(EquationsRules.ExtractSolutionFromEquationInSolvedForm)
                }
            }
        },
    ),

    @PublicMethod
    SolveEquationInOneVariable(
        object : CompositeMethod() {
            override fun run(ctx: Context, sub: Expression): Transformation? {
                if (sub.variables.count() != 1) return null
                return solveEquationInOneVariable.value.run(ctx, sub)
            }
        },
    ),

    @PublicMethod
    SolveEquationWithConstraint(solveEquationWithConstraint),

    @PublicMethod
    SolveConstantEquation(
        plan {
            explanation = Explanation.SolveConstantEquation

            steps {
                apply(solveConstantEquationSteps)
            }
        },
    ),
}

val equationSimplificationSteps = steps {
    whilePossible {
        firstOf {
            option(NormalizationPlans.NormalizeExpression)
            // check for contradiction or identity
            option(EquationsRules.ExtractSolutionFromConstantEquation)
            // normalize the equation
            option(EquationsPlans.SimplifyEquation)
        }
    }
}

private val optimalEquationSolvingSteps = steps {
    firstOf {
        option {
            optionally(EquationsPlans.SimplifyEquation)
            firstOf {
                option(EquationsRules.ExtractSolutionFromEquationInSolvedForm)
                option(EquationsRules.ExtractSolutionFromConstantEquation)
            }
        }
        option(EquationsPlans.SolveEquationInOneVariable)
        option(EquationsPlans.SolveEquationWithConstraint)
    }
}

val solvablePlansForEquations = SolvablePlans(EquationsPlans.SimplifyEquation)

val rearrangeLinearEquationSteps = steps {
    whilePossible {
        firstOf {
            option(NormalizationPlans.NormalizeExpression)
            // check for contradiction or identity
            option(EquationsRules.ExtractSolutionFromConstantEquation)
            // normalize the equation
            option(EquationsPlans.SimplifyEquation)

            option(solvablePlansForEquations.removeConstantDenominatorsSteps)
            option(PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization)
        }
    }

    optionally(solvablePlansForEquations.solvableRearrangementSteps)
    optionally(solvablePlansForEquations.coefficientRemovalSteps)
}

val solveConstantEquationSteps = steps {
    check { it is Equation && it.isConstant() }

    optionally {
        plan {
            explanation = Explanation.SimplifyEquation

            steps {
                whilePossible(constantSimplificationSteps)
            }
        }
    }
    shortcut(EquationsRules.ExtractSolutionFromConstantEquation)

    optionally(EquationsPlans.SimplifyEquation)
    shortcut(EquationsRules.ExtractSolutionFromConstantEquation)

    optionally(solvablePlansForEquations.moveEverythingToTheLeftAndSimplify)
    shortcut(EquationsRules.ExtractSolutionFromConstantEquation)

    inContext(contextFactory = { copy(precision = 10) }) {
        apply(evaluateBothSidesNumerically)
    }
    apply(EquationsRules.ExtractSolutionFromConstantEquation)
}
