package methods.polynomials

import engine.methods.testMethod
import methods.constantexpressions.ConstantExpressionsExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import org.junit.jupiter.api.Test

class MultiplyMonomialsAndSimplifyTest {

    @Test
    fun simpleTest() = testMethod {
        method = PolynomialsPlans.MultiplyMonomialsAndSimplify
        inputExpr = "2 x * 3 [x ^ 2]"

        check {
            fromExpr = "2 x * 3 [x ^ 2]"
            toExpr = "6 [x ^ 3]"
            explanation {
                key = PolynomialsExplanation.MultiplyMonomialsAndSimplify
            }

            step {
                fromExpr = "2 x * 3 [x ^ 2]"
                toExpr = "(2 * 3) (x * [x ^ 2])"
                explanation {
                    key = PolynomialsExplanation.CollectUnitaryMonomialsInProduct
                }
            }

            step {
                fromExpr = "(2 * 3) (x * [x ^ 2])"
                toExpr = "6 (x * [x ^ 2])"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                }
            }

            step {
                fromExpr = "6 (x * [x ^ 2])"
                toExpr = "6 [x ^ 3]"
                explanation {
                    key = PolynomialsExplanation.MultiplyUnitaryMonomialsAndSimplify
                }

                step {
                    fromExpr = "x * [x ^ 2]"
                    toExpr = "[x ^ 1 + 2]"
                    explanation {
                        key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                    }
                }

                step {
                    fromExpr = "[x ^ 1 + 2]"
                    toExpr = "[x ^ 3]"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                    }
                }
            }
        }
    }
}
