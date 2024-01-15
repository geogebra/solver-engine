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

package methods.factor

import engine.methods.testMethod
import methods.constantexpressions.ConstantExpressionsExplanation
import methods.expand.ExpandExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

@Suppress("LargeClass")
class FactorPlansTest {
    @Test
    fun `test extracting the common integer factor`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "16 [x ^ 5] - 32"

            check {
                fromExpr = "16 [x ^ 5] - 32"
                toExpr = "16 ([x ^ 5] - 2)"
                explanation {
                    key = FactorExplanation.FactorGreatestCommonIntegerFactor
                }
            }
        }

    @Test
    fun `test extracting the common monomial factor`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "15 [x ^ 5] - 33 [x ^ 2]"

            check {
                fromExpr = "15 [x ^ 5] - 33 [x ^ 2]"
                toExpr = "3 [x ^ 2] (5 [x ^ 3] - 11)"
                explanation {
                    key = FactorExplanation.FactorGreatestCommonFactor
                }

                step {
                    fromExpr = "15 [x ^ 5] - 33 [x ^ 2]"
                    toExpr = "3 (5 [x ^ 5] - 11 [x ^ 2])"
                    explanation {
                        key = FactorExplanation.FactorGreatestCommonIntegerFactor
                    }
                }

                step {
                    fromExpr = "3 (5 [x ^ 5] - 11 [x ^ 2])"
                    toExpr = "3 <. [x ^ 2] (5 [x ^ 3] - 11) .>"
                    explanation {
                        key = FactorExplanation.FactorCommonFactor
                    }
                }
            }
        }

    @Test
    fun `test extracting factor after rearranging its terms`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "3 * [(x + 1) ^ 3] + 6 * [(1 + x) ^ 2]"

            check {
                fromExpr = "3 * [(x + 1) ^ 3] + 6 * [(1 + x) ^ 2]"
                toExpr = "3 * [(x + 1) ^ 2] (x + 3)"
                explanation {
                    key = FactorExplanation.FactorPolynomial
                }

                step {
                    fromExpr = "3 * [(x + 1) ^ 3] + 6 * [(1 + x) ^ 2]"
                    toExpr = "3 * [(x + 1) ^ 2] ((x + 1) + 2)"
                    explanation {
                        key = FactorExplanation.FactorGreatestCommonFactor
                    }

                    step {
                        fromExpr = "3 * [(x + 1) ^ 3] + 6 * [(1 + x) ^ 2]"
                        toExpr = "3 ([(x + 1) ^ 3] + 2 * [(1 + x) ^ 2])"
                        explanation {
                            key = FactorExplanation.FactorGreatestCommonIntegerFactor
                        }
                    }

                    step {
                        fromExpr = "3 ([(x + 1) ^ 3] + 2 * [(1 + x) ^ 2])"
                        toExpr = "3 ([(x + 1) ^ 3] + 2 * [(x + 1) ^ 2])"
                        explanation {
                            key = FactorExplanation.RearrangeEquivalentSums
                        }
                    }

                    step {
                        fromExpr = "3 ([(x + 1) ^ 3] + 2 * [(x + 1) ^ 2])"
                        toExpr = "3 * <. [(x + 1) ^ 2] ((x + 1) + 2) .>"
                        explanation {
                            key = FactorExplanation.FactorCommonFactor
                        }
                    }
                }

                step {
                    fromExpr = "3 * [(x + 1) ^ 2] ((x + 1) + 2)"
                    toExpr = "3 * [(x + 1) ^ 2] (x + 1 + 2)"
                    explanation {
                        key = GeneralExplanation.RemoveAllBracketSumInSum
                    }
                }

                step {
                    fromExpr = "3 * [(x + 1) ^ 2] (x + 1 + 2)"
                    toExpr = "3 * [(x + 1) ^ 2] (x + 3)"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                    }
                }
            }
        }

    @Test
    fun `test factoring monic perfect square`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "[x ^ 2] + 14 x + 49"

            check {
                fromExpr = "[x ^ 2] + 14 x + 49"
                toExpr = "[(x + 7) ^ 2]"
                explanation {
                    key = FactorExplanation.FactorSquareOfBinomial
                }

                step {
                    fromExpr = "[x ^ 2] + 14 x + 49"
                    toExpr = "[x ^ 2] + 2 * 7 * x + [7 ^ 2]"
                    explanation {
                        key = FactorExplanation.RewriteSquareOfBinomial
                    }
                }

                step {
                    fromExpr = "[x ^ 2] + 2 * 7 * x + [7 ^ 2]"
                    toExpr = "[(x + 7) ^ 2]"
                    explanation {
                        key = FactorExplanation.ApplySquareOfBinomialFormula
                    }
                }
            }
        }

    @Test
    fun `test factoring non-monic perfect square`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "4 [x ^ 2] + 12 x + 9"

            check {
                fromExpr = "4 [x ^ 2] + 12 x + 9"
                toExpr = "[(2 x + 3) ^ 2]"
                explanation {
                    key = FactorExplanation.FactorSquareOfBinomial
                }

                step {
                    fromExpr = "4 [x ^ 2] + 12 x + 9"
                    toExpr = "[(2 x) ^ 2] + 2 * 3 * <. 2 x .> + [3 ^ 2]"
                    explanation {
                        key = FactorExplanation.RewriteSquareOfBinomial
                    }
                }

                step {
                    fromExpr = "[(2 x) ^ 2] + 2 * 3 * <. 2 x .> + [3 ^ 2]"
                    toExpr = "[(2 x + 3) ^ 2]"
                    explanation {
                        key = FactorExplanation.ApplySquareOfBinomialFormula
                    }
                }
            }
        }

    @Test
    fun `test factoring perfect square with negative mixed term`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "4 [x ^ 2] - 12 x + 9"

            check {
                fromExpr = "4 [x ^ 2] - 12 x + 9"
                toExpr = "[(2 x - 3) ^ 2]"
                explanation {
                    key = FactorExplanation.FactorSquareOfBinomial
                }

                step {
                    fromExpr = "4 [x ^ 2] - 12 x + 9"
                    toExpr = "[(2 x) ^ 2] + 2 * (-3) * <. 2 x .> + [(-3) ^ 2]"
                    explanation {
                        key = FactorExplanation.RewriteSquareOfBinomial
                    }
                }

                step {
                    fromExpr = "[(2 x) ^ 2] + 2 * (-3) * <. 2 x .> + [(-3) ^ 2]"
                    toExpr = "[(2 x - 3) ^ 2]"
                    explanation {
                        key = FactorExplanation.ApplySquareOfBinomialFormula
                    }
                }
            }
        }

    @Test
    fun `test factoring perfect cube`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "[x ^ 3] + 1 + 3 [x ^ 2] + 3 x"

            check {
                fromExpr = "[x ^ 3] + 1 + 3 [x ^ 2] + 3 x"
                toExpr = "[(x + 1) ^ 3]"
                explanation {
                    key = FactorExplanation.FactorCubeOfBinomial
                }

                step {
                    fromExpr = "[x ^ 3] + 1 + 3 [x ^ 2] + 3 x"
                    toExpr = "[x ^ 3] + 3 [x ^ 2] + 3 x + 1"
                    explanation {
                        key = PolynomialsExplanation.NormalizePolynomial
                    }
                }

                step {
                    fromExpr = "[x ^ 3] + 3 [x ^ 2] + 3 x + 1"
                    toExpr = "[x ^ 3] + 3 * [x ^ 2] * 1 + 3 * x * [1 ^ 2] + [1 ^ 3]"
                    explanation {
                        key = FactorExplanation.RewriteCubeOfBinomial
                    }
                }

                step {
                    fromExpr = "[x ^ 3] + 3 * [x ^ 2] * 1 + 3 * x * [1 ^ 2] + [1 ^ 3]"
                    toExpr = "[(x + 1) ^ 3]"
                    explanation {
                        key = FactorExplanation.ApplyCubeOfBinomialFormula
                    }
                }
            }
        }

    @Test
    fun `test factoring using the difference of squares formula`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "121 [x ^ 4] - 9"

            check {
                fromExpr = "121 [x ^ 4] - 9"
                toExpr = "(11 [x ^ 2] - 3) (11 [x ^ 2] + 3)"
                explanation {
                    key = FactorExplanation.FactorDifferenceOfSquares
                }

                step {
                    fromExpr = "121 [x ^ 4] - 9"
                    toExpr = "[(11 [x ^ 2]) ^ 2] - [3 ^ 2]"
                    explanation {
                        key = FactorExplanation.RewriteDifferenceOfSquares
                    }
                }

                step {
                    fromExpr = "[(11 [x ^ 2]) ^ 2] - [3 ^ 2]"
                    toExpr = "(11 [x ^ 2] - 3) (11 [x ^ 2] + 3)"
                    explanation {
                        key = FactorExplanation.ApplyDifferenceOfSquaresFormula
                    }
                }
            }
        }

    @Test
    fun `test factoring using the difference of cubes formula`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "64 - [x ^ 3]"

            check {
                fromExpr = "64 - [x ^ 3]"
                toExpr = "(4 - x) (16 + 4 x + [x ^ 2])"
                explanation {
                    key = FactorExplanation.FactorDifferenceOfCubes
                }

                step {
                    fromExpr = "64 - [x ^ 3]"
                    toExpr = "[4 ^ 3] - [x ^ 3]"
                    explanation {
                        key = FactorExplanation.RewriteDifferenceOfCubes
                    }
                }

                step {
                    fromExpr = "[4 ^ 3] - [x ^ 3]"
                    toExpr = "(4 - x) ([4 ^ 2] + 4 x + [x ^ 2])"
                    explanation {
                        key = FactorExplanation.ApplyDifferenceOfCubesFormula
                    }
                }

                step {
                    fromExpr = "(4 - x) ([4 ^ 2] + 4 x + [x ^ 2])"
                    toExpr = "(4 - x) (16 + 4 x + [x ^ 2])"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyPowerOfInteger
                    }
                }
            }
        }

    @Test
    fun `test factoring using the sum of cubes formula`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "125 + 8 [x ^ 6]"

            check {
                fromExpr = "125 + 8 [x ^ 6]"
                toExpr = "(5 + 2 [x ^ 2]) (25 - 10 [x ^ 2] + 4 [x ^ 4])"
                explanation {
                    key = FactorExplanation.FactorSumOfCubes
                }

                step {
                    fromExpr = "125 + 8 [x ^ 6]"
                    toExpr = "[5 ^ 3] + [(2 [x ^ 2]) ^ 3]"
                    explanation {
                        key = FactorExplanation.RewriteSumOfCubes
                    }
                }

                step {
                    fromExpr = "[5 ^ 3] + [(2 [x ^ 2]) ^ 3]"
                    toExpr = "(5 + 2 [x ^ 2]) ([5 ^ 2] - 5 * 2 [x ^ 2] + [(2 [x ^ 2]) ^ 2])"
                    explanation {
                        key = FactorExplanation.ApplySumOfCubesFormula
                    }
                }

                step {
                    fromExpr = "(5 + 2 [x ^ 2]) ([5 ^ 2] - 5 * 2 [x ^ 2] + [(2 [x ^ 2]) ^ 2])"
                    toExpr = "(5 + 2 [x ^ 2]) (25 - 5 * 2 [x ^ 2] + [(2 [x ^ 2]) ^ 2])"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyPowerOfInteger
                    }
                }

                step {
                    fromExpr = "(5 + 2 [x ^ 2]) (25 - 5 * 2 [x ^ 2] + [(2 [x ^ 2]) ^ 2])"
                    toExpr = "(5 + 2 [x ^ 2]) (25 - 10 [x ^ 2] + [(2 [x ^ 2]) ^ 2])"
                    explanation {
                        key = PolynomialsExplanation.SimplifyMonomial
                    }
                }

                step {
                    fromExpr = "(5 + 2 [x ^ 2]) (25 - 10 [x ^ 2] + [(2 [x ^ 2]) ^ 2])"
                    toExpr = "(5 + 2 [x ^ 2]) (25 - 10 [x ^ 2] + 4 [x ^ 4])"
                    explanation {
                        key = PolynomialsExplanation.DistributeProductToIntegerPowerAndSimplify
                    }
                }
            }
        }

    @Test
    fun `test factoring monic trinomial by guessing`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "[x ^ 2] - 5 x + 6"

            check {
                fromExpr = "[x ^ 2] - 5 x + 6"
                toExpr = "(x - 3) (x - 2)"
                explanation {
                    key = FactorExplanation.FactorTrinomialByGuessing
                }

                task {
                    taskId = "#1"
                    startExpr = "a + b = -5 AND a b = 6"
                    explanation {
                        key = FactorExplanation.SetUpAndSolveEquationSystemForMonicTrinomial
                    }

                    step {
                        fromExpr = "a + b = -5 AND a b = 6"
                        toExpr = "a = -3 AND b = -2"
                        explanation {
                            key = FactorExplanation.SolveSumProductDiophantineEquationSystemByGuessing
                        }
                    }
                }

                task {
                    taskId = "#2"
                    startExpr = "(x - 3) (x - 2)"
                    explanation {
                        key = FactorExplanation.FactorTrinomialUsingTheSolutionsOfTheSumAndProductSystem
                    }
                }
            }
        }

    @Test
    fun `test factoring non-monic trinomial by grouping`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "6 [x ^ 2] + 11 x + 4"

            check {
                fromExpr = "6 [x ^ 2] + 11 x + 4"
                toExpr = "(2 x + 1) (3 x + 4)"
                explanation {
                    key = FactorExplanation.FactorNonMonicTrinomial
                }

                step {
                    fromExpr = "6 [x ^ 2] + 11 x + 4"
                    toExpr = "6 [x ^ 2] + 3 x + 8 x + 4"
                    explanation {
                        key = FactorExplanation.SplitNonMonicTrinomial
                    }

                    task {
                        taskId = "#1"
                        startExpr = "a + b = 11 AND a b = 6 * 4"
                        explanation {
                            key = FactorExplanation.SetUpAndSolveEquationSystemForNonMonicTrinomial
                        }

                        step {
                            fromExpr = "a + b = 11 AND a b = 6 * 4"
                            toExpr = "a + b = 11 AND a b = 24"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                            }
                        }

                        step {
                            fromExpr = "a + b = 11 AND a b = 24"
                            toExpr = "a = 3 AND b = 8"
                            explanation {
                                key = FactorExplanation.SolveSumProductDiophantineEquationSystemByGuessing
                            }
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "6 [x ^ 2] + 3 x + 8 x + 4"
                        explanation {
                            key = FactorExplanation.SplitTrinomialUsingTheSolutionsOfTheSumAndProductSystem
                        }
                    }
                }

                step {
                    fromExpr = "6 [x ^ 2] + 3 x + 8 x + 4"
                    toExpr = "(2 x + 1) (3 x + 4)"
                    explanation {
                        key = FactorExplanation.FactorByGrouping
                    }
                }
            }
        }

    @Test
    fun `test factoring by grouping into 2 + 2 terms`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "6 [x ^ 3] + 8 [x ^ 2] + 9 x + 12"

            check {
                fromExpr = "6 [x ^ 3] + 8 [x ^ 2] + 9 x + 12"
                toExpr = "(3 x + 4) (2 [x ^ 2] + 3)"
                explanation {
                    key = FactorExplanation.FactorByGrouping
                }

                step {
                    fromExpr = "6 [x ^ 3] + 8 [x ^ 2] + 9 x + 12"
                    toExpr = "(6 [x ^ 3] + 8 [x ^ 2]) + (9 x + 12)"
                    explanation {
                        key = FactorExplanation.GroupPolynomial
                    }
                }

                step {
                    fromExpr = "(6 [x ^ 3] + 8 [x ^ 2]) + (9 x + 12)"
                    toExpr = "2 [x ^ 2] (3 x + 4) + (9 x + 12)"
                    explanation {
                        key = FactorExplanation.FactorGreatestCommonFactor
                    }
                }

                step {
                    fromExpr = "2 [x ^ 2] (3 x + 4) + (9 x + 12)"
                    toExpr = "2 [x ^ 2] (3 x + 4) + 3 (3 x + 4)"
                    explanation {
                        key = FactorExplanation.FactorGreatestCommonFactor
                    }
                }

                step {
                    fromExpr = "2 [x ^ 2] (3 x + 4) + 3 (3 x + 4)"
                    toExpr = "(3 x + 4) (2 [x ^ 2] + 3)"
                    explanation {
                        key = FactorExplanation.FactorCommonFactor
                    }
                }
            }
        }

    @Test
    fun `test factoring by grouping into 1 + 3 terms`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "[x ^ 4] - [x ^ 2] + 2 x - 1"

            check {
                fromExpr = "[x ^ 4] - [x ^ 2] + 2 x - 1"
                toExpr = "([x ^ 2] - x + 1) ([x ^ 2] + x - 1)"
                explanation {
                    key = FactorExplanation.FactorPolynomial
                }

                step {
                    fromExpr = "[x ^ 4] - [x ^ 2] + 2 x - 1"
                    toExpr = "([x ^ 2] - (x - 1)) ([x ^ 2] + (x - 1))"
                    explanation {
                        key = FactorExplanation.FactorByGrouping
                    }

                    step {
                        fromExpr = "[x ^ 4] - [x ^ 2] + 2 x - 1"
                        toExpr = "[x ^ 4] + (-[x ^ 2] + 2 x - 1)"
                        explanation {
                            key = FactorExplanation.GroupPolynomial
                        }
                    }

                    step {
                        fromExpr = "[x ^ 4] + (-[x ^ 2] + 2 x - 1)"
                        toExpr = "[x ^ 4] - [(x - 1) ^ 2]"
                        explanation {
                            key = FactorExplanation.FactorSquareOfBinomial
                        }
                    }

                    step {
                        fromExpr = "[x ^ 4] - [(x - 1) ^ 2]"
                        toExpr = "([x ^ 2] - (x - 1)) ([x ^ 2] + (x - 1))"
                        explanation {
                            key = FactorExplanation.FactorDifferenceOfSquares
                        }
                    }
                }

                step {
                    fromExpr = "([x ^ 2] - (x - 1)) ([x ^ 2] + (x - 1))"
                    toExpr = "([x ^ 2] + <. -x + 1 .>) ([x ^ 2] + (x - 1))"
                    explanation {
                        key = ExpandExplanation.DistributeNegativeOverBracket
                    }
                }

                step {
                    fromExpr = "([x ^ 2] - x + 1) ([x ^ 2] + (x - 1))"
                    toExpr = "([x ^ 2] - x + 1) ([x ^ 2] + x - 1)"
                    explanation {
                        key = GeneralExplanation.RemoveAllBracketSumInSum
                    }
                }
            }
        }

    @Test
    fun `test factoring by first extracting the gcf then applying difference of squares`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "18 [x ^ 6] - 32 [x ^ 2]"

            check {
                fromExpr = "18 [x ^ 6] - 32 [x ^ 2]"
                toExpr = "2 [x ^ 2] (3 [x ^ 2] - 4) (3 [x ^ 2] + 4)"
                explanation {
                    key = FactorExplanation.FactorPolynomial
                }

                step {
                    fromExpr = "18 [x ^ 6] - 32 [x ^ 2]"
                    toExpr = "2 [x ^ 2] (9 [x ^ 4] - 16)"
                    explanation {
                        key = FactorExplanation.FactorGreatestCommonFactor
                    }

                    step {
                        fromExpr = "18 [x ^ 6] - 32 [x ^ 2]"
                        toExpr = "2 (9 [x ^ 6] - 16 [x ^ 2])"
                        explanation {
                            key = FactorExplanation.FactorGreatestCommonIntegerFactor
                        }
                    }

                    step {
                        fromExpr = "2 (9 [x ^ 6] - 16 [x ^ 2])"
                        toExpr = "2 <. [x ^ 2] (9 [x ^ 4] - 16) .>"
                        explanation {
                            key = FactorExplanation.FactorCommonFactor
                        }
                    }
                }

                step {
                    fromExpr = "2 [x ^ 2] (9 [x ^ 4] - 16)"
                    toExpr = "2 [x ^ 2] <. (3 [x ^ 2] - 4) (3 [x ^ 2] + 4) .>"
                    explanation {
                        key = FactorExplanation.FactorDifferenceOfSquares
                    }

                    step {
                        fromExpr = "(9 [x ^ 4] - 16)"
                        toExpr = "([(3 [x ^ 2]) ^ 2] - [4 ^ 2])"
                        explanation {
                            key = FactorExplanation.RewriteDifferenceOfSquares
                        }
                    }

                    step {
                        fromExpr = "([(3 [x ^ 2]) ^ 2] - [4 ^ 2])"
                        toExpr = "<. (3 [x ^ 2] - 4) (3 [x ^ 2] + 4) .>"
                        explanation {
                            key = FactorExplanation.ApplyDifferenceOfSquaresFormula
                        }
                    }
                }
            }
        }

    @Test
    fun `test factoring by first extracting the gcf then applying difference of squares twice`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "162 [x ^ 6] - 32 [x ^ 2]"

            check {
                fromExpr = "162 [x ^ 6] - 32 [x ^ 2]"
                toExpr = "2 [x ^ 2] (3 x - 2) (3 x + 2) (9 [x ^ 2] + 4)"
                explanation {
                    key = FactorExplanation.FactorPolynomial
                }

                step {
                    fromExpr = "162 [x ^ 6] - 32 [x ^ 2]"
                    toExpr = "2 [x ^ 2] (81 [x ^ 4] - 16)"
                    explanation {
                        key = FactorExplanation.FactorGreatestCommonFactor
                    }

                    step {
                        fromExpr = "162 [x ^ 6] - 32 [x ^ 2]"
                        toExpr = "2 (81 [x ^ 6] - 16 [x ^ 2])"
                        explanation {
                            key = FactorExplanation.FactorGreatestCommonIntegerFactor
                        }
                    }

                    step {
                        fromExpr = "2 (81 [x ^ 6] - 16 [x ^ 2])"
                        toExpr = "2 <. [x ^ 2] (81 [x ^ 4] - 16) .>"
                        explanation {
                            key = FactorExplanation.FactorCommonFactor
                        }
                    }
                }

                step {
                    fromExpr = "2 [x ^ 2] (81 [x ^ 4] - 16)"
                    toExpr = "2 [x ^ 2] <. (9 [x ^ 2] - 4) (9 [x ^ 2] + 4) .>"
                    explanation {
                        key = FactorExplanation.FactorDifferenceOfSquares
                    }

                    step {
                        fromExpr = "(81 [x ^ 4] - 16)"
                        toExpr = "([(9 [x ^ 2]) ^ 2] - [4 ^ 2])"
                        explanation {
                            key = FactorExplanation.RewriteDifferenceOfSquares
                        }
                    }

                    step {
                        fromExpr = "([(9 [x ^ 2]) ^ 2] - [4 ^ 2])"
                        toExpr = "<. (9 [x ^ 2] - 4) (9 [x ^ 2] + 4) .>"
                        explanation {
                            key = FactorExplanation.ApplyDifferenceOfSquaresFormula
                        }
                    }
                }

                step {
                    fromExpr = "2 [x ^ 2] <. (9 [x ^ 2] - 4) (9 [x ^ 2] + 4) .>"
                    toExpr = "2 [x ^ 2] <. <. (3 x - 2) (3 x + 2) .> (9 [x ^ 2] + 4) .>"
                    explanation {
                        key = FactorExplanation.FactorDifferenceOfSquares
                    }

                    step {
                        fromExpr = "(9 [x ^ 2] - 4)"
                        toExpr = "([(3 x) ^ 2] - [2 ^ 2])"
                        explanation {
                            key = FactorExplanation.RewriteDifferenceOfSquares
                        }
                    }

                    step {
                        fromExpr = "([(3 x) ^ 2] - [2 ^ 2])"
                        toExpr = "<. (3 x - 2) (3 x + 2) .>"
                        explanation {
                            key = FactorExplanation.ApplyDifferenceOfSquaresFormula
                        }
                    }
                }
            }
        }

    @Test
    fun `test minus is distributed before factoring if it cancels with the leading coefficient`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "-(-[x ^ 2] - 2 x - 1)"

            check {
                fromExpr = "-(-[x ^ 2] - 2 x - 1)"
                toExpr = "[(x + 1) ^ 2]"
                explanation {
                    key = FactorExplanation.FactorPolynomial
                }

                step {
                    fromExpr = "-(-[x ^ 2] - 2 x - 1)"
                    toExpr = "[x ^ 2] + 2 x + 1"
                    explanation {
                        key = ExpandExplanation.DistributeNegativeOverBracket
                    }
                }

                step {
                    fromExpr = "[x ^ 2] + 2 x + 1"
                    toExpr = "[(x + 1) ^ 2]"
                    explanation {
                        key = FactorExplanation.FactorSquareOfBinomial
                    }
                }
            }
        }

    @Test
    fun `test bracket is not removed if it is useful for factoring`() =
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "[(3 x + 6) ^ 2] - (x + 2)"

            check {
                fromExpr = "[(3 x + 6) ^ 2] - (x + 2)"
                toExpr = "(x + 2) (9 x + 17)"
                explanation {
                    key = FactorExplanation.FactorPolynomial
                }

                step {
                    fromExpr = "[(3 x + 6) ^ 2] - (x + 2)"
                    toExpr = "[(3 (x + 2)) ^ 2] - (x + 2)"
                    explanation {
                        key = FactorExplanation.FactorGreatestCommonFactor
                    }
                }

                step {
                    fromExpr = "[(3 (x + 2)) ^ 2] - (x + 2)"
                    toExpr = "9 * [(x + 2) ^ 2] - (x + 2)"
                    explanation {
                        key = PolynomialsExplanation.DistributeProductToIntegerPowerAndSimplify
                    }
                }

                step {
                    fromExpr = "9 * [(x + 2) ^ 2] - (x + 2)"
                    toExpr = "(x + 2) (9 (x + 2) - 1)"
                    explanation {
                        key = FactorExplanation.FactorCommonFactor
                    }
                }

                step {
                    fromExpr = "(x + 2) (9 (x + 2) - 1)"
                    toExpr = "(x + 2) (<. 9 x + 18 .> - 1)"
                    explanation {
                        key = ExpandExplanation.ExpandSingleBracketAndSimplify
                    }
                }

                step {
                    fromExpr = "(x + 2) (9 x + 18 - 1)"
                    toExpr = "(x + 2) (9 x + 17)"
                    explanation {
                        key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                    }
                }
            }
        }
}
