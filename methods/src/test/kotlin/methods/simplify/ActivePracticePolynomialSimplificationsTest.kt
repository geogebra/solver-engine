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

import engine.context.BooleanSetting
import engine.context.Context
import engine.context.Setting
import engine.methods.testMethod
import methods.collecting.CollectingExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import methods.integerrationalexponents.IntegerRationalExponentsExplanation
import methods.polynomials.PolynomialsExplanation
import org.junit.jupiter.api.Test

class ActivePracticePolynomialSimplificationsTest {
    @Test
    fun testCollectSimpleLikeTerms() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            context = Context(settings = mapOf(Setting.QuickAddLikeTerms setTo BooleanSetting.True))
            inputExpr = "2 x + [x ^ 2] + 3 x - 5 [x ^ 2]"

            check {
                fromExpr = "2 x + [x ^ 2] + 3 x - 5 [x ^ 2]"
                toExpr = "-4 [x ^ 2] + 5 x"
                explanation {
                    key = SimplifyExplanation.SimplifyPolynomialExpression
                }

                step {
                    fromExpr = "2 x + [x ^ 2] + 3 x - 5 [x ^ 2]"
                    toExpr = "5 x + [x ^ 2] - 5 [x ^ 2]"
                    explanation {
                        key = CollectingExplanation.CombineTwoSimpleLikeTerms
                    }
                }

                step {
                    fromExpr = "5 x + [x ^ 2] - 5 [x ^ 2]"
                    toExpr = "5 x - 4 [x ^ 2]"
                    explanation {
                        key = CollectingExplanation.CombineTwoSimpleLikeTerms
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
    fun testMultiplyMonomials() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            context = Context(
                settings = mapOf(
                    Setting.CommutativeReorderInSteps setTo BooleanSetting.True,
                    Setting.QuickAddLikeTerms setTo BooleanSetting.True,
                ),
            )
            inputExpr = "3 [x ^ 2] * 4 [x ^ 3] * x + 5 * 7 [x ^ 6]"

            check {
                fromExpr = "3 [x ^ 2] * 4 [x ^ 3] * x + 5 * 7 [x ^ 6]"
                toExpr = "47 [x ^ 6]"
                explanation {
                    key = SimplifyExplanation.SimplifyPolynomialExpression
                }

                step {
                    fromExpr = "3 [x ^ 2] * 4 [x ^ 3] * x + 5 * 7 [x ^ 6]"
                    toExpr = "12 [x ^ 6] + 5 * 7 [x ^ 6]"
                    explanation {
                        key = PolynomialsExplanation.MultiplyMonomialsAndSimplify
                    }

                    step {
                        fromExpr = "3 [x ^ 2] * 4 [x ^ 3] * x"
                        toExpr = "3 [x ^ 5] * 4 x"
                        explanation {
                            key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                        }

                        step {
                            fromExpr = "3 [x ^ 2] * 4 [x ^ 3] * x"
                            toExpr = "3 [x ^ 2 + 3] * 4 x"
                            explanation {
                                key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                            }
                        }

                        step {
                            fromExpr = "3 [x ^ 2 + 3] * 4 x"
                            toExpr = "3 [x ^ 5] * 4 x"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                            }
                        }
                    }

                    step {
                        fromExpr = "3 [x ^ 5] * 4 x"
                        toExpr = "3 [x ^ 6] * 4"
                        explanation {
                            key = IntegerRationalExponentsExplanation.SimplifyProductOfPowersWithSameBase
                        }

                        step {
                            fromExpr = "3 [x ^ 5] * 4 x"
                            toExpr = "3 [x ^ 5 + 1] * 4"
                            explanation {
                                key = GeneralExplanation.RewriteProductOfPowersWithSameBase
                            }
                        }

                        step {
                            fromExpr = "3 [x ^ 5 + 1] * 4"
                            toExpr = "3 [x ^ 6] * 4"
                            explanation {
                                key = IntegerArithmeticExplanation.EvaluateIntegerAddition
                            }
                        }
                    }

                    step {
                        fromExpr = "3 [x ^ 6] * 4"
                        toExpr = "12 [x ^ 6]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                        }
                    }
                }

                step {
                    fromExpr = "12 [x ^ 6] + 5 * 7 [x ^ 6]"
                    toExpr = "12 [x ^ 6] + 35 [x ^ 6]"
                    explanation {
                        key = PolynomialsExplanation.MultiplyMonomialsAndSimplify
                    }

                    step {
                        fromExpr = "5 * 7 [x ^ 6]"
                        toExpr = "35 [x ^ 6]"
                        explanation {
                            key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                        }
                    }
                }

                step {
                    fromExpr = "12 [x ^ 6] + 35 [x ^ 6]"
                    toExpr = "47 [x ^ 6]"
                    explanation {
                        key = CollectingExplanation.CombineTwoSimpleLikeTerms
                    }
                }
            }
        }

    @Test
    fun testMonomialWithZeroCoefficientEliminated() =
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            context = Context(settings = mapOf(Setting.QuickAddLikeTerms setTo BooleanSetting.True))
            inputExpr = "3 [a ^ 2] + 2 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"

            check {
                fromExpr = "3 [a ^ 2] + 2 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"
                toExpr = "7 a"
                explanation {
                    key = SimplifyExplanation.SimplifyPolynomialExpression
                }

                step {
                    fromExpr = "3 [a ^ 2] + 2 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"
                    toExpr = "5 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"
                    explanation {
                        key = CollectingExplanation.CombineTwoSimpleLikeTerms
                    }
                }

                step {
                    fromExpr = "5 [a ^ 2] - 3 a - 5 [a ^ 2] + 10 a"
                    toExpr = "-3 a + 10 a"
                    explanation {
                        key = GeneralExplanation.CancelAdditiveInverseElements
                    }
                }

                step {
                    fromExpr = "-3 a + 10 a"
                    toExpr = "7 a"
                    explanation {
                        key = CollectingExplanation.CombineTwoSimpleLikeTerms
                    }
                }
            }
        }
}
