package methods.algebra

import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import methods.constantexpressions.ConstantExpressionsPlans
import methods.constantexpressions.simpleTidyUpSteps
import methods.general.NormalizationPlans
import methods.polynomials.PolynomialsPlans
import methods.polynomials.polynomialSimplificationSteps
import methods.rationalexpressions.RationalExpressionsPlans

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
}
