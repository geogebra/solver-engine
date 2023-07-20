package methods.fallback

import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import methods.approximation.ApproximationPlans
import methods.constantexpressions.ConstantExpressionsPlans
import methods.factor.FactorPlans
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
                PolynomialsPlans.SimplifyAlgebraicExpressionInOneVariable,
                FactorPlans.FactorPolynomialInOneVariable,
            )

            explanation = Explanation.ExpressionIsFullySimplified

            steps {
                apply(FallbackRules.ExpressionIsFullySimplified)
            }
        },
    ),
}
