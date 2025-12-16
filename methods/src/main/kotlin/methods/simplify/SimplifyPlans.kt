/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

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
import methods.angles.createUseTrigonometricIdentityAndSimplifyPlan
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
// In case of trigonometric expressions we want to first try simplifying without expanding
val algebraicSimplificationStepsForEquations = algebraicSimplificationSteps(
    addRationalExpressions = false,
    expandTrigonometricFunctions = false,
)

val useTrigonometricIdentityToExpand = createUseTrigonometricIdentityAndSimplifyPlan(simplificationSteps)

private fun algebraicSimplificationSteps(
    addRationalExpressions: Boolean = true,
    expandTrigonometricFunctions: Boolean = true,
): StepsProducer {
    return steps {
        whilePossible {
            firstOf {
                option {
                    deeply(simpleTidyUpSteps)
                }
                option {
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
                                            option(
                                                RationalExpressionsPlans
                                                    .MultiplyRationalExpressionWithNonFractionalFactors,
                                            )
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

                            if (expandTrigonometricFunctions) {
                                option(useTrigonometricIdentityToExpand)
                            }
                        }
                    }
                }
            }
        }
    }
}
