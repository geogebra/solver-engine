package engine.operators

import engine.utility.RecurringDecimal
import java.math.BigDecimal
import java.math.BigInteger

private const val SUM_PRECEDENCE = 10
private const val PLUS_MINUS_PRECEDENCE = 15
private const val PRODUCT_PRECEDENCE = 20
private const val IMPLICIT_PRODUCT_PRECEDENCE = 40
private const val FRACTION_PRECEDENCE = 50
private const val POWER_PRECEDENCE = 60
private const val NATURAL_LOG_PRECEDENCE = 50
private const val DIVIDE_BY_PRECEDENCE = 90
private const val ROOT_PRECEDENCE = 95
private const val MAX_PRECEDENCE = 100

interface ExpressionOperator : Operator {
    override val kind get() = OperatorKind.EXPRESSION
}

abstract class NullaryOperator : ExpressionOperator {
    override val precedence = MAX_PRECEDENCE
    override val arity = ARITY_NULL

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        throw IllegalArgumentException(
            "Nullary operator ${this::class.simpleName} should have no children. " +
                "Child $op is invalid at position $n."
        )
    }

    override fun <T> readableString(children: List<T>): String {
        require(children.isEmpty())
        return toString()
    }

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
        require(children.isEmpty())
        return "{${latexString(ctx)}}"
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

    override fun toString() = value.toString()

    override fun latexString(ctx: RenderContext) = value.toString()
}

object UndefinedOperator : NullaryOperator() {

    override fun toString() = "UNDEFINED"

    override fun latexString(ctx: RenderContext) = "\\text{undefined}"
}

/**
 * Operator representing an unsigned terminating decimal.
 */
data class DecimalOperator(val value: BigDecimal) : NullaryOperator() {
    init {
        require(value.signum() >= 0)
    }

    override fun toString(): String = value.toPlainString()

    override fun latexString(ctx: RenderContext): String = value.toPlainString()
}

/**
 * Operator representing an unsigned recurring decimal, e.g. 1.045454545... = 1.0[45]. The [value] must include the
 * occurrence of the repeating pattern and [repeatingDigits] is the length of the repeating pattern.
 * Examples:
 * - 0.[6] is RecurringDecimalOperator(BigDecimal("0.6", 1)
 * - 1.0[45] is RecurringDecimalOperator(BigDecimal("1.045"), 2)
 */
data class RecurringDecimalOperator(val value: RecurringDecimal) : NullaryOperator() {

    override fun toString() = value.toString()

    override fun latexString(ctx: RenderContext): String {
        val s = value.nonRepeatingValue.toPlainString()
        val repeatingStartIndex = s.length - value.repeatingDigits
        return "${s.substring(0, repeatingStartIndex)}\\overline{${s.substring(repeatingStartIndex)}}"
    }
}

data class VariableOperator(val name: String) : NullaryOperator() {
    override fun toString() = name

    override fun latexString(ctx: RenderContext) = name
}

object MixedNumberOperator : ExpressionOperator {
    override val precedence = MAX_PRECEDENCE
    override val arity = ARITY_THREE

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        require(n in 0 until arity)
        return op is IntegerOperator
    }

    override fun <T> readableString(children: List<T>) = "[${children[0]} ${children[1]}/${children[2]}]"

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>) =
        "{${children[0].toLatexString(ctx)}\\frac${children[1].toLatexString(ctx)}${children[2].toLatexString(ctx)}}"
}

enum class BracketOperator(
    private val opening: String,
    private val closing: String,
    private val latexOpening: String,
    private val latexClosing: String
) : ExpressionOperator {
    Bracket("(", ")", "\\left(", "\\right)"),
    SquareBracket("[.", ".]", "\\left[", "\\right]"),
    CurlyBracket("{.", ".}", "\\left\\{", "\\right\\}");

    override val precedence = MAX_PRECEDENCE
    override val arity = ARITY_ONE

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        require(n == 0)
        return true
    }

    override fun <T> readableString(children: List<T>): String {
        require(children.size == 1)
        return opening + children[0] + closing
    }

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
        require(children.size == 1)
        return "{$latexOpening ${children[0].toLatexString(ctx)} $latexClosing}"
    }

    override fun equiv(other: Operator): Boolean {
        return other is BracketOperator
    }
}

enum class UnaryExpressionOperator(override val precedence: Int) : UnaryOperator, ExpressionOperator {
    InvisibleBracket(MAX_PRECEDENCE) {
        override fun childAllowed(op: Operator) = true
        override fun <T> readableString(child: T) = "{$child}"
        override fun latexString(ctx: RenderContext, child: LatexRenderable) = "{${child.toLatexString(ctx)}}"
    },
    DivideBy(DIVIDE_BY_PRECEDENCE) {
        override fun childAllowed(op: Operator) = op.precedence > NaryOperator.Product.precedence
        override fun <T> readableString(child: T) = " : $child"
        override fun latexString(ctx: RenderContext, child: LatexRenderable) = "{{} \\div ${child.toLatexString(ctx)}}"
    },
    Plus(PLUS_MINUS_PRECEDENCE) {
        override fun <T> readableString(child: T) = "+$child"
        override fun latexString(ctx: RenderContext, child: LatexRenderable) = "{+${child.toLatexString(ctx)}}"
    },
    Minus(PLUS_MINUS_PRECEDENCE) {
        override fun <T> readableString(child: T) = "-$child"
        override fun latexString(ctx: RenderContext, child: LatexRenderable) = "{-${child.toLatexString(ctx)}}"
    },
    SquareRoot(ROOT_PRECEDENCE) {
        override fun childAllowed(op: Operator) = true
        override fun <T> readableString(child: T) = "sqrt[$child]"
        override fun latexString(ctx: RenderContext, child: LatexRenderable) = "{\\sqrt${child.toLatexString(ctx)}}"
    },
    NaturalLog(NATURAL_LOG_PRECEDENCE) {
        override fun childAllowed(op: Operator) =
            op.precedence >= BinaryExpressionOperator.Fraction.precedence

        override fun latexString(ctx: RenderContext, child: LatexRenderable) = "{\\ln${child.toLatexString(ctx)}}"
    };
}

enum class BinaryExpressionOperator(override val precedence: Int) : BinaryOperator, ExpressionOperator {
    Fraction(FRACTION_PRECEDENCE) {
        override fun leftChildAllowed(op: Operator) = true
        override fun rightChildAllowed(op: Operator) = true
        override fun <T> readableString(left: T, right: T) = "[$left / $right]"
        override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable) =
            "{\\frac${left.toLatexString(ctx)}${right.toLatexString(ctx)}}"
    },
    Power(POWER_PRECEDENCE) {
        override fun leftChildAllowed(op: Operator) =
            op.precedence >= BracketOperator.Bracket.precedence

        override fun rightChildAllowed(op: Operator) = true

        override fun <T> readableString(left: T, right: T) = "[$left ^ $right]"

        override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable) =
            "{${left.toLatexString(ctx)} ^ ${right.toLatexString(ctx)}}"
    },
    Root(ROOT_PRECEDENCE) {
        override fun leftChildAllowed(op: Operator) = true
        override fun rightChildAllowed(op: Operator) = true
        override fun <T> readableString(left: T, right: T) = "root[$left, $right]"
        override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable) =
            "{\\sqrt[${right.toLatexString(ctx)}]${left.toLatexString(ctx)}}"
    };
}

enum class NaryOperator(override val precedence: Int) : ExpressionOperator {
    Sum(SUM_PRECEDENCE) {
        override fun <T> readableString(children: List<T>): String {
            return buildString {
                for ((i, child) in children.map { it.toString() }.withIndex()) {
                    when {
                        i == 0 -> append(child)
                        child.startsWith("-") -> append(" - ", child.removePrefix("-"))
                        else -> append(" + ", child)
                    }
                }
            }
        }

        override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
            return buildString {
                append("{")
                for ((i, child) in children.withIndex()) {
                    val childLatex = child.toLatexString(ctx)
                    when {
                        i == 0 -> append(childLatex)
                        childLatex.startsWith("{-") -> append(" {{} - ", childLatex.removePrefix("{-"))
                        else -> append(" + ", childLatex)
                    }
                }
                append("}")
            }
        }
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
                append("{")
                for ((i, child) in children.withIndex()) {
                    val childLatex = child.toLatexString(ctx)
                    when {
                        i == 0 -> append(childLatex)
                        childLatex.startsWith("{{} \\div ") -> append(" ", childLatex)
                        else -> append(" \\times ", childLatex)
                    }
                }
                append("}")
            }
        }
    },
    ImplicitProduct(IMPLICIT_PRODUCT_PRECEDENCE) {
        override fun <T> readableString(children: List<T>): String {
            return children.joinToString(" ")
        }

        override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
            return children.joinToString(separator = " ", prefix = "{", postfix = "}") { it.toLatexString(ctx) }
        }
    };

    override val arity = ARITY_VARIABLE
    override fun nthChildAllowed(n: Int, op: Operator) = op.precedence > this.precedence
}
