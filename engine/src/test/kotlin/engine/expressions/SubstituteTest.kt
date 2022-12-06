package engine.expressions

import engine.context.Context
import engine.methods.Method
import engine.methods.testMethod
import engine.steps.Transformation
import org.junit.jupiter.api.Test
import parser.parseExpression

class TestSubstituteMethod(val f: (Expression) -> Pair<Expression, Expression>) : Method {
    override fun tryExecute(ctx: Context, sub: Expression): Transformation {
        val (subExpr, newExpr) = f(sub)
        val toExpr = sub.substitute(subExpr, newExpr)
        return Transformation(
            fromExpr = sub,
            toExpr = toExpr
        )
    }
}

class SubstituteTest {

    @Test
    fun testSimpleSubstitution() = testMethod {
        method = TestSubstituteMethod { it.firstChild to parseExpression("y") }
        inputExpr = "x + 1"

        check {
            toExpr = "y + 1"

            introduce("./0")
            keep("./1")
        }
    }

    @Test
    fun testSimpleSubstitution2() = testMethod {
        method = TestSubstituteMethod { it.firstChild.secondChild to Constants.One }
        inputExpr = "(x + y)(x - y)(t - 1)"

        check {
            toExpr = "(x + 1)(x - y)(t - 1)"

            introduce("./0/1")
            keep("./0/0", "./1", "./2")
        }
    }

    @Test
    fun testSwap() = testMethod {
        method = TestSubstituteMethod {
            val second = it.secondChild
            second to sumOf(second.secondChild, second.firstChild)
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
    fun testCombine() = testMethod {
        method = TestSubstituteMethod {
            it to Constants.Two.withOrigin(Combine(it.firstChild, it.secondChild))
        }
        inputExpr = "1 + 1"

        check {
            toExpr = "2"

            combine {
                fromPaths("./0", "./1")
                toPaths(".")
            }
        }
    }

    @Test
    fun testFactor() = testMethod {
        method = TestSubstituteMethod {
            it to productOf(
                it.firstChild.secondChild.withOrigin(Factor(it.firstChild.secondChild, it.secondChild.secondChild)),
                sumOf(it.firstChild.firstChild, it.secondChild.firstChild)
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
    fun testDistribute() = testMethod {
        method = TestSubstituteMethod {
            val product = it.firstChild
            val x = product.firstChild.withOrigin(Distribute(product.firstChild))
            val sum = product.secondChild
            it to sumOf(productOf(x, sum.firstChild), productOf(sum.secondChild, x), it.secondChild)
        }
        inputExpr = "x(sqrt[3] + 3) + 1"

        check {
            toExpr = "x*sqrt[3] + 3x + 1"

            distribute {
                fromPaths("./0/0")
                toPaths("./0/0")
            }
            move("./0/1/0", "./0/1")

            move("./0/1/1", "./1/0")
            distribute {
                fromPaths("./0/0")
                toPaths("./1/1")
            }

            move("./1", "./2")
        }
    }

    @Test
    fun testRewrittenProduct() = testMethod {
        method = TestSubstituteMethod {
            val x = it.secondChild.secondChild
            x to Constants.Two.withOrigin(Combine(x))
        }
        inputExpr = "[1/2x] + 2xy"

        check {
            toExpr = "[1/2x] + 2*2y"

            keep("./0")
            move("./1/0", "./1/0")
            transform("./1/1", "./1/1/0")
            move("./1/2", "./1/1/1")
        }
    }

    @Test
    fun testOuterBracketsRemoved() = testMethod {
        method = TestSubstituteMethod { it to it }
        inputExpr = "(x + 1)"

        check {
            toExpr = "x + 1"

            keep(".")
        }
    }
}
