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

package methods.polynomials

import engine.context.BooleanSetting
import engine.context.Setting
import engine.expressions.Minus
import engine.expressions.Power
import engine.expressions.Product
import engine.expressions.allSubterms
import engine.expressions.complexity
import engine.methods.CompositeMethod
import engine.methods.PublicMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.applyAfterMaybeExtractingMinus
import engine.methods.stepsproducers.branchOn
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
import methods.collecting.createCollectLikeTermsInSolutionVariablesAndSimplifyPlan
import methods.constantexpressions.constantSimplificationSteps
import methods.decimals.decimalEvaluationSteps
import methods.expand.ExpandAndSimplifier
import methods.fractionarithmetic.createAddFractionsPlan
import methods.fractionarithmetic.createAddTermAndFractionPlan
import methods.general.GeneralRules
import methods.integerarithmetic.IntegerArithmeticRules
import methods.simplify.SimplifyPlans
import methods.simplify.algebraicSimplificationSteps
import methods.simplify.algebraicSimplificationStepsForEquations

enum class PolynomialsPlans(override val runner: CompositeMethod) : RunnerMethod {
    NormalizePolynomialInSteps(
        plan {
            explanation = Explanation.NormalizePolynomial

            steps {
                whilePossible(PolynomialRules.NormalizePolynomialOneStep)
            }
        },
    ),

    SimplifyCoefficient(simplifyCoefficient),
    SimplifyMonomial(simplifyMonomial),

    MultiplyVariablePowers(multiplyVariablePowers),
    MultiplyMonomials(multiplyMonomials),

    SimplifyPowerOfNegatedVariable(simplifyPowerOfNegatedVariable),
    SimplifyPowerOfVariablePower(simplifyPowerOfVariablePower),
    SimplifyPowerOfMonomial(simplifyPowerOfMonomial),

    /**
     * Expand and simplify an expression containing a product or a power of polynomials in one variable.
     */
    @PublicMethod
    ExpandPolynomialExpression(
        plan {
            explanation = Explanation.ExpandPolynomialExpression

            steps {
                optionally(algebraicSimplificationSteps)
                apply {
                    whilePossible {
                        deeply(expandAndSimplifier.steps, deepFirst = true)
                        optionally(algebraicSimplificationSteps)
                    }
                }

                optionally(normalizePolynomialSteps)
            }
        },
    ),

    ExpandPolynomialExpressionWithoutNormalization(
        plan {
            explanation = Explanation.ExpandPolynomialExpression

            steps {
                whilePossible {
                    firstOf {
                        option(algebraicSimplificationStepsForEquations)
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
                optionally(algebraicSimplificationStepsForEquations)
            }
        },
    ),

    ExpandSingleBracketWithIntegerCoefficient(
        plan {
            explanation = PolynomialsExplanation.ExpandSingleBracketWithIntegerCoefficient
            steps {
                whilePossible {
                    firstOf {
                        option(algebraicSimplificationStepsForEquations)
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

val expandAndSimplifier = ExpandAndSimplifier(SimplifyPlans.SimplifyAlgebraicExpression)

val normalizePolynomialSteps = steps {
    branchOn(Setting.CommutativeReorderInSteps) {
        case(BooleanSetting.True, PolynomialsPlans.NormalizePolynomialInSteps)
        case(BooleanSetting.False, PolynomialRules.NormalizePolynomial)
    }
    whilePossible(PolynomialRules.NormalizeMonomial)
}

private val multiplyVariablePowers = plan {
    explanation = Explanation.MultiplyUnitaryMonomialsAndSimplify

    val simplifyCompletelySteps = engine.methods.stepsproducers.steps {
        whilePossible(simplificationSteps)
    }

    steps {
        apply {
            whilePossible(GeneralRules.RewriteProductOfPowersWithSameBase)
        }
        check { it is Power }
        applyToKind<Power>(simplifyCompletelySteps) { it.exponent }
    }
}

private val simplifyCoefficient = plan {
    explanation = Explanation.SimplifyCoefficient

    steps {
        whilePossible(simplificationSteps)
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
        branchOn(Setting.CommutativeReorderInSteps) {
            case(BooleanSetting.True) {
                whilePossible(simplificationSteps)
            }
            case(BooleanSetting.False) {
                apply(PolynomialRules.RearrangeProductOfMonomials)
                apply {
                    optionally { applyTo(PolynomialsPlans.SimplifyCoefficient) { it.firstChild } }
                    applyToChildren(PolynomialsPlans.MultiplyVariablePowers)
                }
                optionally(GeneralRules.MoveSignOfNegativeFactorOutOfProduct)
            }
        }
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
            optionally(algebraicSimplificationSteps)
        }
    }
}

val addFractionsSteps = createAddFractionsPlan(
    numeratorSimplificationSteps = steps {
        whilePossible {
            firstOf {
                option(algebraicSimplificationSteps)
                option { deeply(expandAndSimplifier.steps, deepFirst = true) }
            }
        }
    },
)

val addTermAndFractionSteps = createAddTermAndFractionPlan(
    numeratorSimplificationSteps = steps {
        whilePossible {
            firstOf {
                option(algebraicSimplificationSteps)
                option { deeply(expandAndSimplifier.steps, deepFirst = true) }
            }
        }
    },
)

internal val simplificationSteps = branchOn(Setting.PreferDecimals) {
    case(BooleanSetting.True, decimalEvaluationSteps)
    case(BooleanSetting.False, constantSimplificationSteps)
}

internal val collectLikeTermsSteps = createCollectLikeTermsAndSimplifyPlan(simplificationSteps)

internal val collectLikeTermsInSolutionVariablesSteps = createCollectLikeTermsInSolutionVariablesAndSimplifyPlan(
    simplificationSteps,
)
