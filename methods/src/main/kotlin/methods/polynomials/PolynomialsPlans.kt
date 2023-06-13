package methods.polynomials

import engine.context.ResourceData
import engine.expressions.Label
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.contextSensitiveSteps
import engine.methods.stepsproducers.steps
import engine.patterns.AnyPattern
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.commutativeProductOf
import engine.patterns.condition
import engine.patterns.monomialPattern
import engine.patterns.powerOf
import engine.patterns.stickyOptionalNegOf
import methods.collecting.createCollectLikeTermsAndSimplifyPlan
import methods.constantexpressions.ConstantExpressionsPlans
import methods.constantexpressions.constantSimplificationSteps
import methods.constantexpressions.simpleTidyUpSteps
import methods.decimals.decimalEvaluationSteps
import methods.expand.ExpandAndSimplifier
import methods.expand.ExpandAndSimplifyMethodsProvider
import methods.general.GeneralRules
import methods.general.NormalizationPlans
import methods.integerarithmetic.IntegerArithmeticPlans

enum class PolynomialsPlans(override val runner: CompositeMethod) : RunnerMethod {

    MultiplyUnitaryMonomialsAndSimplify(multiplyUnitaryMonomialsAndSimplify),
    MultiplyMonomialsAndSimplify(multiplyMonomialsAndSimplify),
    SimplifyMonomial(simplifyMonomial),
    SimplifyPowerOfUnitaryMonomial(simplifyPowerOfUnitaryMonomial),
    DistributeProductToIntegerPowerAndSimplify(distributeProductToIntegerPowerAndSimplify),
    NormalizeAllMonomials(normalizeAllMonomials),

    SimplifyPolynomialExpressionInOneVariable(
        plan {
            explanation = Explanation.SimplifyPolynomialExpressionInOneVariable
            specificPlans(ConstantExpressionsPlans.SimplifyConstantExpression)

            steps {
                whilePossible { deeply(simpleTidyUpSteps) }
                optionally(NormalizationPlans.NormalizeExpression)
                whilePossible(polynomialSimplificationSteps)
                optionally(NormalizeAllMonomials)
                optionally(PolynomialRules.NormalizePolynomial)
            }
            alternative(ResourceData(gmFriendly = true)) {
                whilePossible { deeply(simpleTidyUpSteps) }
                whilePossible { deeply(simplificationSteps) }
                optionally(NormalizationPlans.NormalizeExpression)
                whilePossible(polynomialSimplificationSteps)
                optionally(NormalizeAllMonomials)
            }
        },
    ),

    /**
     * Expand and simplify an expression containing a product or a power of polynomials in one variable.
     */
    @PublicMethod
    ExpandPolynomialExpressionInOneVariable(
        plan {
            explanation = Explanation.ExpandPolynomialExpression
            pattern = condition { it.variables.size == 1 }

            steps {
                whilePossible(polynomialSimplificationSteps)
                apply {
                    whilePossible {
                        deeply(expandAndSimplifier.steps, deepFirst = true)
                        whilePossible(polynomialSimplificationSteps)
                    }
                }

                optionally(PolynomialRules.NormalizePolynomial)
            }
        },
    ),

    ExpandPolynomialExpressionInOneVariableWithoutNormalization(
        plan {
            explanation = Explanation.ExpandPolynomialExpression

            steps {
                whilePossible {
                    firstOf {
                        option(polynomialSimplificationSteps)
                        option { deeply(expandAndSimplifier.steps, deepFirst = true) }
                    }
                }
            }
        },
    ),

    ExpandSingleBracketWithIntegerCoefficient(
        plan {
            explanation = PolynomialsExplanation.ExpandSingleBracketWithIntegerCoefficient
            steps {
                whilePossible {
                    firstOf {
                        option(polynomialSimplificationSteps)
                        option {
                            deeply {
                                checkForm {
                                    stickyOptionalNegOf(
                                        commutativeProductOf(
                                            UnsignedIntegerPattern(),
                                            AnyPattern(),
                                        ),
                                    )
                                }
                                apply(expandAndSimplifier.singleBracketMethod)
                            }
                        }
                    }
                }
            }
        },
    ),
}

// If we don't do it by lazy we get null pointer exceptions because the MultiplyMonomialsAndSimplify RunnerMethod's
// runner property is used before it is initialised.  I am not sure why though so this solution is dubious.
val expandAndSimplifier: ExpandAndSimplifyMethodsProvider by lazy {
    ExpandAndSimplifier(PolynomialsPlans.SimplifyPolynomialExpressionInOneVariable)
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
                        applyTo(PolynomialsPlans.MultiplyUnitaryMonomialsAndSimplify, Label.B)
                    }
                }
            }
            option(PolynomialsPlans.MultiplyUnitaryMonomialsAndSimplify)
        }
    }
    alternative(ResourceData(gmFriendly = true)) {
        whilePossible { deeply(IntegerArithmeticPlans.SimplifyIntegersInProduct) }
        apply(PolynomialsPlans.MultiplyUnitaryMonomialsAndSimplify)
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

private val simplifyMonomial = plan {
    explanation = Explanation.SimplifyMonomial

    steps {
        checkForm { monomialPattern(ArbitraryVariablePattern()) }
        whilePossible(simplificationSteps)
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
    }
}

val polynomialSimplificationSteps = steps {
    firstOf {
        option { deeply(simpleTidyUpSteps) }
        option {
            deeply {
                firstOf {
                    option(PolynomialsPlans.MultiplyMonomialsAndSimplify)
                    option(PolynomialsPlans.DistributeProductToIntegerPowerAndSimplify)
                    option(PolynomialsPlans.SimplifyPowerOfUnitaryMonomial)
                    option(PolynomialsPlans.SimplifyMonomial)
                    option {
                        check { it.isConstant() }
                        apply(simplificationSteps)
                    }
                }
            }
        }
        option { deeply(collectLikeTermsSteps) }
        option(simplificationSteps)
    }
}

private val simplificationSteps = contextSensitiveSteps {
    default(ResourceData(preferDecimals = false), constantSimplificationSteps)
    alternative(ResourceData(preferDecimals = true), decimalEvaluationSteps)
}

private val collectLikeTermsSteps = createCollectLikeTermsAndSimplifyPlan(simplificationSteps)

private val normalizeAllMonomials = plan {
    explanation = Explanation.NormalizeAllMonomials

    steps {
        whilePossible { deeply(PolynomialRules.NormalizeMonomial) }
    }
}
