package methods.algebra

import engine.expressions.Constants
import engine.expressions.DivideBy
import engine.expressions.Expression
import engine.expressions.Fraction
import engine.expressions.Product
import engine.expressions.inequationOf
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.condition
import engine.steps.Task
import engine.steps.metadata.metadata
import methods.constantexpressions.ConstantExpressionsPlans
import methods.constantexpressions.simpleTidyUpSteps
import methods.general.NormalizationPlans
import methods.inequations.InequationsPlans
import methods.polynomials.PolynomialsPlans
import methods.polynomials.polynomialSimplificationSteps
import methods.rationalexpressions.RationalExpressionsPlans
import methods.solvable.computeOverallIntersectionSolution

enum class AlgebraPlans(override val runner: CompositeMethod) : RunnerMethod {

    /**
     * Compute the domain of an algebraic expression then simplify it.
     */
    @PublicMethod
    ComputeDomainAndSimplifyAlgebraicExpression(
        taskSet {
            explanation = Explanation.ComputeDomainAndSimplifyAlgebraicExpression

            tasks {
                val domainComputationTask = task(
                    startExpr = expression,
                    explanation = metadata(Explanation.ComputeDomainOfAlgebraicExpression),
                    stepsProducer = ComputeDomainOfAlgebraicExpression,
                ) ?: return@tasks null

                if (domainComputationTask.result == Constants.Reals) return@tasks null

                task(
                    startExpr = expression,
                    explanation = metadata(Explanation.SimplifyAlgebraicExpression),
                    stepsProducer = algebraicSimplificationSteps,
                )

                allTasks()
            }
        },
    ),

    @PublicMethod
    SimplifyAlgebraicExpression(
        plan {
            explanation = Explanation.SimplifyAlgebraicExpression
            specificPlans(
                ConstantExpressionsPlans.SimplifyConstantExpression,
                ComputeDomainAndSimplifyAlgebraicExpression,
            )

            steps {
                apply(algebraicSimplificationSteps)
            }
        },
    ),

    @PublicMethod
    ComputeDomainOfAlgebraicExpression(
        taskSet {
            explanation = Explanation.ComputeDomainOfAlgebraicExpression
            pattern = condition { solutionVariables.size == 1 }

            tasks {
                val solutionVariable = context.solutionVariables.single()
                val denominatorsAndDivisors = findDenominatorsAndDivisors(expression)
                    .filter { (denominatorOrDivisor, _) -> solutionVariable in denominatorOrDivisor.variables }
                    .toList()

                if (denominatorsAndDivisors.isEmpty()) return@tasks null

                val constraintTasks = mutableListOf<Task>()
                for ((denominatorOrDivisor, inExpression) in denominatorsAndDivisors) {
                    if (solutionVariable in denominatorOrDivisor.variables) {
                        val constraintTask = task(
                            startExpr = inequationOf(denominatorOrDivisor, Constants.Zero),
                            explanation = if (inExpression is Fraction) {
                                metadata(Explanation.DenominatorMustNotBeZero, denominatorOrDivisor, inExpression)
                            } else {
                                metadata(Explanation.DivisorMustNotBeZero, denominatorOrDivisor, inExpression)
                            },
                            stepsProducer = InequationsPlans.SolveInequationInOneVariable,
                        ) ?: return@tasks null
                        constraintTasks.add(constraintTask)
                    }
                }

                if (constraintTasks.isEmpty()) {
                    task(
                        startExpr = Constants.Reals,
                        explanation = metadata(Explanation.ExpressionIsDefinedEverywhere),
                    )
                } else {
                    val overallSolution = computeOverallIntersectionSolution(constraintTasks.map { it.result })
                        ?: return@tasks null
                    task(
                        startExpr = overallSolution,
                        explanation = metadata(Explanation.CollectDomainRestrictions),
                    )
                }

                allTasks()
            }
        },
    ),
}

val algebraicSimplificationSteps = steps {
    whilePossible { deeply(simpleTidyUpSteps) }
    optionally(NormalizationPlans.NormalizeExpression)
    whilePossible {
        firstOf {
            option(polynomialSimplificationSteps)
            option { deeply(RationalExpressionsPlans.SimplifyRationalExpression, deepFirst = true) }
            option { deeply(RationalExpressionsPlans.SimplifyPowerOfRationalExpression, deepFirst = true) }
            option { deeply(RationalExpressionsPlans.MultiplyRationalExpressions, deepFirst = true) }
            option { deeply(RationalExpressionsPlans.AddLikeRationalExpressions, deepFirst = true) }
            option { deeply(RationalExpressionsPlans.AddTermAndRationalExpression, deepFirst = true) }
            option { deeply(RationalExpressionsPlans.AddRationalExpressions, deepFirst = true) }
        }
    }

    optionally(PolynomialsPlans.NormalizeAllMonomials)
}

private fun findDenominatorsAndDivisors(expr: Expression): Sequence<Pair<Expression, Expression>> = sequence {
    when (expr) {
        is Fraction -> {
            yield(Pair(expr.denominator, expr))
        }
        is Product -> {
            for (factor in expr.children) {
                if (factor is DivideBy) {
                    yield(Pair(factor.divisor, expr))
                }
            }
        }
    }

    for (child in expr.children) {
        yieldAll(findDenominatorsAndDivisors(child))
    }
}
