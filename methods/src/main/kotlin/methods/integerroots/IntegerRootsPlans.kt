package methods.integerroots

import engine.methods.plan
import engine.patterns.AnyPattern
import engine.patterns.ConditionPattern
import engine.patterns.UnsignedIntegerPattern
import engine.patterns.integerOrderRootOf
import engine.patterns.numericCondition
import engine.utility.isPowerOfDegree
import methods.constantexpressions.simplifyFractionsInExpression
import methods.constantexpressions.simplifyIntegersInExpression
import methods.fractionarithmetic.multiplyAndSimplifyFractions
import methods.general.moveSignOfNegativeFactorOutOfProduct
import methods.general.removeBracketsProduct
import methods.general.removeRedundantBrackets
import methods.integerarithmetic.evaluateIntegerPowerDirectly
import methods.integerarithmetic.evaluateProductOfIntegers
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
val simplifyProductOfRoots = plan {
    explanation(Explanation.SimplifyProductOfRoots)
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
        // root[2^3 * 5^2 * 7^5, 2]
        optionalSteps(factorizeIntegerUnderSquareRoot)

        // root[2^3, 2] * root[5^2, 2] * root[7^5, 2]
        optionalSteps(splitRootOfProduct)

        optionalSteps {
            plan {
                pipeline {
                    // root[2^2, 2] * root[2] * root[5^2, 2] * root[7^4, 2] * root[7, 2]
                    optionalSteps {
                        plan {
                            pipeline {
                                optionalSteps { whilePossible { deeply(splitPowerUnderRoot) } }
                                optionalSteps { whilePossible { deeply(splitRootOfProduct) } }
                                optionalSteps { whilePossible(removeBracketsProduct) }
                            }
                        }
                    }
                    // 2 * root[2] * 5 * 7^2 * root[7]
                    optionalSteps {
                        plan {
                            whilePossible {
                                deeply(cancelRootOfAPower)
                            }
                        }
                    }

                    // 490 * root[14]
                    optionalSteps(simplifyProductWithRoots)
                }
            }
        }
    }
}

val simplifyIntegerRootToInteger = plan {
    explanation(Explanation.SimplifyIntegerRootToInteger)
    val radicand = UnsignedIntegerPattern()
    val radical = integerOrderRootOf(radicand)
    pattern = ConditionPattern(
        radical,
        numericCondition(radicand, radical.order) { n, order -> n.isPowerOfDegree(order.toInt()) }
    )

    applyTo(simplifyIntegerRoot) { it }
}

/**
 * Collects a set of like roots (square or higher) in a sum and simplifies the resulting expression
 *   E.g. 2 + sqrt[3] - [2/3]*sqrt[3]] + sqrt[5] + [sqrt[3]/2]
 * sqrt[3] is collected first
 *   2 + (1 - [2/3] + [1/2])*sqrt[3] + sqrt[5]
 * then the bracket is simplified
 *   2 + [5/6]*sqrt[3] + sqrt[5]
 * And the expression is written in a normalized way
 *   2 + [5*sqrt[3]/6] + sqrt[5]
 */
val collectLikeRootsAndSimplify = plan {
    explanation(Explanation.CollectLikeRootsAndSimplify)
    pipeline {
        steps(collectLikeRoots)
        steps(simplifyFractionsInExpression)
        optionalSteps { deeply(moveSignOfNegativeFactorOutOfProduct) }
        optionalSteps { deeply(removeRedundantBrackets) }
        optionalSteps { deeply(multiplyAndSimplifyFractions) }
    }
}
