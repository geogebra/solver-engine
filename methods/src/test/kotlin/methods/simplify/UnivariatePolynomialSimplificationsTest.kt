/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

package methods.simplify

import engine.methods.testMethod
import methods.collecting.CollectingExplanation
import methods.expand.ExpandExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.fractionroots.FractionRootsExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

@Suppress("LargeClass")
class SimplifyUnivariatePolynomialsTest {
    @Test
    fun testCollectSimpleLikeTerms() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "2 x + 3 x + [x ^ 2] - 5 [x ^ 2]"

            check {
                fromExpr = "2 x + 3 x + [x ^ 2] - 5 [x ^ 2]"
                toExpr = "-4 [x ^ 2] + 5 x"
                explanation {
                    key = SimplifyExplanation.SimplifyPolynomialExpression
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
    fun testMonomialWithZeroCoefficientEliminated() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "3 [a ^ 2] + 2 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"

            check {
                fromExpr = "3 [a ^ 2] + 2 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"
                toExpr = "7 a"
                explanation {
                    key = SimplifyExplanation.SimplifyPolynomialExpression
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
    fun testRationalCoefficients() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
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
    fun testSurdCoefficients() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "2 t sqrt[3] + [t sqrt[3] / 3]"

            check {
                fromExpr = "2 t sqrt[3] + [t sqrt[3] / 3]"
                toExpr = "[7 sqrt[3] / 3] t"
                explanation {
                    key = SimplifyExplanation.SimplifyPolynomialExpression
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
    fun testMultiplyUnitaryMonomials() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
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
    fun testMultiplyMonomials() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
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
    fun testMultiplyMonomialsWithRationalCoefficients() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
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
    fun testNegatedProductContainingMonomialsNoNegativeFactors() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "1 - 2 x * 3 [x ^ 2]"

            check {
                fromExpr = "1 - 2 x * 3 [x ^ 2]"
                toExpr = "-6 [x ^ 3] + 1"
                explanation {
                    key = SimplifyExplanation.SimplifyPolynomialExpression
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
    fun testNegatedProductContainingMonomialsWithNegativeFactors() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "1 - 2 x * [(-x) ^ 3]"

            check {
                fromExpr = "1 - 2 x * [(-x) ^ 3]"
                toExpr = "2 [x ^ 4] + 1"
                explanation {
                    key = SimplifyExplanation.SimplifyPolynomialExpression
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
    fun testPowerOfUnitaryMonomial() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "[([x ^ 2]) ^ 3]"

            check {
                toExpr = "[x ^ 6]"
            }
        }

    @Test
    fun testCombiningSimplifyingAndNormalizing() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "sqrt[3] + [x ^ 3] + 2 x * 5 x + [(2 x) ^ 3] + 1"

            check {
                fromExpr = "sqrt[3] + [x ^ 3] + 2 x * 5 x + [(2 x) ^ 3] + 1"
                toExpr = "9 [x ^ 3] + 10 [x ^ 2] + sqrt[3] + 1"
                explanation {
                    key = SimplifyExplanation.SimplifyPolynomialExpression
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
    fun testMonomialWithSumCoefficient() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "2 t + [t / sqrt[3]]"

            check {
                fromExpr = "2 t + [t / sqrt[3]]"
                toExpr = "(2 + [sqrt[3] / 3]) t"
                explanation {
                    key = SimplifyExplanation.SimplifyPolynomialExpression
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
    fun testDistributingNegativeIntoPositives() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
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
    fun testDistributingNegativeIntoNegatives() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
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
    fun `Cancel negatives on different factors of product`() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
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
    fun `test simplification left to right`() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "2 x * x + [(2 x) ^ 2] - 3 x * 2 x + 3 * 10 - [([x ^ 2]) ^ 3] + x * x"

            check {
                fromExpr = "2 x * x + [(2 x) ^ 2] - 3 x * 2 x + 3 * 10 - [([x ^ 2]) ^ 3] + x * x"
                toExpr = "-[x ^ 6] + [x ^ 2] + 30"
                explanation {
                    key = SimplifyExplanation.SimplifyPolynomialExpression
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
                        key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
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
    fun `test simplify abs(abs(x))`() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
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
    fun `test simplify abs(x^2)`() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
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
    fun `test simplify abs(-x^2)`() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
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

class PolynomialFractionsTest {
    @Test
    fun `test adding non constant fraction to constant fraction`() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "[x / 2] + [2 / 3]"

            check {
                fromExpr = "[x / 2] + [2 / 3]"
                toExpr = "[3 x + 4 / 6]"
                explanation {
                    key = FractionArithmeticExplanation.AddFractions
                }

                step {
                    fromExpr = "[x / 2] + [2 / 3]"
                    toExpr = "[x * 3 / 2 * 3] + [2 * 2 / 3 * 2]"
                    explanation {
                        key = FractionArithmeticExplanation.BringToCommonDenominator
                    }
                }

                step {
                    fromExpr = "[x * 3 / 2 * 3] + [2 * 2 / 3 * 2]"
                    toExpr = "[x * 3 / 6] + [4 / 6]"
                    explanation {
                        key = FractionArithmeticExplanation.EvaluateProductsInNumeratorAndDenominator
                    }
                }

                step {
                    fromExpr = "[x * 3 / 6] + [4 / 6]"
                    toExpr = "[x * 3 + 4 / 6]"
                    explanation {
                        key = FractionArithmeticExplanation.AddLikeFractions
                    }
                }

                step {
                    fromExpr = "[x * 3 + 4 / 6]"
                    toExpr = "[3 x + 4 / 6]"
                    explanation {
                        key = PolynomialsExplanation.SimplifyMonomial
                    }
                }
            }
        }

    @Test
    fun `test adding two variable fractions`() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "[x + 1 / 2] + [2 x + 3 / 3]"

            check {
                fromExpr = "[x + 1 / 2] + [2 x + 3 / 3]"
                toExpr = "[7 x + 9 / 6]"
                explanation {
                    key = FractionArithmeticExplanation.AddFractions
                }

                step {
                    fromExpr = "[x + 1 / 2] + [2 x + 3 / 3]"
                    toExpr = "[(x + 1) * 3 / 2 * 3] + [(2 x + 3) * 2 / 3 * 2]"
                    explanation {
                        key = FractionArithmeticExplanation.BringToCommonDenominator
                    }
                }

                step {
                    fromExpr = "[(x + 1) * 3 / 2 * 3] + [(2 x + 3) * 2 / 3 * 2]"
                    toExpr = "[(x + 1) * 3 / 6] + [(2 x + 3) * 2 / 6]"
                    explanation {
                        key = FractionArithmeticExplanation.EvaluateProductsInNumeratorAndDenominator
                    }
                }

                step {
                    fromExpr = "[(x + 1) * 3 / 6] + [(2 x + 3) * 2 / 6]"
                    toExpr = "[(x + 1) * 3 + (2 x + 3) * 2 / 6]"
                    explanation {
                        key = FractionArithmeticExplanation.AddLikeFractions
                    }
                }

                step {
                    fromExpr = "[(x + 1) * 3 + (2 x + 3) * 2 / 6]"
                    toExpr = "[7 x + 9 / 6]"
                    explanation {
                        key = FractionArithmeticExplanation.SimplifyNumerator
                    }
                }
            }
        }

    @Test
    fun `test adding monomial term to polynomial fraction`() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "[[x ^ 2] + x / 2] + x"

            check {
                fromExpr = "[[x ^ 2] + x / 2] + x"
                toExpr = "[[x ^ 2] + 3 x / 2]"
                explanation {
                    key = FractionArithmeticExplanation.AddTermAndFraction
                }

                step {
                    fromExpr = "[[x ^ 2] + x / 2] + x"
                    toExpr = "[[x ^ 2] + x / 2] + [x * 2 / 2]"
                    explanation {
                        key = FractionArithmeticExplanation.BringToCommonDenominatorWithNonFractionalTerm
                    }
                }

                step {
                    fromExpr = "[[x ^ 2] + x / 2] + [x * 2 / 2]"
                    toExpr = "[[x ^ 2] + x + x * 2 / 2]"
                    explanation {
                        key = FractionArithmeticExplanation.AddLikeFractions
                    }
                }

                step {
                    fromExpr = "[[x ^ 2] + x + x * 2 / 2]"
                    toExpr = "[[x ^ 2] + 3 x / 2]"
                    explanation {
                        key = FractionArithmeticExplanation.SimplifyNumerator
                    }
                }
            }
        }
}

class SimplifyUnivariatePolynomialsWithMultipleBrackets {
    @Test
    fun `test order of simplification of brackets`() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "3 + (x + 2 + (2x - 1 + (-x + 5))) + 4"

            check {
                fromExpr = "3 + (x + 2 + (2 x - 1 + (-x + 5))) + 4"
                toExpr = "2 x + 13"
                explanation {
                    key = SimplifyExplanation.SimplifyPolynomialExpression
                }

                step {
                    fromExpr = "3 + (x + 2 + (2 x - 1 + (-x + 5))) + 4"
                    toExpr = "3 + (x + 2 + (x + 4)) + 4"
                    explanation {
                        key = SimplifyExplanation.SimplifyExpressionInBrackets
                    }

                    step {
                        fromExpr = "(2 x - 1 + (-x + 5))"
                        toExpr = "(2 x - 1 - x + 5)"
                        explanation {
                            key = GeneralExplanation.RemoveBracketSumInSum
                        }
                    }

                    step {
                        fromExpr = "(2 x - 1 - x + 5)"
                        toExpr = "(2 x + 4 - x)"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                        }
                    }

                    step {
                        fromExpr = "(2 x + 4 - x)"
                        toExpr = "(x + 4)"
                        explanation {
                            key = CollectingExplanation.CollectLikeTermsAndSimplify
                        }
                    }
                }

                step {
                    fromExpr = "3 + (x + 2 + (x + 4)) + 4"
                    toExpr = "3 + (2 x + 6) + 4"
                    explanation {
                        key = SimplifyExplanation.SimplifyExpressionInBrackets
                    }

                    step {
                        fromExpr = "(x + 2 + (x + 4))"
                        toExpr = "(x + 2 + x + 4)"
                        explanation {
                            key = GeneralExplanation.RemoveBracketSumInSum
                        }
                    }

                    step {
                        fromExpr = "(x + 2 + x + 4)"
                        toExpr = "(x + 6 + x)"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                        }
                    }

                    step {
                        fromExpr = "(x + 6 + x)"
                        toExpr = "(2 x + 6)"
                        explanation {
                            key = CollectingExplanation.CollectLikeTermsAndSimplify
                        }
                    }
                }

                step {
                    fromExpr = "3 + (2 x + 6) + 4"
                    toExpr = "3 + 2 x + 6 + 4"
                    explanation {
                        key = GeneralExplanation.RemoveBracketSumInSum
                    }
                }

                step {
                    fromExpr = "3 + 2 x + 6 + 4"
                    toExpr = "13 + 2 x"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                    }
                }

                step {
                    fromExpr = "13 + 2 x"
                    toExpr = "2 x + 13"
                    explanation {
                        key = PolynomialsExplanation.NormalizePolynomial
                    }
                }
            }
        }
}
