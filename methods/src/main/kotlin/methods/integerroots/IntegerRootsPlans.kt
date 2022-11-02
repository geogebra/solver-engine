package methods.integerroots

import engine.methods.plan
import engine.methods.stepsproducers.steps
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.integerCondition
import engine.patterns.integerOrderRootOf
import engine.utility.isPowerOfDegree
import methods.constantexpressions.simplifyAfterCollectingLikeTerms
import methods.general.cancelRootIndexAndExponent
import methods.general.distributePowerOfProduct
import methods.general.multiplyExponentsUsingPowerRule
import methods.general.removeBracketProductInProduct
import methods.general.rewritePowerUnderRoot
import methods.integerarithmetic.evaluateIntegerPowerDirectly
import methods.integerarithmetic.evaluateProductOfIntegers
import methods.integerarithmetic.simplifyIntegersInExpression
import methods.integerarithmetic.simplifyIntegersInProduct

val cancelPowerOfARoot = plan {
    explanation(Explanation.CancelPowerOfARoot)

    steps {
        optionally(prepareCancellingPowerOfARoot)
        deeply(simplifyNthRootToThePowerOfN)
    }
}

val cancelRootOfAPower = plan {
    explanation(Explanation.CancelRootOfAPower)

    steps {
        optionally(prepareCancellingRootOfAPower)
        deeply(simplifyNthRootOfNthPower)
    }
}

val putRootCoefficientUnderRootAndSimplify = plan {
    explanation(Explanation.PutRootCoefficientUnderRootAndSimplify)

    steps {
        apply(putRootCoefficientUnderRoot)
        apply(simplifyIntegersInExpression)
    }
}

val simplifyRootOfRootWithCoefficient = plan {
    pattern = integerOrderRootOf(AnyPattern())

    explanation(Explanation.SimplifyRootOfRootWithCoefficient)

    steps {
        optionally {
            applyTo(putRootCoefficientUnderRootAndSimplify) { it.nthChild(0) }
        }
        apply(simplifyRootOfRoot)
        // evaluate the product in the index of the root
        applyTo(evaluateProductOfIntegers) { it.nthChild(1) }
    }
}

/**
 * Turns a product of roots of integers into a root of a single integer (roots have different orders)
 */
val simplifyProductOfRoots = steps {
    whilePossible(bringRootsToSameIndexInProduct)
    whilePossible { deeply(evaluateIntegerPowerDirectly) }
    optionally(simplifyMultiplicationOfSquareRoots)
    whilePossible(multiplyNthRoots)
    whilePossible { deeply(simplifyIntegersInProduct) }
}

/**
 * Simplifies a product with integer factors and root factors so that the roots are gathered together on the right and
 * the integer factors gathered on the left and multiplied out.
 */
val simplifyProductWithRoots = plan {
    explanation(Explanation.SimplifyProductWithRoots)

    steps {
        optionally(normaliseProductWithRoots)
        whilePossible { deeply(evaluateIntegerPowerDirectly) }
        optionally(simplifyIntegersInProduct)
        optionally(simplifyProductOfRoots)
    }
}

val splitRootsInProduct = plan {
    explanation(Explanation.SplitRootsInProduct)

    steps {
        whilePossible { deeply(splitPowerUnderRoot) }
        whilePossible { deeply(splitRootOfProduct) }
        whilePossible(removeBracketProductInProduct)
    }
}

val cancelAllRootsOfPowers = plan {
    explanation(Explanation.CancelAllRootsOfPowers)

    steps {
        whilePossible {
            deeply(cancelRootOfAPower)
        }
    }
}

val splitRootsAndCancelRootsOfPowers = plan {
    explanation(Explanation.SplitRootsAndCancelRootsOfPowers)
    steps {
        apply(splitRootsInProduct)
        apply(cancelAllRootsOfPowers)
    }
}

val factorizeAndDistributePowerUnderRoot = plan {
    explanation(Explanation.FactorizeAndDistributePowerUnderRoot)

    steps {
        // root[ 24^2, 3] --> root[ (2^3 * 3)^2, 3]
        apply(factorizeIntegerPowerUnderRoot)
        // (2^3 * 3)^2 --> (2^3)^2 * 3^2
        deeply(distributePowerOfProduct, deepFirst = true)
        // (2^3)^2 * 3^2 --> 2^3*2  * 3^2
        whilePossible { deeply(multiplyExponentsUsingPowerRule, deepFirst = true) }
        // 2^6 * 3^2
        whilePossible { deeply(simplifyIntegersInProduct, deepFirst = true) }
    }
}

val rewriteAndCancelPowerUnderRoot = plan {
    explanation(Explanation.RewriteAndCancelPowerUnderRoot)

    steps {
        deeply(rewritePowerUnderRoot, deepFirst = true)
        deeply(cancelRootIndexAndExponent, deepFirst = true)
    }
}

val simplifyPowerOfIntegerUnderRoot = plan {
    explanation(Explanation.SimplifyPowerOfIntegerUnderRoot)

    // root[ 24^5, 3] -> root[24^3, 3] * root[24^2, 3]
    steps {
        whilePossible {
            firstOf {
                option(splitRootsAndCancelRootsOfPowers)
                option { deeply(factorizeAndDistributePowerUnderRoot, deepFirst = true) }
                option { deeply(rewriteAndCancelPowerUnderRoot, deepFirst = true) }
            }
        }
    }
}

/**
 * root[a, p] -> n * root[b, p] where b has no prime factor of multiplicity at least p
 * E.g. root[48, 3] -> 2 * root[6, 3]
 */
val simplifyIntegerRoot = plan {
    pattern = integerOrderRootOf(UnsignedIntegerPattern())

    explanation(Explanation.SimplifyIntegerRoot)

    steps {
        firstOf {

            // First try to do easy factorisation without prime factor decomposition
            option {
                optionally(writeRootAsRootProduct)
                optionally(splitRootOfProduct)
                plan {
                    explanation(Explanation.WriteRootsAsRootPowers)

                    steps {
                        whilePossible {
                            deeply(writeRootAsRootPower)
                        }
                    }
                }
            }

            // If that fails try prime factor decomposition
            option {
                // root[2^3 * 5^2 * 7^5, 2]
                apply(factorizeIntegerUnderRoot)

                // root[2^3, 2] * root[5^2, 2] * root[7^5, 2]
                optionally(splitRootOfProduct)
            }
        }

        optionally(splitRootsInProduct)
        optionally(cancelAllRootsOfPowers)

        // 490 * root[14]
        optionally(simplifyProductWithRoots)
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

    steps {
        applyTo(simplifyIntegerRoot) { it }
    }
}

/**
 * Collect and simplify all terms containing a root of an integer
 * (with a rational coefficient)
 */
val collectLikeRootsAndSimplify = plan {
    explanation(Explanation.CollectLikeRootsAndSimplify)

    steps {
        apply(collectLikeRoots)
        apply(simplifyAfterCollectingLikeTerms)
    }
}
