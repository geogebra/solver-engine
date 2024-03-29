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

package methods.polynomials

import engine.context.BooleanSetting
import engine.context.Context
import engine.context.Setting
import engine.methods.SolverEngineExplanation
import engine.methods.testMethod
import methods.collecting.CollectingExplanation
import methods.expand.ExpandExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.simplify.SimplifyExplanation
import org.junit.jupiter.api.Test

@Suppress("MaxLineLength", "LargeClass", "ktlint:standard:max-line-length")
class ExpandPolynomialsTest {
    @Test
    fun `test expand a fraction`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
            context = context.addSettings(
                mapOf(Setting.RestrictAddingFractionsWithConstantDenominator to BooleanSetting.True),
            )
            inputExpr = "[2 x - 3 / 2]"

            check {
                fromExpr = "[2 x - 3 / 2]"
                toExpr = "x - [3 / 2]"
                explanation {
                    key = ExpandExplanation.ExpandFractionAndSimplify
                }

                step {
                    fromExpr = "[2 x - 3 / 2]"
                    toExpr = "[2 x / 2] - [3 / 2]"
                    explanation {
                        key = ExpandExplanation.DistributeConstantNumerator
                    }
                }

                step {
                    fromExpr = "[2 x / 2] - [3 / 2]"
                    toExpr = "x - [3 / 2]"
                    explanation {
                        key = PolynomialsExplanation.SimplifyMonomial
                    }

                    step {
                        fromExpr = "[2 x / 2]"
                        toExpr = "x"
                        explanation {
                            key = GeneralExplanation.CancelDenominator
                        }
                    }
                }
            }
        }

    @Test
    fun `test expand a fraction with negative terms`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
            inputExpr = "[y - 2 x - 4 / 5]"
            context = context.addSettings(
                mapOf(Setting.RestrictAddingFractionsWithConstantDenominator to BooleanSetting.True),
            )

            check {
                fromExpr = "[y - 2 x - 4 / 5]"
                toExpr = "[y / 5] - [2 x / 5] - [4 / 5]"
                explanation {
                    key = ExpandExplanation.DistributeConstantNumerator
                }
            }
        }

    @Test
    fun `test expand square of binomial, GM or default curriculum`() {
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
            inputExpr = "[(2x - 3) ^ 2]"

            check {
                fromExpr = "[(2 x - 3) ^ 2]"
                toExpr = "4 [x ^ 2] - 12 x + 9"
                explanation {
                    key = ExpandExplanation.ExpandBinomialSquaredAndSimplify
                }

                step {
                    fromExpr = "[(2 x - 3) ^ 2]"
                    toExpr = "[(2 x) ^ 2] + 2 * <. 2 x .> * (-3) + [(-3) ^ 2]"
                    explanation {
                        key = ExpandExplanation.ExpandBinomialSquaredUsingIdentity
                    }
                }

                step {
                    fromExpr = "[(2 x) ^ 2] + 2 * 2 x * (-3) + [(-3) ^ 2]"
                    toExpr = "4 [x ^ 2] - 12 x + 9"
                    explanation {
                        key = SimplifyExplanation.SimplifyPolynomialExpression
                    }
                }
            }
        }
    }

    @Test
    fun `test expand square of binomial without using the identity`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
            context = Context(settings = mapOf(Setting.DontUseIdentitiesForExpanding setTo BooleanSetting.True))
            inputExpr = "[(2x - 3) ^ 2]"

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
                    toExpr = "<. 2 x .> * <. 2 x .> + <. 2 x .> * (-3) + (-3) * <. 2 x .> + (-3) * (-3)"
                    explanation {
                        key = ExpandExplanation.ApplyFoilMethod
                    }
                }

                step {
                    fromExpr = "2 x * 2 x + 2 x * (-3) + (-3) * 2 x + (-3) * (-3)"
                    toExpr = "4 [x ^ 2] - 12 x + 9"
                    explanation {
                        key = SimplifyExplanation.SimplifyPolynomialExpression
                    }
                }
            }
        }

    @Test
    fun `test expand expands inner expressions before the outer ones`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
            inputExpr = "[([(x + 1)^2] + 2) ^ 2]"

            check {
                fromExpr = "[([(x + 1) ^ 2] + 2) ^ 2]"
                toExpr = "[x ^ 4] + 4 [x ^ 3] + 10 [x ^ 2] + 12 x + 9"
                explanation {
                    key = PolynomialsExplanation.ExpandPolynomialExpression
                }

                step {
                    fromExpr = "[([(x + 1) ^ 2] + 2) ^ 2]"
                    toExpr = "[(<. [x ^ 2] + 2 x + 1 .> + 2) ^ 2]"
                    explanation {
                        key = ExpandExplanation.ExpandBinomialSquaredAndSimplify
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
    fun `test expand cube of binomial, default curriculum`() {
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
            inputExpr = "[(2x - 3) ^ 3]"

            check {
                fromExpr = "[(2 x - 3) ^ 3]"
                toExpr = "8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27"
                explanation {
                    key = ExpandExplanation.ExpandBinomialCubedAndSimplify
                }

                step {
                    fromExpr = "[(2 x - 3) ^ 3]"
                    toExpr = "[(2 x) ^ 3] + 3 * [(2 x) ^ 2] * (-3) + 3 * <. 2 x .> * [(-3) ^ 2] + [(-3) ^ 3]"
                    explanation {
                        key = ExpandExplanation.ExpandBinomialCubedUsingIdentity
                    }
                }

                step {
                    fromExpr = "[(2 x) ^ 3] + 3 * [(2 x) ^ 2] * (-3) + 3 * 2 x * [(-3) ^ 2] + [(-3) ^ 3]"
                    toExpr = "8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27"
                    explanation {
                        key = SimplifyExplanation.SimplifyPolynomialExpression
                    }
                }
            }
        }
    }

    @Test
    fun `test expand cube of binomial without using the identity`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
            context = Context(settings = mapOf(Setting.DontUseIdentitiesForExpanding setTo BooleanSetting.True))
            inputExpr = "[(2x - 3) ^ 3]"

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

                    invisibleStep {
                        fromExpr = "(2 x - 3) (2 x - 3) (2 x - 3)"
                        toExpr = "<. (2 x - 3) (2 x - 3) .> (2 x - 3)"
                        explanation {
                            key = SolverEngineExplanation.ExtractPartialExpression
                        }
                    }

                    step {
                        fromExpr = "<. (2 x - 3) (2 x - 3) .> (2 x - 3)"
                        toExpr = "(<. 2 x .> * <. 2 x .> + <. 2 x .> * (-3) + (-3) * <. 2 x .> + (-3) * (-3)) (2 x - 3)"
                        explanation {
                            key = ExpandExplanation.ApplyFoilMethod
                        }
                    }

                    step {
                        fromExpr = "(2 x * 2 x + 2 x * (-3) + (-3) * 2 x + (-3) * (-3)) (2 x - 3)"
                        toExpr = "(4 [x ^ 2] - 12 x + 9)(2 x - 3)"
                        explanation {
                            key = SimplifyExplanation.SimplifyPolynomialExpression
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
                        toExpr = "<. 4 [x ^ 2] .> * <. 2 x .> + <. 4 [x ^ 2] .> * (-3) + (-12 x) * <. 2 x .> " +
                            "+ (-12 x) * (-3) + 9 * <. 2 x .> + 9 * (-3)"
                        explanation {
                            key = ExpandExplanation.ExpandDoubleBrackets
                        }
                    }

                    step {
                        fromExpr = "4 [x ^ 2] * 2 x + 4 [x ^ 2] * (-3) + (-12 x) * 2 x " +
                            "+ (-12 x) * (-3) + 9 * 2 x + 9 * (-3)"
                        toExpr = "8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27"
                        explanation {
                            key = SimplifyExplanation.SimplifyPolynomialExpression
                        }
                    }
                }
            }
        }

    @Test
    fun `test expand cube of binomial multiplied by a constant`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
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
                        toExpr = "([(2 x) ^ 3] + 3 * [(2 x) ^ 2] * (-3) + 3 * <. 2 x .> * [(-3) ^ 2] + [(-3) ^ 3])"
                        explanation {
                            key = ExpandExplanation.ExpandBinomialCubedUsingIdentity
                        }
                    }

                    step {
                        fromExpr = "([(2 x) ^ 3] + 3 * [(2 x) ^ 2] * (-3) + 3 * 2 x * [(-3) ^ 2] + [(-3) ^ 3])"
                        toExpr = "(8 [x ^ 3] - 36 [x ^ 2] + 54 x - 27)"
                        explanation {
                            key = SimplifyExplanation.SimplifyExpressionInBrackets
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
                        toExpr = "5 * <. 8 [x ^ 3] .> + 5 * (-36 [x ^ 2]) + 5 * <. 54 x .> + 5 * (-27)"
                        explanation {
                            key = ExpandExplanation.DistributeMultiplicationOverSum
                        }
                    }

                    step {
                        fromExpr = "5 * 8 [x ^ 3] + 5 * (-36 [x ^ 2]) + 5 * 54 x + 5 * (-27)"
                        toExpr = "40 [x ^ 3] - 180 [x ^ 2] + 270 x - 135"
                        explanation {
                            key = SimplifyExplanation.SimplifyPolynomialExpression
                        }
                    }
                }
            }
        }

    @Test
    fun `test expand square of trinomial, default curriculum`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
            inputExpr = "[(2x + 1 + sqrt[3]) ^ 2]"

            check {
                fromExpr = "[(2 x + 1 + sqrt[3]) ^ 2]"
                toExpr = "4 [x ^ 2] + (4 + 4 sqrt[3]) x + 4 + 2 sqrt[3]"
                explanation {
                    key = ExpandExplanation.ExpandTrinomialSquaredAndSimplify
                }

                step {
                    fromExpr = "[(2 x + 1 + sqrt[3]) ^ 2]"
                    toExpr = "[(2 x) ^ 2] + [1 ^ 2] + [(sqrt[3]) ^ 2] + 2 * <. 2 x .> * 1 " +
                        "+ 2 * 1 * sqrt[3] + 2 * sqrt[3] * <. 2 x .>"
                    explanation {
                        key = ExpandExplanation.ExpandTrinomialSquaredUsingIdentity
                    }
                }

                step {
                    fromExpr = "[(2 x) ^ 2] + [1 ^ 2] + [(sqrt[3]) ^ 2] + 2 * 2 x * 1 + 2 * 1 * sqrt[3] + 2 * sqrt[3] * 2 x"
                    toExpr = "4 [x ^ 2] + (4 + 4 sqrt[3]) x + 4 + 2 sqrt[3]"
                    explanation {
                        key = SimplifyExplanation.SimplifyPolynomialExpression
                    }
                }
            }
        }

    @Test
    fun `test expand square of trinomial 2, default curriculum`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
            inputExpr = "[(2[x ^ 2] + x - 3) ^ 2]"

            check {
                fromExpr = "[(2 [x ^ 2] + x - 3) ^ 2]"
                toExpr = "4 [x ^ 4] + 4 [x ^ 3] - 11 [x ^ 2] - 6 x + 9"
                explanation {
                    key = ExpandExplanation.ExpandTrinomialSquaredAndSimplify
                }

                step {
                    fromExpr = "[(2 [x ^ 2] + x - 3) ^ 2]"
                    toExpr = "[(2 [x ^ 2]) ^ 2] + [x ^ 2] + [(-3) ^ 2] + 2 * <. 2 [x ^ 2] .> * x + " +
                        "2 * x * (-3) + 2 * (-3) * <. 2 [x ^ 2] .>"
                    explanation {
                        key = ExpandExplanation.ExpandTrinomialSquaredUsingIdentity
                    }
                }

                step {
                    fromExpr = "[(2 [x ^ 2]) ^ 2] + [x ^ 2] + [(-3) ^ 2] + 2 * 2 [x ^ 2] * x " +
                        "+ 2 * x * (-3) + 2 * (-3) * 2 [x ^ 2]"
                    toExpr = "4 [x ^ 4] + 4 [x ^ 3] - 11 [x ^ 2] - 6 x + 9"
                    explanation {
                        key = SimplifyExplanation.SimplifyPolynomialExpression
                    }
                }
            }
        }

    @Test
    fun `test expand square of trinomial without using the identity`() =
        testMethod {
            context = Context(settings = mapOf(Setting.DontUseIdentitiesForExpanding setTo BooleanSetting.True))
            method = PolynomialsPlans.ExpandPolynomialExpression
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
                    toExpr = "<. 2 x .> * <. 2 x .> + <. 2 x .> * 1 + <. 2 x .> * sqrt[3] + " +
                        "1 * <. 2 x .> + 1 * 1 + 1 * sqrt[3] + sqrt[3] * <. 2 x .> + sqrt[3] * 1 + sqrt[3] * sqrt[3]"
                    explanation {
                        key = ExpandExplanation.ExpandDoubleBrackets
                    }
                }

                step {
                    fromExpr = "2 x * 2 x + 2 x * 1 + 2 x * sqrt[3] + 1 * 2 x + 1 * 1 + 1 * sqrt[3] " +
                        "+ sqrt[3] * 2 x + sqrt[3] * 1 + sqrt[3] * sqrt[3]"
                    toExpr = "4 [x ^ 2] + (4 + 4 sqrt[3]) x + 4 + 2 sqrt[3]"
                    explanation {
                        key = SimplifyExplanation.SimplifyPolynomialExpression
                    }
                }
            }
        }

    @Test
    fun `test expand product of sum and difference using the identity`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
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
                        key = SimplifyExplanation.SimplifyPolynomialExpression
                    }
                }
            }
        }

    @Test
    fun `test expand product of sum and difference using the identity 2`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
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
                        key = GeneralExplanation.ReorderProduct
                    }
                }

                step {
                    fromExpr = "11 (2 x - 3) (2 x + 3)"
                    toExpr = "11 (4 [x ^ 2] - 9)"

                    invisibleStep {
                        fromExpr = "11 (2 x - 3) (2 x + 3)"
                        toExpr = "11 <. (2 x - 3) (2 x + 3) .>"
                        explanation {
                            key = SolverEngineExplanation.ExtractPartialExpression
                        }
                    }

                    step {
                        fromExpr = "11 <. (2 x - 3) (2 x + 3) .>"
                        toExpr = "11 ([(2 x) ^ 2] - [3 ^ 2])"
                        explanation {
                            key = ExpandExplanation.ExpandProductOfSumAndDifference
                        }
                    }

                    step {
                        fromExpr = "11 ([(2 x) ^ 2] - [3 ^ 2])"
                        toExpr = "11 (4 [x ^ 2] - 9)"
                        explanation {
                            key = SimplifyExplanation.SimplifyPolynomialExpression
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
                        toExpr = "11 * <. 4 [x ^ 2] .> + 11 * (-9)"
                        explanation {
                            key = ExpandExplanation.DistributeMultiplicationOverSum
                        }
                    }

                    step {
                        fromExpr = "11 * 4 [x ^ 2] + 11 * (-9)"
                        toExpr = "44 [x ^ 2] - 99"
                        explanation {
                            key = SimplifyExplanation.SimplifyPolynomialExpression
                        }
                    }
                }
            }
        }

    @Test
    fun `test expand the product of binomials`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
            inputExpr = "(2x + 3) (3x - 2)"

            check {
                fromExpr = "(2 x + 3) (3 x - 2)"
                toExpr = "6 [x ^ 2] + 5 x - 6"
                explanation {
                    key = ExpandExplanation.ExpandDoubleBracketsAndSimplify
                }

                step {
                    fromExpr = "(2 x + 3) (3 x - 2)"
                    toExpr = "<. 2 x .> * <. 3 x .> + <. 2 x .> * (-2) + 3 * <. 3 x .> + 3 * (-2)"
                    explanation {
                        key = ExpandExplanation.ApplyFoilMethod
                    }
                }

                step {
                    fromExpr = "2 x * 3 x + 2 x * (-2) + 3 * 3 x + 3 * (-2)"
                    toExpr = "6 [x ^ 2] + 5 x - 6"
                    explanation {
                        key = SimplifyExplanation.SimplifyPolynomialExpression
                    }
                }
            }
        }

    @Test
    fun `test expand the product of a trinomial and a binomial`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
            inputExpr = "([x^2] + 5x - 2) (3x - 5)"

            check {
                fromExpr = "([x ^ 2] + 5 x - 2) (3 x - 5)"
                toExpr = "3 [x ^ 3] + 10 [x ^ 2] - 31 x + 10"
                explanation {
                    key = ExpandExplanation.ExpandDoubleBracketsAndSimplify
                }

                step {
                    fromExpr = "([x ^ 2] + 5 x - 2) (3 x - 5)"
                    toExpr = "[x ^ 2] * <. 3 x .> + [x ^ 2] * (-5) + <. 5 x .> * <. 3 x .> + <. 5 x .> * (-5) " +
                        "+ (-2) * <. 3 x .> + (-2) * (-5)"
                    explanation {
                        key = ExpandExplanation.ExpandDoubleBrackets
                    }
                }

                step {
                    fromExpr = "[x ^ 2] * 3 x + [x ^ 2] * (-5) + 5 x * 3 x + 5 x * (-5) + (-2) * 3 x + (-2) * (-5)"
                    toExpr = "3 [x ^ 3] + 10 [x ^ 2] - 31 x + 10"
                    explanation {
                        key = SimplifyExplanation.SimplifyPolynomialExpression
                    }
                }
            }
        }

    @Test
    fun `test expanding product of a sum and a monomial on the left`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
            inputExpr = "3 [x ^ 2] (2 x - 7)"

            check {
                fromExpr = "3 [x ^ 2] (2 x - 7)"
                toExpr = "6 [x ^ 3] - 21 [x ^ 2]"
                explanation {
                    key = ExpandExplanation.ExpandSingleBracketAndSimplify
                }

                step {
                    fromExpr = "3 [x ^ 2] (2 x - 7)"
                    toExpr = "<. 3 [x ^ 2] .> * <. 2 x .> + <. 3 [x ^ 2] .> * (-7)"
                    explanation {
                        key = ExpandExplanation.DistributeMultiplicationOverSum
                    }
                }

                step {
                    fromExpr = "3 [x ^ 2] * 2 x + 3 [x ^ 2] * (-7)"
                    toExpr = "6 [x ^ 3] - 21 [x ^ 2]"
                    explanation {
                        key = SimplifyExplanation.SimplifyPolynomialExpression
                    }
                }
            }
        }

    @Test
    fun `test expanding product of a sum and a constant on the right`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
            inputExpr = "(x + 1) * 5"

            check {
                fromExpr = "(x + 1) * 5"
                toExpr = "5 x + 5"
                explanation {
                    key = PolynomialsExplanation.ExpandPolynomialExpression
                }

                // This reordering should not happen
                step {
                    fromExpr = "(x + 1) * 5"
                    toExpr = "5 (x + 1)"
                    explanation {
                        key = GeneralExplanation.ReorderProduct
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
    fun `test expanding product of a sum and a monomial on the right`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
            inputExpr = "(x + 1) * 5 [x ^ 2]"

            check {
                fromExpr = "(x + 1) * 5 [x ^ 2]"
                toExpr = "5 [x ^ 3] + 5 [x ^ 2]"
                explanation {
                    key = PolynomialsExplanation.ExpandPolynomialExpression
                }

                // This reordering should not happen
                step {
                    fromExpr = "(x + 1) * 5 [x ^ 2]"
                    toExpr = "5 [x ^ 2] (x + 1)"
                    explanation {
                        key = GeneralExplanation.ReorderProduct
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
                        toExpr = "<. 5 [x ^ 2] .> * x + <. 5 [x ^ 2] .> * 1"
                        explanation {
                            key = ExpandExplanation.DistributeMultiplicationOverSum
                        }
                    }

                    step {
                        fromExpr = "5 [x ^ 2] * x + 5 [x ^ 2] * 1"
                        toExpr = "5 [x ^ 3] + 5 [x ^ 2]"
                        explanation {
                            key = SimplifyExplanation.SimplifyPolynomialExpression
                        }
                    }
                }
            }
        }

    @Test
    fun `test expanding product of a sum with factors on both sides`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
            inputExpr = "5 (x + 1) * [x ^ 2]"

            check {
                fromExpr = "5 (x + 1) * [x ^ 2]"
                toExpr = "5 [x ^ 3] + 5 [x ^ 2]"
                explanation {
                    key = PolynomialsExplanation.ExpandPolynomialExpression
                }

                step {
                    fromExpr = "5 (x + 1) * [x ^ 2]"
                    toExpr = "5 [x ^ 2] (x + 1)"
                    explanation {
                        key = GeneralExplanation.ReorderProduct
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
                        toExpr = "<. 5 [x ^ 2] .> * x + <. 5 [x ^ 2] .> * 1"
                        explanation {
                            key = ExpandExplanation.DistributeMultiplicationOverSum
                        }
                    }

                    step {
                        fromExpr = "5 [x ^ 2] * x + 5 [x ^ 2] * 1"
                        toExpr = "5 [x ^ 3] + 5 [x ^ 2]"
                        explanation {
                            key = SimplifyExplanation.SimplifyPolynomialExpression
                        }
                    }
                }
            }
        }

    @Test
    fun testDistributeMonomiaFromLhsAndConstantFromRhs() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
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
                        key = GeneralExplanation.ReorderProduct
                    }
                }

                step {
                    fromExpr = "3 sqrt[2] * [x ^ 2] (2 x - 7)"
                    toExpr = "6 sqrt[2] * [x ^ 3] - 21 sqrt[2] * [x ^ 2]"
                    explanation {
                        key = ExpandExplanation.ExpandSingleBracketAndSimplify
                    }
                }
            }
        }

    @Test
    fun `test expanding two brackets in an expression`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
            inputExpr = "3(x+1) - 2(x+6)"

            check {
                fromExpr = "3 (x + 1) - 2 (x + 6)"
                toExpr = "x - 9"
                explanation {
                    key = PolynomialsExplanation.ExpandPolynomialExpression
                }

                step {
                    fromExpr = "3 (x + 1) - 2 (x + 6)"
                    toExpr = "<. 3 x + 3 .> - 2 (x + 6)"
                    explanation {
                        key = ExpandExplanation.ExpandSingleBracketAndSimplify
                    }

                    step {
                        fromExpr = "3 (x + 1)"
                        toExpr = "<. 3 * x + 3 * 1 .>"
                        explanation {
                            key = ExpandExplanation.DistributeMultiplicationOverSum
                        }
                    }

                    step {
                        fromExpr = "<. 3 * x + 3 * 1 .>"
                        toExpr = "<. 3 x + 3 .>"
                        // probably will be fixed in PLUT-478 (key shouldn't be SimplifyExpressionInBrackets)
                        explanation {
                            key = SimplifyExplanation.SimplifyPolynomialExpression
                        }
                    }
                }

                step {
                    fromExpr = "3 x + 3 - 2 (x + 6)"
                    toExpr = "3 x + 3 + <. -2 x - 12 .>"
                    explanation {
                        key = ExpandExplanation.ExpandSingleBracketAndSimplify
                    }

                    step {
                        fromExpr = "-2 (x + 6)"
                        toExpr = "<.-2 * x - 2 * 6.>"
                        explanation {
                            key = ExpandExplanation.DistributeMultiplicationOverSum
                        }
                    }

                    step {
                        fromExpr = "<.-2 * x - 2 * 6.>"
                        toExpr = "<.-2 x - 12.>"
                        explanation {
                            key = SimplifyExplanation.SimplifyPolynomialExpression
                        }
                    }
                }

                step {
                    fromExpr = "3 x + 3 - 2 x - 12"
                    toExpr = "3 x - 9 - 2 x"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerSubtraction
                    }
                }

                step {
                    fromExpr = "3 x - 9 - 2 x"
                    toExpr = "x - 9"
                    explanation {
                        key = CollectingExplanation.CollectLikeTermsAndSimplify
                    }
                }
            }
        }

    @Test
    fun `expand (ax + b)^2 (cx + d)`() =
        testMethod {
            method = PolynomialsPlans.ExpandPolynomialExpression
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
                        toExpr = "([(2 x) ^ 2] + 2 * <. 2 x .> * 3 + [3 ^ 2])"
                        explanation {
                            key = ExpandExplanation.ExpandBinomialSquaredUsingIdentity
                        }
                    }

                    step {
                        fromExpr = "([(2 x) ^ 2] + 2 * 2 x * 3 + [3 ^ 2])"
                        toExpr = "(4 [x ^ 2] + 12 x + 9)"
                        explanation {
                            key = SimplifyExplanation.SimplifyExpressionInBrackets
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
                        toExpr = "<. 4 [x ^ 2] .> * x + <. 4 [x ^ 2] .> * 1 + <. 12 x .> * x " +
                            "+ <. 12 x .> * 1 + 9 * x + 9 * 1"
                        explanation {
                            key = ExpandExplanation.ExpandDoubleBrackets
                        }
                    }

                    step {
                        fromExpr = "4 [x ^ 2] * x + 4 [x ^ 2] * 1 + 12 x * x + 12 x * 1 + 9 * x + 9 * 1"
                        toExpr = "4 [x ^ 3] + 16 [x ^ 2] + 21 x + 9"
                        explanation {
                            key = SimplifyExplanation.SimplifyPolynomialExpression
                        }
                    }
                }
            }
        }
}
