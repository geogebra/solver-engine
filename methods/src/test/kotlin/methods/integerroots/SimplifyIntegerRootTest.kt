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

package methods.integerroots

import engine.methods.testMethod
import methods.general.GeneralExplanation
import methods.integerarithmetic.IntegerArithmeticExplanation
import org.junit.jupiter.api.Test

class SimplifyIntegerRootTest {
    @Test
    fun `doesn't apply for irreducible root`() =
        testMethod {
            method = IntegerRootsPlans.SimplifyIntegerRoot
            inputExpr = "root[211, 3]"

            check { noTransformation() }
        }

    @Test
    fun `doesn't apply for smart-factorisable irreducible root`() =
        testMethod {
            method = IntegerRootsPlans.SimplifyIntegerRoot
            inputExpr = "root[1000, 4]"

            check { noTransformation() }
        }

    @Test
    fun `perfect square sqrt(49)`() =
        testMethod {
            method = IntegerRootsPlans.SimplifyIntegerRoot
            inputExpr = "sqrt[49]"

            check {
                fromExpr = "sqrt[49]"
                toExpr = "7"
                explanation {
                    key = IntegerRootsExplanation.SimplifyIntegerRoot
                }

                step {
                    fromExpr = "sqrt[49]"
                    toExpr = "sqrt[[7 ^ 2]]"
                    explanation {
                        key = IntegerRootsExplanation.WriteRootAsRootPower
                    }
                }

                step {
                    fromExpr = "sqrt[[7 ^ 2]]"
                    toExpr = "7"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                    }
                }
            }
        }

    @Test
    fun `perfect cube root(216, 3)`() =
        testMethod {
            method = IntegerRootsPlans.SimplifyIntegerRoot
            inputExpr = "root[216, 3]"

            check {
                fromExpr = "root[216, 3]"
                toExpr = "6"
                explanation {
                    key = IntegerRootsExplanation.SimplifyIntegerRootToInteger
                }

                step {
                    fromExpr = "root[216, 3]"
                    toExpr = "root[[2 ^ 3] * [3 ^ 3], 3]"
                    explanation {
                        key = IntegerRootsExplanation.FactorizeIntegerUnderRoot
                    }
                }

                step {
                    fromExpr = "root[[2 ^ 3] * [3 ^ 3], 3]"
                    toExpr = "root[[2 ^ 3], 3] * root[[3 ^ 3], 3]"
                    explanation {
                        key = IntegerRootsExplanation.SplitRootOfProduct
                    }
                }

                step {
                    fromExpr = "root[[2 ^ 3], 3] * root[[3 ^ 3], 3]"
                    toExpr = "2 root[[3 ^ 3], 3]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyPowerOfIntegerUnderRoot
                    }
                }

                step {
                    fromExpr = "2 root[[3 ^ 3], 3]"
                    toExpr = "2 * 3"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyPowerOfIntegerUnderRoot
                    }
                }

                step {
                    fromExpr = "2 * 3"
                    toExpr = "6"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                    }
                }
            }
        }

    @Test
    fun `smart factorization sqrt(100)`() =
        testMethod {
            method = IntegerRootsPlans.SimplifyIntegerRoot
            inputExpr = "sqrt[100]"

            check {
                fromExpr = "sqrt[100]"
                toExpr = "10"
                explanation {
                    key = IntegerRootsExplanation.SimplifyIntegerRoot
                }

                step {
                    fromExpr = "sqrt[100]"
                    toExpr = "sqrt[[10 ^ 2]]"
                    explanation {
                        key = IntegerRootsExplanation.WriteRootAsRootPower
                    }
                }

                step {
                    fromExpr = "sqrt[[10 ^ 2]]"
                    toExpr = "10"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                    }
                }
            }
        }

    @Test
    fun `smart factorization sqrt(1000)`() =
        testMethod {
            method = IntegerRootsPlans.SimplifyIntegerRoot
            inputExpr = "sqrt[1000]"

            check {
                fromExpr = "sqrt[1000]"
                toExpr = "10 sqrt[10]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyIntegerRoot
                }

                step {
                    fromExpr = "sqrt[1000]"
                    toExpr = "sqrt[[10 ^ 3]]"
                    explanation {
                        key = IntegerRootsExplanation.WriteRootAsRootPower
                    }
                }

                step {
                    fromExpr = "sqrt[[10 ^ 3]]"
                    toExpr = "10 sqrt[10]"
                    explanation {
                        key = IntegerRootsExplanation.SplitAndCancelRootOfPower
                    }
                }
            }
        }

    @Test
    fun `smart factorization root(10000, 3)`() =
        testMethod {
            method = IntegerRootsPlans.SimplifyIntegerRoot
            inputExpr = "root[10000, 3]"

            check {
                fromExpr = "root[10000, 3]"
                toExpr = "10 root[10, 3]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyIntegerRoot
                }

                step {
                    fromExpr = "root[10000, 3]"
                    toExpr = "root[[10 ^ 4], 3]"
                    explanation {
                        key = IntegerRootsExplanation.WriteRootAsRootPower
                    }
                }

                step {
                    fromExpr = "root[[10 ^ 4], 3]"
                    toExpr = "10 root[10, 3]"
                    explanation {
                        key = IntegerRootsExplanation.SplitAndCancelRootOfPower
                    }
                }
            }
        }

    @Test
    fun `smart factorization root(1000000, 3)`() =
        testMethod {
            method = IntegerRootsPlans.SimplifyIntegerRoot
            inputExpr = "root[1000000, 3]"

            check {
                fromExpr = "root[1000000, 3]"
                toExpr = "100"
                explanation {
                    key = IntegerRootsExplanation.SimplifyIntegerRoot
                }

                step {
                    fromExpr = "root[1000000, 3]"
                    toExpr = "root[[100 ^ 3], 3]"
                    explanation {
                        key = IntegerRootsExplanation.WriteRootAsRootPower
                    }
                }

                step {
                    fromExpr = "root[[100 ^ 3], 3]"
                    toExpr = "100"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                    }
                }
            }
        }

    @Test
    fun `smart factorization sqrt(144)`() =
        testMethod {
            method = IntegerRootsPlans.SimplifyIntegerRoot
            inputExpr = "sqrt[144]"

            check {
                fromExpr = "sqrt[144]"
                toExpr = "12"
                explanation {
                    key = IntegerRootsExplanation.SimplifyIntegerRoot
                }

                step {
                    fromExpr = "sqrt[144]"
                    toExpr = "sqrt[[12 ^ 2]]"
                    explanation {
                        key = IntegerRootsExplanation.WriteRootAsRootPower
                    }
                }

                step {
                    fromExpr = "sqrt[[12 ^ 2]]"
                    toExpr = "12"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyNthRootOfNthPower
                    }
                }
            }
        }

    @Test
    fun `smart factorization sqrt(8100)`() =
        testMethod {
            method = IntegerRootsPlans.SimplifyIntegerRoot
            inputExpr = "sqrt[8100]"

            check {
                fromExpr = "sqrt[8100]"
                toExpr = "90"
                explanation {
                    key = IntegerRootsExplanation.SimplifyIntegerRootToInteger
                }

                step {
                    fromExpr = "sqrt[8100]"
                    toExpr = "sqrt[81 * 100]"
                    explanation {
                        key = IntegerRootsExplanation.WriteRootAsRootProduct
                    }
                }

                step {
                    fromExpr = "sqrt[81 * 100]"
                    toExpr = "sqrt[81] * sqrt[100]"
                    explanation {
                        key = IntegerRootsExplanation.SplitRootOfProduct
                    }
                }

                step {
                    fromExpr = "sqrt[81] * sqrt[100]"
                    toExpr = "9 sqrt[100]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyIntegerRoot
                    }
                }

                step {
                    fromExpr = "9 sqrt[100]"
                    toExpr = "9 * 10"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyIntegerRoot
                    }
                }

                step {
                    fromExpr = "9 * 10"
                    toExpr = "90"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                    }
                }
            }
        }

    @Test
    fun `smart factorization root(27000, 3)`() =
        testMethod {
            method = IntegerRootsPlans.SimplifyIntegerRoot
            inputExpr = "root[27000, 3]"

            check {
                fromExpr = "root[27000, 3]"
                toExpr = "30"
                explanation {
                    key = IntegerRootsExplanation.SimplifyIntegerRootToInteger
                }

                step {
                    fromExpr = "root[27000, 3]"
                    toExpr = "root[27 * 1000, 3]"
                    explanation {
                        key = IntegerRootsExplanation.WriteRootAsRootProduct
                    }
                }

                step {
                    fromExpr = "root[27 * 1000, 3]"
                    toExpr = "root[27, 3] * root[1000, 3]"
                    explanation {
                        key = IntegerRootsExplanation.SplitRootOfProduct
                    }
                }

                step {
                    fromExpr = "root[27, 3] * root[1000, 3]"
                    toExpr = "3 root[1000, 3]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyIntegerRoot
                    }
                }

                step {
                    fromExpr = "3 root[1000, 3]"
                    toExpr = "3 * 10"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyIntegerRoot
                    }
                }

                step {
                    fromExpr = "3 * 10"
                    toExpr = "30"
                    explanation {
                        key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                    }
                }
            }
        }

    @Test
    fun `smart factorization root(9, 4)`() =
        testMethod {
            method = IntegerRootsPlans.SimplifyIntegerRoot
            inputExpr = "root[9, 4]"

            check {
                toExpr = "sqrt[3]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyIntegerRoot
                }

                step {
                    fromExpr = "root[9, 4]"
                    toExpr = "root[[3 ^ 2], 4]"
                    explanation {
                        key = IntegerRootsExplanation.FactorizeIntegerUnderRoot
                    }
                }

                step {
                    fromExpr = "root[[3 ^ 2], 4]"
                    toExpr = "sqrt[3]"
                    explanation {
                        key = IntegerRootsExplanation.RewriteAndCancelPowerUnderRoot
                    }

                    step {
                        fromExpr = "root[[3 ^ 2], 4]"
                        toExpr = "root[[3 ^ 2], 2 * 2]"
                        explanation {
                            key = GeneralExplanation.RewritePowerUnderRoot
                        }
                    }

                    step {
                        fromExpr = "root[[3 ^ 2], 2 * 2]"
                        toExpr = "sqrt[3]"
                        explanation {
                            key = GeneralExplanation.CancelRootIndexAndExponent
                        }
                    }
                }
            }
        }

    @Test
    fun `factoring into multiple factors with the same power root(7776, 10)`() =
        testMethod {
            method = IntegerRootsPlans.SimplifyIntegerRoot
            inputExpr = "root[7776, 10]"

            check {
                fromExpr = "root[7776, 10]"
                toExpr = "sqrt[6]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyIntegerRoot
                }

                step {
                    fromExpr = "root[7776, 10]"
                    toExpr = "root[[2 ^ 5] * [3 ^ 5], 10]"
                    explanation {
                        key = IntegerRootsExplanation.FactorizeIntegerUnderRoot
                    }
                }

                step {
                    fromExpr = "root[[2 ^ 5] * [3 ^ 5], 10]"
                    toExpr = "root[[2 ^ 5], 10] * root[[3 ^ 5], 10]"
                    explanation {
                        key = IntegerRootsExplanation.SplitRootOfProduct
                    }
                }

                step {
                    fromExpr = "root[[2 ^ 5], 10] * root[[3 ^ 5], 10]"
                    toExpr = "sqrt[2] * root[[3 ^ 5], 10]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyPowerOfIntegerUnderRoot
                    }
                }

                step {
                    fromExpr = "sqrt[2] * root[[3 ^ 5], 10]"
                    toExpr = "sqrt[2] * sqrt[3]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyPowerOfIntegerUnderRoot
                    }
                }

                step {
                    fromExpr = "sqrt[2] * sqrt[3]"
                    toExpr = "sqrt[6]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyProductWithRoots
                    }
                }
            }
        }

    @Test
    fun `factoring into multiple factors with different powers sqrt(113400)`() =
        testMethod {
            method = IntegerRootsPlans.SimplifyIntegerRoot
            inputExpr = "sqrt[113400]"

            check {
                fromExpr = "sqrt[113400]"
                toExpr = "90 sqrt[14]"
                explanation {
                    key = IntegerRootsExplanation.SimplifyIntegerRoot
                }

                step {
                    fromExpr = "sqrt[113400]"
                    toExpr = "sqrt[[2 ^ 3] * [3 ^ 4] * [5 ^ 2] * 7]"
                    explanation {
                        key = IntegerRootsExplanation.FactorizeIntegerUnderRoot
                    }
                }

                step {
                    fromExpr = "sqrt[[2 ^ 3] * [3 ^ 4] * [5 ^ 2] * 7]"
                    toExpr = "sqrt[[2 ^ 3]] * sqrt[[3 ^ 4]] * sqrt[[5 ^ 2]] * sqrt[7]"
                    explanation {
                        key = IntegerRootsExplanation.SplitRootOfProduct
                    }
                }

                step {
                    fromExpr = "sqrt[[2 ^ 3]] * sqrt[[3 ^ 4]] * sqrt[[5 ^ 2]] * sqrt[7]"
                    toExpr = "<.2 sqrt[2].> * sqrt[[3 ^ 4]] * sqrt[[5 ^ 2]] * sqrt[7]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyPowerOfIntegerUnderRoot
                    }
                }

                step {
                    fromExpr = "<.2 sqrt[2].> * sqrt[[3 ^ 4]] * sqrt[[5 ^ 2]] * sqrt[7]"
                    toExpr = "<.2 sqrt[2].> * 9 sqrt[[5 ^ 2]] * sqrt[7]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyPowerOfIntegerUnderRoot
                    }
                }

                step {
                    fromExpr = "<.2 sqrt[2].> * 9 sqrt[[5 ^ 2]] * sqrt[7]"
                    toExpr = "<.2 sqrt[2].> * 9 * 5 sqrt[7]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyPowerOfIntegerUnderRoot
                    }
                }

                step {
                    fromExpr = "2 sqrt[2] * 9 * 5 sqrt[7]"
                    toExpr = "90 sqrt[14]"
                    explanation {
                        key = IntegerRootsExplanation.SimplifyProductWithRoots
                    }
                }
            }
        }
}
