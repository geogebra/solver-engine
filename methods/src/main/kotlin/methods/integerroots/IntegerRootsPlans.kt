package methods.integerroots

import engine.expressions.Root
import engine.expressions.SquareRoot
import engine.methods.CompositeMethod
import engine.methods.RunnerMethod
import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.SignedIntegerPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.integerCondition
import engine.patterns.integerOrderRootOf
import engine.patterns.powerOf
import engine.patterns.rootOf
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

            steps {
                whilePossible { deeply(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) }
                optionally(IntegerArithmeticPlans.SimplifyIntegersInProduct)
                optionally(simplifyProductOfRoots)
            }
        },
    ),

    /**
     * root[a, p] -> n * root[b, p] where b has no prime factor of multiplicity at least p
     * E.g. root[48, 3] -> 2 * root[6, 3]
     */
    SimplifyIntegerRoot(
        plan {
            pattern = integerOrderRootOf(UnsignedIntegerPattern())

            explanation = Explanation.SimplifyIntegerRoot

            steps {
                firstOf {

                    // First try to do easy factorisation without prime factor decomposition
                    option {
                        optionally(IntegerRootsRules.WriteRootAsRootProduct)
                        optionally(IntegerRootsRules.SplitRootOfProduct)
                        plan {
                            explanation = Explanation.WriteRootsAsRootPowers

                            steps {
                                whilePossible {
                                    deeply(IntegerRootsRules.WriteRootAsRootPower)
                                }
                            }
                        }
                    }

                    // If that fails try prime factor decomposition
                    option {
                        // root[2^3 * 5^2 * 7^5, 2]
                        apply(IntegerRootsRules.FactorizeIntegerUnderRoot)

                        // root[2^3, 2] * root[5^2, 2] * root[7^5, 2]
                        optionally(IntegerRootsRules.SplitRootOfProduct)
                    }
                }

                optionally(SplitRootsInProduct)
                optionally(CancelAllRootsOfPowers)

                // 490 * root[14]
                optionally(SimplifyProductWithRoots)
            }
        },
    ),

    SimplifyIntegerRootToInteger(
        plan {
            explanation = Explanation.SimplifyIntegerRootToInteger
            val radicand = UnsignedIntegerPattern()
            val radical = integerOrderRootOf(radicand)
            pattern = ConditionPattern(
                radical,
                integerCondition(radicand, radical.order) { n, order -> n.isPowerOfDegree(order.toInt()) },
            )

            steps {
                applyTo(SimplifyIntegerRoot) { it }
            }
        },
    ),

    SplitRootsInProduct(
        plan {
            explanation = Explanation.SplitRootsInProduct

            steps {
                whilePossible { deeply(IntegerRootsRules.SplitPowerUnderRoot) }
                whilePossible { deeply(IntegerRootsRules.SplitRootOfProduct) }
            }
        },
    ),

    SplitRootsAndCancelRootsOfPowers(
        plan {
            explanation = Explanation.SplitRootsAndCancelRootsOfPowers
            steps {
                apply(SplitRootsInProduct)
                apply(CancelAllRootsOfPowers)
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

    CancelAllRootsOfPowers(
        plan {
            explanation = Explanation.CancelAllRootsOfPowers

            steps {
                whilePossible {
                    deeply(cancelRootOfPower)
                }
            }
        },
    ),

    SimplifyPowerOfIntegerUnderRoot(
        plan {
            explanation = Explanation.SimplifyPowerOfIntegerUnderRoot

            // root[[24 ^ 5], 3] -> root[[24 ^ 3], 3] * root[[24 ^ 2], 3]
            steps {
                optionally(FactorizeAndDistributePowerUnderRoot)
                optionally(IntegerRootsRules.SplitRootOfProduct)
                apply {
                    whilePossible { deeply(cancelRootOfPower) }
                }
                whilePossible { deeply(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) }
            }
        },
    ),
}

val cancelRootOfPower = steps {
    firstOf {
        option {
            optionally {
                check { it is SquareRoot || it is Root }
                applyTo(IntegerArithmeticRules.SimplifyEvenPowerOfNegative) { it.firstChild }
            }
            apply(IntegerRootsRules.SimplifyNthRootOfNthPower)
        }
        option {
            plan {
                pattern = rootOf(powerOf(AnyPattern(), SignedIntegerPattern()), SignedIntegerPattern())
                explanation = Explanation.RewriteAndCancelPowerUnderRoot

                steps {
                    optionally {
                        applyTo(IntegerArithmeticRules.SimplifyEvenPowerOfNegative) { it.firstChild }
                    }
                    optionally(GeneralRules.RewritePowerUnderRoot)
                    apply(GeneralRules.CancelRootIndexAndExponent)
                }
            }
        }
    }
}

/**
 * Turns a product of roots of integers into a root of a single integer (roots have different orders)
 */
val simplifyProductOfRoots = steps {
    whilePossible(IntegerRootsRules.BringRootsToSameIndexInProduct)
    whilePossible { deeply(IntegerArithmeticRules.EvaluateIntegerPowerDirectly) }
    optionally(IntegerRootsRules.SimplifyMultiplicationOfSquareRoots)
    whilePossible(IntegerRootsRules.MultiplyNthRoots)
    whilePossible { deeply(IntegerArithmeticPlans.SimplifyIntegersInProduct) }
}
