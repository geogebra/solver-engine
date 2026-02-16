/*
 * Copyright (c) 2025 GeoGebra GmbH, office@geogebra.org
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

package methods.angles

import engine.methods.testMethod
import methods.algebra.AlgebraPlans
import methods.constantexpressions.ConstantExpressionsExplanation
import methods.constantexpressions.ConstantExpressionsPlans
import methods.factor.FactorPlans
import methods.simplify.SimplifyPlans
import org.junit.jupiter.api.Test

class TrigonometricFunctionsPlansTest {
    @Test
    fun usePythagoreanIdentityTest() {
        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "3 [sin ^ 2 ][x] + 3 [cos ^ 2][x]"

            check {
                step {
                    toExpr = "3 ([sin ^ 2][x] + [cos ^ 2][x])"
                }

                step {
                    toExpr = "3 * 1"
                }

                step {
                    toExpr = "3"
                }
            }
        }

        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "[sin ^ 2][x] - 1 + [cos ^ 2][x]"

            check {
                step {
                    toExpr = "1 - 1"
                }

                step {
                    toExpr = "0"
                }
            }
        }

        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "-[sin ^ 2][x] - [cos ^ 2][x]"

            check {
                explanation {
                    key = AnglesExplanation.ApplyPythagoreanIdentityAndSimplify
                }

                step {
                    toExpr = "-([sin ^ 2][x] + [cos ^ 2][x])"
                }

                step {
                    toExpr = "-1"
                }
            }
        }
    }

    @Suppress("LongMethod")
    @Test
    fun reduceDoubleAngleTest() {
        testMethod {
            method = FactorPlans.FactorPolynomial
            inputExpr = "sin[2 x] + sin[x]"

            check {
                toExpr = "sin[x] (2 cos[x] + 1)"

                step {
                    toExpr = "2 sin[x] * cos[x] + sin[x]"
                }

                step {
                }
            }
        }

        testMethod {
            method = AlgebraPlans.ComputeDomainAndSimplifyAlgebraicExpression
            inputExpr = "tan[2 x] + [tan[x] / 1 - [tan ^ 2][x]]"

            check {
                toExpr = "[3 tan[x] / 1 - [tan ^ 2][x]] GIVEN [tan ^ 2][x] != 1"

                task {}

                task {
                    step {
                        toExpr = "[2 tan[x] / 1 - [tan ^ 2][x]] + [tan[x] / 1 - [tan ^ 2][x]]"
                    }

                    step {
                        toExpr = "[2 tan[x] + tan[x] / 1 - [tan ^ 2][x]]"
                    }

                    step {
                        toExpr = "[3 tan[x] / 1 - [tan ^ 2][x]]"
                    }
                }

                task {}
            }
        }

        testMethod {
            method = ConstantExpressionsPlans.SimplifyConstantExpression
            inputExpr = "tan[degree[ 33 ]] + tan[degree[ 66 ]]"

            check {
                toExpr = "[3 tan[degree[ 33 ]] - [tan ^ 3][degree[ 33 ]] / 1 - [tan ^ 2][degree[ 33 ]]]"
                explanation {
                    key = ConstantExpressionsExplanation.SimplifyConstantExpression
                }

                step {
                    toExpr = "tan[degree[ 33 ]] + [2 tan[degree[ 33 ]] / 1 - [tan ^ 2][degree[ 33 ]]]"
                    explanation {
                        key = AnglesExplanation.SimplifySumContainingDoubleAngles
                    }
                }

                step {}
            }
        }

        testMethod {
            method = SimplifyPlans.SimplifyAlgebraicExpression
            inputExpr = "cos[2 x] - sin[2 x] + 1 + 2 sin[x] * cos[x]"

            check {
                step {
                    toExpr = "-sin[2 x] + 1 + <.[cos ^ 2][x] - [sin ^ 2][x] + 2 sin[x] * cos[x].>"
                }

                step {
                    toExpr = "1 + <.-2 sin[x] * cos[x] + [cos ^ 2][x].> - [sin ^ 2][x] + 2 sin[x] * cos[x]"
                }

                step {
                    toExpr = "1 + [cos ^ 2][x] - [sin ^ 2][x]"
                }
            }
        }
    }
}
