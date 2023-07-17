package methods.inequations

import engine.context.Context
import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.FiniteSet
import engine.expressions.Inequation
import engine.expressions.SetSolution
import engine.expressions.Solution
import engine.expressions.equationOf
import engine.expressions.setDifferenceOf
import engine.expressions.setSolutionOf
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.AnyPattern
import engine.patterns.condition
import engine.patterns.inequationOf
import engine.steps.Transformation
import engine.steps.metadata.metadata
import methods.constantexpressions.constantSimplificationSteps
import methods.constantexpressions.simpleTidyUpSteps
import methods.equations.EquationsPlans
import methods.equations.EquationsRules
import methods.general.NormalizationPlans
import methods.inequalities.solvablePlansForInequalities
import methods.polynomials.PolynomialsPlans
import methods.polynomials.algebraicSimplificationSteps
import methods.solvable.SolvablePlans
import methods.solvable.SolvableRules
import methods.solvable.evaluateBothSidesNumerically

enum class InequationsPlans(override val runner: CompositeMethod) : RunnerMethod {

    SimplifyInequation(
        plan {
            explanation = Explanation.SimplifyInequation

            steps {
                whilePossible { deeply(simpleTidyUpSteps) }
                optionally(NormalizationPlans.NormalizeExpression)
                whilePossible(EquationsRules.EliminateConstantFactorOfLhsWithZeroRhs)
                whilePossible(SolvableRules.CancelCommonTermsOnBothSides)
                whilePossible(algebraicSimplificationSteps)
            }
        },
    ),

    SolveLinearInequation(
        plan {
            explanation = Explanation.SolveInequationInOneVariable
            pattern = condition { it is Inequation && it.variables.size == 1 }
            resultPattern = condition { it is Solution }

            val solvablePlansForInequations = SolvablePlans(SimplifyInequation)

            steps {
                whilePossible {
                    firstOf {
                        option(NormalizationPlans.NormalizeExpression)
                        // check for contradiction or identity
                        option(InequationsRules.ExtractSolutionFromConstantInequation)
                        // normalize the equation
                        option(SimplifyInequation)

                        option(solvablePlansForInequations.removeConstantDenominatorsSteps)
                        option(PolynomialsPlans.ExpandPolynomialExpressionInOneVariableWithoutNormalization)
                    }
                }

                optionally(solvablePlansForInequations.solvableRearrangementSteps)
                optionally(solvablePlansForInequations.coefficientRemovalSteps)
                optionally(InequationsRules.ExtractSolutionFromInequationInSolvedForm)
            }
        },
    ),

    SolveInequationBySolvingCorrespondingEquation(
        taskSet {
            explanation = Explanation.SolveInequationInOneVariable

            val lhs = AnyPattern()
            val rhs = AnyPattern()
            pattern = condition(inequationOf(lhs, rhs)) { it.variables.size == 1 }

            tasks {
                val equationSolutionTask = task(
                    startExpr = equationOf(get(lhs), get(rhs)),
                    explanation = metadata(Explanation.SolveEquationCorrespondingToInequation),
                    stepsProducer = EquationsPlans.SolveEquationInOneVariable,
                ) ?: return@tasks null

                val result = equationSolutionTask.result

                if (result is SetSolution && result.solutionSet is FiniteSet) {
                    task(
                        startExpr = setSolutionOf(
                            result.solutionVariables,
                            setDifferenceOf(Constants.Reals, result.solutionSet),
                        ),
                        explanation = metadata(Explanation.TakeComplementOfSolution),
                    )
                } else {
                    return@tasks null
                }

                allTasks()
            }
        },
    ),

    @PublicMethod
    SolveInequationInOneVariable(
        object : CompositeMethod() {
            override fun run(ctx: Context, sub: Expression): Transformation? {
                return SolveLinearInequation.tryExecute(ctx, sub)
                    ?: SolveInequationBySolvingCorrespondingEquation.tryExecute(ctx, sub)
            }
        },
    ),

    @PublicMethod
    SolveConstantInequation(
        plan {
            explanation = Explanation.SolveConstantInequation

            steps {
                apply(solveConstantInequationSteps)
            }
        },
    ),
}

val solveConstantInequationSteps = steps {
    check { it is Inequation && it.isConstant() }
    optionally {
        plan {
            explanation = Explanation.SimplifyInequation

            steps {
                whilePossible(constantSimplificationSteps)
            }
        }
    }
    shortcut(InequationsRules.ExtractSolutionFromConstantInequation)
    optionally(InequationsPlans.SimplifyInequation)
    shortcut(InequationsRules.ExtractSolutionFromConstantInequation)
    optionally(solvablePlansForInequalities.moveEverythingToTheLeftAndSimplify)
    shortcut(InequationsRules.ExtractSolutionFromConstantInequation)
    inContext(contextFactory = { copy(precision = 10) }) {
        apply(evaluateBothSidesNumerically)
    }
}
