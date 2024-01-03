package engine.expressions

import engine.context.Context
import engine.methods.DummyKey
import engine.methods.Method
import engine.methods.plan
import engine.methods.testMethod
import engine.steps.Transformation
import org.junit.jupiter.api.Test
import parser.parseExpression

class TestSubstituteMethod(val f: (Expression) -> Pair<Expression, Expression>) : Method {
    override fun tryExecute(ctx: Context, sub: Expression): Transformation {
        val (subExpr, newExpr) = f(sub)
        return Transformation(
            type = Transformation.Type.Rule,
            fromExpr = subExpr,
            toExpr = newExpr,
        )
    }
}

fun appliedSubstituteMethod(f: (Expression) -> Pair<Expression, Expression>) =
    plan {
        explanation = DummyKey("SubstitutionMethod")

        steps {
            apply(TestSubstituteMethod(f))
        }
    }

class SubstituteTest {
    @Test
    fun testSimpleSubstitution() =
        testMethod {
            method = appliedSubstituteMethod { it.firstChild to parseExpression("y") }
            inputExpr = "x + 1"

            check {
                toExpr = "y + 1"

                keep("./1")
                introduce("./0")
            }
        }

    @Test
    fun testSimpleSubstitution2() =
        testMethod {
            method = appliedSubstituteMethod { it.firstChild.secondChild to Constants.One }
            inputExpr = "(x + y)(x - y)(t - 1)"

            check {
                toExpr = "(x + 1)(x - y)(t - 1)"

                keep("./0/0", "./1", "./2")
                introduce("./0/1")
            }
        }

    @Test
    fun testSwap() =
        testMethod {
            method = appliedSubstituteMethod {
                val second = it.secondChild
                val swappedSum = sumOf(
                    second.secondChild.withOrigin(Move(second.secondChild)),
                    second.firstChild.withOrigin(Move(second.firstChild)),
                )

                second to swappedSum
            }
            inputExpr = "2(5x + 2y)"

            check {
                toExpr = "2(2y + 5x)"

                keep("./0")
                move("./1/0", "./1/1")
                move("./1/1", "./1/0")
            }
        }

    @Test
    fun testCombine() =
        testMethod {
            method = appliedSubstituteMethod {
                it to Constants.Two.withOrigin(Combine(it.firstChild, it.secondChild))
            }
            inputExpr = "1 + 1"

            check {
                toExpr = "2"

                combine {
                    fromPaths("./0", "./1", "./1:outerOp")
                    toPaths(".")
                }
            }
        }

    @Test
    fun testFactor() =
        testMethod {
            method = appliedSubstituteMethod {
                it to productOf(
                    it.firstChild.secondChild.withOrigin(Factor(it.firstChild.secondChild, it.secondChild.secondChild)),
                    sumOf(
                        it.firstChild.firstChild.withOrigin(Move(it.firstChild.firstChild)),
                        it.secondChild.firstChild.withOrigin(Move(it.secondChild.firstChild)),
                    ),
                )
            }
            inputExpr = "2x + [1/3]x"

            check {
                toExpr = "x(2 + [1/3])"

                move("./0/0", "./1/0")
                move("./1/0", "./1/1")
                factor {
                    fromPaths("./0/1", "./1/1")
                    toPaths("./0")
                }
            }
        }

    @Test
    fun testDistribute() =
        testMethod {
            method = appliedSubstituteMethod {
                val product = it.firstChild
                val x = product.firstChild.withOrigin(Distribute(product.firstChild))
                val sum = product.secondChild
                it to sumOf(
                    productOf(x, sum.firstChild.withOrigin(Move(sum.firstChild))),
                    productOf(sum.secondChild.withOrigin(Move(sum.secondChild)), x),
                    it.secondChild,
                )
            }
            inputExpr = "x(sqrt[3] + 3) + 1"

            check {
                toExpr = "x sqrt[3] + 3x + 1"

                distribute {
                    fromPaths("./0/0")
                    toPaths("./0/0", "./1/1")
                }
                move("./0/1/0", "./0/1")

                move("./0/1/1", "./1/0")

                shift("./1", "./2")
            }
        }

    @Test
    fun testRewrittenProduct() =
        testMethod {
            method = appliedSubstituteMethod {
                val x = it.secondChild.secondChild
                x to Constants.Two.withOrigin(Combine(x))
            }
            inputExpr = "[1/2x] + 2xy"

            check {
                toExpr = "[1/2x] + 2*2y"

                keep("./0", "./1/0")
                transform("./1/1", "./1/1")
                shift("./1/2", "./1/2")
            }
        }

    @Test
    fun testOuterBracketsRemoved() =
        testMethod {
            method = appliedSubstituteMethod { it to it }
            inputExpr = "(x + 1)"

            check {
                toExpr = "x + 1"
            }
        }
}
