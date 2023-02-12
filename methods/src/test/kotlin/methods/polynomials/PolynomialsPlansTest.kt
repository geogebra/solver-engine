package methods.polynomials

import engine.context.Context
import engine.context.Curriculum
import engine.methods.SolverEngineExplanation
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
                        key = PolynomialsExplanation.SimplifyCoefficient
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
                        key = PolynomialsExplanation.SimplifyCoefficient
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
                        key = PolynomialsExplanation.SimplifyCoefficient
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
                    key = PolynomialsExplanation.SimplifyCoefficient
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
                    key = PolynomialsExplanation.SimplifyCoefficient
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
                    key = PolynomialsExplanation.SimplifyCoefficient
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
                key = GeneralExplanation.DistributeNegativeOverBracket
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
                toExpr = "a + 3"
                explanation {
                    key = GeneralExplanation.DistributeNegativeOverBracket
                }
            }
        }
    }
}

class ExpandAndSimplifySquareOfBinomial {
    @Test
    fun testExpandSquareOfBinomialEU() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "[(2x - 3) ^ 2]"

        check {
            fromExpr = "[(2 x - 3) ^ 2]"
            toExpr = "4 [x ^ 2] - 12 x + 9"
            explanation {
                key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
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
        context = Context(curriculum = Curriculum.US)

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
                toExpr = "4 [x ^ 2] - 12 x + 9"
                explanation {
                    key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
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
    }

    @Test
    fun `test inner square first outer square later`() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "[( [(x + 1)^2] + 2 ) ^2]"

        check {
            fromExpr = "[([(x + 1) ^ 2] + 2) ^ 2]"
            toExpr = "[x ^ 4] + 4 [x ^ 3] + 10 [x ^ 2] + 12 x + 9"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "[([(x + 1) ^ 2] + 2) ^ 2]"
                toExpr = "[(([x ^ 2] + 2 x + 1) + 2) ^ 2]"
                explanation {
                    key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
                }

                step {
                    fromExpr = "[(x + 1) ^ 2]"
                    toExpr = "[x ^ 2] + 2 x * 1 + [1 ^ 2]"
                    explanation {
                        key = GeneralExplanation.ExpandBinomialSquaredUsingIdentity
                    }
                }

                step {
                    fromExpr = "[x ^ 2] + 2 x * 1 + [1 ^ 2]"
                    toExpr = "[x ^ 2] + 2 x + 1"
                    explanation {
                        key = PolynomialsExplanation.SimplifyAlgebraicExpression
                    }
                }
            }

            step {
                fromExpr = "[(([x ^ 2] + 2 x + 1) + 2) ^ 2]"
                toExpr = "[([x ^ 2] + 2 x + 1 + 2) ^ 2]"
                explanation {
                    key = GeneralExplanation.RemoveBracketSumInSum
                }
            }

            step {
                fromExpr = "[([x ^ 2] + 2 x + 1 + 2) ^ 2]"
                toExpr = "[([x ^ 2] + 2 x + 3) ^ 2]"
                explanation {
                    key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                }
            }

            step {
                fromExpr = "[([x ^ 2] + 2 x + 3) ^ 2]"
                toExpr = "[x ^ 4] + 4 [x ^ 3] + 10 [x ^ 2] + 12 x + 9"
                explanation {
                    key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
                }

                step {
                    fromExpr = "[([x ^ 2] + 2 x + 3) ^ 2]"
                    toExpr = "[([x ^ 2]) ^ 2] + [(2 x) ^ 2] + [3 ^ 2] + 2 [x ^ 2] * 2 x + 2 * 2 x * 3 + 2 * 3 [x ^ 2]"
                    explanation {
                        key = GeneralExplanation.ExpandTrinomialSquaredUsingIdentity
                    }
                }

                step {
                    fromExpr = "[([x ^ 2]) ^ 2] + [(2 x) ^ 2] + [3 ^ 2] + 2 [x ^ 2] * 2 x + 2 * 2 x * 3 + 2 * 3 [x ^ 2]"
                    toExpr = "[x ^ 4] + 4 [x ^ 3] + 10 [x ^ 2] + 12 x + 9"
                    explanation {
                        key = PolynomialsExplanation.SimplifyAlgebraicExpression
                    }
                }
            }
        }
    }
}

class ExpandAndSimplifyCubeOfBinomial {
    @Test
    fun testExpandCubeOfBinomialEU() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "[(2x - 3) ^ 3]"

        check {
            fromExpr = "[(2 x - 3) ^ 3]"
            toExpr = "8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27"
            explanation {
                key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
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
        context = Context(curriculum = Curriculum.US)

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
                toExpr = "(4 [x ^ 2] - 12 x + 9) (2 x - 3)"
                explanation {
                    key = SolverEngineExplanation.SimplifyPartialExpression
                }

                task {
                    startExpr = "(2 x - 3) (2 x - 3)"
                    explanation {
                        key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
                    }

                    step {
                        toExpr = "2 x * 2 x + 2 x * (-3) + (-3) * 2 x + (-3) * (-3)"
                    }

                    step {
                        toExpr = "4 [x ^ 2] - 12 x + 9"
                    }
                }

                task {
                    startExpr = "(4 [x ^ 2] - 12 x + 9) (2 x - 3)"
                }
            }

            step {
                fromExpr = "(4 [x ^ 2] - 12 x + 9) (2 x - 3)"
                toExpr = "8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27"
                explanation {
                    key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
                }

                step {
                    fromExpr = "(4 [x ^ 2] - 12 x + 9) (2 x - 3)"
                    toExpr = "4 [x ^ 2] * 2 x + 4 [x ^ 2] * (-3) + (-12 x) * 2 x + (-12 x) * (-3) + 9 * 2 x + 9 * (-3)"
                    explanation {
                        key = GeneralExplanation.ExpandDoubleBrackets
                    }
                }

                step {
                    fromExpr = "4 [x ^ 2] * 2 x + 4 [x ^ 2] * (-3) + (-12 x) * 2 x + " +
                        "(-12 x) * (-3) + 9 * 2 x + 9 * (-3)"
                    toExpr = "8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27"
                    explanation {
                        key = PolynomialsExplanation.SimplifyAlgebraicExpression
                    }
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
                toExpr = "5 (8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27)"
                explanation {
                    key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
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

            step {
                fromExpr = "5 (8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27)"
                toExpr = "40 [x ^ 3] - 180 [x ^ 2] + 270 x - 135"
                explanation {
                    key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
                }

                step {
                    fromExpr = "5 (8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27)"
                    toExpr = "5 * 8 [x ^ 3] + 5 * (-36 [x ^ 2]) + 5 * 54 x + 5 * (-27)"
                    explanation {
                        key = GeneralExplanation.DistributeMultiplicationOverSum
                    }
                }

                step {
                    fromExpr = "5 * 8 [x ^ 3] + 5 * (-36 [x ^ 2]) + 5 * 54 x + 5 * (-27)"
                    toExpr = "40 [x ^ 3] - 180 [x ^ 2] + 270 x - 135"
                    explanation {
                        key = PolynomialsExplanation.SimplifyAlgebraicExpression
                    }
                }
            }
        }
    }
}

class ExpandAndSimplifySquareOfTrinomial {
    @Test
    fun testExpandSquareOfTrinomialEU() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "[(2x + 1 + sqrt[3]) ^ 2]"

        check {
            fromExpr = "[(2 x + 1 + sqrt[3]) ^ 2]"
            // notice here: (4 + 4 sqrt[3]) x, isn't expanded intentionally
            toExpr = "4 [x ^ 2] + (4 + 4 sqrt[3]) x + 4 + 2 sqrt[3]"
            explanation {
                key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
            }

            step {
                fromExpr = "[(2 x + 1 + sqrt[3]) ^ 2]"
                toExpr = "[(2 x) ^ 2] + [1 ^ 2] + [(sqrt[3]) ^ 2] + 2 * 2 x * 1 + 2 * 1 sqrt[3] + 2 sqrt[3] * 2 x"
                explanation {
                    key = GeneralExplanation.ExpandTrinomialSquaredUsingIdentity
                }
            }

            step {
                fromExpr = "[(2 x) ^ 2] + [1 ^ 2] + [(sqrt[3]) ^ 2] + 2 * 2 x * 1 + 2 * 1 sqrt[3] + 2 sqrt[3] * 2 x"
                toExpr = "4 [x ^ 2] + (4 + 4 sqrt[3]) x + 4 + 2 sqrt[3]"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }
        }
    }

    @Test
    fun testExpandSquareOfTrinomialUS() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        context = Context(curriculum = Curriculum.US)
        inputExpr = "[(2x + 1 + sqrt[3]) ^ 2]"

        check {
            fromExpr = "[(2 x + 1 + sqrt[3]) ^ 2]"
            toExpr = "4 [x ^ 2] + (4 + 4 sqrt[3]) x + 4 + 2 sqrt[3]"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "[(2 x + 1 + sqrt[3]) ^ 2]"
                toExpr = "(2 x + 1 + sqrt[3]) (2 x + 1 + sqrt[3])"
                explanation {
                    key = GeneralExplanation.RewritePowerAsProduct
                }
            }

            step {
                fromExpr = "(2 x + 1 + sqrt[3]) (2 x + 1 + sqrt[3])"
                toExpr = "4 [x ^ 2] + (4 + 4 sqrt[3]) x + 4 + 2 sqrt[3]"
                explanation {
                    key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
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
                key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
            }

            step {
                fromExpr = "[(2 [x ^ 2] + x - 3) ^ 2]"
                toExpr = "[(2 [x ^ 2]) ^ 2] + [x ^ 2] + [(-3) ^ 2] + 2 * 2 [x ^ 2] * x + " +
                    "2 x * (-3) + 2 * (-3) * 2 [x ^ 2]"
                explanation {
                    key = GeneralExplanation.ExpandTrinomialSquaredUsingIdentity
                }
            }

            step {
                fromExpr = "[(2 [x ^ 2]) ^ 2] + [x ^ 2] + [(-3) ^ 2] + 2 * 2 [x ^ 2] * x + " +
                    "2 x * (-3) + 2 * (-3) * 2 [x ^ 2]"
                toExpr = "4 [x ^ 4] + 4 [x ^ 3] - 11 [x ^ 2] - 6 x + 9"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }
        }
    }
}

class ExpandAndSimplifyProductOfBrackets {
    @Test
    fun testIdentifyProductOfSumAndDifference1() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "(2x - 3) (2x + 3)"

        check {
            fromExpr = "(2 x - 3) (2 x + 3)"
            toExpr = "4 [x ^ 2] - 9"
            explanation {
                key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
            }

            step {
                fromExpr = "(2 x - 3) (2 x + 3)"
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
                toExpr = "11 (2 x - 3) (2 x + 3)"
                explanation {
                    key = GeneralExplanation.NormaliseSimplifiedProduct
                }
            }

            step {
                fromExpr = "11 (2 x - 3) (2 x + 3)"
                toExpr = "11 (4 [x ^ 2] - 9)"
                explanation {
                    key = SolverEngineExplanation.SimplifyPartialExpression
                }

                task {
                    explanation {
                        key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
                    }
                    startExpr = "(2 x - 3) (2 x + 3)"

                    step {
                        explanation {
                            key = GeneralExplanation.ExpandProductOfSumAndDifference
                        }
                        toExpr = "[(2 x) ^ 2] - [3 ^ 2]"
                    }

                    step {
                        explanation {
                            key = PolynomialsExplanation.SimplifyAlgebraicExpression
                        }
                        toExpr = "4 [x ^ 2] - 9"
                    }
                }

                task {
                    explanation {
                        key = SolverEngineExplanation.SubstitutePartialExpression
                    }
                    startExpr = "11 (4 [x ^ 2] - 9)"
                }
            }

            step {
                fromExpr = "11 (4 [x ^ 2] - 9)"
                toExpr = "44 [x ^ 2] - 99"
                explanation {
                    key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
                }

                step {
                    fromExpr = "11 (4 [x ^ 2] - 9)"
                    toExpr = "11 * 4 [x ^ 2] + 11 * (-9)"
                    explanation {
                        key = GeneralExplanation.DistributeMultiplicationOverSum
                    }
                }

                step {
                    fromExpr = "11 * 4 [x ^ 2] + 11 * (-9)"
                    toExpr = "44 [x ^ 2] - 99"
                    explanation {
                        key = PolynomialsExplanation.SimplifyAlgebraicExpression
                    }
                }
            }
        }
    }

    @Test
    fun testMultiplyBinomials1() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "(2x + 3) * (3x - 2)"

        check {
            fromExpr = "(2 x + 3) * (3 x - 2)"
            toExpr = "6 [x ^ 2] + 5 x - 6"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "(2 x + 3) * (3 x - 2)"
                toExpr = "(2 x + 3) (3 x - 2)"
                explanation {
                    key = GeneralExplanation.NormaliseSimplifiedProduct
                }
            }

            step {
                fromExpr = "(2 x + 3) (3 x - 2)"
                toExpr = "6 [x ^ 2] + 5 x - 6"
                explanation {
                    key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
                }

                step {
                    fromExpr = "(2 x + 3) (3 x - 2)"
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
    }

    @Test
    fun testProductOfTrinomialAndBinomial() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "([x^2] + 5x - 2) (3x - 5)"

        check {
            fromExpr = "([x ^ 2] + 5 x - 2) (3 x - 5)"
            toExpr = "3 [x ^ 3] + 10 [x ^ 2] - 31 x + 10"
            explanation {
                key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
            }

            step {
                fromExpr = "([x ^ 2] + 5 x - 2) (3 x - 5)"
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

class ExpandAndSimplifyUsingDistributiveProperty {
    @Test
    fun testDistributeConstantFromRhs() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "(x + 1)*5"

        check {
            fromExpr = "(x + 1) * 5"
            toExpr = "5 x + 5"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "(x + 1) * 5"
                toExpr = "5 (x + 1)"
                explanation {
                    key = GeneralExplanation.NormaliseSimplifiedProduct
                }
            }

            step {
                fromExpr = "5 (x + 1)"
                toExpr = "5 x + 5"
                explanation {
                    key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
                }

                step {
                    fromExpr = "5 (x + 1)"
                    toExpr = "5 * x + 5 * 1"
                    explanation {
                        key = GeneralExplanation.DistributeMultiplicationOverSum
                    }
                }

                step {
                    fromExpr = "5 * x + 5 * 1"
                    toExpr = "5 x + 5"
                    explanation {
                        key = PolynomialsExplanation.SimplifyAlgebraicExpression
                    }
                }
            }
        }
    }

    @Test
    fun testDistributeMonomialFromRhs() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "(x + 1)*5[x^2]"

        check {
            fromExpr = "(x + 1) * 5 [x ^ 2]"
            toExpr = "5 [x ^ 3] + 5 [x ^ 2]"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "(x + 1) * 5 [x ^ 2]"
                toExpr = "5 [x ^ 2] (x + 1)"
                explanation {
                    key = GeneralExplanation.NormaliseSimplifiedProduct
                }
            }

            step {
                fromExpr = "5 [x ^ 2] (x + 1)"
                toExpr = "5 [x ^ 3] + 5 [x ^ 2]"
                explanation {
                    key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
                }

                step {
                    fromExpr = "5 [x ^ 2] (x + 1)"
                    toExpr = "5 [x ^ 2] * x + 5 [x ^ 2] * 1"
                    explanation {
                        key = GeneralExplanation.DistributeMultiplicationOverSum
                    }
                }

                step {
                    fromExpr = "5 [x ^ 2] * x + 5 [x ^ 2] * 1"
                    toExpr = "5 [x ^ 3] + 5 [x ^ 2]"
                    explanation {
                        key = PolynomialsExplanation.SimplifyAlgebraicExpression
                    }
                }
            }
        }
    }

    @Test
    fun testDistributeMonomialFromLhs() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "3[x^2] (2x - 7)"

        check {
            fromExpr = "3 [x ^ 2] (2 x - 7)"
            toExpr = "6 [x ^ 3] - 21 [x ^ 2]"
            explanation {
                key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
            }

            step {
                fromExpr = "3 [x ^ 2] (2 x - 7)"
                toExpr = "3 [x ^ 2] * 2 x + 3 [x ^ 2] * (-7)"
                explanation {
                    key = GeneralExplanation.DistributeMultiplicationOverSum
                }
            }

            step {
                fromExpr = "3 [x ^ 2] * 2 x + 3 [x ^ 2] * (-7)"
                toExpr = "6 [x ^ 3] - 21 [x ^ 2]"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }
        }
    }

    @Test
    fun testDistributeMonomiaFromLhsAndConstantFromRhs() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "3 [x^2] * (2x - 7) sqrt[2]"

        check {
            fromExpr = "3 [x ^ 2] * (2 x - 7) sqrt[2]"
            toExpr = "6 sqrt[2] * [x ^ 3] - 21 sqrt[2] * [x ^ 2]"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "3 [x ^ 2] * (2 x - 7) sqrt[2]"
                toExpr = "3 sqrt[2] * [x ^ 2] (2 x - 7)"
                explanation {
                    key = GeneralExplanation.NormaliseSimplifiedProduct
                }
            }

            step {
                fromExpr = "3 sqrt[2] * [x ^ 2] (2 x - 7)"
                toExpr = "3 sqrt[2] (2 [x ^ 3] - 7 [x ^ 2])"
                explanation {
                    key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
                }

                step {
                    fromExpr = "[x ^ 2] (2 x - 7)"
                    toExpr = "[x ^ 2] * 2 x + [x ^ 2] * (-7)"
                    explanation {
                        key = GeneralExplanation.DistributeMultiplicationOverSum
                    }
                }

                step {
                    fromExpr = "[x ^ 2] * 2 x + [x ^ 2] * (-7)"
                    toExpr = "2 [x ^ 3] - 7 [x ^ 2]"
                    explanation {
                        key = PolynomialsExplanation.SimplifyAlgebraicExpression
                    }
                }
            }

            step {
                fromExpr = "3 sqrt[2] (2 [x ^ 3] - 7 [x ^ 2])"
                toExpr = "6 sqrt[2] * [x ^ 3] - 21 sqrt[2] * [x ^ 2]"
                explanation {
                    key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
                }

                step {
                    fromExpr = "3 sqrt[2] (2 [x ^ 3] - 7 [x ^ 2])"
                    toExpr = "3 sqrt[2] * 2 [x ^ 3] + 3 sqrt[2] * (-7 [x ^ 2])"
                    explanation {
                        key = GeneralExplanation.DistributeMultiplicationOverSum
                    }
                }

                step {
                    fromExpr = "3 sqrt[2] * 2 [x ^ 3] + 3 sqrt[2] * (-7 [x ^ 2])"
                    toExpr = "6 sqrt[2] * [x ^ 3] - 21 sqrt[2] * [x ^ 2]"
                    explanation {
                        key = PolynomialsExplanation.SimplifyAlgebraicExpression
                    }
                }
            }
        }
    }
}

class ExpandAndSimplifyMultipleBrackets {
    @Test
    fun testRepeatedExpandAndSimplify() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "3*(x+1)-2*(x+6)"

        check {
            fromExpr = "3 * (x + 1) - 2 * (x + 6)"
            toExpr = "x - 9"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            // Product normalizations
            step { }
            step { }

            step {
                fromExpr = "3(x + 1) - 2(x + 6)"
                toExpr = "(3 x + 3) - 2(x + 6)"
                explanation {
                    key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
                }

                step {
                    fromExpr = "3(x + 1)"
                    toExpr = "3 * x + 3 * 1"
                    explanation {
                        key = GeneralExplanation.DistributeMultiplicationOverSum
                    }
                }

                step {
                    fromExpr = "3 * x + 3 * 1"
                    toExpr = "3 x + 3"
                    explanation {
                        key = PolynomialsExplanation.SimplifyAlgebraicExpression
                    }
                }
            }

            // Remove bracket
            step {}

            step {
                fromExpr = "3 x + 3 - 2(x + 6)"
                toExpr = "3 x + 3 + (-2x - 12)"
                explanation {
                    key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
                }

                step {
                    fromExpr = "-2(x + 6)"
                    toExpr = "(-2) * x + (-2) * 6"
                    explanation {
                        key = GeneralExplanation.DistributeMultiplicationOverSum
                    }
                }

                step {
                    fromExpr = "(-2) * x + (-2) * 6"
                    toExpr = "-2x - 12"
                    explanation {
                        key = PolynomialsExplanation.SimplifyAlgebraicExpression
                    }
                }
            }

            step {
                fromExpr = "3 x + 3 + (-2 x - 12)"
                toExpr = "3 x + 3 - 2 x - 12"
                explanation {
                    key = GeneralExplanation.RemoveBracketSumInSum
                }
            }

            step {
                fromExpr = "3 x + 3 - 2 x - 12"
                toExpr = "x + 3 - 12"
                explanation {
                    key = PolynomialsExplanation.CollectLikeTermsAndSimplify
                }
            }

            step {
                fromExpr = "x + 3 - 12"
                toExpr = "x - 9"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                }
            }
        }
    }

    @Test
    fun `expand (ax + b)^2 (cx + d)`() = testMethod {
        method = PolynomialPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "[(2x + 3)^2] (x + 1)"

        check {
            fromExpr = "[(2 x + 3) ^ 2] (x + 1)"
            toExpr = "4 [x ^ 3] + 16 [x ^ 2] + 21 x + 9"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "[(2 x + 3) ^ 2] (x + 1)"
                toExpr = "(4 [x ^ 2] + 12 x + 9) (x + 1)"
                explanation {
                    key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
                }

                step {
                    fromExpr = "[(2 x + 3) ^ 2]"
                    toExpr = "[(2 x) ^ 2] + 2 * 2 x * 3 + [3 ^ 2]"
                    explanation {
                        key = GeneralExplanation.ExpandBinomialSquaredUsingIdentity
                    }
                }

                step {
                    fromExpr = "[(2 x) ^ 2] + 2 * 2 x * 3 + [3 ^ 2]"
                    toExpr = "4 [x ^ 2] + 12 x + 9"
                    explanation {
                        key = PolynomialsExplanation.SimplifyAlgebraicExpression
                    }
                }
            }

            step {
                fromExpr = "(4 [x ^ 2] + 12 x + 9) (x + 1)"
                toExpr = "4 [x ^ 3] + 16 [x ^ 2] + 21 x + 9"
                explanation {
                    key = PolynomialsExplanation.ApplyExpandRuleAndSimplify
                }

                step {
                    fromExpr = "(4 [x ^ 2] + 12 x + 9) (x + 1)"
                    toExpr = "4 [x ^ 2] * x + 4 [x ^ 2] * 1 + 12 x * x + 12 x * 1 + 9 * x + 9 * 1"
                    explanation {
                        key = GeneralExplanation.ExpandDoubleBrackets
                    }
                }

                step {
                    fromExpr = "4 [x ^ 2] * x + 4 [x ^ 2] * 1 + 12 x * x + 12 x * 1 + 9 * x + 9 * 1"
                    toExpr = "4 [x ^ 3] + 16 [x ^ 2] + 21 x + 9"
                    explanation {
                        key = PolynomialsExplanation.SimplifyAlgebraicExpression
                    }
                }
            }
        }
    }
}
