package methods.integerroots

import methods.constantexpressions.simplifyConstantExpression
import methods.plans.testPlan
import org.junit.jupiter.api.Test

class IntegerRootsPlansTest {
    @Test
    fun testSimplifyIntegerRootsTest() = testPlan {
        plan = simplifyIntegerRoot
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
                toExpr = "90 * sqrt[14]"

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
    }

    @Test
    fun testRootOfRootWithCoefficient() = testPlan {
        plan = simplifyConstantExpression
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
}

class SimplifyProductOfRootsTest {

    @Test
    fun testProductOfEqualSquareRoots() = testPlan {
        plan = simplifyProductOfRoots
        inputExpr = "sqrt[6] * sqrt[6]"

        check {
            toExpr = "6"

            step {}
        }
    }

    @Test
    fun testProductOfDifferentSquareRoots() = testPlan {
        plan = simplifyProductOfRoots
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
