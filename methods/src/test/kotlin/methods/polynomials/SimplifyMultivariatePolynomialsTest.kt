package methods.polynomials

import engine.methods.testMethod
import methods.collecting.CollectingExplanation
import methods.constantexpressions.ConstantExpressionsExplanation
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test

class SimplifyMultivariatePolynomialsTest {

    @Test
    fun testFactorOfOneIsEliminatedInPolynomial() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "1 x * 1 y"

        check {
            fromExpr = "1 x * 1 y"
            toExpr = "x y"
            explanation {
                key = PolynomialsExplanation.SimplifyPolynomialExpressionInOneVariable
            }

            step {
                fromExpr = "1 x * 1 y"
                toExpr = "x * 1 y"
                explanation {
                    key = GeneralExplanation.RemoveUnitaryCoefficient
                }
            }

            step {
                fromExpr = "x * 1 y"
                toExpr = "x y"
                explanation {
                    key = GeneralExplanation.RemoveUnitaryCoefficient
                }
            }
        }
    }

    @Test
    fun `test rearranging multivariate monomial`() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "3 [x ^ 4] [a ^ 2] b y"

        check {
            fromExpr = "3 [x ^ 4] [a ^ 2] b y"
            toExpr = "3 [a ^ 2] b [x ^ 4] y"
            explanation {
                key = GeneralExplanation.ReorderProduct
            }
        }
    }

    @Test
    fun `test collecting like multivariate terms`() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "a x + [1 / 3] a b x - 2 a x + [3 / 4] a b x"

        check {
            fromExpr = "a x + [1 / 3] a b x - 2 a x + [3 / 4] a b x"
            toExpr = "-a x + [13 / 12] a b x"
            explanation {
                key = PolynomialsExplanation.SimplifyPolynomialExpressionInOneVariable
            }

            step {
                fromExpr = "a x + [1 / 3] a b x - 2 a x + [3 / 4] a b x"
                toExpr = "-a x + [1 / 3] a b x + [3 / 4] a b x"
                explanation {
                    key = CollectingExplanation.CollectLikeTermsAndSimplify
                }

                step {
                    fromExpr = "a x + [1 / 3] a b x - 2 a x + [3 / 4] a b x"
                    toExpr = "(1 - 2) a x + [1 / 3] a b x + [3 / 4] a b x"
                    explanation {
                        key = CollectingExplanation.CollectLikeTerms
                    }
                }

                step {
                    fromExpr = "(1 - 2) a x + [1 / 3] a b x + [3 / 4] a b x"
                    toExpr = "-a x + [1 / 3] a b x + [3 / 4] a b x"
                    explanation {
                        key = CollectingExplanation.SimplifyCoefficient
                    }
                }
            }

            step {
                fromExpr = "-a x + [1 / 3] a b x + [3 / 4] a b x"
                toExpr = "-a x + [13 / 12] a b x"
                explanation {
                    key = CollectingExplanation.CollectLikeTermsAndSimplify
                }

                step {
                    fromExpr = "-a x + [1 / 3] a b x + [3 / 4] a b x"
                    toExpr = "-a x + ([1 / 3] + [3 / 4]) a b x"
                    explanation {
                        key = CollectingExplanation.CollectLikeTerms
                    }
                }

                step {
                    fromExpr = "-a x + ([1 / 3] + [3 / 4]) a b x"
                    toExpr = "-a x + [13 / 12] a b x"
                    explanation {
                        key = CollectingExplanation.SimplifyCoefficient
                    }
                }
            }
        }
    }

    @Test
    fun `test multiplying multivariate monomials`() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "-[2 / 3] b [x ^ 3] y * 5 a b [x ^ 2] y"

        check {
            fromExpr = "-[2 / 3] b [x ^ 3] y * 5 a b [x ^ 2] y"
            toExpr = "-[10 / 3] a [b ^ 2] [x ^ 5] [y ^ 2]"
            explanation {
                key = PolynomialsExplanation.MultiplyMonomialsAndSimplify
            }

            step {
                fromExpr = "[2 / 3] b [x ^ 3] y * 5 a b [x ^ 2] y"
                toExpr = "([2 / 3] * 5) a (b * b) ([x ^ 3] * [x ^ 2]) (y * y)"
                explanation {
                    key = PolynomialsExplanation.RearrangeProductOfMonomials
                }
            }

            step {
                fromExpr = "([2 / 3] * 5) a (b * b) ([x ^ 3] * [x ^ 2]) (y * y)"
                toExpr = "[10 / 3] a (b * b) ([x ^ 3] * [x ^ 2]) (y * y)"
                explanation {
                    key = PolynomialsExplanation.SimplifyCoefficient
                }
            }

            step {
                fromExpr = "[10 / 3] a (b * b) ([x ^ 3] * [x ^ 2]) (y * y)"
                toExpr = "[10 / 3] a [b ^ 2] ([x ^ 3] * [x ^ 2]) (y * y)"
                explanation {
                    key = PolynomialsExplanation.MultiplyUnitaryMonomialsAndSimplify
                }
            }

            step {
                fromExpr = "[10 / 3] a [b ^ 2] ([x ^ 3] * [x ^ 2]) (y * y)"
                toExpr = "[10 / 3] a [b ^ 2] [x ^ 5] (y * y)"
                explanation {
                    key = PolynomialsExplanation.MultiplyUnitaryMonomialsAndSimplify
                }
            }

            step {
                fromExpr = "[10 / 3] a [b ^ 2] [x ^ 5] (y * y)"
                toExpr = "[10 / 3] a [b ^ 2] [x ^ 5] [y ^ 2]"
                explanation {
                    key = PolynomialsExplanation.MultiplyUnitaryMonomialsAndSimplify
                }
            }
        }
    }

    @Test
    fun `test simplifying power of multivariate monomial`() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "[(3 a [b ^ 2] c [x ^ 4]) ^ 3]"

        check {
            fromExpr = "[(3 a [b ^ 2] c [x ^ 4]) ^ 3]"
            toExpr = "27 [a ^ 3] [b ^ 6] [c ^ 3] [x ^ 12]"
            explanation {
                key = PolynomialsExplanation.DistributeProductToIntegerPowerAndSimplify
            }

            step {
                fromExpr = "[(3 a [b ^ 2] c [x ^ 4]) ^ 3]"
                toExpr = "[3 ^ 3] [a ^ 3] [([b ^ 2]) ^ 3] [c ^ 3] [([x ^ 4]) ^ 3]"
                explanation {
                    key = GeneralExplanation.DistributePowerOfProduct
                }
            }

            step {
                fromExpr = "[3 ^ 3] [a ^ 3] [([b ^ 2]) ^ 3] [c ^ 3] [([x ^ 4]) ^ 3]"
                toExpr = "27 [a ^ 3] [([b ^ 2]) ^ 3] [c ^ 3] [([x ^ 4]) ^ 3]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyPowerOfInteger
                }
            }

            step {
                fromExpr = "27 [a ^ 3] [([b ^ 2]) ^ 3] [c ^ 3] [([x ^ 4]) ^ 3]"
                toExpr = "27 [a ^ 3] [b ^ 6] [c ^ 3] [([x ^ 4]) ^ 3]"
                explanation {
                    key = PolynomialsExplanation.SimplifyPowerOfUnitaryMonomial
                }
            }

            step {
                fromExpr = "27 [a ^ 3] [b ^ 6] [c ^ 3] [([x ^ 4]) ^ 3]"
                toExpr = "27 [a ^ 3] [b ^ 6] [c ^ 3] [x ^ 12]"
                explanation {
                    key = PolynomialsExplanation.SimplifyPowerOfUnitaryMonomial
                }
            }
        }
    }
}
