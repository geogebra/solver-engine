package methods.algebra

import engine.expressions.Constants
import engine.expressions.DivideBy
import engine.expressions.Expression
import engine.expressions.ExpressionWithConstraint
import engine.expressions.Fraction
import engine.expressions.Identity
import engine.expressions.ListExpression
import engine.expressions.Product
import engine.expressions.SetSolution
import engine.expressions.ValueExpression
import engine.expressions.inequationOf
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.condition
import engine.steps.metadata.metadata
import methods.constantexpressions.simpleTidyUpSteps
import methods.general.NormalizationPlans
import methods.inequations.InequationsPlans
import methods.polynomials.PolynomialsPlans
import methods.polynomials.addFractionsSteps
import methods.polynomials.addTermAndFractionSteps
import methods.polynomials.collectLikeTermsSteps
import methods.polynomials.simplificationSteps
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
            pattern = condition { it is ValueExpression }

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
                    stepsProducer = algebraicSimplificationSteps(),
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
            specificPlans(ComputeDomainAndSimplifyAlgebraicExpression)

            tasks {
                val denominatorsAndDivisors = findDenominatorsAndDivisors(expression)
                    .filter { (denominatorOrDivisor, _) -> !denominatorOrDivisor.isConstant() }
                    .groupBy(
                        keySelector = { (denominatorOrDivisor, _) -> denominatorOrDivisor.removeBrackets() },
                        valueTransform = { (_, inExpression) -> inExpression },
                    )

                if (denominatorsAndDivisors.isEmpty()) return@tasks null

                val constraintTasks = denominatorsAndDivisors.map { (denominatorOrDivisor, inExpressions) ->
                    val explanation = if (inExpressions.size == 1) {
                        metadata(
                            Explanation.ExpressionMustNotBeZero,
                            denominatorOrDivisor,
                            inExpressions[0],
                        )
                    } else {
                        metadata(
                            Explanation.ExpressionMustNotBeZeroPlural,
                            denominatorOrDivisor,
                            ListExpression(inExpressions),
                        )
                    }

                    taskWithOptionalSteps(
                        startExpr = inequationOf(denominatorOrDivisor, Constants.Zero),
                        explanation = explanation,
                    ) {
                        inContext({ copy(solutionVariables = it.variables.toList()) }) {
                            apply(InequationsPlans.SolveInequation)
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

val algebraicSimplificationSteps = algebraicSimplificationSteps(true)

// when solving equations or inequalities we don't want to add fractions
// e.g. in x/2 + x/3 = 1 we want to multiply through by 6 instead of adding the fractions
val algebraicSimplificationStepsWithoutFractionAddition = algebraicSimplificationSteps(false)

private fun algebraicSimplificationSteps(addRationalExpressions: Boolean = true): StepsProducer {
    return steps {
        whilePossible { deeply(simpleTidyUpSteps) }
        optionally(NormalizationPlans.NormalizeExpression)
        whilePossible {
            deeply(deepFirst = true) {
                firstOf {
                    option(RationalExpressionsPlans.SimplifyDivisionOfPolynomial)
                    option(RationalExpressionsPlans.SimplifyRationalExpression)
                    option(RationalExpressionsPlans.SimplifyPowerOfRationalExpression)
                    option(RationalExpressionsPlans.MultiplyRationalExpressions)
                    option(RationalExpressionsPlans.MultiplyRationalExpressionWithNonFractionalFactors)
                    option(PolynomialsPlans.MultiplyVariablePowers)
                    option(PolynomialsPlans.MultiplyMonomials)
                    option(PolynomialsPlans.SimplifyPowerOfNegatedVariable)
                    option(PolynomialsPlans.SimplifyPowerOfVariablePower)
                    option(PolynomialsPlans.SimplifyPowerOfMonomial)
                    option(PolynomialsPlans.SimplifyMonomial)

                    option(simplificationSteps)
                    option(collectLikeTermsSteps)

                    if (addRationalExpressions) {
                        option(addFractionsSteps)
                        option(addTermAndFractionSteps)
                        option(RationalExpressionsPlans.AddLikeRationalExpressions)
                        option(RationalExpressionsPlans.AddTermAndRationalExpression)
                        option(RationalExpressionsPlans.AddRationalExpressions)
                    }
                }
            }
        }
    }
}

fun findDenominatorsAndDivisors(expr: Expression): Sequence<Pair<Expression, Expression>> = sequence {
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
