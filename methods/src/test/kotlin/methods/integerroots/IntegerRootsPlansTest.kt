package methods.integerroots

import methods.constantexpressions.simplifyConstantExpression
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.plans.testMethod
import org.junit.jupiter.api.Test

class IntegerRootsPlansTest {
    @Test
    fun testSimplifyIntegerRootsTest() = testMethod {
        method = simplifyIntegerRoot
        inputExpr = "sqrt[113400]"

        check {
            // toExpr = "90 * sqrt[14]"

            step {
                fromExpr = "sqrt[113400]"
                toExpr = "sqrt[[2^3] * [3^4] * [5^2] * 7]"
            }

            step {
                toExpr = "sqrt[[2^3]] * sqrt[[3^4]] * sqrt[[5^2]] * sqrt[7]"
            }

            step {
                fromExpr = "sqrt[[2 ^ 3]] * sqrt[[3 ^ 4]] * sqrt[[5 ^ 2]] * sqrt[7]"
                toExpr = "sqrt[[2 ^ 2]] * sqrt[2] * sqrt[[3 ^ 4]] * sqrt[[5 ^ 2]] * sqrt[7]"
            }

            step {
                toExpr = "2 * sqrt[2] * [3 ^ 2] * 5 * sqrt[7]"
            }

            step {
                toExpr = "90 * sqrt[14]"
            }
        }
    }

    @Test
    fun testRootOfRootWithCoefficient() = testMethod {
        method = simplifyConstantExpression
        inputExpr = "root[3 * sqrt[4], 3]"

        check {
            toExpr = "root[6, 3]"

            step {
                toExpr = "root[3 * 2, 3]"

                step {
                    fromExpr = "sqrt[4]"
                    toExpr = "2"
                }
            }

            step {
                toExpr = "root[6, 3]"
            }
        }
    }

    @Test
    fun testCollectLikeRootsAndSimplify() = testMethod {
        method = collectLikeRootsAndSimplify
        inputExpr = "2 - 3 * sqrt[3] + root[3, 3] + [2 * sqrt[3] / 3] + 2 * sqrt[3]"

        check {
            fromExpr = "2 - 3 * sqrt[3] + root[3, 3] + [2 * sqrt[3] / 3] + 2 * sqrt[3]"
            toExpr = "2 - [sqrt[3] / 3] + root[3, 3]"
            explanation {
                key = IntegerRootsExplanation.CollectLikeRootsAndSimplify
            }

            step {
                fromExpr = "2 - 3 * sqrt[3] + root[3, 3] + [2 * sqrt[3] / 3] + 2 * sqrt[3]"
                toExpr = "2 + (-3 + [2 / 3] + 2) * sqrt[3] + root[3, 3]"
                explanation {
                    key = IntegerRootsExplanation.CollectLikeRoots
                }
            }

            step {
                fromExpr = "2 + (-3 + [2 / 3] + 2) * sqrt[3] + root[3, 3]"
                toExpr = "2 + (-1 + [2 / 3]) * sqrt[3] + root[3, 3]"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                }
            }

            step {
                fromExpr = "2 + (-1 + [2 / 3]) * sqrt[3] + root[3, 3]"
                toExpr = "2 + (-[1 / 3]) * sqrt[3] + root[3, 3]"
            }

            step {
                fromExpr = "2 + (-[1 / 3]) * sqrt[3] + root[3, 3]"
                toExpr = "2 - [1 / 3] * sqrt[3] + root[3, 3]"
                explanation {
                    key = GeneralExplanation.MoveSignOfNegativeFactorOutOfProduct
                }
            }

            step {
                fromExpr = "2 - [1 / 3] * sqrt[3] + root[3, 3]"
                toExpr = "2 - [sqrt[3] / 3] + root[3, 3]"
            }
        }
    }
}

class SimplifyProductOfRootsTest {

    @Test
    fun testProductOfEqualSquareRoots() = testMethod {
        method = simplifyProductWithRoots
        inputExpr = "sqrt[6] * sqrt[6]"

        check {
            toExpr = "6"

            step {}
        }
    }

    @Test
    fun testProductOfDifferentSquareRoots() = testMethod {
        method = simplifyProductWithRoots
        inputExpr = "sqrt[6] * sqrt[3]"

        check {
            toExpr = "sqrt[18]"

            step {
                toExpr = "sqrt[6 * 3]"
            }

            step {
                toExpr = "sqrt[18]"
            }
        }
    }
}
