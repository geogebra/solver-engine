package methods.algebra

import engine.expressions.Constants
import engine.expressions.DivideBy
import engine.expressions.Expression
import engine.expressions.ExpressionWithConstraint
import engine.expressions.Fraction
import engine.expressions.Identity
import engine.expressions.Product
import engine.expressions.SetSolution
import engine.expressions.ValueExpression
import engine.expressions.inequationOf
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.condition
import engine.steps.metadata.metadata
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

                val simplificationTask = task(
                    startExpr = expression,
                    explanation = metadata(Explanation.SimplifyAlgebraicExpression),
                    stepsProducer = algebraicSimplificationSteps,
                ) ?: return@tasks null

                task(
                    startExpr = ExpressionWithConstraint(simplificationTask.result, domainComputationTask.result),
                    explanation = metadata(Explanation.CombineSimplifiedExpressionWithConstraint),
                )

                allTasks()
            }
        },
    ),

    @PublicMethod
    SimplifyAlgebraicExpression(
        plan {
            explanation = Explanation.SimplifyAlgebraicExpression
            pattern = condition { it is ValueExpression }
            specificPlans(
                PolynomialsPlans.SimplifyPolynomialExpression,
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

            tasks {
                val denominatorsAndDivisors = findDenominatorsAndDivisors(expression)
                    .filter { (denominatorOrDivisor, _) -> !denominatorOrDivisor.isConstant() }
                    .toList()

                if (denominatorsAndDivisors.isEmpty()) return@tasks null

                val constraintTasks = denominatorsAndDivisors.map { (denominatorOrDivisor, inExpression) ->
                    taskWithOptionalSteps(
                        startExpr = inequationOf(denominatorOrDivisor, Constants.Zero),
                        explanation = if (inExpression is Fraction) {
                            metadata(Explanation.DenominatorMustNotBeZero, denominatorOrDivisor, inExpression)
                        } else {
                            metadata(Explanation.DivisorMustNotBeZero, denominatorOrDivisor, inExpression)
                        },
                    ) {
                        inContext({ copy(solutionVariables = listOfNotNull(it.variables.singleOrNull())) }) {
                            apply(InequationsPlans.SolveInequationInOneVariable)
                        }
                    }
                }

                val overallSolution = computeOverallIntersectionSolution(constraintTasks.map { it.result })

                val explanation = when {
                    overallSolution is Identity ->
                        metadata(Explanation.ExpressionIsDefinedEverywhere, overallSolution.solutionVariables)
                    overallSolution is SetSolution && overallSolution.solutionSet == Constants.Reals ->
                        metadata(Explanation.ExpressionIsDefinedEverywhere, overallSolution.solutionVariables)
                    else -> metadata(Explanation.CollectDomainRestrictions)
                }

                task(
                    startExpr = overallSolution,
                    explanation = explanation,
                )

                allTasks()
            }
        },
    ),
}

val algebraicSimplificationSteps = steps {
    whilePossible { deeply(simpleTidyUpSteps) }
    optionally(NormalizationPlans.NormalizeExpression)
    whilePossible {
        deeply(deepFirst = true) {
            firstOf {
                option(RationalExpressionsPlans.SimplifyDivisionOfPolynomial)
                option(RationalExpressionsPlans.SimplifyRationalExpression)
                option(RationalExpressionsPlans.SimplifyPowerOfRationalExpression)
                option(RationalExpressionsPlans.MultiplyRationalExpressions)
                option(RationalExpressionsPlans.AddLikeRationalExpressions)
                option(RationalExpressionsPlans.AddTermAndRationalExpression)
                option(RationalExpressionsPlans.AddRationalExpressions)
                option(polynomialSimplificationSteps)
            }
        }
    }
}

private fun findDenominatorsAndDivisors(expr: Expression): Sequence<Pair<Expression, Expression>> = sequence {
    for (child in expr.children) {
        yieldAll(findDenominatorsAndDivisors(child))
    }

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
}
