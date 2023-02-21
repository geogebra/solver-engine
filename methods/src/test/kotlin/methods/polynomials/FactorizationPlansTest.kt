package methods.polynomials

import engine.methods.testMethod
import org.junit.jupiter.api.Test

class FactorizationPlansTest {

    @Test
    fun `test extracting the common integer factor`() = testMethod {
        method = PolynomialsPlans.FactorPolynomialInOneVariable
        inputExpr = "16 [x ^ 5] - 32"

        check {
            fromExpr = "16 [x ^ 5] - 32"
            toExpr = "16 ([x ^ 5] - 2)"
            explanation {
                key = PolynomialsExplanation.FactorGreatestCommonFactor
            }

            step {
                fromExpr = "16 [x ^ 5] - 32"
                toExpr = "16 [x ^ 5] - 16 * 2"
                explanation {
                    key = PolynomialsExplanation.SplitIntegersInMonomialsBeforeFactoring
                }
            }

            step {
                fromExpr = "16 [x ^ 5] - 16 * 2"
                toExpr = "16 ([x ^ 5] - 2)"
                explanation {
                    key = PolynomialsExplanation.ExtractCommonTerms
                }
            }
        }
    }

    @Test
    fun `test extracting the common monomial factor`() = testMethod {
        method = PolynomialsPlans.FactorPolynomialInOneVariable
        inputExpr = "15 [x ^ 5] - 33 [x ^ 2]"

        check {
            fromExpr = "15 [x ^ 5] - 33 [x ^ 2]"
            toExpr = "3 [x ^ 2] (5 [x ^ 3] - 11)"
            explanation {
                key = PolynomialsExplanation.FactorGreatestCommonFactor
            }

            step {
                fromExpr = "15 [x ^ 5] - 33 [x ^ 2]"
                toExpr = "3 * 5 [x ^ 5] - 3 * 11 [x ^ 2]"
                explanation {
                    key = PolynomialsExplanation.SplitIntegersInMonomialsBeforeFactoring
                }
            }

            step {
                fromExpr = "3 * 5 [x ^ 5] - 3 * 11 [x ^ 2]"
                toExpr = "3 * 5 [x ^ 2] * [x ^ 3] - 3 * 11 [x ^ 2]"
                explanation {
                    key = PolynomialsExplanation.SplitVariablePowersInMonomialsBeforeFactoring
                }
            }

            step {
                fromExpr = "3 * 5 [x ^ 2] * [x ^ 3] - 3 * 11 [x ^ 2]"
                toExpr = "3 [x ^ 2] (5 [x ^ 3] - 11)"
                explanation {
                    key = PolynomialsExplanation.ExtractCommonTerms
                }
            }
        }
    }

    @Test
    fun `test factoring trinomial by guessing`() = testMethod {
        method = PolynomialsPlans.FactorPolynomialInOneVariable
        inputExpr = "[x ^ 2] - 5 x + 6"

        check {
            fromExpr = "[x ^ 2] - 5 x + 6"
            toExpr = "(x - 3) (x - 2)"
            explanation {
                key = PolynomialsExplanation.FactorTrinomialByGuessing
            }

            task {
                taskId = "#1"
                startExpr = "a + b = -5, a b = 6"
                explanation {
                    key = PolynomialsExplanation.SetUpAndSolveEquationSystemForTrinomial
                }

                step {
                    fromExpr = "a + b = -5, a b = 6"
                    toExpr = "a = -3, b = -2"
                    explanation {
                        key = PolynomialsExplanation.SolveSumProductDiophantineEquationSystemByGuessing
                    }
                }
            }

            task {
                taskId = "#2"
                startExpr = "(x - 3) (x - 2)"
                explanation {
                    key = PolynomialsExplanation.FactorTrinomialUsingTheSolutionsOfTheSumAndProductSystem
                }
            }
        }
    }

    @Test
    fun `test factoring using the difference of squares formula`() = testMethod {
        method = PolynomialsPlans.FactorPolynomialInOneVariable
        inputExpr = "121 [x ^ 4] - 9"

        check {
            fromExpr = "121 [x ^ 4] - 9"
            toExpr = "(11 [x ^ 2] - 3) (11 [x ^ 2] + 3)"
            explanation {
                key = PolynomialsExplanation.FactorDifferenceOfSquares
            }

            step {
                fromExpr = "121 [x ^ 4] - 9"
                toExpr = "[(11 [x ^ 2]) ^ 2] - [3 ^ 2]"
                explanation {
                    key = PolynomialsExplanation.RewriteDifferenceOfSquares
                }
            }

            step {
                fromExpr = "[(11 [x ^ 2]) ^ 2] - [3 ^ 2]"
                toExpr = "(11 [x ^ 2] - 3) (11 [x ^ 2] + 3)"
                explanation {
                    key = PolynomialsExplanation.ApplyDifferenceOfSquaresFormula
                }
            }
        }
    }

    @Test
    fun `test factoring by first extracting the gcf then applying difference of squares`() = testMethod {
        method = PolynomialsPlans.FactorPolynomialInOneVariable
        inputExpr = "18 [x ^ 6] - 32 [x ^ 2]"

        check {
            fromExpr = "18 [x ^ 6] - 32 [x ^ 2]"
            toExpr = "2 [x ^ 2] (3 [x ^ 2] - 4) (3 [x ^ 2] + 4)"
            explanation {
                key = PolynomialsExplanation.FactorPolynomial
            }

            step {
                fromExpr = "18 [x ^ 6] - 32 [x ^ 2]"
                toExpr = "2 [x ^ 2] (9 [x ^ 4] - 16)"
                explanation {
                    key = PolynomialsExplanation.FactorGreatestCommonFactor
                }

                step {
                    fromExpr = "18 [x ^ 6] - 32 [x ^ 2]"
                    toExpr = "2 * 9 [x ^ 6] - 2 * 16 [x ^ 2]"
                    explanation {
                        key = PolynomialsExplanation.SplitIntegersInMonomialsBeforeFactoring
                    }
                }

                step {
                    fromExpr = "2 * 9 [x ^ 6] - 2 * 16 [x ^ 2]"
                    toExpr = "2 * 9 [x ^ 2] * [x ^ 4] - 2 * 16 [x ^ 2]"
                    explanation {
                        key = PolynomialsExplanation.SplitVariablePowersInMonomialsBeforeFactoring
                    }
                }

                step {
                    fromExpr = "2 * 9 [x ^ 2] * [x ^ 4] - 2 * 16 [x ^ 2]"
                    toExpr = "2 [x ^ 2] (9 [x ^ 4] - 16)"
                    explanation {
                        key = PolynomialsExplanation.ExtractCommonTerms
                    }
                }
            }

            step {
                fromExpr = "2 [x ^ 2] (9 [x ^ 4] - 16)"
                toExpr = "2 [x ^ 2] (3 [x ^ 2] - 4) (3 [x ^ 2] + 4)"
                explanation {
                    key = PolynomialsExplanation.FactorDifferenceOfSquares
                }

                step {
                    fromExpr = "9 [x ^ 4] - 16"
                    toExpr = "[(3 [x ^ 2]) ^ 2] - [4 ^ 2]"
                    explanation {
                        key = PolynomialsExplanation.RewriteDifferenceOfSquares
                    }
                }

                step {
                    fromExpr = "[(3 [x ^ 2]) ^ 2] - [4 ^ 2]"
                    toExpr = "(3 [x ^ 2] - 4) (3 [x ^ 2] + 4)"
                    explanation {
                        key = PolynomialsExplanation.ApplyDifferenceOfSquaresFormula
                    }
                }
            }
        }
    }

    @Test
    fun `test factoring by first extracting the gcf then applying difference of squares twice`() = testMethod {
        method = PolynomialsPlans.FactorPolynomialInOneVariable
        inputExpr = "162 [x ^ 6] - 32 [x ^ 2]"

        check {
            fromExpr = "162 [x ^ 6] - 32 [x ^ 2]"
            toExpr = "2 [x ^ 2] (3 x - 2) (3 x + 2) (9 [x ^ 2] + 4)"
            explanation {
                key = PolynomialsExplanation.FactorPolynomial
            }

            step {
                fromExpr = "162 [x ^ 6] - 32 [x ^ 2]"
                toExpr = "2 [x ^ 2] (81 [x ^ 4] - 16)"
                explanation {
                    key = PolynomialsExplanation.FactorGreatestCommonFactor
                }

                step {
                    fromExpr = "162 [x ^ 6] - 32 [x ^ 2]"
                    toExpr = "2 * 81 [x ^ 6] - 2 * 16 [x ^ 2]"
                    explanation {
                        key = PolynomialsExplanation.SplitIntegersInMonomialsBeforeFactoring
                    }
                }

                step {
                    fromExpr = "2 * 81 [x ^ 6] - 2 * 16 [x ^ 2]"
                    toExpr = "2 * 81 [x ^ 2] * [x ^ 4] - 2 * 16 [x ^ 2]"
                    explanation {
                        key = PolynomialsExplanation.SplitVariablePowersInMonomialsBeforeFactoring
                    }
                }

                step {
                    fromExpr = "2 * 81 [x ^ 2] * [x ^ 4] - 2 * 16 [x ^ 2]"
                    toExpr = "2 [x ^ 2] (81 [x ^ 4] - 16)"
                    explanation {
                        key = PolynomialsExplanation.ExtractCommonTerms
                    }
                }
            }

            step {
                fromExpr = "2 [x ^ 2] (81 [x ^ 4] - 16)"
                toExpr = "2 [x ^ 2] (9 [x ^ 2] - 4) (9 [x ^ 2] + 4)"
                explanation {
                    key = PolynomialsExplanation.FactorDifferenceOfSquares
                }

                step {
                    fromExpr = "81 [x ^ 4] - 16"
                    toExpr = "[(9 [x ^ 2]) ^ 2] - [4 ^ 2]"
                    explanation {
                        key = PolynomialsExplanation.RewriteDifferenceOfSquares
                    }
                }

                step {
                    fromExpr = "[(9 [x ^ 2]) ^ 2] - [4 ^ 2]"
                    toExpr = "(9 [x ^ 2] - 4) (9 [x ^ 2] + 4)"
                    explanation {
                        key = PolynomialsExplanation.ApplyDifferenceOfSquaresFormula
                    }
                }
            }

            step {
                fromExpr = "2 [x ^ 2] (9 [x ^ 2] - 4) (9 [x ^ 2] + 4)"
                toExpr = "2 [x ^ 2] (3 x - 2) (3 x + 2) (9 [x ^ 2] + 4)"
                explanation {
                    key = PolynomialsExplanation.FactorDifferenceOfSquares
                }

                step {
                    fromExpr = "9 [x ^ 2] - 4"
                    toExpr = "[(3 x) ^ 2] - [2 ^ 2]"
                    explanation {
                        key = PolynomialsExplanation.RewriteDifferenceOfSquares
                    }
                }

                step {
                    fromExpr = "[(3 x) ^ 2] - [2 ^ 2]"
                    toExpr = "(3 x - 2) (3 x + 2)"
                    explanation {
                        key = PolynomialsExplanation.ApplyDifferenceOfSquaresFormula
                    }
                }
            }
        }
    }
}
