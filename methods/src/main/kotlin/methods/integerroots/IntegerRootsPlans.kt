package methods.integerroots

import engine.expressions.Product
import engine.expressions.Root
import engine.expressions.SquareRoot
import engine.expressions.containsRoots
import engine.methods.CompositeMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.applyToFactors
import engine.methods.stepsproducers.steps
import engine.patterns.AnyPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.condition
import engine.patterns.integerOrderRootOf
import engine.patterns.powerOf
import engine.patterns.rootOf
import engine.patterns.squareRootOf
import engine.steps.metadata.metadata
import engine.utility.isPowerOfDegree
import methods.general.GeneralRules
import methods.integerarithmetic.IntegerArithmeticPlans
import methods.integerarithmetic.IntegerArithmeticRules
import methods.integerarithmetic.simplifyIntegersInExpression

enum class IntegerRootsPlans(override val runner: CompositeMethod) : RunnerMethod {

    CancelPowerOfARoot(
        plan {
            explanation = Explanation.CancelPowerOfARoot

            steps {
                optionally(IntegerRootsRules.PrepareCancellingPowerOfARoot)
                deeply(IntegerRootsRules.SimplifyNthRootToThePowerOfN)
            }
        },
    ),

    PutRootCoefficientUnderRootAndSimplify(
        plan {
            explanation = Explanation.PutRootCoefficientUnderRootAndSimplify

            steps {
                apply(IntegerRootsRules.PutRootCoefficientUnderRoot)
                apply(simplifyIntegersInExpression)
            }
        },
    ),

    SimplifyRootOfRootWithCoefficient(
        plan {
            pattern = integerOrderRootOf(AnyPattern())

            explanation = Explanation.SimplifyRootOfRootWithCoefficient

            steps {
                optionally {
                    applyTo(PutRootCoefficientUnderRootAndSimplify) { it.firstChild }
                }
                apply(IntegerRootsRules.SimplifyRootOfRoot)
                // evaluate the product in the index of the root
                applyTo(IntegerArithmeticRules.EvaluateIntegerProductAndDivision) { it.secondChild }
            }
        },
    ),

    /**
     * Simplifies a product with integer factors and root factors so that the roots are gathered together on the right
     * and the integer factors gathered on the left and multiplied out.
     */
    SimplifyProductWithRoots(
        plan {
            explanation = Explanation.SimplifyProductWithRoots
            pattern = condition { it is Product && it.containsRoots() }

            steps {
                optionally(IntegerArithmeticPlans.SimplifyIntegersInProduct)

                apply {
                    whilePossible(IntegerRootsRules.SimplifyMultiplicationOfSquareRoots)

                    whilePossible(IntegerRootsRules.BringRootsToSameIndexInProduct)
                    whilePossible(IntegerRootsRules.MultiplyNthRoots)
                }

                whilePossible { deeply(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) }
                optionally { deeply(IntegerArithmeticPlans.SimplifyIntegersInProduct) }
            }
        },
    ),

    /**
     * root[a, p] -> n * root[b, p] where b has no prime factor of multiplicity at least p
     * E.g. root[48, 3] -> 2 * root[6, 3]
     */
    SimplifyIntegerRoot(
        plan {
            val radicand = UnsignedIntegerPattern()
            val radical = integerOrderRootOf(radicand)
            pattern = radical

            explanation {
                if (getValue(radicand).isPowerOfDegree(getValue(radical.order).toInt())) {
                    metadata(Explanation.SimplifyIntegerRootToInteger)
                } else {
                    metadata(Explanation.SimplifyIntegerRoot)
                }
            }

            steps {
                firstOf {
                    // First try to do easy factorisation without prime factor decomposition
                    option {
                        // sqrt[81000]
                        optionally(IntegerRootsRules.WriteRootAsRootProduct)
                        // sqrt[81 * 1000]
                        optionally(IntegerRootsRules.SplitRootOfProduct)
                        // sqrt[81] * sqrt[1000]
                        applyToFactors(simplifyRootOfReducedInteger)
                        // 9 * 10 sqrt[10]
                    }

                    // If that fails try prime factor decomposition
                    option {
                        // root[430259200, 4]
                        apply(IntegerRootsRules.FactorizeIntegerUnderRoot)
                        // root[2^10 * 5^2 * 7^5, 4]
                        optionally(IntegerRootsRules.SplitRootOfProduct)
                        // root[2^10, 4] * root[5^2, 4] * root[7^5, 4]
                        applyToFactors(simplifyRootOfReducedIntegerPower)
                        // 4 sqrt[2] * sqrt[5] * 7 root[7, 4]
                    }
                }

                optionally(SimplifyProductWithRoots)
                optionally(IntegerArithmeticPlans.SimplifyIntegersInProduct)
            }
        },
    ),

    SplitAndCancelRootOfPower(
        plan {
            explanation = Explanation.SplitAndCancelRootOfPower

            steps {
                apply(IntegerRootsRules.SplitPowerUnderRoot)
                apply(IntegerRootsRules.SplitRootOfProduct)
                applyToKind<Product>(cancelRootOfPower) { it.firstChild }
            }
        },
    ),

    RewriteAndCancelPowerUnderRoot(
        plan {
            pattern = rootOf(powerOf(AnyPattern(), UnsignedIntegerPattern()), UnsignedIntegerPattern())
            explanation = Explanation.RewriteAndCancelPowerUnderRoot

            steps {
                optionally {
                    applyTo(GeneralRules.SimplifyEvenPowerOfNegative) { it.firstChild }
                }

                optionally(GeneralRules.RewritePowerUnderRoot)
                apply(GeneralRules.CancelRootIndexAndExponent)
            }
        },
    ),

    FactorizeAndDistributePowerUnderRoot(
        plan {
            explanation = Explanation.FactorizeAndDistributePowerUnderRoot

            steps {
                // root[24 ^ 2, 3] --> root[(2^3 * 3) ^ 2, 3]
                apply(IntegerRootsRules.FactorizeIntegerPowerUnderRoot)
                // (2^3 * 3)^2 --> (2^3)^2 * 3^2
                deeply(GeneralRules.DistributePowerOfProduct)
                // (2^3)^2 * 3^2 --> 2^3*2  * 3^2
                whilePossible { deeply(GeneralRules.MultiplyExponentsUsingPowerRule) }
                // 2^6 * 3^2
                whilePossible { deeply(IntegerArithmeticPlans.SimplifyIntegersInProduct) }
            }
        },
    ),

    SimplifyPowerOfIntegerUnderRoot(
        plan {
            explanation = Explanation.SimplifyPowerOfIntegerUnderRoot
            pattern = integerOrderRootOf(powerOf(SignedIntegerPattern(), UnsignedIntegerPattern()))

            // root[[24 ^ 5], 3] -> root[[24 ^ 3], 3] * root[[24 ^ 2], 3]
            steps {
                optionally {
                    applyTo(GeneralRules.SimplifyEvenPowerOfNegative) { it.firstChild }
                }

                apply(FactorizeAndDistributePowerUnderRoot)
                optionally(IntegerRootsRules.SplitRootOfProduct)

                applyToFactors(simplifyRootOfReducedIntegerPower)

                optionally(SimplifyProductWithRoots)
                optionally(IntegerArithmeticPlans.SimplifyIntegersInProduct)
            }
        },
    ),

    SimplifySquareRootWithASquareFactorRadicand(simplifySquareRootWithASquareFactorRadicand),
}

val cancelRootOfPower = steps {
    firstOf {
        option {
            optionally {
                check { it is SquareRoot || it is Root }
                applyTo(GeneralRules.SimplifyEvenPowerOfNegative) { it.firstChild }
            }
            apply(IntegerRootsRules.SimplifyNthRootOfNthPower)
        }
        option(IntegerRootsPlans.RewriteAndCancelPowerUnderRoot)
    }
}

val simplifyRootOfReducedIntegerPower = plan {
    explanation = Explanation.SimplifyPowerOfIntegerUnderRoot

    steps {
        optionally(cancelRootOfPower)
        optionally(IntegerRootsPlans.SplitAndCancelRootOfPower)
        // after the above simplifications powers can remain in various positions
        // we tidy up all cases with a deeply
        whilePossible { deeply(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) }
    }
}

val simplifyRootOfReducedInteger = plan {
    explanation = Explanation.SimplifyIntegerRoot

    steps {
        apply(IntegerRootsRules.WriteRootAsRootPower)
        optionally(cancelRootOfPower)
        optionally(IntegerRootsPlans.SplitAndCancelRootOfPower)
        // after the above simplifications powers can remain in various positions
        // we tidy up all cases with a deeply
        whilePossible { deeply(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) }
    }
}

val simplifySquareRootWithASquareFactorRadicand = plan {
    explanation = Explanation.SimplifySquareRootWithASquareFactorRadicand
    pattern = squareRootOf(AnyPattern())

    steps {
        optionally {
            applyToKind<SquareRoot>(IntegerRootsRules.FactorGreatestCommonSquareIntegerFactor) { it.argument }
        }
        apply(IntegerRootsRules.MoveSquareFactorOutOfRoot)
        applyTo(IntegerRootsPlans.SimplifyIntegerRoot) { it.firstChild }
    }
}
