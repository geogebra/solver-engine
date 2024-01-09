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
import methods.general.GeneralExplanation
import org.junit.jupiter.api.Test

class ExpandConstantExpressionsTest {
    @Test
    fun `test expanding single bracket with positive factor on the right`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "(1 + sqrt[2]) * 2"

            check {
                fromExpr = "(1 + sqrt[2]) * 2"
                toExpr = "2 + 2 sqrt[2]"
                explanation {
                    key = ExpandExplanation.ExpandSingleBracketAndSimplify
                }

                step {
                    fromExpr = "(1 + sqrt[2]) * 2"
                    toExpr = "1 * 2 + sqrt[2] * 2"
                    explanation {
                        key = ExpandExplanation.DistributeMultiplicationOverSum
                    }
                }

                step {
                    fromExpr = "1 * 2 + sqrt[2] * 2"
                    toExpr = "2 + 2 sqrt[2]"
                    explanation {
                        key = ConstantExpressionsExplanation.SimplifyConstantExpression
                    }
                }
            }
        }

    @Test
    fun `test expanding single bracket with negative factor on the right`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "(1 + sqrt[2]) * (-2)"

            check {
                fromExpr = "(1 + sqrt[2]) * (-2)"
                toExpr = "-2 - 2 sqrt[2]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "(1 + sqrt[2]) * (-2)"
                    toExpr = "(-2) (1 + sqrt[2])"
                    explanation {
                        key = GeneralExplanation.ReorderProduct
                    }
                }

                step {
                    fromExpr = "(-2) (1 + sqrt[2])"
                    toExpr = "-2 (1 + sqrt[2])"
                    explanation {
                        key = GeneralExplanation.MoveSignOfNegativeFactorOutOfProduct
                    }
                }

                step {
                    fromExpr = "-2 (1 + sqrt[2])"
                    toExpr = "-2 - 2 sqrt[2]"
                    explanation {
                        key = ExpandExplanation.ExpandSingleBracketAndSimplify
                    }

                    step {
                        fromExpr = "-2 (1 + sqrt[2])"
                        toExpr = "-2 * 1 - 2 * sqrt[2]"
                        explanation {
                            key = ExpandExplanation.DistributeMultiplicationOverSum
                            param { expr = "-2" }
                        }
                    }

                    step {
                        fromExpr = "-2 * 1 - 2 * sqrt[2]"
                        toExpr = "-2 - 2 sqrt[2]"
                        explanation {
                            key = ConstantExpressionsExplanation.SimplifyConstantExpression
                        }
                    }
                }
            }
        }

    @Test
    fun `test expanding single bracket in negated product`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "(1 + sqrt[2]) * (-2)"

            check {
                fromExpr = "(1 + sqrt[2]) * (-2)"
                toExpr = "-2 - 2 sqrt[2]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "(1 + sqrt[2]) * (-2)"
                    toExpr = "(-2) (1 + sqrt[2])"
                    explanation {
                        key = GeneralExplanation.ReorderProduct
                    }
                }

                step {
                    fromExpr = "(-2) (1 + sqrt[2])"
                    toExpr = "-2 (1 + sqrt[2])"
                    explanation {
                        key = GeneralExplanation.MoveSignOfNegativeFactorOutOfProduct
                    }
                }

                step {
                    fromExpr = "-2 (1 + sqrt[2])"
                    toExpr = "-2 - 2 sqrt[2]"
                    explanation {
                        key = ExpandExplanation.ExpandSingleBracketAndSimplify
                    }

                    step {
                        fromExpr = "-2 (1 + sqrt[2])"
                        toExpr = "-2 * 1 - 2 * sqrt[2]"
                        explanation {
                            key = ExpandExplanation.DistributeMultiplicationOverSum
                        }
                    }

                    step {
                        fromExpr = "-2 * 1 - 2 * sqrt[2]"
                        toExpr = "-2 - 2 sqrt[2]"
                        explanation {
                            key = ConstantExpressionsExplanation.SimplifyConstantExpression
                        }
                    }
                }
            }
        }

    @Test
    fun `test expanding single bracket with factors on both sides`() =
        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "2 (1 + sqrt[2]) sqrt[2]"

            check {
                fromExpr = "2 (1 + sqrt[2]) sqrt[2]"
                toExpr = "2 sqrt[2] + 4"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    fromExpr = "2 (1 + sqrt[2]) sqrt[2]"
                    toExpr = "2 sqrt[2] (1 + sqrt[2])"
                    explanation {
                        key = GeneralExplanation.ReorderProduct
                    }
                }

                step {
                    fromExpr = "2 sqrt[2] (1 + sqrt[2])"
                    toExpr = "2 sqrt[2] + 4"
                    explanation {
                        key = ExpandExplanation.ExpandSingleBracketAndSimplify
                    }

                    step {
                        fromExpr = "2 sqrt[2] (1 + sqrt[2])"
                        toExpr = "<.2 sqrt[2].> * 1 + <.2 sqrt[2].> * sqrt[2]"
                        explanation {
                            key = ExpandExplanation.DistributeMultiplicationOverSum
                        }
                    }

                    step {
                        fromExpr = "2 sqrt[2] * 1 + 2 sqrt[2] * sqrt[2]"
                        toExpr = "2 sqrt[2] + 4"
                        explanation {
                            key = ConstantExpressionsExplanation.SimplifyConstantExpression
                        }
                    }
                }
            }
        }
}
