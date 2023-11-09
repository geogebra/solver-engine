package methods.integerroots

import engine.methods.testMethod
import org.junit.jupiter.api.Test

class SimplifyPowerOfIntegerUnderRootTest {

    @Test
    fun testSimplificationOfIntegerPowerUnderHigherOrderRoot() = testMethod {
        method = IntegerRootsPlans.SimplifyPowerOfIntegerUnderRoot
        inputExpr = "root[[24 ^ 5], 6]"

        check {
            fromExpr = "root[[24 ^ 5], 6]"
            toExpr = "4 root[1944, 6]"
            explanation {
                key = IntegerRootsExplanation.SimplifyPowerOfIntegerUnderRoot
            }

            step {
                fromExpr = "root[[24 ^ 5], 6]"
                toExpr = "root[[2 ^ 15] * [3 ^ 5], 6]"
                explanation {
                    key = IntegerRootsExplanation.FactorizeAndDistributePowerUnderRoot
                }
            }

            step {
                fromExpr = "root[[2 ^ 15] * [3 ^ 5], 6]"
                toExpr = "root[[2 ^ 15], 6] * root[[3 ^ 5], 6]"
                explanation {
                    key = IntegerRootsExplanation.SplitRootOfProduct
                }
            }

            step {
                fromExpr = "root[[2 ^ 15], 6] * root[[3 ^ 5], 6]"
                toExpr = "<.4 sqrt[2].> * root[[3 ^ 5], 6]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyPowerOfIntegerUnderRoot
                }
            }

            step {
                fromExpr = "<.4 sqrt[2].> * root[[3 ^ 5], 6]"
                toExpr = "<.4 sqrt[2].> * root[243, 6]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyPowerOfIntegerUnderRoot
                }
            }

            step {
                fromExpr = "4 sqrt[2] * root[243, 6]"
                toExpr = "4 root[1944, 6]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyProductWithRoots
                }
            }
        }
    }
}
