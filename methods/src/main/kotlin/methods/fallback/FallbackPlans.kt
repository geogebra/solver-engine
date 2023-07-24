package methods.fallback

import engine.expressions.Constants
import engine.expressions.Identity
import engine.expressions.VoidExpression
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.taskSet
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.QuadraticPolynomialPattern
import engine.steps.metadata.metadata
import methods.approximation.ApproximationPlans
import methods.constantexpressions.ConstantExpressionsPlans
import methods.factor.FactorPlans
import methods.inequalities.solveConstantInequalitySteps
import methods.integerarithmetic.IntegerArithmeticPlans
import methods.polynomials.PolynomialsPlans

enum class FallbackPlans(override val runner: CompositeMethod) : RunnerMethod {

    // Users shouldn't call this method
    @PublicMethod
    ExpressionIsFullySimplified(
        plan {

            specificPlans(
                ConstantExpressionsPlans.SimplifyConstantExpression,
                IntegerArithmeticPlans.EvaluateArithmeticExpression,
                ApproximationPlans.EvaluateExpressionNumerically,
                PolynomialsPlans.SimplifyPolynomialExpressionInOneVariable,
                FactorPlans.FactorPolynomialInOneVariable,
            )

            explanation = Explanation.ExpressionIsFullySimplified

            steps {
                apply(FallbackRules.ExpressionIsFullySimplified)
            }
        },
    ),

    // Users shouldn't call this method
    @PublicMethod
    QuadraticHasNegativeDiscriminant(
        taskSet {

            specificPlans(
                PolynomialsPlans.SimplifyPolynomialExpressionInOneVariable,
                FactorPlans.FactorPolynomialInOneVariable,
            )

            explanation = Explanation.QuadraticIsIrreducible
            val quadratic = QuadraticPolynomialPattern(ArbitraryVariablePattern())
            pattern = quadratic

            tasks {
                val a = get(quadratic::quadraticCoefficient)!!
                val b = get(quadratic::linearCoefficient)!!
                val c = get(quadratic::constantTerm)!!

                val discriminant = sumOf(
                    powerOf(b, Constants.Two),
                    negOf(
                        productOf(Constants.Four, a, c),
                    ),
                )

                val checkDiscriminantIsNegative = task(
                    context = context.copy(solutionVariables = emptyList()),
                    startExpr = engine.expressions.lessThanOf(discriminant, Constants.Zero),
                    explanation = metadata(Explanation.CheckDiscriminantIsNegative),
                    stepsProducer = solveConstantInequalitySteps,
                ) ?: return@tasks null

                if (checkDiscriminantIsNegative.result !is Identity) {
                    return@tasks null
                }

                task(
                    startExpr = VoidExpression(),
                    explanation = metadata(Explanation.QuadraticIsIrreducibleBecauseDiscriminantIsNegative),
                )

                allTasks()
            }
        },
    ),
}
