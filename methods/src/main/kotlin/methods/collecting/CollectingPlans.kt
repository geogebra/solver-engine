package methods.collecting

import engine.expressions.Label
import engine.methods.Method
import engine.methods.plan
import engine.methods.stepsproducers.StepsProducer
import engine.methods.stepsproducers.steps
import methods.fractionarithmetic.FractionArithmeticPlans
import methods.general.GeneralRules

private fun createSimplifyCoefficientPlan(simplificationSteps: StepsProducer): Method {
    return plan {
        explanation = Explanation.SimplifyCoefficient

        steps {
            whilePossible(simplificationSteps)
        }
    }
}

private fun createSimplifyAfterCollectingLikeTermsSteps(
    simplificationSteps: StepsProducer,
    preferFractionalForm: Boolean,
): StepsProducer {
    return steps {
        applyTo(Label.A) {
            applyTo(createSimplifyCoefficientPlan(simplificationSteps)) { it.firstChild }
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
        optionally(GeneralRules.EliminateZeroInSum)
    }
}

/**
 * Create a plan to collect and simplify (using the specified [simplificationSteps]) all terms
 * containing a root (with a rational coefficient)
 */
fun createCollectLikeRootsAndSimplifyPlan(simplificationSteps: StepsProducer): Method {
    val coefficientSimplificationSteps =
        createSimplifyAfterCollectingLikeTermsSteps(simplificationSteps, preferFractionalForm = true)

    return plan {
        explanation = Explanation.CollectLikeRootsAndSimplify

        steps {
            withNewLabels {
                firstOf {
                    option(CollectingRules.CombineTwoSimpleLikeRoots)
                    option(CollectingRules.CollectLikeRoots)
                }
                optionally(coefficientSimplificationSteps)
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
        createSimplifyAfterCollectingLikeTermsSteps(simplificationSteps, preferFractionalForm = true)

    return plan {
        explanation = Explanation.CollectLikeRationalPowersAndSimplify

        steps {
            withNewLabels {
                firstOf {
                    option(CollectingRules.CombineTwoSimpleLikeRationalPowers)
                    option(CollectingRules.CollectLikeRationalPowers)
                }
                optionally(coefficientSimplificationSteps)
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
        createSimplifyAfterCollectingLikeTermsSteps(simplificationSteps, preferFractionalForm = false)

    return plan {
        explanation = Explanation.CollectLikeTermsAndSimplify

        steps {
            withNewLabels {
                firstOf {
                    option(CollectingRules.CombineTwoSimpleLikeTerms)
                    option(CollectingRules.CollectLikeTerms)
                }
                optionally(coefficientSimplificationSteps)
            }
        }
    }
}
