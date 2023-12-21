package methods.simplify

import engine.expressions.ValueExpression
import engine.expressions.containsFractions
import engine.expressions.isPolynomial
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.steps
import engine.patterns.condition
import engine.steps.metadata.metadata
import methods.algebra.AlgebraPlans
import methods.constantexpressions.ConstantExpressionsPlans
import methods.constantexpressions.simpleTidyUpSteps
import methods.general.NormalizationPlans
import methods.polynomials.PolynomialsPlans
import methods.polynomials.addFractionsSteps
import methods.polynomials.addTermAndFractionSteps
import methods.polynomials.collectLikeTermsSteps
import methods.polynomials.normalizePolynomialSteps
import methods.polynomials.simplificationSteps
import methods.rationalexpressions.RationalExpressionsPlans

enum class SimplifyPlans(override val runner: CompositeMethod) : RunnerMethod {
    SimplifySubexpression(
        plan {
            explanation = Explanation.SimplifyExpressionInBrackets
            pattern = condition { it.hasVisibleBracket() }

            steps {
                apply(algebraicSimplificationSteps)
            }
        },
    ),

    @PublicMethod
    SimplifyAlgebraicExpression(
        plan {
            explanation {
                if (expression.isPolynomial()) {
                    metadata(Explanation.SimplifyPolynomialExpression)
                } else {
                    metadata(Explanation.SimplifyAlgebraicExpression)
                }
            }
            pattern = condition { it is ValueExpression }
            specificPlans(
                ConstantExpressionsPlans.SimplifyConstantExpression,
                AlgebraPlans.ComputeDomainAndSimplifyAlgebraicExpression,
            )

            steps {
                apply(simplifyAlgebraicExpressionSteps)
            }
        },
    ),
}

val simplifyAlgebraicExpressionSteps = steps {
    whilePossible { deeply(simpleTidyUpSteps) }
    optionally(NormalizationPlans.NormalizeExpression)
    whilePossible { deeply(SimplifyPlans.SimplifySubexpression, deepFirst = true) }
    optionally(algebraicSimplificationSteps)
    optionally(normalizePolynomialSteps)
}

val algebraicSimplificationSteps = algebraicSimplificationSteps(true)

// when solving equations or inequalities, we don't want to add fractions
// e.g., in x/2 + x/3 = 1 we want to multiply through by 6 instead of adding the fractions
val algebraicSimplificationStepsWithoutFractionAddition = algebraicSimplificationSteps(false)

private fun algebraicSimplificationSteps(addRationalExpressions: Boolean = true): StepsProducer {
    return steps {
        whilePossible {
            deeply(deepFirst = true) {
                firstOf {
                    option {
                        check { !it.isConstant() }
                        firstOf {
                            option(RationalExpressionsPlans.SimplifyDivisionOfPolynomial)
                            option {
                                check { it.containsFractions() }
                                firstOf {
                                    option(RationalExpressionsPlans.SimplifyRationalExpression)
                                    option(RationalExpressionsPlans.SimplifyPowerOfRationalExpression)
                                    option(RationalExpressionsPlans.MultiplyRationalExpressions)
                                    option(RationalExpressionsPlans.MultiplyRationalExpressionWithNonFractionalFactors)
                                }
                            }
                            option(PolynomialsPlans.MultiplyVariablePowers)
                            option(PolynomialsPlans.MultiplyMonomials)
                            option(PolynomialsPlans.SimplifyPowerOfNegatedVariable)
                            option(PolynomialsPlans.SimplifyPowerOfVariablePower)
                            option(PolynomialsPlans.SimplifyPowerOfMonomial)
                            option(PolynomialsPlans.SimplifyMonomial)
                        }
                    }

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
