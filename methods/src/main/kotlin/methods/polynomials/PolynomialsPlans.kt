package methods.polynomials

import engine.context.ResourceData
import engine.expressions.Expression
import engine.expressions.Minus
import engine.expressions.Power
import engine.expressions.Product
import engine.expressions.isPolynomial
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.applyAfterMaybeExtractingMinus
import engine.methods.stepsproducers.contextSensitiveSteps
import engine.methods.stepsproducers.steps
import engine.patterns.AnyPattern
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.commutativeProductOf
import engine.patterns.condition
import engine.patterns.monomialPattern
import engine.patterns.negOf
import engine.patterns.oneOf
import engine.patterns.optionalNegOf
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
import methods.integerarithmetic.IntegerArithmeticRules

enum class PolynomialsPlans(override val runner: CompositeMethod) : RunnerMethod {

    SimplifyCoefficient(simplifyCoefficient),
    SimplifyMonomial(simplifyMonomial),

    MultiplyVariablePowers(multiplyVariablePowers),
    MultiplyMonomials(multiplyMonomials),

    SimplifyPowerOfNegatedVariable(simplifyPowerOfNegatedVariable),
    SimplifyPowerOfVariablePower(simplifyPowerOfVariablePower),
    SimplifyPowerOfMonomial(simplifyPowerOfMonomial),

    @PublicMethod
    SimplifyPolynomialExpression(
        plan {
            explanation = Explanation.SimplifyPolynomialExpressionInOneVariable
            pattern = condition { it.isPolynomial() }
            specificPlans(ConstantExpressionsPlans.SimplifyConstantExpression)

            steps {
                whilePossible { deeply(simpleTidyUpSteps) }
                optionally(NormalizationPlans.NormalizeExpression)
                whilePossible(polynomialSimplificationSteps)
                optionally(PolynomialRules.NormalizePolynomial)
            }
        },
    ),

    /**
     * Expand and simplify an expression containing a product or a power of polynomials in one variable.
     */
    @PublicMethod
    ExpandPolynomialExpression(
        plan {
            explanation = Explanation.ExpandPolynomialExpression

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

    ExpandPolynomialExpressionWithoutNormalization(
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

    ExpandMostComplexSubterm(
        plan {
            explanation = Explanation.ExpandPolynomialExpression

            steps {
                firstOf {
                    optionsFor({
                        it.allSubterms().sortedByDescending { subterm -> subterm.complexity() }
                    }) { subterm ->
                        applyTo(expandAndSimplifier.steps) { subterm }
                    }
                }
                whilePossible(polynomialSimplificationSteps)
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
    ExpandAndSimplifier(PolynomialsPlans.SimplifyPolynomialExpression)
}

private val multiplyVariablePowers = plan {
    explanation = Explanation.MultiplyUnitaryMonomialsAndSimplify

    steps {
        apply {
            whilePossible(GeneralRules.RewriteProductOfPowersWithSameBase)
        }
        check { it is Power }
        applyToKind<Power>(engine.methods.stepsproducers.steps { whilePossible(simplificationSteps) }) { it.exponent }
    }
}

private val multiplyMonomials = plan {
    explanation = Explanation.MultiplyMonomialsAndSimplify
    pattern = oneOf(
        // grab the minus sign only if there is another minus among to coefficients so it can simplify
        negOf(condition { it is Product && !it.isConstant() && it.children.any { child -> child is Minus } }),
        condition { it is Product && !it.isConstant() },
    )

    steps {
        apply(PolynomialRules.RearrangeProductOfMonomials)
        apply {
            optionally { applyTo(PolynomialsPlans.SimplifyCoefficient) { it.firstChild } }
            applyToChildren(PolynomialsPlans.MultiplyVariablePowers)
        }
        optionally(GeneralRules.MoveSignOfNegativeFactorOutOfProduct)
    }

    alternative(ResourceData(gmFriendly = true)) {
        whilePossible(simplificationSteps)
    }
}

private val simplifyCoefficient = plan {
    explanation = Explanation.SimplifyCoefficient

    steps {
        whilePossible(simplificationSteps)
    }
}

private val simplifyMonomial = plan {
    explanation = Explanation.SimplifyMonomial

    steps {
        checkForm { monomialPattern(ArbitraryVariablePattern()) }
        whilePossible(simplificationSteps)
    }
}

private val simplifyPowerOfNegatedVariable = plan {
    explanation = Explanation.SimplifyPowerOfUnitaryMonomial
    pattern = powerOf(optionalNegOf(ArbitraryVariablePattern()), UnsignedIntegerPattern())

    steps {
        firstOf {
            option(GeneralRules.SimplifyOddPowerOfNegative)
            option(GeneralRules.SimplifyEvenPowerOfNegative)
        }
    }
}

private val simplifyPowerOfVariablePower = plan {
    explanation = Explanation.SimplifyPowerOfUnitaryMonomial
    pattern =
        powerOf(optionalNegOf(powerOf(ArbitraryVariablePattern(), UnsignedIntegerPattern())), UnsignedIntegerPattern())

    steps {
        optionally(GeneralRules.SimplifyOddPowerOfNegative)
        optionally(GeneralRules.SimplifyEvenPowerOfNegative)

        applyAfterMaybeExtractingMinus {
            apply(GeneralRules.MultiplyExponentsUsingPowerRule)
            applyToKind<Power>(IntegerArithmeticRules.EvaluateIntegerProductAndDivision) { it.exponent }
        }
    }
}

private val simplifyPowerOfMonomial = plan {
    explanation = Explanation.DistributeProductToIntegerPowerAndSimplify

    steps {
        optionally(GeneralRules.SimplifyOddPowerOfNegative)
        optionally(GeneralRules.SimplifyEvenPowerOfNegative)

        applyAfterMaybeExtractingMinus {
            apply(GeneralRules.DistributePowerOfProduct)
            whilePossible(polynomialSimplificationSteps)
        }
    }
}

val polynomialSimplificationSteps = steps {
    firstOf {
        option { deeply(simpleTidyUpSteps) }
        option {
            deeply {
                firstOf {
                    option(PolynomialsPlans.MultiplyVariablePowers)
                    option(PolynomialsPlans.MultiplyMonomials)
                    option(PolynomialsPlans.SimplifyPowerOfNegatedVariable)
                    option(PolynomialsPlans.SimplifyPowerOfVariablePower)
                    option(PolynomialsPlans.SimplifyPowerOfMonomial)
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

private fun Expression.allSubterms(): List<Expression> = listOf(this) + children.flatMap { it.allSubterms() }
private fun Expression.complexity(): Int = 1 + children.sumOf { it.complexity() }
