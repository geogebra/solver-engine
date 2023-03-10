package methods.integerroots

import engine.methods.testMethod
import methods.constantexpressions.ConstantExpressionsPlans
import methods.integerarithmetic.IntegerArithmeticExplanation
import org.junit.jupiter.api.Test

class IntegerRootsPlansTest {
    @Test
    fun testSimplifyIntegerRootsTest() = testMethod {
        method = IntegerRootsPlans.SimplifyIntegerRoot
        inputExpr = "sqrt[113400]"

        check {
            toExpr = "90 sqrt[14]"

            step {
                fromExpr = "sqrt[113400]"
                toExpr = "sqrt[[2^3] * [3^4] * [5^2] * 7]"
            }

            step {
                toExpr = "sqrt[[2^3]] * sqrt[[3^4]] * sqrt[[5^2]] * sqrt[7]"
            }

            step {
                fromExpr = "sqrt[[2 ^ 3]] * sqrt[[3 ^ 4]] * sqrt[[5 ^ 2]] * sqrt[7]"
                toExpr = "sqrt[[2 ^ 2]] * sqrt[2] * sqrt[[3 ^ 4]] * sqrt[[5 ^ 2]] * sqrt[7]"
            }

            step {
                toExpr = "2 sqrt[2] * [3 ^ 2] * 5 sqrt[7]"
            }

            step {
                toExpr = "90 sqrt[14]"
            }
        }
    }

    @Test
    fun testSimplifyIntegerRootsPerfectSquareSquareRoot() = testMethod {
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
    fun testSimplifyIntegerRootsSquareRootPowerOfTenEven() = testMethod {
        method = IntegerRootsPlans.SimplifyIntegerRoot
        inputExpr = "sqrt[100]"

        check {
            toExpr = "10"

            step { toExpr = "sqrt[[10 ^ 2]]" }

            step { toExpr = "10" }
        }
    }

    @Test
    fun testSimplifyIntegerRootsSquareRootPowerOfTenOdd() = testMethod {
        method = IntegerRootsPlans.SimplifyIntegerRoot
        inputExpr = "sqrt[1000]"

        check {
            toExpr = "10 sqrt[10]"

            step { toExpr = "sqrt[[10 ^ 3]]" }

            step { toExpr = "sqrt[[10^2]] * sqrt[10]" }

            step { toExpr = "10 sqrt[10]" }
        }
    }

    @Test
    fun testSimplifyIntegerRootsHigherOrderRootPowerOfHundredEven() = testMethod {
        method = IntegerRootsPlans.SimplifyIntegerRoot
        inputExpr = "root[1000000, 3]"

        check {
            toExpr = "100"

            step { toExpr = "root[[100 ^ 3], 3]" }

            step { toExpr = "100" }
        }
    }

    @Test
    fun testSimplifyIntegerRootsHigherOrderRootPowerOfHundredOdd() = testMethod {
        method = IntegerRootsPlans.SimplifyIntegerRoot
        inputExpr = "root[10000, 3]"

        check {
            toExpr = "10 root[10, 3]"

            step { toExpr = "root[[10^4], 3]" }

            step { toExpr = "root[[10^3], 3] * root[10, 3]" }

            step { toExpr = "10 root[10, 3]" }
        }
    }

    @Test
    fun testSimplifyIntegerRootNoSimplificationPowerOfTen() = testMethod {
        method = IntegerRootsPlans.SimplifyIntegerRoot
        inputExpr = "root[1000, 4]"

        check { noTransformation() }
    }

    @Test
    fun testSimplifyIntegerRootsSquareRootPerfectSquare() = testMethod {
        method = IntegerRootsPlans.SimplifyIntegerRoot
        inputExpr = "sqrt[144]"

        check {
            toExpr = "12"

            step { toExpr = "sqrt[[12 ^ 2]]" }

            step { toExpr = "12" }
        }
    }

    @Test
    fun testSimplifyIntegerRootsCubeRootPerfectCube() = testMethod {
        method = IntegerRootsPlans.SimplifyIntegerRoot
        inputExpr = "root[216, 3]"

        check {
            toExpr = "6"

            step {
                toExpr = "root[[2 ^ 3] * [3 ^ 3], 3]"
                explanation {
                    key = IntegerRootsExplanation.FactorizeIntegerUnderRoot
                }
            }

            step {
                toExpr = "root[[2 ^ 3], 3] * root[[3 ^ 3], 3]"
            }

            step {
                toExpr = "2 * 3"
            }

            step {
                fromExpr = "2 * 3"
                toExpr = "6"
            }
        }
    }

    @Test
    fun testSimplifyIntegerRootsSquareRootZerosAtEnd() = testMethod {
        method = IntegerRootsPlans.SimplifyIntegerRoot
        inputExpr = "sqrt[8100]"

        check {
            toExpr = "90"

            step {
                toExpr = "sqrt[81 * 100]"
                explanation {
                    key = IntegerRootsExplanation.WriteRootAsRootProduct
                }
            }

            step {
                toExpr = "sqrt[81] * sqrt[100]"
                explanation {
                    key = IntegerRootsExplanation.SplitRootOfProduct
                }
            }

            step {
                toExpr = "sqrt[[9 ^ 2]] * sqrt[[10 ^ 2]]"
                explanation {
                    key = IntegerRootsExplanation.WriteRootsAsRootPowers
                }
            }

            step {
                toExpr = "9 * 10"
                explanation {
                    key = IntegerRootsExplanation.CancelAllRootsOfPowers
                }
            }

            step {
                toExpr = "90"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                }
            }
        }
    }

    @Test
    fun testSimplifyIntegerRootsCubeRootZerosAtEnd() = testMethod {
        method = IntegerRootsPlans.SimplifyIntegerRoot
        inputExpr = "root[27000, 3]"

        check {
            toExpr = "30"

            step {
                toExpr = "root[27 * 1000, 3]"
                explanation {
                    key = IntegerRootsExplanation.WriteRootAsRootProduct
                }
            }

            step {
                toExpr = "root[27, 3] * root[1000, 3]"
                explanation {
                    key = IntegerRootsExplanation.SplitRootOfProduct
                }
            }

            step {
                toExpr = "root[[3 ^ 3], 3] * root[[10 ^ 3], 3]"
                explanation {
                    key = IntegerRootsExplanation.WriteRootsAsRootPowers
                }
            }
            step {
                toExpr = "3 * 10"
                explanation {
                    key = IntegerRootsExplanation.CancelAllRootsOfPowers
                }
            }

            step {
                toExpr = "30"
                explanation {
                    key = IntegerArithmeticExplanation.EvaluateIntegerProduct
                }
            }
        }
    }

    @Test
    fun testSimplifyIntegerRootsCubeRootNonPerfectCube() = testMethod {
        method = IntegerRootsPlans.SimplifyIntegerRoot
        inputExpr = "root[211, 3]"

        check { noTransformation() }
    }

    @Test
    fun testRootOfRootWithCoefficient() = testMethod {
        method = ConstantExpressionsPlans.SimplifyConstantExpression
        inputExpr = "root[3 * sqrt[4], 3]"

        check {
            toExpr = "root[6, 3]"

            step {
                toExpr = "root[3 * 2, 3]"

                step {
                    fromExpr = "sqrt[4]"
                    toExpr = "2"
                }
            }

            step {
                toExpr = "root[6, 3]"
            }
        }
    }
}

class SimplifyProductOfRootsTest {

    @Test
    fun testProductOfEqualSquareRoots() = testMethod {
        method = IntegerRootsPlans.SimplifyProductWithRoots
        inputExpr = "sqrt[6] * sqrt[6]"

        check {
            toExpr = "6"

            step {}
        }
    }

    @Test
    fun testProductOfDifferentSquareRoots() = testMethod {
        method = IntegerRootsPlans.SimplifyProductWithRoots
        inputExpr = "sqrt[6] * sqrt[3]"

        check {
            toExpr = "sqrt[18]"

            step {
                toExpr = "sqrt[6 * 3]"
            }

            step {
                toExpr = "sqrt[18]"
            }
        }
    }
}
