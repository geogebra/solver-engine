package methods.integerroots

import engine.methods.plan
import engine.methods.steps
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.integerCondition
import engine.patterns.integerOrderRootOf
import engine.utility.isPowerOfDegree
import methods.general.collectLikeTermsAndSimplify
import methods.general.removeBracketProductInProduct
import methods.integerarithmetic.evaluateIntegerPowerDirectly
import methods.integerarithmetic.evaluateProductOfIntegers
import methods.integerarithmetic.simplifyIntegersInExpression
import methods.integerarithmetic.simplifyIntegersInProduct

val cancelPowerOfARoot = plan {
    explanation(Explanation.CancelPowerOfARoot)

    pipeline {
        optionalSteps(prepareCancellingPowerOfARoot)
        steps { deeply(simplifyNthRootToThePowerOfN) }
    }
}

val cancelRootOfAPower = plan {
    explanation(Explanation.CancelRootOfAPower)

    pipeline {
        optionalSteps(prepareCancellingRootOfAPower)
        steps { deeply(simplifyNthRootOfNthPower) }
    }
}

val putRootCoefficientUnderRootAndSimplify = plan {
    explanation(Explanation.PutRootCoefficientUnderRootAndSimplify)
    pipeline {
        steps(putRootCoefficientUnderRoot)
        steps(simplifyIntegersInExpression)
    }
}

val simplifyRootOfRootWithCoefficient = plan {
    pattern = integerOrderRootOf(AnyPattern())

    explanation(Explanation.SimplifyRootOfRootWithCoefficient)

    pipeline {
        optionalSteps {
            applyTo(putRootCoefficientUnderRootAndSimplify) { it.nthChild(0) }
        }
        steps(simplifyRootOfRoot)
        steps {
            // evaluate the product in the index of the root
            applyTo(evaluateProductOfIntegers) { it.nthChild(1) }
        }
    }
}

/**
 * Turns a product of roots of integers into a root of a single integer (roots have different orders)
 */
val simplifyProductOfRoots = steps {
    pipeline {
        optionalSteps {
            whilePossible(bringRootsToSameIndexInProduct)
        }
        optionalSteps {
            whilePossible {
                deeply(evaluateIntegerPowerDirectly)
            }
        }
        optionalSteps(simplifyMultiplicationOfSquareRoots)
        optionalSteps {
            whilePossible(multiplyNthRoots)
        }
        optionalSteps {
            whilePossible {
                deeply(simplifyIntegersInProduct)
            }
        }
    }
}

/**
 * Simplifies a product with integer factors and root factors so that the roots are gathered together on the right and
 * the integer factors gathered on the left and multiplied out.
 */
val simplifyProductWithRoots = plan {
    explanation(Explanation.SimplifyProductWithRoots)

    pipeline {
        optionalSteps(normaliseProductWithRoots)
        optionalSteps {
            whilePossible {
                deeply(evaluateIntegerPowerDirectly)
            }
        }
        optionalSteps(simplifyIntegersInProduct)
        optionalSteps(simplifyProductOfRoots)
    }
}

/**
 * root[a, p] -> n * root[b, p] where b has no prime factor of multiplicity at least p
 * E.g. root[48, 3] -> 2 * root[6, 3]
 */
val simplifyIntegerRoot = plan {
    pattern = integerOrderRootOf(UnsignedIntegerPattern())

    explanation(Explanation.SimplifyIntegerRoot)
    pipeline {

        steps {
            firstOf {

                // First try to do easy factorisation without prime factor decomposition
                option {
                    pipeline {
                        optionalSteps(writeRootAsRootProduct)
                        optionalSteps(splitRootOfProduct)
                        steps {
                            plan {
                                explanation(Explanation.WriteRootsAsRootPowers)
                                whilePossible {
                                    deeply(writeRootAsRootPower)
                                }
                            }
                        }
                    }
                }

                // If that fails try prime factor decomposition
                option {
                    pipeline {
                        // root[2^3 * 5^2 * 7^5, 2]
                        steps(factorizeIntegerUnderRoot)

                        // root[2^3, 2] * root[5^2, 2] * root[7^5, 2]
                        optionalSteps(splitRootOfProduct)
                    }
                }
            }
        }

        // root[2^2, 2] * root[2] * root[5^2, 2] * root[7^4, 2] * root[7, 2]
        optionalSteps {
            plan {
                explanation(Explanation.SplitRootsInProduct)
                pipeline {
                    optionalSteps { whilePossible { deeply(splitPowerUnderRoot) } }
                    optionalSteps { whilePossible { deeply(splitRootOfProduct) } }
                    optionalSteps { whilePossible(removeBracketProductInProduct) }
                }
            }
        }

        // 2 * root[2] * 5 * 7^2 * root[7]
        optionalSteps {
            plan {
                explanation(Explanation.CancelAllRootsOfPowers)
                whilePossible {
                    deeply(cancelRootOfAPower)
                }
            }
        }

        // 490 * root[14]
        optionalSteps(simplifyProductWithRoots)
    }
}

val simplifyIntegerRootToInteger = plan {
    explanation(Explanation.SimplifyIntegerRootToInteger)
    val radicand = UnsignedIntegerPattern()
    val radical = integerOrderRootOf(radicand)
    pattern = ConditionPattern(
        radical,
        integerCondition(radicand, radical.order) { n, order -> n.isPowerOfDegree(order.toInt()) }
    )

    applyTo(simplifyIntegerRoot) { it }
}

/**
 * Use the method factory [collectLikeTermsAndSimplify] to collect
 * and simplify all terms containing a root of an integer (with a
 * rational coefficient)
 */
val collectLikeRootsAndSimplify = collectLikeTermsAndSimplify(
    integerOrderRootOf(UnsignedIntegerPattern()),
    Explanation.CollectLikeRootsAndSimplify,
    Explanation.CollectLikeRoots
)
