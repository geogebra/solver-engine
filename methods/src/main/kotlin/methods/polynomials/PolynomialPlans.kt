package methods.polynomials

import engine.methods.Plan
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import methods.constantexpressions.ConstantExpressionsPlans
import methods.constantexpressions.simpleTidyUpSteps
import methods.constantexpressions.simplificationSteps
import methods.general.NormalizationPlans

enum class PolynomialPlans(override val runner: Plan) : RunnerMethod {

    CollectLikeTermsAndSimplify(
        plan {
            explanation = Explanation.CollectLikeTermsAndSimplify

            steps {
                apply(PolynomialRules.CollectLikeTerms)
                optionally {
                    deeply(ConstantExpressionsPlans.SimplifyConstantSubexpression)
                }
            }
        }
    ),

    @PublicMethod
    SimplifyAlgebraicExpression(
        plan {
            explanation = Explanation.SimplifyAlgebraicExpression

            steps {
                whilePossible { deeply(simpleTidyUpSteps) }
                optionally(NormalizationPlans.NormalizeExpression)
                whilePossible(AlgebraicSimplificationSteps)
            }
        }
    )
}

val AlgebraicSimplificationSteps = steps {
    firstOf {
        option { deeply(simplificationSteps) }
        option { deeply(PolynomialPlans.CollectLikeTermsAndSimplify) }
    }
}
