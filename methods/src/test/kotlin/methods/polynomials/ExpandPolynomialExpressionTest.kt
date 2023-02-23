package methods.polynomials

import engine.context.Context
import engine.context.Curriculum
import engine.methods.SolverEngineExplanation
import engine.methods.testMethod
import methods.expand.ExpandExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import org.junit.jupiter.api.Test

@Suppress("LargeClass")
class ExpandPolynomialExpressionTest {

    @Test
    fun `test expand square of binomial, default curriculum`() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "[(2x - 3) ^ 2]"

        check {
            fromExpr = "[(2 x - 3) ^ 2]"
            toExpr = "4 [x ^ 2] - 12 x + 9"
            explanation {
                key = ExpandExplanation.ExpandBinomialSquaredAndSimplify
            }

            step {
                fromExpr = "[(2 x - 3) ^ 2]"
                toExpr = "[(2 x) ^ 2] + 2 * 2 x * (-3) + [(-3) ^ 2]"
                explanation {
                    key = ExpandExplanation.ExpandBinomialSquaredUsingIdentity
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
    fun `test expand square of binomial, US curriculum`() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "[(2x - 3) ^ 2]"
        context = Context(curriculum = Curriculum.US)

        check {
            fromExpr = "[(2 x - 3) ^ 2]"
            toExpr = "4 [x ^ 2] - 12 x + 9"
            explanation {
                key = ExpandExplanation.ExpandBinomialSquaredAndSimplify
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
                    key = ExpandExplanation.ApplyFoilMethod
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

    @Test
    fun `test expand expands inner expressions before the outer ones`() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "[([(x + 1)^2] + 2) ^ 2]"

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
                    key = ExpandExplanation.ExpandBinomialSquaredAndSimplify
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
                    key = ExpandExplanation.ExpandTrinomialSquaredAndSimplify
                }
            }
        }
    }

    @Test
    fun `test expand cube of binomial, default curriculum`() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "[(2x - 3) ^ 3]"

        check {
            fromExpr = "[(2 x - 3) ^ 3]"
            toExpr = "8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27"
            explanation {
                key = ExpandExplanation.ExpandBinomialCubedAndSimplify
            }

            step {
                fromExpr = "[(2 x - 3) ^ 3]"
                toExpr = "[(2 x) ^ 3] + 3 * [(2 x) ^ 2] * (-3) + 3 * 2 x * [(-3) ^ 2] + [(-3) ^ 3]"
                explanation {
                    key = ExpandExplanation.ExpandBinomialCubedUsingIdentity
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
    fun `test expand cube of binomial, US curriculum`() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "[(2x - 3) ^ 3]"
        context = Context(curriculum = Curriculum.US)

        check {
            fromExpr = "[(2 x - 3) ^ 3]"
            toExpr = "8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27"
            explanation {
                key = ExpandExplanation.ExpandBinomialCubedAndSimplify
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
                    taskId = "#1"
                    startExpr = "(2 x - 3) (2 x - 3)"
                    explanation {
                        key = ExpandExplanation.ExpandDoubleBracketsAndSimplify
                    }

                    step {
                        toExpr = "2 x * 2 x + 2 x * (-3) + (-3) * 2 x + (-3) * (-3)"
                        explanation {
                            key = ExpandExplanation.ApplyFoilMethod
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

                task {
                    taskId = "#2"
                    explanation {
                        key = SolverEngineExplanation.SubstitutePartialExpression
                    }
                }
            }

            step {
                fromExpr = "(4 [x ^ 2] - 12 x + 9) (2 x - 3)"
                toExpr = "8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27"
                explanation {
                    key = ExpandExplanation.ExpandDoubleBracketsAndSimplify
                }

                step {
                    fromExpr = "(4 [x ^ 2] - 12 x + 9) (2 x - 3)"
                    toExpr = "4 [x ^ 2] * 2 x + 4 [x ^ 2] * (-3) + " +
                        "(-12 x) * 2 x + (-12 x) * (-3) + 9 * 2 x + 9 * (-3)"
                    explanation {
                        key = ExpandExplanation.ExpandDoubleBrackets
                    }
                }

                step {
                    fromExpr = "4 [x ^ 2] * 2 x + 4 [x ^ 2] * (-3) + " +
                        "(-12 x) * 2 x + (-12 x) * (-3) + 9 * 2 x + 9 * (-3)"
                    toExpr = "8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27"
                    explanation {
                        key = PolynomialsExplanation.SimplifyAlgebraicExpression
                    }
                }
            }
        }
    }

    @Test
    fun `test expand cube of binomial multiplied by a constant`() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
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
                    key = ExpandExplanation.ExpandBinomialCubedAndSimplify
                }

                step {
                    fromExpr = "[(2 x - 3) ^ 3]"
                    toExpr = "[(2 x) ^ 3] + 3 * [(2 x) ^ 2] * (-3) + 3 * 2 x * [(-3) ^ 2] + [(-3) ^ 3]"
                    explanation {
                        key = ExpandExplanation.ExpandBinomialCubedUsingIdentity
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
                    key = ExpandExplanation.ExpandSingleBracketAndSimplify
                }

                step {
                    fromExpr = "5 (8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27)"
                    toExpr = "5 * 8 [x ^ 3] + 5 * (-36 [x ^ 2]) + 5 * 54 x + 5 * (-27)"
                    explanation {
                        key = ExpandExplanation.DistributeMultiplicationOverSum
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

    @Test
    fun `test expand square of trinomial, default curriculum`() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "[(2x + 1 + sqrt[3]) ^ 2]"

        check {
            fromExpr = "[(2 x + 1 + sqrt[3]) ^ 2]"
            toExpr = "4 [x ^ 2] + (4 + 4 sqrt[3]) x + 4 + 2 sqrt[3]"
            explanation {
                key = ExpandExplanation.ExpandTrinomialSquaredAndSimplify
            }

            step {
                fromExpr = "[(2 x + 1 + sqrt[3]) ^ 2]"
                toExpr = "[(2 x) ^ 2] + [1 ^ 2] + [(sqrt[3]) ^ 2] + 2 * 2 x * 1 + 2 * 1 * sqrt[3] + 2 * sqrt[3] * 2 x"
                explanation {
                    key = ExpandExplanation.ExpandTrinomialSquaredUsingIdentity
                }
            }

            step {
                fromExpr = "[(2 x) ^ 2] + [1 ^ 2] + [(sqrt[3]) ^ 2] + 2 * 2 x * 1 + 2 * 1 * sqrt[3] + 2 * sqrt[3] * 2 x"
                toExpr = "4 [x ^ 2] + (4 + 4 sqrt[3]) x + 4 + 2 sqrt[3]"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }
        }
    }

    @Test
    fun `test expand square of trinomial 2, default curriculum`() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "[(2[x ^ 2] + x - 3) ^ 2]"

        check {
            fromExpr = "[(2 [x ^ 2] + x - 3) ^ 2]"
            toExpr = "4 [x ^ 4] + 4 [x ^ 3] - 11 [x ^ 2] - 6 x + 9"
            explanation {
                key = ExpandExplanation.ExpandTrinomialSquaredAndSimplify
            }

            step {
                fromExpr = "[(2 [x ^ 2] + x - 3) ^ 2]"
                toExpr = "[(2 [x ^ 2]) ^ 2] + [x ^ 2] + [(-3) ^ 2] + " +
                    "2 * 2 [x ^ 2] * x + 2 * x * (-3) + 2 * (-3) * 2 [x ^ 2]"
                explanation {
                    key = ExpandExplanation.ExpandTrinomialSquaredUsingIdentity
                }
            }

            step {
                fromExpr = "[(2 [x ^ 2]) ^ 2] + [x ^ 2] + [(-3) ^ 2] + " +
                    "2 * 2 [x ^ 2] * x + 2 * x * (-3) + 2 * (-3) * 2 [x ^ 2]"
                toExpr = "4 [x ^ 4] + 4 [x ^ 3] - 11 [x ^ 2] - 6 x + 9"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }
        }
    }

    @Test
    fun `test expand square of trinomial, US curriculum`() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
        context = Context(curriculum = Curriculum.US)
        inputExpr = "[(2x + 1 + sqrt[3]) ^ 2]"

        check {
            fromExpr = "[(2 x + 1 + sqrt[3]) ^ 2]"
            toExpr = "4 [x ^ 2] + (4 + 4 sqrt[3]) x + 4 + 2 sqrt[3]"
            explanation {
                key = ExpandExplanation.ExpandTrinomialSquaredAndSimplify
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
                toExpr = "2 x * 2 x + 2 x * 1 + 2 x * sqrt[3] + 1 * 2 x + 1 * 1 + " +
                    "1 * sqrt[3] + sqrt[3] * 2 x + sqrt[3] * 1 + sqrt[3] * sqrt[3]"
                explanation {
                    key = ExpandExplanation.ExpandDoubleBrackets
                }
            }

            step {
                fromExpr = "2 x * 2 x + 2 x * 1 + 2 x * sqrt[3] + 1 * 2 x + 1 * 1 + " +
                    "1 * sqrt[3] + sqrt[3] * 2 x + sqrt[3] * 1 + sqrt[3] * sqrt[3]"
                toExpr = "4 [x ^ 2] + (4 + 4 sqrt[3]) x + 4 + 2 sqrt[3]"
                explanation {
                    key = PolynomialsExplanation.SimplifyAlgebraicExpression
                }
            }
        }
    }

    @Test
    fun `test expand product of sum and difference using the identity`() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "(2x - 3) (2x + 3)"

        check {
            fromExpr = "(2 x - 3) (2 x + 3)"
            toExpr = "4 [x ^ 2] - 9"
            explanation {
                key = ExpandExplanation.ExpandDoubleBracketsAndSimplify
            }

            step {
                fromExpr = "(2 x - 3) (2 x + 3)"
                toExpr = "[(2 x) ^ 2] - [3 ^ 2]"
                explanation {
                    key = ExpandExplanation.ExpandProductOfSumAndDifference
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
    fun `test expand product of sum and difference using the identity 2`() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "(2x - 3) * (2x + 3) * 11"

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
                    taskId = "#1"
                    startExpr = "(2 x - 3) (2 x + 3)"
                    explanation {
                        key = ExpandExplanation.ExpandDoubleBracketsAndSimplify
                    }

                    step {
                        fromExpr = "(2 x - 3) (2 x + 3)"
                        toExpr = "[(2 x) ^ 2] - [3 ^ 2]"
                        explanation {
                            key = ExpandExplanation.ExpandProductOfSumAndDifference
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

                task {
                    taskId = "#2"
                    explanation {
                        key = SolverEngineExplanation.SubstitutePartialExpression
                    }
                }
            }

            step {
                fromExpr = "11 (4 [x ^ 2] - 9)"
                toExpr = "44 [x ^ 2] - 99"
                explanation {
                    key = ExpandExplanation.ExpandSingleBracketAndSimplify
                }

                step {
                    fromExpr = "11 (4 [x ^ 2] - 9)"
                    toExpr = "11 * 4 [x ^ 2] + 11 * (-9)"
                    explanation {
                        key = ExpandExplanation.DistributeMultiplicationOverSum
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
    fun `test expand the product of binomials`() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "(2x + 3) (3x - 2)"

        check {
            fromExpr = "(2 x + 3) (3 x - 2)"
            toExpr = "6 [x ^ 2] + 5 x - 6"
            explanation {
                key = ExpandExplanation.ExpandDoubleBracketsAndSimplify
            }

            step {
                fromExpr = "(2 x + 3) (3 x - 2)"
                toExpr = "2 x * 3 x + 2 x * (-2) + 3 * 3 x + 3 * (-2)"
                explanation {
                    key = ExpandExplanation.ApplyFoilMethod
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
    fun `test expand the product of a trinomial and a binomial`() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "([x^2] + 5x - 2) (3x - 5)"

        check {
            fromExpr = "([x ^ 2] + 5 x - 2) (3 x - 5)"
            toExpr = "3 [x ^ 3] + 10 [x ^ 2] - 31 x + 10"
            explanation {
                key = ExpandExplanation.ExpandDoubleBracketsAndSimplify
            }

            step {
                fromExpr = "([x ^ 2] + 5 x - 2) (3 x - 5)"
                toExpr = "[x ^ 2] * 3 x + [x ^ 2] * (-5) + 5 x * 3 x + 5 x * (-5) + (-2) * 3 x + (-2) * (-5)"
                explanation {
                    key = ExpandExplanation.ExpandDoubleBrackets
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

    @Test
    fun `test distribute monomial from the left`() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "3[x^2] (2x - 7)"

        check {
            fromExpr = "3 [x ^ 2] (2 x - 7)"
            toExpr = "6 [x ^ 3] - 21 [x ^ 2]"
            explanation {
                key = ExpandExplanation.ExpandSingleBracketAndSimplify
            }

            step {
                fromExpr = "3 [x ^ 2] (2 x - 7)"
                toExpr = "3 [x ^ 2] * 2 x + 3 [x ^ 2] * (-7)"
                explanation {
                    key = ExpandExplanation.DistributeMultiplicationOverSum
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
    fun testDistributeConstantFromRhs() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "(x + 1) * 5"

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
                    key = ExpandExplanation.ExpandSingleBracketAndSimplify
                }
            }
        }
    }

    @Test
    fun testDistributeMonomialFromRhs() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
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
                    key = ExpandExplanation.ExpandSingleBracketAndSimplify
                }

                step {
                    fromExpr = "5 [x ^ 2] (x + 1)"
                    toExpr = "5 [x ^ 2] * x + 5 [x ^ 2] * 1"
                    explanation {
                        key = ExpandExplanation.DistributeMultiplicationOverSum
                    }
                }

                step {
                    fromExpr = "5 [x ^ 2] * x + 5 [x ^ 2] * 1"
                    toExpr = "5 [x ^ 3] + 5 [x ^ 2]"
                    explanation {
                        key = PolynomialsExplanation.SimplifyAlgebraicExpression
                    }

                    step {
                        fromExpr = "5 [x ^ 2] * x + 5 [x ^ 2] * 1"
                        toExpr = "5 [x ^ 3] + 5 [x ^ 2] * 1"
                        explanation {
                            key = PolynomialsExplanation.MultiplyMonomialsAndSimplify
                        }

                        step {
                            fromExpr = "5 [x ^ 2] * x"
                            toExpr = "5 ([x ^ 2] * x)"
                            explanation {
                                key = PolynomialsExplanation.CollectUnitaryMonomialsInProduct
                            }
                        }

                        step {
                            fromExpr = "5 ([x ^ 2] * x)"
                            toExpr = "5 [x ^ 3]"
                            explanation {
                                key = PolynomialsExplanation.MultiplyUnitaryMonomialsAndSimplify
                            }

                            step {
                                fromExpr = "[x ^ 2] * x"
                                toExpr = "[x ^ 2 + 1]"
                                explanation {
                                    key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                                }
                            }

                            step {
                                fromExpr = "[x ^ 2 + 1]"
                                toExpr = "[x ^ 3]"
                                explanation {
                                    key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                                }

                                step {
                                    fromExpr = "2 + 1"
                                    toExpr = "3"
                                    explanation {
                                        key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                                    }
                                }
                            }
                        }
                    }

                    step {
                        fromExpr = "5 [x ^ 3] + 5 [x ^ 2] * 1"
                        toExpr = "5 [x ^ 3] + 5 [x ^ 2]"
                        explanation {
                            key = PolynomialsExplanation.NormalizeMonomialAndSimplify
                        }

                        step {
                            fromExpr = "5 [x ^ 2] * 1"
                            toExpr = "5 * 1 [x ^ 2]"
                            explanation {
                                key = PolynomialsExplanation.NormalizeMonomial
                            }
                        }

                        step {
                            fromExpr = "5 * 1 [x ^ 2]"
                            toExpr = "5 [x ^ 2]"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun testDistributeMonomiaFromLhsAndConstantFromRhs() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
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
                    key = ExpandExplanation.ExpandSingleBracketAndSimplify
                }
            }

            step {
                fromExpr = "3 sqrt[2] (2 [x ^ 3] - 7 [x ^ 2])"
                toExpr = "6 sqrt[2] * [x ^ 3] - 21 sqrt[2] * [x ^ 2]"
                explanation {
                    key = ExpandExplanation.ExpandSingleBracketAndSimplify
                }
            }
        }
    }

    @Test
    fun `test expanding two brackets in an expression`() = testMethod {
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
        inputExpr = "3(x+1) - 2(x+6)"

        check {
            fromExpr = "3 (x + 1) - 2 (x + 6)"
            toExpr = "x - 9"
            explanation {
                key = PolynomialsExplanation.ExpandPolynomialExpression
            }

            step {
                fromExpr = "3 (x + 1) - 2 (x + 6)"
                toExpr = "(3 x + 3) - 2 (x + 6)"
                explanation {
                    key = ExpandExplanation.ExpandSingleBracketAndSimplify
                }

                step {
                    fromExpr = "3 (x + 1)"
                    toExpr = "3 * x + 3 * 1"
                    explanation {
                        key = ExpandExplanation.DistributeMultiplicationOverSum
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

            step {
                fromExpr = "(3 x + 3) - 2 (x + 6)"
                toExpr = "3 x + 3 - 2 (x + 6)"
                explanation {
                    key = GeneralExplanation.RemoveBracketSumInSum
                }
            }

            step {
                fromExpr = "3 x + 3 - 2 (x + 6)"
                toExpr = "3 x + 3 + (-2 x - 12)"
                explanation {
                    key = ExpandExplanation.ExpandSingleBracketAndSimplify
                }

                step {
                    fromExpr = "-2 (x + 6)"
                    toExpr = "(-2) * x + (-2) * 6"
                    explanation {
                        key = ExpandExplanation.DistributeMultiplicationOverSum
                    }
                }

                step {
                    fromExpr = "(-2) * x + (-2) * 6"
                    toExpr = "-2 x - 12"
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
        method = PolynomialsPlans.ExpandPolynomialExpressionInOneVariable
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
                    key = ExpandExplanation.ExpandBinomialSquaredAndSimplify
                }

                step {
                    fromExpr = "[(2 x + 3) ^ 2]"
                    toExpr = "[(2 x) ^ 2] + 2 * 2 x * 3 + [3 ^ 2]"
                    explanation {
                        key = ExpandExplanation.ExpandBinomialSquaredUsingIdentity
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
                    key = ExpandExplanation.ExpandDoubleBracketsAndSimplify
                }

                step {
                    fromExpr = "(4 [x ^ 2] + 12 x + 9) (x + 1)"
                    toExpr = "4 [x ^ 2] * x + 4 [x ^ 2] * 1 + 12 x * x + 12 x * 1 + 9 * x + 9 * 1"
                    explanation {
                        key = ExpandExplanation.ExpandDoubleBrackets
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
