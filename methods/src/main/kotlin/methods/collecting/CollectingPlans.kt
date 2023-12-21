package methods.collecting

import engine.context.Setting
import engine.expressions.Label
import engine.methods.Method
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import methods.fractionarithmetic.FractionArithmeticPlans
import methods.general.GeneralRules

private fun createSimplifyCoefficientPlan(simplificationSteps: StepsProducer, preferFractionalForm: Boolean): Method {
    return plan {
        explanation = Explanation.SimplifyCoefficient

        steps {
            applyTo(extractor = { it.firstChild }) {
                whilePossible(simplificationSteps)
            }
            optionally(GeneralRules.EvaluateProductContainingZero)
            optionally(GeneralRules.MoveSignOfNegativeFactorOutOfProduct)
            optionally {
                // deeply because of the minus sign
                deeply(GeneralRules.RemoveUnitaryCoefficient)
            }
            if (preferFractionalForm) {
                // we prefer writing roots as [2 sqrt[2] / 3]
                optionally {
                    // deeply because of the potential minus sign
                    deeply(FractionArithmeticPlans.MultiplyAndSimplifyFractions)
                }
            }
        }
    }
}

/**
 * Create a plan to collect and simplify (using the specified [simplificationSteps]) all terms
 * containing a root (with a rational coefficient)
 */
fun createCollectLikeRootsAndSimplifyPlan(simplificationSteps: StepsProducer): Method {
    val coefficientSimplificationSteps =
        createSimplifyCoefficientPlan(simplificationSteps, preferFractionalForm = true)

    return plan {
        explanation = Explanation.CollectLikeRootsAndSimplify

        steps {
            firstOf {
                option {
                    check { isSet(Setting.QuickAddLikeTerms) }
                    apply(CollectingRules.CombineTwoSimpleLikeRoots)
                }
                option {
                    withNewLabels {
                        apply(CollectingRules.CollectLikeRoots)
                        optionally { applyTo(coefficientSimplificationSteps, Label.A) }
                        optionally(GeneralRules.EliminateZeroInSum)
                    }
                }
            }
        }
    }
}

/**
 * Create a plan to collect and simplify (using the specified [simplificationSteps]) all terms
 * containing a rational exponent of an integer (with a rational coefficient)
 */
fun createCollectLikeRationalPowersAndSimplifyPlan(simplificationSteps: StepsProducer): Method {
    val coefficientSimplificationSteps =
        createSimplifyCoefficientPlan(simplificationSteps, preferFractionalForm = true)

    return plan {
        explanation = Explanation.CollectLikeRationalPowersAndSimplify

        steps {
            firstOf {
                option {
                    check { isSet(Setting.QuickAddLikeTerms) }
                    apply(CollectingRules.CombineTwoSimpleLikeRationalPowers)
                }
                option {
                    withNewLabels {
                        apply(CollectingRules.CollectLikeRationalPowers)
                        optionally { applyTo(coefficientSimplificationSteps, Label.A) }
                        optionally(GeneralRules.EliminateZeroInSum)
                    }
                }
            }
        }
    }
}

/**
 * Create a plan to collect and simplify (using the specified [simplificationSteps]) all terms
 * containing the same variable power (with a constant coefficient)
 */
fun createCollectLikeTermsAndSimplifyPlan(simplificationSteps: StepsProducer): Method {
    val coefficientSimplificationSteps =
        createSimplifyCoefficientPlan(simplificationSteps, preferFractionalForm = false)

    return plan {
        explanation = Explanation.CollectLikeTermsAndSimplify

        steps {
            firstOf {
                option {
                    check { isSet(Setting.QuickAddLikeTerms) }
                    apply(CollectingRules.CombineTwoSimpleLikeTerms)
                }
                option {
                    withNewLabels {
                        apply(CollectingRules.CollectLikeTerms)
                        optionally { applyTo(coefficientSimplificationSteps, Label.A) }
                        optionally(GeneralRules.EliminateZeroInSum)
                    }
                }
            }
        }
    }
}
