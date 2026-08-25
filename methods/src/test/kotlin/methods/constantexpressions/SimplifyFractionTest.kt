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

import engine.methods.testMethod
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.fractionarithmetic.FractionArithmeticPlans
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test

class SimplifyFractionTest {
    @Test
    fun `test simplify identical non-obvious logarithmic factors`() =
        testMethod {
            method = FractionArithmeticPlans.SimplifyFraction
            inputExpr = "[ln[3] - ln[2] / ln[3] - ln[2]]"

            check {
                toExpr = "1"
            }
        }

    @Test
    fun `test simplify fraction with logarithmic additive inverse`() =
        testMethod {
            method = FractionArithmeticPlans.SimplifyFraction
            inputExpr = "[ln[2] - ln[3] / ln[3] - ln[2]]"

            check {
                toExpr = "-1"

                step {
                    toExpr = "[-(-ln[2] + ln[3]) / ln[3] - ln[2]]"
                    explanation {
                        key = GeneralExplanation.FactorMinusFromSum
                    }
                }

                step {
                    toExpr = "[-(-ln[2] + ln[3]) / -ln[2] + ln[3]]"
                    explanation {
                        key = FractionArithmeticExplanation.ReorganizeCommonSumFactorInFraction
                    }
                }

                step {
                    toExpr = "[-1 / 1]"
                    explanation {
                        key = FractionArithmeticExplanation.DetermineCommonFactorIsNotZeroAndCancel
                    }

                    task {
                        taskId = "#1"
                        startExpr = "-ln[2] + ln[3] != 0"
                        explanation {
                            key = FractionArithmeticExplanation.DetermineCommonFactorIsNotZero
                            param { expr = "(-ln[2] + ln[3])" }
                        }

                        step {
                            toExpr = "Identity[ln[[3 / 2]] != 0]"
                            explanation {
                                key = methods.inequations.InequationsExplanation.SolveConstantInequation
                            }
                        }
                    }

                    task {
                        taskId = "#2"
                        startExpr = "[-(-ln[2] + ln[3]) / -ln[2] + ln[3]]"
                        explanation {
                            key = FractionArithmeticExplanation.CancelCommonFactorInFraction
                        }

                        step {
                            toExpr = "[-1 / 1]"
                            explanation {
                                key = FractionArithmeticExplanation.CancelCommonFactorInFraction
                            }
                        }
                    }
                }

                step {
                    toExpr = "-1"
                    explanation {
                        key = GeneralExplanation.SimplifyFractionWithOneDenominator
                    }
                }
            }
        }

    @Test
    fun `test do not cancel zero logarithmic factor`() =
        testMethod {
            method = FractionArithmeticPlans.SimplifyFraction
            inputExpr = "[ln[2] - ln[2] / ln[2] - ln[2]]"

            check {
                noTransformation()
            }
        }

    @Test
    fun `test simplify fraction with sum numerator & denominator additive inverse`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[1 - 2 sqrt[2] / 2 sqrt[2] - 1]"

            check {
                fromExpr = "[1 - 2 sqrt[2] / 2 sqrt[2] - 1]"
                toExpr = "-1"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyFraction
                }

                step {
                    fromExpr = "[1 - 2 sqrt[2] / 2 sqrt[2] - 1]"
                    toExpr = "[-(-1 + 2 sqrt[2]) / 2 sqrt[2] - 1]"
                    explanation {
                        key = GeneralExplanation.FactorMinusFromSum
                    }
                }

                step {
                    fromExpr = "[-(-1 + 2 sqrt[2]) / 2 sqrt[2] - 1]"
                    toExpr = "[-(-1 + 2 sqrt[2]) / -1 + 2 sqrt[2]]"
                    explanation {
                        key = FractionArithmeticExplanation.ReorganizeCommonSumFactorInFraction
                    }
                }

                step {
                    fromExpr = "[-(-1 + 2 sqrt[2]) / -1 + 2 sqrt[2]]"
                    toExpr = "[-1 / 1]"
                    explanation {
                        key = FractionArithmeticExplanation.CancelCommonFactorInFraction
                    }
                }

                step {
                    fromExpr = "[-1 / 1]"
                    toExpr = "-1"
                    explanation {
                        key = GeneralExplanation.SimplifyFractionWithOneDenominator
                    }
                }
            }
        }

    @Test
    fun `test simplify fraction with power numerator & denominator additive inverse base even power`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "[[(sqrt[2] - 1)^2] / [(1 - sqrt[2])^2]]"

            check {
                fromExpr = "[[(sqrt[2] - 1) ^ 2] / [(1 - sqrt[2]) ^ 2]]"
                toExpr = "1"
                explanation {
                    key = FractionArithmeticExplanation.SimplifyFraction
                }

                step {
                    fromExpr = "[[(sqrt[2] - 1) ^ 2] / [(1 - sqrt[2]) ^ 2]]"
                    toExpr = "[[(sqrt[2] - 1) ^ 2] / [(-(-1 + sqrt[2])) ^ 2]]"
                    explanation {
                        key = GeneralExplanation.FactorMinusFromSum
                    }
                }

                step {
                    fromExpr = "[[(sqrt[2] - 1) ^ 2] / [(-(-1 + sqrt[2])) ^ 2]]"
                    toExpr = "[[(sqrt[2] - 1) ^ 2] / [(-1 + sqrt[2]) ^ 2]]"
                    explanation {
                        key = GeneralExplanation.SimplifyEvenPowerOfNegative
                    }
                }

                step {
                    fromExpr = "[[(sqrt[2] - 1) ^ 2] / [(-1 + sqrt[2]) ^ 2]]"
                    toExpr = "[[(sqrt[2] - 1) ^ 2] / [(sqrt[2] - 1) ^ 2]]"
                    explanation {
                        key = FractionArithmeticExplanation.ReorganizeCommonSumFactorInFraction
                    }
                }

                step {
                    fromExpr = "[[(sqrt[2] - 1) ^ 2] / [(sqrt[2] - 1) ^ 2]]"
                    toExpr = "[1 / 1]"
                    explanation {
                        key = FractionArithmeticExplanation.CancelCommonFactorInFraction
                    }
                }

                step {
                    fromExpr = "[1 / 1]"
                    toExpr = "1"
                    explanation {
                        key = GeneralExplanation.SimplifyUnitFractionToOne
                    }
                }
            }
        }
}
