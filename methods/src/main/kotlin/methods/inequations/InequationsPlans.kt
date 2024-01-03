package methods.inequations

import engine.context.Context
import engine.expressions.Constants
import engine.expressions.Contradiction
import engine.expressions.Expression
import engine.expressions.FiniteSet
import engine.expressions.Identity
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
import engine.patterns.inequationOf
import engine.steps.Transformation
import engine.steps.metadata.MetadataKey
import engine.steps.metadata.metadata
import methods.constantexpressions.constantSimplificationSteps
import methods.constantexpressions.simpleTidyUpSteps
import methods.equations.EquationsPlans
import methods.factor.FactorPlans
import methods.general.NormalizationPlans
import methods.polynomials.PolynomialsPlans
import methods.simplify.algebraicSimplificationStepsWithoutFractionAddition
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
                whilePossible(EquationsPlans.SimplifyByEliminatingConstantFactorOfLhsWithZeroRhs)
                whilePossible(SolvableRules.CancelCommonTermsOnBothSides)
                optionally(algebraicSimplificationStepsWithoutFractionAddition)
            }
        },
    ),

    @PublicMethod
    SolveInequation(solveInequation),

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

private val solveInequation = object : CompositeMethod() {
    val solveOrSimplifyInequation = steps {
        whilePossible {
            firstOf {
                option(NormalizationPlans.NormalizeExpression)
                // check for contradiction or identity
                option(InequationsRules.ExtractSolutionFromConstantInequation)
                // normalize the equation
                option(InequationsPlans.SimplifyInequation)

                option(solvablePlansForInequations.removeConstantDenominatorsSteps)
                option(PolynomialsPlans.ExpandPolynomialExpressionWithoutNormalization)
            }
        }

        optionally(solvablePlansForInequations.solvableRearrangementSteps)
        optionally(solvablePlansForInequations.coefficientRemovalSteps)
        optionally(InequationsRules.ExtractSolutionFromInequationInSolvedForm)

        optionally {
            // fallback, in case it wasn't solved by the previous steps
            check { it is Inequation }

            optionally(solvablePlansForInequations.moveEverythingToTheLeftAndSimplify)
            applyTo(FactorPlans.FactorGreatestCommonFactor) { it.firstChild }
            apply(EquationsPlans.SimplifyByEliminatingConstantFactorOfLhsWithZeroRhs)
        }
    }

    val solveInequationBySolvingCorrespondingEquation = taskSet {
        explanation = Explanation.SolveInequationInOneVariable

        val lhs = AnyPattern()
        val rhs = AnyPattern()
        pattern = inequationOf(lhs, rhs)

        tasks {
            val equationSolutionTask = task(
                startExpr = equationOf(get(lhs), get(rhs)),
                explanation = metadata(Explanation.SolveEquationCorrespondingToInequation),
                stepsProducer = EquationsPlans.SolveEquation,
            ) ?: return@tasks null

            val solution = when (val result = equationSolutionTask.result) {
                is SetSolution -> if (result.solutionSet is FiniteSet) {
                    setSolutionOf(
                        result.solutionVariables,
                        setDifferenceOf(Constants.Reals, result.solutionSet),
                    )
                } else {
                    return@tasks null
                }
                is Contradiction -> Identity(result.solutionVariables, result.contradictionExpression)
                is Identity -> Contradiction(result.solutionVariables, result.identityExpression)
                else -> return@tasks null
            }

            task(
                startExpr = solution,
                explanation = metadata(Explanation.TakeComplementOfSolution),
            )

            allTasks()
        }
    }

    private fun List<Transformation>.wrap(key: MetadataKey): Transformation {
        return Transformation(
            type = Transformation.Type.Plan,
            fromExpr = first().fromExpr,
            toExpr = last().toExpr,
            explanation = metadata(key),
            steps = this,
        )
    }

    override fun run(ctx: Context, sub: Expression): Transformation? {
        if (sub !is Inequation || ctx.solutionVariables.intersect(sub.variables).isEmpty()) {
            return null
        }

        val steps = solveOrSimplifyInequation.produceSteps(ctx, sub)
        if (steps?.lastOrNull()?.toExpr is Solution) return steps.wrap(Explanation.SolveInequationInOneVariable)

        return solveInequationBySolvingCorrespondingEquation.tryExecute(ctx, sub)
            ?: steps?.wrap(Explanation.ReduceInequation)
    }
}

val solvablePlansForInequations = SolvablePlans(InequationsPlans.SimplifyInequation)

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

    optionally(solvablePlansForInequations.moveEverythingToTheLeftAndSimplify)
    shortcut(InequationsRules.ExtractSolutionFromConstantInequation)

    inContext(contextFactory = { copy(precision = 10) }) {
        apply(evaluateBothSidesNumerically)
    }
    apply(InequationsRules.ExtractSolutionFromConstantInequation)
}
