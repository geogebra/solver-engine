package methods.polynomials

import engine.methods.testMethod
import methods.constantexpressions.ConstantExpressionsExplanation
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test

class CollectLikeTermsTest {

    @Test
    fun testCollectSimpleLikeTerms() = testMethod {
        method = PolynomialPlans.SimplifyAlgebraicExpression
        inputExpr = "2 x + 3 x + y - 5 y"

        check {
            fromExpr = "2 x + 3 x + y - 5 y"
            toExpr = "5 x - 4 y"
            explanation {
                key = PolynomialsExplanation.SimplifyAlgebraicExpression
            }

            step {
                fromExpr = "2 x + 3 x + y - 5 y"
                toExpr = "5 x + y - 5 y"
                explanation {
                    key = PolynomialsExplanation.CollectLikeTermsAndSimplify
                }

                step {
                    fromExpr = "2 x + 3 x + y - 5 y"
                    toExpr = "(2 + 3) x + y - 5 y"
                    explanation {
                        key = PolynomialsExplanation.CollectLikeTerms
                    }
                }

                step {
                    fromExpr = "(2 + 3) x + y - 5 y"
                    toExpr = "5 x + y - 5 y"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                    }
                }
            }

            step {
                fromExpr = "5 x + y - 5 y"
                toExpr = "5 x + (-4) y"
                explanation {
                    key = PolynomialsExplanation.CollectLikeTermsAndSimplify
                }

                step {
                    fromExpr = "5 x + y - 5 y"
                    toExpr = "5 x + (1 - 5) y"
                    explanation {
                        key = PolynomialsExplanation.CollectLikeTerms
                    }
                }

                step {
                    fromExpr = "5 x + (1 - 5) y"
                    toExpr = "5 x + (-4) y"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                    }
                }
            }

            step {
                fromExpr = "5 x + (-4) y"
                toExpr = "5 x - 4 y"
                explanation {
                    key = GeneralExplanation.MoveSignOfNegativeFactorOutOfProduct
                }
            }
        }
    }

    @Test
    fun testRationalCoefficients() = testMethod {
        method = PolynomialPlans.SimplifyAlgebraicExpression
        inputExpr = "[z / 2] - [3 z / 4] + 4 z"

        check {
            fromExpr = "[z / 2] - [3 z / 4] + 4 z"
            toExpr = "[15 / 4] z"
            explanation {
                key = PolynomialsExplanation.CollectLikeTermsAndSimplify
            }

            step {
                fromExpr = "[z / 2] - [3 z / 4] + 4 z"
                toExpr = "([1 / 2] - [3 / 4] + 4) z"
                explanation {
                    key = PolynomialsExplanation.CollectLikeTerms
                }
            }

            step {
                fromExpr = "([1 / 2] - [3 / 4] + 4) z"
                toExpr = "[15 / 4] z"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                }
            }
        }
    }

    @Test
    fun testSurdCoefficients() = testMethod {
        method = PolynomialPlans.SimplifyAlgebraicExpression
        inputExpr = "2 t sqrt[3] + [t * sqrt[3] / 3]"

        check {
            fromExpr = "2 t sqrt[3] + [t * sqrt[3] / 3]"
            toExpr = "[7 sqrt[3] / 3] t"
            explanation {
                key = PolynomialsExplanation.CollectLikeTermsAndSimplify
            }

            step {
                fromExpr = "2 t sqrt[3] + [t * sqrt[3] / 3]"
                toExpr = "(2 sqrt[3] + [sqrt[3] / 3]) t"
                explanation {
                    key = PolynomialsExplanation.CollectLikeTerms
                }
            }

            step {
                fromExpr = "(2 sqrt[3] + [sqrt[3] / 3]) t"
                toExpr = "[7 sqrt[3] / 3] t"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                }
            }
        }
    }
}
