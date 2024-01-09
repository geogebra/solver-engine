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

package methods.general

import engine.context.BooleanSetting
import engine.context.Context
import engine.context.Setting
import engine.methods.testMethod
import org.junit.jupiter.api.Test

class NormalizationPlansTests {
    @Test
    fun testReplaceAllInvisibleBracketsSimple() =
        testMethod {
            method = NormalizationPlans.NormalizeExpression
            inputExpr = "3*-4"
            check {
                toExpr = "3*(-4)"

                explanation {
                    key = Explanation.AddClarifyingBracket
                }
            }
        }

    @Test
    fun testReplaceAllInvisibleBracketsSimpleGm() =
        testMethod {
            method = NormalizationPlans.NormalizeExpression
            inputExpr = "3*-4"
            context = Context(settings = mapOf(Setting.DontAddClarifyingBrackets setTo BooleanSetting.True))
            check {
                noTransformation()
            }
        }

    @Test
    fun testReplaceAllInvisibleBracketsNoTransformation() =
        testMethod {
            method = NormalizationPlans.NormalizeExpression
            inputExpr = "[1/3 - 4x]"

            check {
                noTransformation()
            }
        }

    @Test
    fun testReplaceAllInvisibleBracketsNested() =
        testMethod {
            method = NormalizationPlans.NormalizeExpression
            inputExpr = "[3 * -4/1 --+-2]"

            check {
                fromExpr = "[3 * -4 / 1 - -+-2]"
                toExpr = "[3 * (-4) / 1 - (-(-2))]"
                explanation {
                    key = GeneralExplanation.NormalizeExpression
                }

                step {
                    fromExpr = "[3 * -4 / 1 - -+-2]"
                    toExpr = "[3 * (-4) / 1 - -+-2]"
                    explanation {
                        key = GeneralExplanation.AddClarifyingBracket
                    }
                }

                step {
                    fromExpr = "[3 * (-4) / 1 - -+-2]"
                    toExpr = "[3 * (-4) / 1 - (-+-2)]"
                    explanation {
                        key = GeneralExplanation.AddClarifyingBracket
                    }
                }

                step {
                    fromExpr = "[3 * (-4) / 1 - (-+-2)]"
                    toExpr = "[3 * (-4) / 1 - (-(+-2))]"
                    explanation {
                        key = GeneralExplanation.AddClarifyingBracket
                    }
                }

                step {
                    fromExpr = "[3 * (-4) / 1 - (-(+-2))]"
                    toExpr = "[3 * (-4) / 1 - (-(+(-2)))]"
                    explanation {
                        key = GeneralExplanation.AddClarifyingBracket
                    }
                }

                step {
                    fromExpr = "[3 * (-4) / 1 - (-(+(-2)))]"
                    toExpr = "[3 * (-4) / 1 - (-(-2))]"
                    explanation {
                        key = GeneralExplanation.RemoveRedundantPlusSign
                    }
                }
            }
        }

    @Test
    fun testDoRemoveBracketInSumWithRedundantUnaryPlusInFirstPosition() =
        testMethod {
            method = NormalizationPlans.NormalizeExpression
            inputExpr = "+1 + 2"

            check {
                fromExpr = "+1 + 2"
                toExpr = "1 + 2"
                explanation {
                    key = GeneralExplanation.RemoveRedundantPlusSign
                }
            }
        }

    @Test
    fun testDoNotRemoveBracketInSumWithRedundantUnaryPlusInSecondPosition() =
        testMethod {
            method = NormalizationPlans.NormalizeExpression
            inputExpr = "1 + +2"
            check {
                toExpr = "1 + 2"
                step { toExpr = "1 + (+2)" }
                step { toExpr = "1 + 2" }
            }
        }

    @Test
    fun testRearrangeTermsInAProductGm() =
        testMethod {
            method = NormalizationPlans.ReorderProductInSteps
            inputExpr = "sqrt[3] * 5 * ([y ^ 2] + 1) * (1 + sqrt[3]) * sqrt[y] * y"

            check {
                step { toExpr = "5 sqrt[3] ([y ^ 2] + 1) (1 + sqrt[3]) sqrt[y] * y" }
                step { toExpr = "5 sqrt[3] (1 + sqrt[3]) ([y ^ 2] + 1) sqrt[y] * y" }
                step { toExpr = "5 sqrt[3] (1 + sqrt[3]) sqrt[y] ([y ^ 2] + 1) y" }
                step { toExpr = "5 sqrt[3] (1 + sqrt[3]) y sqrt[y] ([y ^ 2] + 1)" }
            }
        }

    @Test
    fun testRemoveAllBracketProductInProduct() =
        testMethod {
            method = NormalizationPlans.RemoveAllBracketProductInProduct
            inputExpr = "sqrt[2] (2 sqrt[2]) (3 sqrt[2])"

            check {
                toExpr = "sqrt[2] * 2 sqrt[2] * 3 sqrt[2]"

                step { toExpr = "sqrt[2] * 2 sqrt[2] (3 sqrt[2])" }
                step { toExpr = "sqrt[2] * 2 sqrt[2] * 3 sqrt[2]" }
            }
        }
}
