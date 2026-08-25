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

package methods.constantexpressions

import engine.context.emptyContextWithLabels
import engine.expressions.RootOrigin
import engine.methods.testMethod
import engine.sign.Sign
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test
import parser.parseExpression
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NormalizationTest {
    @Test
    fun testSimpleNormalization() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "(+1 + (3))"

            check {
                step {
                    toExpr = "1 + 3"
                    explanation {
                        key = GeneralExplanation.NormalizeExpression
                    }

                    step { toExpr = "+1 + (3)" }
                    step { toExpr = "+1 + 3" }
                    step { toExpr = "1 + 3" }
                }

                step { toExpr = "4" }
            }
        }

    @Test
    fun testNoNormalizationIfNotNeeded() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "0 * (1)"

            check {
                fromExpr = "0 * (1)"
                toExpr = "0"

                explanation {
                    key = GeneralExplanation.EvaluateProductContainingZero
                }
            }
        }

    @Test
    fun `test remove brackets and simplify a (bc) (de)`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"

            check {
                fromExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
                toExpr = "12 sqrt[2]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
                    toExpr = "sqrt[2] * 2 sqrt[2] * 3 sqrt[2]"
                    explanation {
                        key = GeneralExplanation.RemoveAllBracketProductInProduct
                    }

                    step {
                        fromExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
                        toExpr = "sqrt[2] * 2 sqrt[2] (3 sqrt[2])"
                        explanation {
                            key = GeneralExplanation.RemoveBracketProductInProduct
                        }
                    }

                    step {
                        fromExpr = "sqrt[2] * 2 sqrt[2] (3 sqrt[2])"
                        toExpr = "sqrt[2] * 2 sqrt[2] * 3 sqrt[2]"
                        explanation {
                            key = GeneralExplanation.RemoveBracketProductInProduct
                        }
                    }
                }

                step { }
            }
        }

    @Test
    fun `test remove brackets and simplify (a) (bc) (de)`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "(sqrt[2]) (2 sqrt[2]) (3 sqrt[2])"

            check {
                fromExpr = "(sqrt[2]) (2 sqrt[2]) (3 sqrt[2])"
                toExpr = "12 sqrt[2]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "(sqrt[2]) (2 sqrt[2]) (3 sqrt[2])"
                    toExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
                    explanation {
                        key = GeneralExplanation.RemoveRedundantBracket
                    }
                }

                step {
                    fromExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
                    toExpr = "sqrt[2] * 2 sqrt[2] * 3 sqrt[2]"
                    explanation {
                        key = GeneralExplanation.RemoveAllBracketProductInProduct
                    }

                    step {
                        fromExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"
                        toExpr = "sqrt[2] * 2 sqrt[2] (3 sqrt[2])"
                        explanation {
                            key = GeneralExplanation.RemoveBracketProductInProduct
                        }
                    }

                    step {
                        fromExpr = "sqrt[2] * 2 sqrt[2] (3 sqrt[2])"
                        toExpr = "sqrt[2] * 2 sqrt[2] * 3 sqrt[2]"
                        explanation {
                            key = GeneralExplanation.RemoveBracketProductInProduct
                        }
                    }
                }

                step { }
            }
        }
}

class NormalizeConstantExpressionForSignTest {
    private fun testNormalizedSign(expression: String, expectedSign: Sign) {
        val parsedExpression = parseExpression(expression).withOrigin(RootOrigin())
        val steps = normalizeConstantExpressionForSign.produceSteps(emptyContextWithLabels(), parsedExpression)
        assertEquals(expectedSign, steps?.lastOrNull()?.toExpr?.signOf())
    }

    @Test
    fun `test normalize radical expression for sign`() {
        testNormalizedSign("sqrt[2] - sqrt[8]", Sign.NEGATIVE)
        testNormalizedSign("sqrt[8] - sqrt[2]", Sign.POSITIVE)
    }

    @Test
    fun `test normalize logarithmic expression for sign`() {
        testNormalizedSign("ln[2] - 2 ln[3]", Sign.NEGATIVE)
    }

    @Test
    fun `test normalize zero expression for sign`() {
        testNormalizedSign("ln[4] - 2 ln[2]", Sign.ZERO)
    }

    @Test
    fun `test do not normalize non-constant expression for sign`() {
        val expression = parseExpression("a").withOrigin(RootOrigin())
        assertNull(normalizeConstantExpressionForSign.produceSteps(emptyContextWithLabels(), expression))
    }
}
