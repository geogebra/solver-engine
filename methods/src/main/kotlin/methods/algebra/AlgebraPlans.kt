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
import engine.methods.taskSet
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
     * Simplify an algebraic expression with one variable.
     */
    @PublicMethod
    SimplifyAlgebraicExpressionInOneVariable(
        plan {
            explanation = Explanation.SimplifyAlgebraicExpression
            specificPlans(
                ConstantExpressionsPlans.SimplifyConstantExpression,
                PolynomialsPlans.SimplifyPolynomialExpressionInOneVariable,
            )

            steps {
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
        },
    ),

    @PublicMethod
    ComputeDomainOfAlgebraicExpression(
        taskSet {
            explanation = Explanation.ComputeDomainOfAlgebraicExpression

            tasks {
                val solutionVariable = context.solutionVariables.single()
                val denominatorsAndDivisors = findDenominatorsAndDivisors(expression)

                val constraintTasks = mutableListOf<Task>()
                for ((denominatorOrDivisor, inExpression) in denominatorsAndDivisors) {
                    if (solutionVariable in denominatorOrDivisor.variables) {
                        val constraintTask = task(
                            startExpr = inequationOf(denominatorOrDivisor, Constants.Zero),
                            explanation = metadata(Explanation.MustNotBeZero, denominatorOrDivisor, inExpression),
                            stepsProducer = InequationsPlans.SolveInequationInOneVariable,
                        ) ?: return@tasks null
                        constraintTasks.add(constraintTask)
                    }
                }

                val overallSolution = computeOverallIntersectionSolution(constraintTasks.map { it.result }) ?: return@tasks null
                task(
                    startExpr = overallSolution,
                    explanation = metadata(Explanation.ComputeDomain),
                )

                allTasks()
            }
        },
    ),
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
