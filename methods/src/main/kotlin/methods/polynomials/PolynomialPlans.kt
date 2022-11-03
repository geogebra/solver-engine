package methods.polynomials

import engine.methods.plan
import engine.methods.stepsproducers.steps
import methods.constantexpressions.simpleTidyUpSteps
import methods.constantexpressions.simplificationSteps
import methods.constantexpressions.simplifyConstantSubexpression
import methods.general.normalizeExpression

val collectLikeTermsAndSimplify = plan {
    explanation(Explanation.CollectLikeTermsAndSimplify)

    steps {
        apply(PolynomialRules.CollectLikeTerms)
        optionally {
            deeply(simplifyConstantSubexpression)
        }
    }
}

val algebraicSimplificationSteps = steps {
    firstOf {
        option { deeply(simplificationSteps) }
        option { deeply(collectLikeTermsAndSimplify) }
    }
}

val simplifyAlgebraicExpression = plan {
    explanation(Explanation.SimplifyAlgebraicExpression)

    steps {
        whilePossible { deeply(simpleTidyUpSteps) }
        optionally(normalizeExpression)
        whilePossible(algebraicSimplificationSteps)
    }
}
