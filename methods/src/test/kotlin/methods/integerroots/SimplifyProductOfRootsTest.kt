package methods.integerroots

import engine.methods.testMethod
import org.junit.jupiter.api.Test

class SimplifyProductOfRootsTest {
    @Test
    fun testProductOfEqualSquareRoots() =
        testMethod {
            method = IntegerRootsPlans.SimplifyProductWithRoots
            inputExpr = "sqrt[6] * sqrt[6]"

            check {
                fromExpr = "sqrt[6] * sqrt[6]"
                toExpr = "6"
                explanation {
                    key = IntegerRootsExplanation.SimplifyMultiplicationOfSquareRoots
                }
            }
        }

    @Test
    fun testProductOfDifferentSquareRoots() =
        testMethod {
            method = IntegerRootsPlans.SimplifyProductWithRoots
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
