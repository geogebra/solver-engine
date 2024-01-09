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
import methods.expand.ExpandExplanation
import methods.fractionarithmetic.FractionArithmeticExplanation
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import org.junit.jupiter.api.Test

class AbsoluteValuesTest {
    @Test
    fun `test constant expression containing absolute values`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "abs[3 * 2 - 4] - abs[1 - [6 / 5]] + abs[2 - 1 - 1]"

            check {
                fromExpr = "abs[3 * 2 - 4] - abs[1 - [6 / 5]] + abs[2 - 1 - 1]"
                toExpr = "[9 / 5]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "abs[3 * 2 - 4] - abs[1 - [6 / 5]] + abs[2 - 1 - 1]"
                    toExpr = "2 - abs[1 - [6 / 5]] + abs[2 - 1 - 1]"
                    explanation {
                        key = GeneralExplanation.EvaluateAbsoluteValue
                    }

                    step {
                        fromExpr = "abs[3 * 2 - 4]"
                        toExpr = "abs[6 - 4]"
                        explanation {
                            key = IntegerArithmeticExplanation.SimplifyIntegersInProduct
                        }
                    }

                    step {
                        fromExpr = "abs[6 - 4]"
                        toExpr = "abs[2]"
                        explanation {
                            key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                        }
                    }

                    step {
                        fromExpr = "abs[2]"
                        toExpr = "2"
                        explanation {
                            key = GeneralExplanation.ResolveAbsoluteValueOfNonNegativeValue
                        }
                    }
                }

                step {
                    fromExpr = "2 - abs[1 - [6 / 5]] + abs[2 - 1 - 1]"
                    toExpr = "2 - [1 / 5] + abs[2 - 1 - 1]"
                    explanation {
                        key = GeneralExplanation.EvaluateAbsoluteValue
                    }

                    step {
                        fromExpr = "abs[1 - [6 / 5]]"
                        toExpr = "abs[-[1 / 5]]"
                        explanation {
                            key = FractionArithmeticExplanation.AddIntegerAndFraction
                        }
                    }

                    step {
                        fromExpr = "abs[-[1 / 5]]"
                        toExpr = "[1 / 5]"
                        explanation {
                            key = GeneralExplanation.ResolveAbsoluteValueOfNonPositiveValue
                        }
                    }
                }

                step {
                    fromExpr = "2 - [1 / 5] + abs[2 - 1 - 1]"
                    toExpr = "2 - [1 / 5] + 0"
                    explanation {
                        key = GeneralExplanation.EvaluateAbsoluteValue
                    }

                    step {
                        fromExpr = "abs[2 - 1 - 1]"
                        toExpr = "abs[0]"
                        explanation {
                            key = IntegerArithmeticExplanation.SimplifyIntegersInSum
                        }
                    }

                    step {
                        fromExpr = "abs[0]"
                        toExpr = "0"
                        explanation {
                            key = GeneralExplanation.ResolveAbsoluteValueOfZero
                        }
                    }
                }

                step {
                    fromExpr = "2 - [1 / 5] + 0"
                    toExpr = "2 - [1 / 5]"
                    explanation {
                        key = GeneralExplanation.EliminateZeroInSum
                    }
                }

                step {
                    fromExpr = "2 - [1 / 5]"
                    toExpr = "[9 / 5]"
                    explanation {
                        key = FractionArithmeticExplanation.AddIntegerAndFraction
                    }
                }
            }
        }

    @Test
    fun `test absolute values left to right`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "abs[2 * [1 / 4]] * abs[-2 - [2 / 3]] : abs[[5 / 2] - [1 / 4]]"
            check {
                fromExpr = "abs[2 * [1 / 4]] * abs[-2 - [2 / 3]] : abs[[5 / 2] - [1 / 4]]"
                toExpr = "[16 / 27]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }
                step {
                    fromExpr = "abs[2 * [1 / 4]] * abs[-2 - [2 / 3]] : abs[[5 / 2] - [1 / 4]]"
                    toExpr = "[1 / 2] * abs[-2 - [2 / 3]] : abs[[5 / 2] - [1 / 4]]"
                    explanation {
                        key = GeneralExplanation.EvaluateAbsoluteValue
                    }
                    step {
                        fromExpr = "abs[2 * [1 / 4]]"
                        toExpr = "abs[[1 / 2]]"
                        explanation {
                            key = FractionArithmeticExplanation.MultiplyAndSimplifyFractions
                        }
                    }
                    step {
                        fromExpr = "abs[[1 / 2]]"
                        toExpr = "[1 / 2]"
                        explanation {
                            key = GeneralExplanation.ResolveAbsoluteValueOfNonNegativeValue
                        }
                    }
                }
                step {
                    fromExpr = "[1 / 2] * abs[-2 - [2 / 3]] : abs[[5 / 2] - [1 / 4]]"
                    toExpr = "[1 / 2] * [8 / 3] : abs[[5 / 2] - [1 / 4]]"
                    explanation {
                        key = GeneralExplanation.EvaluateAbsoluteValue
                    }
                    step {
                        fromExpr = "abs[-2 - [2 / 3]]"
                        toExpr = "abs[-[8 / 3]]"
                        explanation {
                            key = FractionArithmeticExplanation.AddIntegerAndFraction
                        }
                    }
                    step {
                        fromExpr = "abs[-[8 / 3]]"
                        toExpr = "[8 / 3]"
                        explanation {
                            key = GeneralExplanation.ResolveAbsoluteValueOfNonPositiveValue
                        }
                    }
                }
                step {
                    fromExpr = "[1 / 2] * [8 / 3] : abs[[5 / 2] - [1 / 4]]"
                    toExpr = "[1 / 2] * [8 / 3] : [9 / 4]"
                    explanation {
                        key = GeneralExplanation.EvaluateAbsoluteValue
                    }
                    step {
                        fromExpr = "abs[[5 / 2] - [1 / 4]]"
                        toExpr = "abs[[9 / 4]]"
                        explanation {
                            key = FractionArithmeticExplanation.AddFractions
                        }
                    }
                    step {
                        fromExpr = "abs[[9 / 4]]"
                        toExpr = "[9 / 4]"
                        explanation {
                            key = GeneralExplanation.ResolveAbsoluteValueOfNonNegativeValue
                        }
                    }
                }
                step {
                    fromExpr = "[1 / 2] * [8 / 3] : [9 / 4]"
                    toExpr = "[1 / 2] * [8 / 3] * [4 / 9]"
                    explanation {
                        key = FractionArithmeticExplanation.RewriteDivisionAsMultiplicationByReciprocal
                    }
                }
                step {
                    fromExpr = "[1 / 2] * [8 / 3] * [4 / 9]"
                    toExpr = "[16 / 27]"
                    explanation {
                        key = FractionArithmeticExplanation.MultiplyAndSimplifyFractions
                    }
                }
            }
        }

    @Test
    fun `test simplify abs(definitelyNotNegativeValueContainingRoot)`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "abs[2 - sqrt[2]]"

            check {
                fromExpr = "abs[2 - sqrt[2]]"
                toExpr = "2 - sqrt[2]"
                explanation {
                    key = GeneralExplanation.ResolveAbsoluteValueOfNonNegativeValue
                }
            }
        }

    @Test
    fun `test simplify abs(definitelyNegativeValueContainingRoot)`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "abs[-2 + sqrt[2]]"

            check {
                fromExpr = "abs[-2 + sqrt[2]]"
                toExpr = "2 - sqrt[2]"
                explanation {
                    key = GeneralExplanation.ResolveAbsoluteValueOfNonPositiveValue
                }
            }
        }

    @Test
    fun `test simplify abs(negOf definitelyPositiveValueContainingRoot)`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "abs[-(2 - sqrt[2])]"

            check {
                fromExpr = "abs[-(2 - sqrt[2])]"
                toExpr = "2 - sqrt[2]"
                explanation {
                    key = GeneralExplanation.EvaluateAbsoluteValue
                }
                step {
                    fromExpr = "abs[-(2 - sqrt[2])]"
                    toExpr = "abs[-2 + sqrt[2]]"
                    explanation {
                        key = ExpandExplanation.DistributeNegativeOverBracket
                    }
                }
                step {
                    fromExpr = "abs[-2 + sqrt[2]]"
                    toExpr = "2 - sqrt[2]"
                    explanation {
                        key = GeneralExplanation.ResolveAbsoluteValueOfNonPositiveValue
                    }
                }
            }
        }

    @Test
    fun `test simplify abs(negOf definitelyNegativeValueContainingRoot)`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "abs[-(-2 + sqrt[2])]"

            check {
                fromExpr = "abs[-(-2 + sqrt[2])]"
                toExpr = "2 - sqrt[2]"
                explanation {
                    key = GeneralExplanation.EvaluateAbsoluteValue
                }
                step {
                    fromExpr = "abs[-(-2 + sqrt[2])]"
                    toExpr = "abs[2 - sqrt[2]]"
                    explanation {
                        key = ExpandExplanation.DistributeNegativeOverBracket
                    }
                }
                step {
                    fromExpr = "abs[2 - sqrt[2]]"
                    toExpr = "2 - sqrt[2]"
                    explanation {
                        key = GeneralExplanation.ResolveAbsoluteValueOfNonNegativeValue
                    }
                }
            }
        }
}
