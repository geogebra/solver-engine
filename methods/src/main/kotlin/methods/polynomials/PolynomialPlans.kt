package methods.polynomials

import engine.context.ResourceData
import engine.expressions.Label
import engine.methods.Plan
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.contextSensitiveSteps
import engine.methods.stepsproducers.steps
import engine.patterns.AnyPattern
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.powerOf
import methods.constantexpressions.ConstantExpressionsPlans
import methods.constantexpressions.constantSimplificationSteps
import methods.constantexpressions.simpleTidyUpSteps
import methods.decimals.decimalEvaluationSteps
import methods.general.GeneralPlans
import methods.general.GeneralRules
import methods.general.NormalizationPlans
import methods.general.NormalizationRules

enum class PolynomialPlans(override val runner: Plan) : RunnerMethod {

    SimplifyCoefficient(simplifyCoefficient),
    CollectLikeTermsAndSimplify(collectLikeTermsAndSimplify),
    MultiplyUnitaryMonomialsAndSimplify(multiplyUnitaryMonomialsAndSimplify),
    MultiplyMonomialsAndSimplify(multiplyMonomialsAndSimplify),
    NormalizeMonomialAndSimplify(normalizeMonomialAndSimplify),
    SimplifyPowerOfUnitaryMonomial(simplifyPowerOfUnitaryMonomial),
    DistributeProductToIntegerPowerAndSimplify(distributeProductToIntegerPowerAndSimplify),
    ApplyExpandRuleAndSimplify(applyExpandRuleAndSimplify),

    /**
     * Simplify an algebraic expression with one variable.
     */
    @PublicMethod
    SimplifyAlgebraicExpressionInOneVariable(
        plan {
            explanation = Explanation.SimplifyAlgebraicExpression
            specificPlans(ConstantExpressionsPlans.SimplifyConstantExpression)

            steps {
                whilePossible { deeply(simpleTidyUpSteps) }
                optionally(NormalizationPlans.NormalizeExpression)
                whilePossible(AlgebraicSimplificationSteps)
            }
        }
    ),

    @PublicMethod
    ExpandPolynomialExpressionInOneVariable(
        plan {
            explanation = Explanation.ExpandPolynomialExpression
            pattern = condition(AnyPattern()) { it.variables.size == 1 }

            steps {
                optionally(NormalizationRules.NormaliseSimplifiedProduct)
                whilePossible {
                    deeply(ApplyExpandRuleAndSimplify, deepFirst = true)
                }
            }
        }
    )
}

private val simplifyCoefficient = plan {
    explanation = Explanation.SimplifyCoefficient

    steps {
        whilePossible(simplificationSteps)
    }
}

private val applyExpandRuleAndSimplify = plan {
    explanation = Explanation.ApplyExpandRuleAndSimplify

    steps {
        firstOf {
            option(GeneralPlans.ExpandBinomialSquared)
            option(GeneralPlans.ExpandBinomialCubed)
            option(GeneralPlans.ExpandTrinomialSquared)
            option(GeneralRules.ExpandProductOfSumAndDifference)
            option(GeneralRules.ApplyFoilMethod)
            option(GeneralRules.ExpandDoubleBrackets)
            option(GeneralRules.DistributeNegativeOverBracket)
            option(GeneralRules.DistributeMultiplicationOverSum)
            option(NormalizationPlans.NormalizeExpression)
        }
        optionally(PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable)
    }
}

private val collectLikeTermsAndSimplify = plan {
    explanation = Explanation.CollectLikeTermsAndSimplify

    steps {
        withNewLabels {
            apply(PolynomialRules.CollectLikeTerms)
            optionally {
                applyTo(Label.A) {
                    applyTo(PolynomialPlans.SimplifyCoefficient) { it.firstChild }
                    optionally(PolynomialRules.NormalizeMonomial)
                }
            }
            optionally(GeneralRules.EliminateZeroInSum)
        }
    }
}

private val multiplyMonomialsAndSimplify = plan {
    explanation = Explanation.MultiplyMonomialsAndSimplify

    steps {
        firstOf {
            option {
                withNewLabels {
                    apply(PolynomialRules.CollectUnitaryMonomialsInProduct)
                    optionally {
                        applyTo(ConstantExpressionsPlans.SimplifyConstantSubexpression, Label.A)
                    }
                    optionally {
                        applyTo(PolynomialPlans.MultiplyUnitaryMonomialsAndSimplify, Label.B)
                    }
                    optionally(PolynomialRules.NormalizeMonomial)
                }
            }
            option(PolynomialPlans.MultiplyUnitaryMonomialsAndSimplify)
        }
    }
}

private val multiplyUnitaryMonomialsAndSimplify = plan {
    explanation = Explanation.MultiplyUnitaryMonomialsAndSimplify

    steps {
        apply {
            whilePossible(GeneralRules.RewriteProductOfPowersWithSameBase)
        }
        whilePossible { deeply(simplificationSteps, deepFirst = true) }
    }
}

private val normalizeMonomialAndSimplify = plan {
    explanation = Explanation.NormalizeMonomialAndSimplify

    steps {
        apply(PolynomialRules.NormalizeMonomial)
        optionally(simplificationSteps)
    }
}

private val simplifyPowerOfUnitaryMonomial = plan {
    explanation = Explanation.SimplifyPowerOfUnitaryMonomial
    pattern = powerOf(powerOf(ArbitraryVariablePattern(), UnsignedIntegerPattern()), UnsignedIntegerPattern())

    steps {
        apply(GeneralRules.MultiplyExponentsUsingPowerRule)
        apply(simplificationSteps)
    }
}

private val distributeProductToIntegerPowerAndSimplify = plan {
    explanation = Explanation.DistributeProductToIntegerPowerAndSimplify

    steps {
        withNewLabels {
            firstOf {
                option(PolynomialRules.DistributeMonomialToIntegerPower)
                option(PolynomialRules.DistributeProductToIntegerPower)
            }
            optionally {
                applyTo(ConstantExpressionsPlans.SimplifyConstantExpression, Label.A)
            }
        }
        whilePossible {
            deeply {
                apply(GeneralRules.MultiplyExponentsUsingPowerRule)
                apply(simplificationSteps)
            }
        }
        optionally { deeply(PolynomialRules.NormalizeMonomial) }
    }
}

val AlgebraicSimplificationSteps = steps {
    firstOf {
        option { deeply(simpleTidyUpSteps) }
        option { deeply(PolynomialPlans.MultiplyMonomialsAndSimplify) }
        option { deeply(PolynomialPlans.DistributeProductToIntegerPowerAndSimplify) }
        option { deeply(PolynomialPlans.SimplifyPowerOfUnitaryMonomial) }
        option { deeply(PolynomialPlans.CollectLikeTermsAndSimplify) }
        option { deeply(PolynomialPlans.NormalizeMonomialAndSimplify) }
        option(simplificationSteps)
        option { deeply(PolynomialRules.NormalizePolynomial) }
    }
}

private val simplificationSteps = contextSensitiveSteps {
    default(ResourceData(preferDecimals = false), constantSimplificationSteps)
    alternative(ResourceData(preferDecimals = true), decimalEvaluationSteps)
}
