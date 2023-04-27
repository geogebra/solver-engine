package engine.operators

import engine.utility.RecurringDecimal
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

private const val SUM_PRECEDENCE = 10
private const val PLUS_MINUS_PRECEDENCE = 15
private const val PRODUCT_PRECEDENCE = 20
private const val IMPLICIT_PRODUCT_PRECEDENCE = 40
private const val FRACTION_PRECEDENCE = 50
private const val POWER_PRECEDENCE = 60
private const val NATURAL_LOG_PRECEDENCE = 50
private const val DIVIDE_BY_PRECEDENCE = 90
private const val FUNCTION_LIKE_PRECEDENCE = 95
private const val MAX_PRECEDENCE = 100

interface ExpressionOperator : Operator {
    override val kind get() = OperatorKind.EXPRESSION

    fun eval(children: List<Double>): Double
}

abstract class NullaryOperator : ExpressionOperator {
    override val precedence = MAX_PRECEDENCE
    override val arity = ARITY_NULL

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        throw IllegalArgumentException(
            "Nullary operator ${this::class.simpleName} should have no children. " +
                "Child $op is invalid at position $n.",
        )
    }

    override fun <T> readableString(children: List<T>): String {
        require(children.isEmpty())
        return toString()
    }

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
        require(children.isEmpty())
        return latexString(ctx)
    }

    abstract fun latexString(ctx: RenderContext): String
}

/**
 * Operator representing an unsigned integer.
 */
data class IntegerOperator(val value: BigInteger) : NullaryOperator() {
    init {
        require(value.signum() >= 0)
    }

    override val name = value.toString()

    override fun toString() = value.toString()

    override fun latexString(ctx: RenderContext) = value.toString()

    override fun eval(children: List<Double>) = value.toDouble()
}

object InfinityOperator : NullaryOperator() {

    override val name = "INFINITY"

    override fun toString() = "INFINITY"

    override fun latexString(ctx: RenderContext) = "\\infty"

    override fun eval(children: List<Double>) = Double.POSITIVE_INFINITY
}

object UndefinedOperator : NullaryOperator() {

    override val name = "UNDEFINED"

    override fun toString() = "UNDEFINED"

    override fun latexString(ctx: RenderContext) = "\\text{undefined}"

    override fun eval(children: List<Double>) = Double.NaN
}

/**
 * Operator representing an unsigned terminating decimal.
 */
data class DecimalOperator(val value: BigDecimal) : NullaryOperator() {
    init {
        require(value.signum() >= 0)
    }

    override val name: String = value.toPlainString()

    override fun toString(): String = value.toPlainString()

    override fun latexString(ctx: RenderContext): String = value.toPlainString()

    override fun eval(children: List<Double>) = value.toDouble()
}

/**
 * Operator representing an unsigned recurring decimal, e.g. 1.045454545... = 1.0[45]. The [value] must include the
 * occurrence of the repeating pattern and [RecurringDecimal.repeatingDigits] is the length of the repeating pattern.
 * Examples:
 * - 0.[6] is RecurringDecimalOperator(BigDecimal("0.6", 1)
 * - 1.0[45] is RecurringDecimalOperator(BigDecimal("1.045"), 2)
 */
data class RecurringDecimalOperator(val value: RecurringDecimal) : NullaryOperator() {

    override val name = value.toString()

    override fun toString() = value.toString()

    override fun latexString(ctx: RenderContext): String {
        val s = value.nonRepeatingValue.toPlainString()
        val repeatingStartIndex = s.length - value.repeatingDigits
        return "${s.substring(0, repeatingStartIndex)}\\overline{${s.substring(repeatingStartIndex)}}"
    }

    override fun eval(children: List<Double>) = value.toDouble()
}

data class VariableOperator(override val name: String) : NullaryOperator() {
    override fun toString() = name

    override fun latexString(ctx: RenderContext) = name

    override fun eval(children: List<Double>) = Double.NaN
}

object MixedNumberOperator : ExpressionOperator {
    override val name = "MixedNumber"

    override val precedence = MAX_PRECEDENCE
    override val arity = ARITY_THREE

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        require(n in 0 until arity)
        return op is IntegerOperator
    }

    override fun <T> readableString(children: List<T>) = "[${children[0]} ${children[1]}/${children[2]}]"

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>) =
        "${children[0].toLatexString(ctx)}\\frac${children[1].toLatexString(ctx)}${children[2].toLatexString(ctx)}"

    override fun eval(children: List<Double>) = children[0] + children[1] / children[2]
}

enum class UnaryExpressionOperator(override val precedence: Int) : UnaryOperator, ExpressionOperator {
    DivideBy(DIVIDE_BY_PRECEDENCE) {
        override fun childAllowed(op: Operator) = op.precedence > NaryOperator.Product.precedence
        override fun <T> readableString(child: T) = " : $child"
        override fun latexString(ctx: RenderContext, child: LatexRenderable) = "\\div ${child.toLatexString(ctx)}"
        override fun eval(operand: Double) = 1.0 / operand
    },
    Plus(PLUS_MINUS_PRECEDENCE) {
        override fun <T> readableString(child: T) = "+$child"
        override fun latexString(ctx: RenderContext, child: LatexRenderable) = "+${child.toLatexString(ctx)}"
        override fun eval(operand: Double) = operand
    },
    Minus(PLUS_MINUS_PRECEDENCE) {
        override fun <T> readableString(child: T) = "-$child"
        override fun latexString(ctx: RenderContext, child: LatexRenderable) = "-${child.toLatexString(ctx)}"
        override fun eval(operand: Double) = -operand
    },
    PlusMinus(PLUS_MINUS_PRECEDENCE) {
        override fun <T> readableString(child: T) = "+/-$child"
        override fun latexString(ctx: RenderContext, child: LatexRenderable) = "\\pm ${child.toLatexString(ctx)}"
        override fun eval(operand: Double) = if (operand == 0.0) operand else Double.NaN
    },
    SquareRoot(FUNCTION_LIKE_PRECEDENCE) {
        override fun childAllowed(op: Operator) = true
        override fun <T> readableString(child: T) = "sqrt[$child]"
        override fun latexString(ctx: RenderContext, child: LatexRenderable) = "\\sqrt{${child.toLatexString(ctx)}}"
        override fun eval(operand: Double) = sqrt(operand)
    },
    NaturalLog(NATURAL_LOG_PRECEDENCE) {
        override fun childAllowed(op: Operator) =
            op.precedence >= BinaryExpressionOperator.Fraction.precedence

        override fun latexString(ctx: RenderContext, child: LatexRenderable) = "\\ln${child.toLatexString(ctx)}"
        override fun eval(operand: Double) = ln(operand)
    },
    AbsoluteValue(FUNCTION_LIKE_PRECEDENCE) {
        override fun childAllowed(op: Operator) = true
        override fun <T> readableString(child: T) = "abs[$child]"
        override fun latexString(ctx: RenderContext, child: LatexRenderable) =
            "\\left|${child.toLatexString(ctx)}\\right|"
        override fun eval(operand: Double) = abs(operand)
    },

    ;

    protected abstract fun eval(operand: Double): Double

    override fun eval(children: List<Double>) = eval(children[0])
}

enum class BinaryExpressionOperator(override val precedence: Int) : BinaryOperator, ExpressionOperator {
    Fraction(FRACTION_PRECEDENCE) {
        override fun leftChildAllowed(op: Operator) = true
        override fun rightChildAllowed(op: Operator) = true
        override fun <T> readableString(left: T, right: T) = "[$left / $right]"
        override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable) =
            "\\frac{${left.toLatexString(ctx)}}{${right.toLatexString(ctx)}}"

        override fun eval(first: Double, second: Double) = first / second
    },
    Power(POWER_PRECEDENCE) {
        override fun leftChildAllowed(op: Operator) = op.precedence == MAX_PRECEDENCE

        override fun rightChildAllowed(op: Operator) = true

        override fun <T> readableString(left: T, right: T) = "[$left ^ $right]"

        override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable) =
            "${left.toLatexString(ctx)}^{${right.toLatexString(ctx)}}"

        override fun eval(first: Double, second: Double) = first.pow(second)
    },
    Root(FUNCTION_LIKE_PRECEDENCE) {
        override fun leftChildAllowed(op: Operator) = true
        override fun rightChildAllowed(op: Operator) = true
        override fun <T> readableString(left: T, right: T) = "root[$left, $right]"
        override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable) =
            "\\sqrt[${right.toLatexString(ctx)}]{${left.toLatexString(ctx)}}"

        override fun eval(first: Double, second: Double): Double {
            val exp = 1.0 / second
            val pow = first.pow(exp)
            if (!pow.isNaN()) {
                return pow
            }
            // Try to deal with odd roots of negatives as a special case
            if (first < 0) {
                val n = second.toInt()
                if (n.toDouble() == second && n % 2 != 0) {
                    return -(-first).pow(1.0 / second)
                }
            }
            return Double.NaN
        }
    },

    ;
    protected abstract fun eval(first: Double, second: Double): Double

    override fun eval(children: List<Double>) = eval(children[0], children[1])
}

enum class NaryOperator(override val precedence: Int) : ExpressionOperator {
    Sum(SUM_PRECEDENCE) {
        override fun <T> readableString(children: List<T>): String {
            return buildString {
                for ((i, child) in children.withIndex()) {
                    if (i == 0) {
                        append(child.toString())
                    } else {
                        val (termKind, termBody) = when (child) {
                            is LatexRenderable -> child.asSumTerm()
                            else -> Pair(SumTermKind.PLUS, child)
                        }
                        append(
                            when (termKind) {
                                SumTermKind.PLUS -> " + "
                                SumTermKind.MINUS -> " - "
                                SumTermKind.PLUSMINUS -> " +/- "
                            },
                            termBody.toString(),
                        )
                    }
                }
            }
        }

        override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
            return buildString {
                for ((i, child) in children.withIndex()) {
                    if (i == 0) {
                        append(child.toLatexString(ctx))
                    } else {
                        val (termKind, termBody) = child.asSumTerm()
                        append(
                            when (termKind) {
                                SumTermKind.PLUS -> " + "
                                SumTermKind.MINUS -> " - "
                                SumTermKind.PLUSMINUS -> " \\pm "
                            },
                            termBody.toLatexString(ctx),
                        )
                    }
                }
            }
        }

        override fun eval(children: List<Double>) = children.sum()
    },
    Product(PRODUCT_PRECEDENCE) {
        override fun <T> readableString(children: List<T>): String {
            return buildString {
                for ((i, child) in children.map { it.toString() }.withIndex()) {
                    when {
                        i == 0 -> append(child)
                        child.startsWith(" : ") -> append(child)
                        else -> append(" * ", child)
                    }
                }
            }
        }

        override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
            return buildString {
                for ((i, child) in children.withIndex()) {
                    val childLatex = child.toLatexString(ctx)
                    when {
                        i == 0 -> append(childLatex)
                        child.isInlineDivideByTerm() -> append(" ", childLatex)
                        else -> append(" \\times ", childLatex)
                    }
                }
            }
        }

        override fun eval(children: List<Double>) = children.reduce { x, y -> x * y }
    },
    ImplicitProduct(IMPLICIT_PRODUCT_PRECEDENCE) {
        override fun <T> readableString(children: List<T>): String {
            return children.joinToString(" ")
        }

        override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
            return children.joinToString(" ") { it.toLatexString(ctx) }
        }

        override fun eval(children: List<Double>) = children.reduce { x, y -> x * y }
    }, ;

    override val arity = ARITY_VARIABLE
    override fun nthChildAllowed(n: Int, op: Operator) = op.precedence > this.precedence

    override fun equiv(other: Operator): Boolean {
        return (this == other) ||
            (this == Product && other == ImplicitProduct) ||
            (this == ImplicitProduct && other == Product)
    }
}
