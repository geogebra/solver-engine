package methods.polynomials

import engine.methods.testMethod
import methods.collecting.CollectingExplanation
import methods.expand.ExpandExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.fractionroots.FractionRootsExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.integerroots.IntegerRootsExplanation
import org.junit.jupiter.api.Test

@Suppress("LargeClass")
class SimplifyUnivariatePolynomialsTest {

    @Test
    fun testCollectSimpleLikeTerms() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "2 x + 3 x + [x ^ 2] - 5 [x ^ 2]"

        check {
            fromExpr = "2 x + 3 x + [x ^ 2] - 5 [x ^ 2]"
            toExpr = "-4 [x ^ 2] + 5 x"
            explanation {
                key = PolynomialsExplanation.SimplifyPolynomialExpressionInOneVariable
            }

            step {
                fromExpr = "2 x + 3 x + [x ^ 2] - 5 [x ^ 2]"
                toExpr = "5 x + [x ^ 2] - 5 [x ^ 2]"
                explanation {
                    key = CollectingExplanation.CollectLikeTermsAndSimplify
                }

                step {
                    fromExpr = "2 x + 3 x + [x ^ 2] - 5 [x ^ 2]"
                    toExpr = "(2 + 3) x + [x ^ 2] - 5 [x ^ 2]"
                    explanation {
                        key = CollectingExplanation.CollectLikeTerms
                    }
                }

                step {
                    fromExpr = "(2 + 3) x + [x ^ 2] - 5 [x ^ 2]"
                    toExpr = "5 x + [x ^ 2] - 5 [x ^ 2]"
                    explanation {
                        key = CollectingExplanation.SimplifyCoefficient
                    }
                }
            }

            step {
                fromExpr = "5 x + [x ^ 2] - 5 [x ^ 2]"
                toExpr = "5 x - 4 [x ^ 2]"
                explanation {
                    key = CollectingExplanation.CollectLikeTermsAndSimplify
                }

                step {
                    fromExpr = "5 x + [x ^ 2] - 5 [x ^ 2]"
                    toExpr = "5 x + (1 - 5) [x ^ 2]"
                    explanation {
                        key = CollectingExplanation.CollectLikeTerms
                    }
                }

                step {
                    fromExpr = "5 x + (1 - 5) [x ^ 2]"
                    toExpr = "5 x - 4 [x ^ 2]"
                    explanation {
                        key = CollectingExplanation.SimplifyCoefficient
                    }
                }
            }

            step {
                fromExpr = "5 x - 4 [x ^ 2]"
                toExpr = "-4 [x ^ 2] + 5 x"
                explanation {
                    key = PolynomialsExplanation.NormalizePolynomial
                }
            }
        }
    }

    @Test
    fun testMonomialWithZeroCoefficientEliminated() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "3 [a ^ 2] + 2 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"

        check {
            fromExpr = "3 [a ^ 2] + 2 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"
            toExpr = "7 a"
            explanation {
                key = PolynomialsExplanation.SimplifyPolynomialExpressionInOneVariable
            }

            step {
                fromExpr = "3 [a ^ 2] + 2 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"
                toExpr = "-3 a + 10 a"
                explanation {
                    key = CollectingExplanation.CollectLikeTermsAndSimplify
                }

                step {
                    fromExpr = "3 [a ^ 2] + 2 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"
                    toExpr = "(3 + 2 - 5) [a ^ 2] - 3 a + 10 a"
                    explanation {
                        key = CollectingExplanation.CollectLikeTerms
                    }
                }

                step {
                    fromExpr = "(3 + 2 - 5) [a ^ 2] - 3 a + 10 a"
                    toExpr = "0 - 3 a + 10 a"
                    explanation {
                        key = CollectingExplanation.SimplifyCoefficient
                    }
                }

                step {
                    fromExpr = "0 - 3 a + 10 a"
                    toExpr = "-3 a + 10 a"
                    explanation {
                        key = GeneralExplanation.EliminateZeroInSum
                    }
                }
            }

            step {
                fromExpr = "-3 a + 10 a"
                toExpr = "7 a"
                explanation {
                    key = CollectingExplanation.CollectLikeTermsAndSimplify
                }
            }
        }
    }

    @Test
    fun testRationalCoefficients() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "[z / 2] - [3 z / 4] + 4 z"

        check {
            fromExpr = "[z / 2] - [3 z / 4] + 4 z"
            toExpr = "[15 / 4] z"
            explanation {
                key = CollectingExplanation.CollectLikeTermsAndSimplify
            }

            step {
                fromExpr = "[z / 2] - [3 z / 4] + 4 z"
                toExpr = "([1 / 2] - [3 / 4] + 4) z"
                explanation {
                    key = CollectingExplanation.CollectLikeTerms
                }
            }

            step {
                fromExpr = "([1 / 2] - [3 / 4] + 4) z"
                toExpr = "[15 / 4] z"
                explanation {
                    key = CollectingExplanation.SimplifyCoefficient
                }
            }
        }
    }

    @Test
    fun testSurdCoefficients() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "2 t sqrt[3] + [t sqrt[3] / 3]"

        check {
            fromExpr = "2 t sqrt[3] + [t sqrt[3] / 3]"
            toExpr = "[7 sqrt[3] / 3] t"
            explanation {
                key = PolynomialsExplanation.SimplifyPolynomialExpressionInOneVariable
            }

            step {
                fromExpr = "2 t sqrt[3] + [t sqrt[3] / 3]"
                toExpr = "2 sqrt[3] * t + [t sqrt[3] / 3]"
                explanation {
                    key = PolynomialsExplanation.SimplifyMonomial
                }
            }

            step {
                fromExpr = "2 sqrt[3] * t + [t sqrt[3] / 3]"
                toExpr = "2 sqrt[3] * t + [sqrt[3] * t / 3]"
                explanation {
                    key = PolynomialsExplanation.SimplifyMonomial
                }
            }

            step {
                fromExpr = "2 sqrt[3] * t + [sqrt[3] * t / 3]"
                toExpr = "[7 sqrt[3] / 3] t"
                explanation {
                    key = CollectingExplanation.CollectLikeTermsAndSimplify
                }
                step {
                    fromExpr = "2 sqrt[3] * t + [sqrt[3] * t / 3]"
                    toExpr = "(2 sqrt[3] + [sqrt[3] / 3]) t"
                    explanation {
                        key = CollectingExplanation.CollectLikeTerms
                    }
                }

                step {
                    fromExpr = "(2 sqrt[3] + [sqrt[3] / 3]) t"
                    toExpr = "[7 sqrt[3] / 3] t"
                    explanation {
                        key = CollectingExplanation.CollectLikeRootsAndSimplify
                    }
                }
            }
        }
    }

    @Test
    fun testMultiplyUnitaryMonomials() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "t * [t ^ 2] * [t ^ 3]"

        check {
            fromExpr = "t * [t ^ 2] * [t ^ 3]"
            toExpr = "[t ^ 6]"
            explanation {
                key = PolynomialsExplanation.MultiplyUnitaryMonomialsAndSimplify
            }

            step {
                fromExpr = "t * [t ^ 2] * [t ^ 3]"
                toExpr = "[t ^ 1 + 2] * [t ^ 3]"
                explanation {
                    key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                }
            }

            step {
                fromExpr = "[t ^ 1 + 2] * [t ^ 3]"
                toExpr = "[t ^ 1 + 2 + 3]"
                explanation {
                    key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                }
            }

            step {
                fromExpr = "[t ^ 1 + 2 + 3]"
                toExpr = "[t ^ 6]"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                }
            }
        }
    }

    @Test
    fun testMultiplyMonomials() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "3 [x ^ 2] * 4 [x ^ 3]"

        check {
            fromExpr = "3 [x ^ 2] * 4 [x ^ 3]"
            toExpr = "12 [x ^ 5]"
            explanation {
                key = PolynomialsExplanation.MultiplyMonomialsAndSimplify
            }

            step {
                fromExpr = "3 [x ^ 2] * 4 [x ^ 3]"
                toExpr = "(3 * 4) ([x ^ 2] * [x ^ 3])"
                explanation {
                    key = PolynomialsExplanation.RearrangeProductOfMonomials
                }
            }

            step {
                fromExpr = "(3 * 4) ([x ^ 2] * [x ^ 3])"
                toExpr = "12 ([x ^ 2] * [x ^ 3])"
                explanation {
                    key = PolynomialsExplanation.SimplifyCoefficient
                }
            }

            step {
                fromExpr = "12 ([x ^ 2] * [x ^ 3])"
                toExpr = "12 [x ^ 5]"
                explanation {
                    key = PolynomialsExplanation.MultiplyUnitaryMonomialsAndSimplify
                }

                step {
                    fromExpr = "([x ^ 2] * [x ^ 3])"
                    toExpr = "[x ^ 2 + 3]"
                    explanation {
                        key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                    }
                }

                step {
                    fromExpr = "[x ^ 2 + 3]"
                    toExpr = "[x ^ 5]"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                    }
                }
            }
        }
    }

    @Test
    fun testMultiplyMonomialsWithRationalCoefficients() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "2 y * [1 / 3] [y ^ 2]"

        check {
            fromExpr = "2 y * [1 / 3] [y ^ 2]"
            toExpr = "[2 / 3] [y ^ 3]"
            explanation {
                key = PolynomialsExplanation.MultiplyMonomialsAndSimplify
            }

            step {
                fromExpr = "2 y * [1 / 3] [y ^ 2]"
                toExpr = "(2 * [1 / 3]) (y * [y ^ 2])"
                explanation {
                    key = PolynomialsExplanation.RearrangeProductOfMonomials
                }
            }

            step {
                fromExpr = "(2 * [1 / 3]) (y * [y ^ 2])"
                toExpr = "[2 / 3] (y * [y ^ 2])"
                explanation {
                    key = PolynomialsExplanation.SimplifyCoefficient
                }

                step {
                    fromExpr = "(2 * [1 / 3])"
                    toExpr = "[2 / 3]"
                    explanation {
                        key = FractionArithmeticExplanation.MultiplyAndSimplifyFractions
                    }
                }
            }

            step {
                fromExpr = "[2 / 3] (y * [y ^ 2])"
                toExpr = "[2 / 3] [y ^ 3]"
                explanation {
                    key = PolynomialsExplanation.MultiplyUnitaryMonomialsAndSimplify
                }

                step {
                    fromExpr = "(y * [y ^ 2])"
                    toExpr = "[y ^ 1 + 2]"
                    explanation {
                        key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                    }
                }

                step {
                    fromExpr = "[y ^ 1 + 2]"
                    toExpr = "[y ^ 3]"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                    }
                }
            }
        }
    }

    @Test
    fun testNegatedProductContainingMonomialsNoNegativeFactors() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "1 - 2 x * 3 [x ^ 2]"

        check {
            fromExpr = "1 - 2 x * 3 [x ^ 2]"
            toExpr = "-6 [x ^ 3] + 1"
            explanation {
                key = PolynomialsExplanation.SimplifyPolynomialExpressionInOneVariable
            }

            step {
                fromExpr = "1 - 2 x * 3 [x ^ 2]"
                toExpr = "1 - 6 [x ^ 3]"
                explanation {
                    key = PolynomialsExplanation.MultiplyMonomialsAndSimplify
                }

                step {
                    fromExpr = "2 x * 3 [x ^ 2]"
                    toExpr = "(2 * 3) (x * [x ^ 2])"
                    explanation {
                        key = PolynomialsExplanation.RearrangeProductOfMonomials
                    }
                }

                step {
                    fromExpr = "(2 * 3) (x * [x ^ 2])"
                    toExpr = "6 (x * [x ^ 2])"
                    explanation {
                        key = PolynomialsExplanation.SimplifyCoefficient
                    }
                }

                step {
                    fromExpr = "6 (x * [x ^ 2])"
                    toExpr = "6 [x ^ 3]"
                    explanation {
                        key = PolynomialsExplanation.MultiplyUnitaryMonomialsAndSimplify
                    }
                }
            }

            step {
                fromExpr = "1 - 6 [x ^ 3]"
                toExpr = "-6 [x ^ 3] + 1"
                explanation {
                    key = PolynomialsExplanation.NormalizePolynomial
                }
            }
        }
    }

    @Test
    fun testNegatedProductContainingMonomialsWithNegativeFactors() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "1 - 2 x * [(-x) ^ 3]"

        check {
            fromExpr = "1 - 2 x * [(-x) ^ 3]"
            toExpr = "2 [x ^ 4] + 1"
            explanation {
                key = PolynomialsExplanation.SimplifyPolynomialExpressionInOneVariable
            }

            step {
                fromExpr = "1 - 2 x * [(-x) ^ 3]"
                toExpr = "1 - 2 x (-[x ^ 3])"
                explanation {
                    key = PolynomialsExplanation.SimplifyPowerOfUnitaryMonomial
                }
            }

            step {
                fromExpr = "1 - 2 x (-[x ^ 3])"
                toExpr = "1 + 2 x * [x ^ 3]"
                explanation {
                    key = GeneralExplanation.NormalizeNegativeSignsInProduct
                }
            }

            step {
                fromExpr = "1 + 2 x * [x ^ 3]"
                toExpr = "1 + 2 [x ^ 4]"
                explanation {
                    key = PolynomialsExplanation.MultiplyMonomialsAndSimplify
                }

                step {
                    fromExpr = "2 x * [x ^ 3]"
                    toExpr = "2 (x * [x ^ 3])"
                    explanation {
                        key = PolynomialsExplanation.RearrangeProductOfMonomials
                    }
                }

                step {
                    fromExpr = "2 (x * [x ^ 3])"
                    toExpr = "2 [x ^ 4]"
                    explanation {
                        key = PolynomialsExplanation.MultiplyUnitaryMonomialsAndSimplify
                    }
                }
            }

            step {
                fromExpr = "1 + 2 [x ^ 4]"
                toExpr = "2 [x ^ 4] + 1"
                explanation {
                    key = PolynomialsExplanation.NormalizePolynomial
                }
            }
        }
    }

    @Test
    fun testPowerOfUnitaryMonomial() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "[([x ^ 2]) ^ 3]"

        check {
            toExpr = "[x ^ 6]"
        }
    }

    @Test
    fun testCombiningSimplifyingAndNormalizing() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "sqrt[3] + [x ^ 3] + 2 x * 5 x + [(2 x) ^ 3] + 1"

        check {
            fromExpr = "sqrt[3] + [x ^ 3] + 2 x * 5 x + [(2 x) ^ 3] + 1"
            toExpr = "9 [x ^ 3] + 10 [x ^ 2] + sqrt[3] + 1"
            explanation {
                key = PolynomialsExplanation.SimplifyPolynomialExpressionInOneVariable
            }

            step {
                fromExpr = "sqrt[3] + [x ^ 3] + 2 x * 5 x + [(2 x) ^ 3] + 1"
                toExpr = "sqrt[3] + [x ^ 3] + 10 [x ^ 2] + [(2 x) ^ 3] + 1"
                explanation {
                    key = PolynomialsExplanation.MultiplyMonomialsAndSimplify
                }
            }

            step {
                fromExpr = "sqrt[3] + [x ^ 3] + 10 [x ^ 2] + [(2 x) ^ 3] + 1"
                toExpr = "sqrt[3] + [x ^ 3] + 10 [x ^ 2] + 8 [x ^ 3] + 1"
                explanation {
                    key = PolynomialsExplanation.DistributeProductToIntegerPowerAndSimplify
                }
            }

            step {
                fromExpr = "sqrt[3] + [x ^ 3] + 10 [x ^ 2] + 8 [x ^ 3] + 1"
                toExpr = "sqrt[3] + 9 [x ^ 3] + 10 [x ^ 2] + 1"
                explanation {
                    key = CollectingExplanation.CollectLikeTermsAndSimplify
                }
            }

            step {
                fromExpr = "sqrt[3] + 9 [x ^ 3] + 10 [x ^ 2] + 1"
                toExpr = "9 [x ^ 3] + 10 [x ^ 2] + sqrt[3] + 1"
                explanation {
                    key = PolynomialsExplanation.NormalizePolynomial
                }
            }
        }
    }

    /**
     * This tests that after like terms have been collected, in the case that their coefficients do not simplify
     * together, the bracket is not expanded back again.
     */
    @Test
    fun testMonomialWithSumCoefficient() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "2 t + [t / sqrt[3]]"

        check {
            fromExpr = "2 t + [t / sqrt[3]]"
            toExpr = "(2 + [sqrt[3] / 3]) t"
            explanation {
                key = PolynomialsExplanation.SimplifyPolynomialExpressionInOneVariable
            }

            step {
                fromExpr = "2 t + [t / sqrt[3]]"
                toExpr = "2 t + [sqrt[3] * t / 3]"
                explanation {
                    key = PolynomialsExplanation.SimplifyMonomial
                }

                step {
                    fromExpr = "[t / sqrt[3]]"
                    toExpr = "[t sqrt[3] / 3]"
                    explanation {
                        key = FractionRootsExplanation.RationalizeDenominator
                    }
                }

                step {
                    fromExpr = "[t sqrt[3] / 3]"
                    toExpr = "[sqrt[3] * t / 3]"
                    explanation {
                        key = GeneralExplanation.ReorderProduct
                    }
                }
            }

            step {
                fromExpr = "2 t + [sqrt[3] * t / 3]"
                toExpr = "(2 + [sqrt[3] / 3]) t"
                explanation {
                    key = CollectingExplanation.CollectLikeTerms
                }
            }
        }
    }

    @Test
    fun testDistributingNegativeIntoPositives() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "-(a + 3)"

        check {
            fromExpr = "-(a + 3)"
            toExpr = "-a - 3"
            explanation {
                key = ExpandExplanation.DistributeNegativeOverBracket
            }
        }
    }

    @Test
    fun testDistributingNegativeIntoNegatives() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "-(-a - 3)"

        check {
            fromExpr = "-(-a - 3)"
            toExpr = "a + 3"
            explanation {
                key = ExpandExplanation.DistributeNegativeOverBracket
            }
        }
    }

    @Test
    fun `Cancel negatives on different factors of product`() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "-3 (-a)"

        check {
            fromExpr = "-3 (-a)"
            toExpr = "3 a"
            explanation {
                key = GeneralExplanation.SimplifyProductWithTwoNegativeFactors
            }
        }
    }

    @Test
    fun `test simplification left to right`() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "2 x * x + [(2 x) ^ 2] - 3 x * 2 x + 3 * 10 - [([x ^ 2]) ^ 3] + x * x"

        check {
            fromExpr = "2 x * x + [(2 x) ^ 2] - 3 x * 2 x + 3 * 10 - [([x ^ 2]) ^ 3] + x * x"
            toExpr = "-[x ^ 6] + [x ^ 2] + 30"
            explanation {
                key = PolynomialsExplanation.SimplifyPolynomialExpressionInOneVariable
            }

            step {
                fromExpr = "2 x * x + [(2 x) ^ 2] - 3 x * 2 x + 3 * 10 - [([x ^ 2]) ^ 3] + x * x"
                toExpr = "2 [x ^ 2] + [(2 x) ^ 2] - 3 x * 2 x + 3 * 10 - [([x ^ 2]) ^ 3] + x * x"
                explanation {
                    key = PolynomialsExplanation.MultiplyMonomialsAndSimplify
                }
            }

            step {
                fromExpr = "2 [x ^ 2] + [(2 x) ^ 2] - 3 x * 2 x + 3 * 10 - [([x ^ 2]) ^ 3] + x * x"
                toExpr = "2 [x ^ 2] + 4 [x ^ 2] - 3 x * 2 x + 3 * 10 - [([x ^ 2]) ^ 3] + x * x"
                explanation {
                    key = PolynomialsExplanation.DistributeProductToIntegerPowerAndSimplify
                }
            }

            step {
                fromExpr = "2 [x ^ 2] + 4 [x ^ 2] - 3 x * 2 x + 3 * 10 - [([x ^ 2]) ^ 3] + x * x"
                toExpr = "2 [x ^ 2] + 4 [x ^ 2] - 6 [x ^ 2] + 3 * 10 - [([x ^ 2]) ^ 3] + x * x"
                explanation {
                    key = PolynomialsExplanation.MultiplyMonomialsAndSimplify
                }
            }

            step {
                fromExpr = "2 [x ^ 2] + 4 [x ^ 2] - 6 [x ^ 2] + 3 * 10 - [([x ^ 2]) ^ 3] + x * x"
                toExpr = "2 [x ^ 2] + 4 [x ^ 2] - 6 [x ^ 2] + 30 - [([x ^ 2]) ^ 3] + x * x"
                explanation {
                    // This is a strange explanation!
                    key = IntegerRootsExplanation.SimplifyProductWithRoots
                }
            }

            step {
                fromExpr = "2 [x ^ 2] + 4 [x ^ 2] - 6 [x ^ 2] + 30 - [([x ^ 2]) ^ 3] + x * x"
                toExpr = "2 [x ^ 2] + 4 [x ^ 2] - 6 [x ^ 2] + 30 - [x ^ 6] + x * x"
                explanation {
                    key = PolynomialsExplanation.SimplifyPowerOfUnitaryMonomial
                }
            }

            step {
                fromExpr = "2 [x ^ 2] + 4 [x ^ 2] - 6 [x ^ 2] + 30 - [x ^ 6] + x * x"
                toExpr = "2 [x ^ 2] + 4 [x ^ 2] - 6 [x ^ 2] + 30 - [x ^ 6] + [x ^ 2]"
                explanation {
                    key = PolynomialsExplanation.MultiplyUnitaryMonomialsAndSimplify
                }
            }

            step {
                fromExpr = "2 [x ^ 2] + 4 [x ^ 2] - 6 [x ^ 2] + 30 - [x ^ 6] + [x ^ 2]"
                toExpr = "[x ^ 2] + 30 - [x ^ 6]"
                explanation {
                    key = CollectingExplanation.CollectLikeTermsAndSimplify
                }
            }

            step {
                fromExpr = "[x ^ 2] + 30 - [x ^ 6]"
                toExpr = "-[x ^ 6] + [x ^ 2] + 30"
                explanation {
                    key = PolynomialsExplanation.NormalizePolynomial
                }
            }
        }
    }

    @Test
    fun `test simplify abs(abs(x))`() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "abs[abs[x]]"

        check {
            fromExpr = "abs[abs[x]]"
            toExpr = "abs[x]"
            explanation {
                key = GeneralExplanation.ResolveAbsoluteValueOfNonNegativeValue
            }
        }
    }

    @Test
    fun `test simplify abs(x^2)`() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "abs[[x^2]]"

        check {
            fromExpr = "abs[[x ^ 2]]"
            toExpr = "[x ^ 2]"
            explanation {
                key = GeneralExplanation.ResolveAbsoluteValueOfNonNegativeValue
            }
        }
    }

    @Test
    fun `test simplify abs(-x^2)`() = testMethod {
        method = PolynomialsPlans.SimplifyPolynomialExpression
        inputExpr = "abs[-[x^2]]"
        check {
            fromExpr = "abs[-[x ^ 2]]"
            toExpr = "[x ^ 2]"
            explanation {
                key = GeneralExplanation.ResolveAbsoluteValueOfNonPositiveValue
            }
        }
    }
}
