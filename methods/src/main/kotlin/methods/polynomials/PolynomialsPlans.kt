package methods.polynomials

import engine.context.ResourceData
import engine.expressions.Constants
import engine.expressions.Label
import engine.expressions.equationOf
import engine.expressions.equationSystemOf
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.contextSensitiveSteps
import engine.methods.stepsproducers.steps
import engine.methods.taskSet
import engine.patterns.AnyPattern
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.FixedPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.monomialPattern
import engine.patterns.powerOf
import engine.patterns.sumOf
import engine.patterns.withOptionalIntegerCoefficient
import engine.steps.metadata.metadata
import methods.collecting.createCollectLikeTermsAndSimplifyPlan
import methods.constantexpressions.ConstantExpressionsPlans
import methods.constantexpressions.constantSimplificationSteps
import methods.constantexpressions.simpleTidyUpSteps
import methods.decimals.decimalEvaluationSteps
import methods.expand.createExpandAndSimplifySteps
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

    FactorGreatestCommonFactor(
        plan {
            explanation = Explanation.FactorGreatestCommonFactor

            steps {
                optionally(PolynomialRules.SplitIntegersInMonomialsBeforeFactoring)
                optionally(PolynomialRules.SplitVariablePowersInMonomialsBeforeFactoring)
                apply(PolynomialRules.ExtractCommonTerms)
            }
        },
    ),

    FactorDifferenceOfSquares(
        plan {
            explanation = Explanation.FactorDifferenceOfSquares

            steps {
                optionally(PolynomialRules.RewriteDifferenceOfSquares)
                apply(PolynomialRules.ApplyDifferenceOfSquaresFormula)
            }
        },
    ),

    FactorTrinomialByGuessing(
        taskSet {
            explanation = Explanation.FactorTrinomialByGuessing

            val variable = ArbitraryVariablePattern()

            val quadraticTerm = powerOf(variable, FixedPattern(Constants.Two))
            val linearTerm = withOptionalIntegerCoefficient(variable, positiveOnly = false)
            val constantTerm = SignedIntegerPattern()

            pattern = sumOf(quadraticTerm, linearTerm, constantTerm)

            tasks {
                val solvedSystem = task(
                    startExpr = equationSystemOf(
                        equationOf(sumOf(xp("a"), xp("b")), get(linearTerm::coefficient)!!),
                        equationOf(productOf(xp("a"), xp("b")), move(constantTerm)),
                    ),
                    explanation = metadata(Explanation.SetUpAndSolveEquationSystemForTrinomial),
                    stepsProducer = PolynomialRules.SolveSumProductDiophantineEquationSystemByGuessing,
                ) ?: return@tasks null

                val solution1 = solvedSystem.result.firstChild.secondChild
                val solution2 = solvedSystem.result.secondChild.secondChild

                task(
                    startExpr = productOf(sumOf(move(variable), solution1), sumOf(move(variable), solution2)),
                    explanation = metadata(Explanation.FactorTrinomialUsingTheSolutionsOfTheSumAndProductSystem),
                    dependsOn = listOf(solvedSystem),
                )

                allTasks()
            }
        },
    ),

    FactorTrinomialToSquareAndSimplify(
        plan {
            explanation = Explanation.FactorTrinomialToSquareAndSimplify

            steps {
                apply(PolynomialRules.FactorTrinomialToSquare)
                optionally(SimplifyAlgebraicExpressionInOneVariable)
            }
        },
    ),

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
                whilePossible(algebraicSimplificationSteps)
                optionally(NormalizeAllMonomials)
                whilePossible { deeply(PolynomialRules.NormalizePolynomial) }
            }
            alternative(ResourceData(gmFriendly = true)) {
                whilePossible { deeply(simpleTidyUpSteps) }
                whilePossible { deeply(simplificationSteps) }
                optionally(NormalizationPlans.NormalizeExpression)
                whilePossible(algebraicSimplificationSteps)
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
            pattern = condition(AnyPattern()) { it.variables.size == 1 }

            steps {
                optionally(NormalizationPlans.NormaliseSimplifiedProduct)
                whilePossible {
                    firstOf {
                        option(algebraicSimplificationSteps)
                        option { deeply(expandAndSimplifySteps.value, deepFirst = true) }
                    }
                }
                whilePossible { deeply(PolynomialRules.NormalizePolynomial) }
            }
        },
    ),

    ExpandPolynomialExpressionInOneVariableWithoutNormalization(
        plan {
            explanation = Explanation.ExpandPolynomialExpression
            pattern = condition(AnyPattern()) { it.variables.size == 1 }

            steps {
                optionally(NormalizationPlans.NormaliseSimplifiedProduct)
                whilePossible {
                    firstOf {
                        option(algebraicSimplificationSteps)
                        option { deeply(expandAndSimplifySteps.value, deepFirst = true) }
                    }
                }
            }
        },
    ),

    /**
     * Factor a polynomial in one variable.
     */
    @PublicMethod
    FactorPolynomialInOneVariable(
        plan {
            explanation = Explanation.FactorPolynomial
            pattern = condition(AnyPattern()) { it.variables.size == 1 }

            steps {
                optionally(NormalizationPlans.NormaliseSimplifiedProduct)
                whilePossible {
                    firstOf {
                        option(algebraicSimplificationSteps)
                        option { deeply(FactorGreatestCommonFactor) }
                        option { deeply(FactorDifferenceOfSquares) }
                        option { deeply(FactorTrinomialByGuessing) }
                    }
                }
            }
        },
    ),
}

private val expandAndSimplifySteps = lazy {
    createExpandAndSimplifySteps(PolynomialsPlans.SimplifyAlgebraicExpressionInOneVariable)
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

val algebraicSimplificationSteps = steps {
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
