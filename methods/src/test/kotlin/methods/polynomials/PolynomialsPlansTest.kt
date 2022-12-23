package methods.polynomials

import engine.context.Context
import engine.methods.testMethod
import methods.constantexpressions.ConstantExpressionsExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import org.junit.jupiter.api.Test

class PolynomialsPlansTest {

    @Test
    fun testCollectSimpleLikeTerms() = testMethod {
        method = PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable
        inputExpr = "2 x + 3 x + [x ^ 2] - 5 [x ^ 2]"

        check {
            fromExpr = "2 x + 3 x + [x ^ 2] - 5 [x ^ 2]"
            toExpr = "-4 [x ^ 2] + 5 x"
            explanation {
                key = PolynomialsExplanation.SimplifyAlgebraicExpression
            }

            step {
                fromExpr = "2 x + 3 x + [x ^ 2] - 5 [x ^ 2]"
                toExpr = "5 x + [x ^ 2] - 5 [x ^ 2]"
                explanation {
                    key = PolynomialsExplanation.CollectLikeTermsAndSimplify
                }

                step {
                    fromExpr = "2 x + 3 x + [x ^ 2] - 5 [x ^ 2]"
                    toExpr = "(2 + 3) x + [x ^ 2] - 5 [x ^ 2]"
                    explanation {
                        key = PolynomialsExplanation.CollectLikeTerms
                    }
                }

                step {
                    fromExpr = "(2 + 3) x + [x ^ 2] - 5 [x ^ 2]"
                    toExpr = "5 x + [x ^ 2] - 5 [x ^ 2]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                    }
                }
            }

            step {
                fromExpr = "5 x + [x ^ 2] - 5 [x ^ 2]"
                toExpr = "5 x - 4 [x ^ 2]"
                explanation {
                    key = PolynomialsExplanation.CollectLikeTermsAndSimplify
                }

                step {
                    fromExpr = "5 x + [x ^ 2] - 5 [x ^ 2]"
                    toExpr = "5 x + (1 - 5) [x ^ 2]"
                    explanation {
                        key = PolynomialsExplanation.CollectLikeTerms
                    }
                }

                step {
                    fromExpr = "5 x + (1 - 5) [x ^ 2]"
                    toExpr = "5 x + (-4) [x ^ 2]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                    }
                }

                step {
                    fromExpr = "5 x + (-4) [x ^ 2]"
                    toExpr = "5 x - 4 [x ^ 2]"
                    explanation {
                        key = PolynomialsExplanation.NormalizeMonomial
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
        method = PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable
        inputExpr = "3 [a ^ 2] + 2 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"

        check {
            fromExpr = "3 [a ^ 2] + 2 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"
            toExpr = "7 a"
            explanation {
                key = PolynomialsExplanation.SimplifyAlgebraicExpression
            }

            step {
                fromExpr = "3 [a ^ 2] + 2 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"
                toExpr = "-3 a + 10 a"
                explanation {
                    key = PolynomialsExplanation.CollectLikeTermsAndSimplify
                }

                step {
                    fromExpr = "3 [a ^ 2] + 2 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"
                    toExpr = "(3 + 2 - 5) [a ^ 2] - 3 a + 10 a"
                    explanation {
                        key = PolynomialsExplanation.CollectLikeTerms
                    }
                }

                step {
                    fromExpr = "(3 + 2 - 5) [a ^ 2] - 3 a + 10 a"
                    toExpr = "0 [a ^ 2] - 3 a + 10 a"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                    }
                }

                step {
                    fromExpr = "0 [a ^ 2] - 3 a + 10 a"
                    toExpr = "0 - 3 a + 10 a"
                    explanation {
                        key = PolynomialsExplanation.NormalizeMonomial
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
                    key = PolynomialsExplanation.CollectLikeTermsAndSimplify
                }
            }
        }
    }

    @Test
    fun testRationalCoefficients() = testMethod {
        method = PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable
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
        method = PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable
        inputExpr = "2 t sqrt[3] + [t sqrt[3] / 3]"

        check {
            fromExpr = "2 t sqrt[3] + [t sqrt[3] / 3]"
            toExpr = "[7 sqrt[3] / 3] t"
            explanation {
                key = PolynomialsExplanation.CollectLikeTermsAndSimplify
            }

            step {
                fromExpr = "2 t sqrt[3] + [t sqrt[3] / 3]"
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

    @Test
    fun testMultiplyUnitaryMonomials() = testMethod {
        method = PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable
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
        method = PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable
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
                    key = PolynomialsExplanation.CollectUnitaryMonomialsInProduct
                }
            }

            step {
                fromExpr = "(3 * 4) ([x ^ 2] * [x ^ 3])"
                toExpr = "12 ([x ^ 2] * [x ^ 3])"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                }
            }

            step {
                fromExpr = "12 ([x ^ 2] * [x ^ 3])"
                toExpr = "12 [x ^ 5]"
                explanation {
                    key = PolynomialsExplanation.MultiplyUnitaryMonomialsAndSimplify
                }

                step {
                    fromExpr = "[x ^ 2] * [x ^ 3]"
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
        method = PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable
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
                    key = PolynomialsExplanation.CollectUnitaryMonomialsInProduct
                }
            }

            step {
                fromExpr = "(2 * [1 / 3]) (y * [y ^ 2])"
                toExpr = "[2 / 3] (y * [y ^ 2])"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                }

                step {
                    fromExpr = "2 * [1 / 3]"
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
                    fromExpr = "y * [y ^ 2]"
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
        method = PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable
        inputExpr = "1 - 2 x * 3 [x ^ 2]"

        check {
            fromExpr = "1 - 2 x * 3 [x ^ 2]"
            toExpr = "-6 [x ^ 3] + 1"
            explanation {
                key = PolynomialsExplanation.SimplifyAlgebraicExpression
            }

            step {
                fromExpr = "1 - 2 x * 3 [x ^ 2]"
                toExpr = "1 - 6 [x ^ 3]"
                explanation {
                    key = PolynomialsExplanation.MultiplyMonomialsAndSimplify
                }

                step {
                    fromExpr = "-2 x * 3 [x ^ 2]"
                    toExpr = "-(2 * 3) (x * [x ^ 2])"
                    explanation {
                        key = PolynomialsExplanation.CollectUnitaryMonomialsInProduct
                    }
                }

                step {
                    fromExpr = "-(2 * 3) (x * [x ^ 2])"
                    toExpr = "-6 (x * [x ^ 2])"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                    }
                }

                step {
                    fromExpr = "-6 (x * [x ^ 2])"
                    toExpr = "-6 [x ^ 3]"
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
        method = PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable
        inputExpr = "1 - 2 x * [(-x) ^ 3]"

        check {
            fromExpr = "1 - 2 x * [(-x) ^ 3]"
            toExpr = "2 [x ^ 4] + 1"
            explanation {
                key = PolynomialsExplanation.SimplifyAlgebraicExpression
            }

            step {
                fromExpr = "1 - 2 x * [(-x) ^ 3]"
                toExpr = "1 - 2 x (-[x ^ 3])"
                explanation {
                    key = PolynomialsExplanation.DistributeProductToIntegerPowerAndSimplify
                }
            }

            step {
                fromExpr = "1 - 2 x (-[x ^ 3])"
                toExpr = "1 + 2 [x ^ 4]"
                explanation {
                    key = PolynomialsExplanation.MultiplyMonomialsAndSimplify
                }

                step {
                    fromExpr = "-2 x (-[x ^ 3])"
                    toExpr = "((-2) * (-1)) (x * [x ^ 3])"
                    explanation {
                        key = PolynomialsExplanation.CollectUnitaryMonomialsInProduct
                    }
                }

                step {
                    fromExpr = "((-2) * (-1)) (x * [x ^ 3])"
                    toExpr = "2 (x * [x ^ 3])"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
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
        method = PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable
        inputExpr = "[([x ^ 2]) ^ 3]"

        check {
            toExpr = "[x ^ 6]"
        }
    }

    @Test
    fun testCombiningSimplifyingAndNormalizing() = testMethod {
        method = PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable
        inputExpr = "sqrt[3] + [x ^ 3] + 2 x * 5 x + [(2 x) ^ 3] + 1"

        check {
            fromExpr = "sqrt[3] + [x ^ 3] + 2 x * 5 x + [(2 x) ^ 3] + 1"
            toExpr = "9 [x ^ 3] + 10 [x ^ 2] + sqrt[3] + 1"
            explanation {
                key = PolynomialsExplanation.SimplifyAlgebraicExpression
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
                    key = PolynomialsExplanation.CollectLikeTermsAndSimplify
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
        method = PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable
        inputExpr = "2 t + [t / sqrt[3]]"

        check {
            fromExpr = "2 t + [t / sqrt[3]]"
            toExpr = "(2 + [sqrt[3] / 3]) t"
            explanation {
                key = PolynomialsExplanation.CollectLikeTermsAndSimplify
            }

            step {
                fromExpr = "2 t + [t / sqrt[3]]"
                toExpr = "(2 + [1 / sqrt[3]]) t"
                explanation {
                    key = PolynomialsExplanation.CollectLikeTerms
                }
            }

            step {
                fromExpr = "(2 + [1 / sqrt[3]]) t"
                toExpr = "(2 + [sqrt[3] / 3]) t"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyExpressionInBrackets
                }
            }
        }
    }

    @Test
    fun testDistributingNegativeIntoPositives() = testMethod {
        method = PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable
        inputExpr = "-(a+3)"

        check {
            fromExpr = "-(a + 3)"
            toExpr = "-a - 3"
            explanation {
                key = GeneralExplanation.DistributeMultiplicationOverSum
            }
        }
    }

    @Test
    fun testDistributingNegativeIntoNegatives() = testMethod {
        method = PolynomialPlans.SimplifyAlgebraicExpressionInOneVariable
        inputExpr = "-(-a-3)"

        check {
            fromExpr = "-(-a - 3)"
            toExpr = "a + 3"
            explanation {
                key = PolynomialsExplanation.SimplifyAlgebraicExpression
            }

            step {
                fromExpr = "-(-a - 3)"
                toExpr = "-(-a) - (-3)"
                explanation {
                    key = GeneralExplanation.DistributeMultiplicationOverSum
                }
            }

            step {
                fromExpr = "-(-a) - (-3)"
                toExpr = "a + 3"
                explanation {
                    key = FractionArithmeticExplanation.NormalizeSignsInFraction
                }

                step {
                    fromExpr = "-(-a) - (-3)"
                    toExpr = "a - (-3)"
                    explanation {
                        key = GeneralExplanation.SimplifyDoubleMinus
                    }
                }

                step {
                    fromExpr = "a - (-3)"
                    toExpr = "a + 3"
                    explanation {
                        key = GeneralExplanation.SimplifyDoubleMinus
                    }
                }
            }
        }
    }
}

class ExpandPolynomial {
    // Binomial Squared
    @Test
    fun testExpandSquareOfBinomialEU() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "[(2x - 3) ^ 2]"

        check {
            fromExpr = "[(2 x - 3) ^ 2]"
            toExpr = "4 [x ^ 2] - 12 x + 9"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "[(2 x - 3) ^ 2]"
                toExpr = "[(2 x) ^ 2] + 2 * 2 x * (-3) + [(-3) ^ 2]"
                explanation {
                    key = GeneralExplanation.ExpandBinomialSquaredUsingIdentity
                }
            }

            step {
                fromExpr = "[(2 x) ^ 2] + 2 * 2 x * (-3) + [(-3) ^ 2]"
                toExpr = "4 [x ^ 2] - 12 x + 9"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }
        }
    }

    @Test
    fun testExpandSquareOfBinomialUS() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "[(2x - 3) ^ 2]"
        context = Context(curriculum = "US")

        check {
            fromExpr = "[(2 x - 3) ^ 2]"
            toExpr = "4 [x ^ 2] - 12 x + 9"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "[(2 x - 3) ^ 2]"
                toExpr = "(2 x - 3) (2 x - 3)"
                explanation {
                    key = GeneralExplanation.RewritePowerAsProduct
                }
            }

            step {
                fromExpr = "(2 x - 3) (2 x - 3)"
                toExpr = "2 x * 2 x + 2 x * (-3) + (-3) * 2 x + (-3) * (-3)"
                explanation {
                    key = GeneralExplanation.ApplyFoilMethod
                }
            }

            step {
                fromExpr = "2 x * 2 x + 2 x * (-3) + (-3) * 2 x + (-3) * (-3)"
                toExpr = "4 [x ^ 2] - 12 x + 9"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }
        }
    }

    // Binomial cubed
    @Test
    fun testExpandCubeOfBinomial() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "[(2x - 3) ^ 3]"

        check {
            fromExpr = "[(2 x - 3) ^ 3]"
            toExpr = "8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "[(2 x - 3) ^ 3]"
                toExpr = "[(2 x) ^ 3] + 3 * [(2 x) ^ 2] * (-3) + 3 * 2 x * [(-3) ^ 2] + [(-3) ^ 3]"
                explanation {
                    key = GeneralExplanation.ExpandBinomialCubedUsingIdentity
                }
            }

            step {
                fromExpr = "[(2 x) ^ 3] + 3 * [(2 x) ^ 2] * (-3) + 3 * 2 x * [(-3) ^ 2] + [(-3) ^ 3]"
                toExpr = "8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }
        }
    }

    @Test
    fun testExpandCubeOfBinomialUS() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "[(2x - 3) ^ 3]"
        context = Context(curriculum = "US")

        check {
            fromExpr = "[(2 x - 3) ^ 3]"
            toExpr = "8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "[(2 x - 3) ^ 3]"
                toExpr = "(2 x - 3) (2 x - 3) (2 x - 3)"
                explanation {
                    key = GeneralExplanation.RewritePowerAsProduct
                }
            }

            step {
                fromExpr = "(2 x - 3) (2 x - 3) (2 x - 3)"
                toExpr = "(2 x * 2 x + 2 x * (-3) + (-3) * 2 x + (-3) * (-3)) (2 x - 3)"
                explanation {
                    key = GeneralExplanation.ApplyFoilMethod
                }
            }

            step {
                fromExpr = "(2 x * 2 x + 2 x * (-3) + (-3) * 2 x + (-3) * (-3)) (2 x - 3)"
                toExpr = "(4 [x ^ 2] - 12 x + 9) (2 x - 3)"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }

            step {
                fromExpr = "(4 [x ^ 2] - 12 x + 9) (2 x - 3)"
                toExpr = "4 [x ^ 2] * 2 x + 4 [x ^ 2] * (-3) + (-12 x) * 2 x + (-12 x) * (-3) + 9 * 2 x + 9 * (-3)"
                explanation {
                    key = GeneralExplanation.ExpandDoubleBrackets
                }
            }

            step {
                fromExpr = "4 [x ^ 2] * 2 x + 4 [x ^ 2] * (-3) + (-12 x) * 2 x + (-12 x) * (-3) + 9 * 2 x + 9 * (-3)"
                toExpr = "8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }
        }
    }

    @Test
    fun testExpandCubeOfBinomialWithConstantMul() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "5 * [(2x - 3) ^ 3]"

        check {
            fromExpr = "5 * [(2 x - 3) ^ 3]"
            toExpr = "40 [x ^ 3] - 180 [x ^ 2] + 270 x - 135"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "5 * [(2 x - 3) ^ 3]"
                toExpr = "5 ([(2 x) ^ 3] + 3 * [(2 x) ^ 2] * (-3) + 3 * 2 x * [(-3) ^ 2] + [(-3) ^ 3])"
                explanation {
                    key = GeneralExplanation.ExpandBinomialCubed
                }
            }

            step {
                fromExpr = "5 ([(2 x) ^ 3] + 3 * [(2 x) ^ 2] * (-3) + 3 * 2 x * [(-3) ^ 2] + [(-3) ^ 3])"
                toExpr = "5 * [(2 x) ^ 3] + 5 * 3 * [(2 x) ^ 2] * (-3) + 5 * 3 * 2 x * [(-3) ^ 2] + 5 * [(-3) ^ 3]"
                explanation {
                    key = GeneralExplanation.DistributeMultiplicationOverSum
                }
            }

            step {
                fromExpr = "5 * [(2 x) ^ 3] + 5 * 3 * [(2 x) ^ 2] * (-3) + 5 * 3 * 2 x * [(-3) ^ 2] + 5 * [(-3) ^ 3]"
                toExpr = "40 [x ^ 3] - 180 [x ^ 2] + 270 x - 135"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }
        }
    }

    @Test
    fun testIdentifyProductOfSumAndDifference1() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "(2x - 3)*(2x + 3)"

        check {
            fromExpr = "(2 x - 3) * (2 x + 3)"
            toExpr = "4 [x ^ 2] - 9"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "(2 x - 3) * (2 x + 3)"
                toExpr = "[(2 x) ^ 2] - [3 ^ 2]"
                explanation {
                    key = GeneralExplanation.ExpandProductOfSumAndDifference
                }
            }

            step {
                fromExpr = "[(2 x) ^ 2] - [3 ^ 2]"
                toExpr = "4 [x ^ 2] - 9"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }
        }
    }

    @Test
    fun testIdentifyProductOfSumAndDifference2() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "(2x - 3)*(2x + 3) * 11"

        check {
            fromExpr = "(2 x - 3) * (2 x + 3) * 11"
            toExpr = "44 [x ^ 2] - 99"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "(2 x - 3) * (2 x + 3) * 11"
                toExpr = "([(2 x) ^ 2] - [3 ^ 2]) * 11"
                explanation {
                    key = GeneralExplanation.ExpandProductOfSumAndDifference
                }
            }

            step {
                fromExpr = "([(2 x) ^ 2] - [3 ^ 2]) * 11"
                toExpr = "[(2 x) ^ 2] * 11 - [3 ^ 2] * 11"
                explanation {
                    key = GeneralExplanation.DistributeMultiplicationOverSum
                }
            }

            step {
                fromExpr = "[(2 x) ^ 2] * 11 - [3 ^ 2] * 11"
                toExpr = "44 [x ^ 2] - 99"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }
        }
    }

    @Test
    fun testMultiplyBinomials1() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "(2x + 3)*(3x - 2)"

        check {
            fromExpr = "(2 x + 3) * (3 x - 2)"
            toExpr = "6 [x ^ 2] + 5 x - 6"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "(2 x + 3) * (3 x - 2)"
                toExpr = "2 x * 3 x + 2 x * (-2) + 3 * 3 x + 3 * (-2)"
                explanation {
                    key = GeneralExplanation.ApplyFoilMethod
                }
            }

            step {
                fromExpr = "2 x * 3 x + 2 x * (-2) + 3 * 3 x + 3 * (-2)"
                toExpr = "6 [x ^ 2] + 5 x - 6"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }
        }
    }

    @Test
    fun testTrinomialSquared() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "[( 2[x ^ 2] + x - 3 ) ^ 2]"

        check {
            fromExpr = "[(2 [x ^ 2] + x - 3) ^ 2]"
            toExpr = "4 [x ^ 4] + 4 [x ^ 3] - 11 [x ^ 2] - 6 x + 9"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "[(2 [x ^ 2] + x - 3) ^ 2]"
                toExpr = "[(2 [x ^ 2]) ^ 2] + [x ^ 2] + [(-3) ^ 2] + 2 * 2 [x ^ 2] * x + 2 x * (-3) " +
                    "+ 2 * (-3) * 2 [x ^ 2]"
                explanation {
                    key = GeneralExplanation.ExpandTrinomialSquaredUsingIdentity
                }
            }

            step {
                fromExpr = "[(2 [x ^ 2]) ^ 2] + [x ^ 2] + [(-3) ^ 2] + 2 * 2 [x ^ 2] * x " +
                    "+ 2 x * (-3) + 2 * (-3) * 2 [x ^ 2]"
                toExpr = "4 [x ^ 4] + 4 [x ^ 3] - 11 [x ^ 2] - 6 x + 9"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }
        }
    }

    @Test
    fun testProductOfTrinomialAndBinomial() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "([x^2] + 5x - 2) * (3x - 5)"

        check {
            fromExpr = "([x ^ 2] + 5 x - 2) * (3 x - 5)"
            toExpr = "3 [x ^ 3] + 10 [x ^ 2] - 31 x + 10"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "([x ^ 2] + 5 x - 2) * (3 x - 5)"
                toExpr = "[x ^ 2] * 3 x + [x ^ 2] * (-5) + 5 x * 3 x + 5 x * (-5) + (-2) * 3 x + (-2) * (-5)"
                explanation {
                    key = GeneralExplanation.ExpandDoubleBrackets
                }
            }

            step {
                fromExpr = "[x ^ 2] * 3 x + [x ^ 2] * (-5) + 5 x * 3 x + 5 x * (-5) + (-2) * 3 x + (-2) * (-5)"
                toExpr = "3 [x ^ 3] + 10 [x ^ 2] - 31 x + 10"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }
        }
    }
}
