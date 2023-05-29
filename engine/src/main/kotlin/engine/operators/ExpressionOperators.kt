package engine.operators

import engine.expressions.Constants
import engine.utility.RecurringDecimal
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.acosh
import kotlin.math.asin
import kotlin.math.asinh
import kotlin.math.atan
import kotlin.math.atanh
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.ln
import kotlin.math.log
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

private const val SUM_PRECEDENCE = 10
private const val PLUS_MINUS_PRECEDENCE = 15
private const val PERCENTAGE_OF_PRECEDENCE = 17
private const val PRODUCT_PRECEDENCE = 20
private const val FRACTION_PRECEDENCE = 50
private const val DERIVATIVE_PRECEDENCE = 50
private const val LOG_PRECEDENCE = 50
private const val TRIG_PRECEDENCE = 50
private const val PERCENTAGE_PRECEDENCE = 55
private const val INTEGRAL_PRECEDENCE = 55
private const val POWER_PRECEDENCE = 60
private const val DIVIDE_BY_PRECEDENCE = 90
private const val FUNCTION_LIKE_PRECEDENCE = 95
private const val MAX_PRECEDENCE = 100

private const val ONE_PERCENT = 0.01

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

data class NameOperator(val value: String) : NullaryOperator() {

    override val name = toString()

    override fun toString() = "\"${value}\""

    override fun latexString(ctx: RenderContext) = "\\textrm{$value}"

    override fun eval(children: List<Double>) = Double.NaN
}

/**
 * Operator representing an unsigned integer.
 */
internal data class IntegerOperator(val value: BigInteger) : NullaryOperator() {
    init {
        require(value.signum() >= 0)
    }

    override val name = value.toString()

    override fun toString() = value.toString()

    override fun latexString(ctx: RenderContext) = value.toString()

    override fun eval(children: List<Double>) = value.toDouble()
}

object InfinityOperator : NullaryOperator() {

    override val name = "/infinity/"

    override fun toString() = "/infinity/"

    override fun latexString(ctx: RenderContext) = "\\infty"

    override fun eval(children: List<Double>) = Double.POSITIVE_INFINITY
}

object UndefinedOperator : NullaryOperator() {

    override val name = "/undefined/"

    override fun toString() = "/undefined/"

    override fun latexString(ctx: RenderContext) = "\\text{undefined}"

    override fun eval(children: List<Double>) = Double.NaN
}

object PiOperator : NullaryOperator() {

    override val name = "/pi/"

    override fun toString() = "/pi/"

    override fun latexString(ctx: RenderContext) = "\\pi"

    override fun eval(children: List<Double>) = Math.PI
}

object EulerEOperator : NullaryOperator() {

    override val name = "/e/"

    override fun toString() = "/e/"

    override fun latexString(ctx: RenderContext) = "e"

    override fun eval(children: List<Double>) = Math.E
}

object ImaginaryUnitOperator : NullaryOperator() {

    override val name = "/i/"

    override fun toString() = "/i/"

    override fun latexString(ctx: RenderContext) = "i"

    override fun eval(children: List<Double>) = Double.NaN
}

/**
 * Operator representing an unsigned terminating decimal.
 */
internal data class DecimalOperator(val value: BigDecimal) : NullaryOperator() {
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
internal data class RecurringDecimalOperator(val value: RecurringDecimal) : NullaryOperator() {

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
        "${children[0].toLatexString(ctx)}\\frac{${children[1].toLatexString(ctx)}}{${children[2].toLatexString(ctx)}}"

    override fun eval(children: List<Double>) = children[0] + children[1] / children[2]
}

enum class UnaryExpressionOperator(override val precedence: Int) : UnaryOperator, ExpressionOperator {
    DivideBy(DIVIDE_BY_PRECEDENCE) {
        override fun childAllowed(op: Operator) = op.precedence > PRODUCT_PRECEDENCE
        override fun <T> readableString(child: T) = ": $child"
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
    NaturalLog(LOG_PRECEDENCE) {
        override fun childAllowed(op: Operator) = op.precedence >= PRODUCT_PRECEDENCE

        override fun <T> readableString(child: T) = "ln $child"
        override fun latexString(ctx: RenderContext, child: LatexRenderable) = "\\ln ${child.toLatexString(ctx)}"
        override fun eval(operand: Double) = ln(operand)
    },
    LogBase10(LOG_PRECEDENCE) {
        override fun childAllowed(op: Operator) = op.precedence >= PRODUCT_PRECEDENCE

        override fun <T> readableString(child: T) = "log $child"
        override fun latexString(ctx: RenderContext, child: LatexRenderable) = "\\log ${child.toLatexString(ctx)}"
        override fun eval(operand: Double) = log10(operand)
    },
    AbsoluteValue(FUNCTION_LIKE_PRECEDENCE) {
        override fun childAllowed(op: Operator) = true
        override fun <T> readableString(child: T) = "abs[$child]"
        override fun latexString(ctx: RenderContext, child: LatexRenderable) =
            "\\left|${child.toLatexString(ctx)}\\right|"
        override fun eval(operand: Double) = abs(operand)
    },
    Percentage(PERCENTAGE_PRECEDENCE) {
        override fun <T> readableString(child: T) = "$child%"
        override fun latexString(ctx: RenderContext, child: LatexRenderable) =
            "${child.toLatexString(ctx)} \\%"
        override fun eval(operand: Double) = operand * ONE_PERCENT
    },
    ;

    protected abstract fun eval(operand: Double): Double

    override fun eval(children: List<Double>) = eval(children[0])
}

enum class TrigonometricFunctionOperator : UnaryOperator, ExpressionOperator {
    Sin { override fun eval(operand: Double) = sin(operand) },
    Cos { override fun eval(operand: Double) = cos(operand) },
    Tan { override fun eval(operand: Double) = tan(operand) },
    Arcsin { override fun eval(operand: Double) = asin(operand) },
    Arccos { override fun eval(operand: Double) = acos(operand) },
    Arctan { override fun eval(operand: Double) = atan(operand) },
    Sec { override fun eval(operand: Double) = 1 / cos(operand) },
    Csc { override fun eval(operand: Double) = 1 / sin(operand) },
    Cot { override fun eval(operand: Double) = 1 / tan(operand) },
    Arcsec { override fun eval(operand: Double) = acos(1 / operand) },
    Arccsc { override fun eval(operand: Double) = asin(1 / operand) },
    Arccot { override fun eval(operand: Double) = atan(1 / operand) },
    Sinh { override fun eval(operand: Double) = sinh(operand) },
    Cosh { override fun eval(operand: Double) = cosh(operand) },
    Tanh { override fun eval(operand: Double) = tanh(operand) },
    Arsinh { override fun eval(operand: Double) = asinh(operand) },
    Arcosh { override fun eval(operand: Double) = acosh(operand) },
    Artanh { override fun eval(operand: Double) = atanh(operand) },
    Sech { override fun eval(operand: Double) = 1 / cosh(operand) },
    Csch { override fun eval(operand: Double) = 1 / sinh(operand) },
    Coth { override fun eval(operand: Double) = 1 / tanh(operand) },
    Arsech { override fun eval(operand: Double) = acosh(1 / operand) },
    Arcsch { override fun eval(operand: Double) = asinh(1 / operand) },
    Arcoth { override fun eval(operand: Double) = atanh(1 / operand) },
    ;

    override val precedence = TRIG_PRECEDENCE

    override fun childAllowed(op: Operator) = op.precedence >= PRODUCT_PRECEDENCE

    override fun <T> readableString(child: T) = "${name.lowercase()} $child"
    override fun latexString(ctx: RenderContext, child: LatexRenderable) =
        "\\${name.lowercase()} ${child.toLatexString(ctx)}"

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
    Log(LOG_PRECEDENCE) {
        override fun leftChildAllowed(op: Operator) = true
        override fun rightChildAllowed(op: Operator) = op.precedence >= PRODUCT_PRECEDENCE

        override fun <T> readableString(left: T, right: T) = "log[$left] $right"
        override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable) =
            "\\log_{${left.toLatexString(ctx)}} ${right.toLatexString(ctx)}"

        override fun eval(first: Double, second: Double) = log(second, first)
    },

    PercentageOf(PERCENTAGE_OF_PRECEDENCE) {
        override fun <T> readableString(left: T, right: T) = "$left %of $right"
        override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable) =
            "${left.toLatexString(ctx)}\\% \text{ of } ${right.toLatexString(ctx)}"

        override fun eval(first: Double, second: Double) = first * second * ONE_PERCENT
    },

    ;

    protected abstract fun eval(first: Double, second: Double): Double

    override fun eval(children: List<Double>) = eval(children[0], children[1])
}

sealed class NaryOperator(override val precedence: Int) : ExpressionOperator {

    override val arity = ARITY_VARIABLE
    override fun nthChildAllowed(n: Int, op: Operator) = op.precedence > this.precedence

    open fun matches(operator: Operator) = operator == this
}

object SumOperator : NaryOperator(SUM_PRECEDENCE) {

    override val name = "Sum"
    override fun <T> readableString(children: List<T>): String {
        return buildString {
            for ((i, child) in children.withIndex()) {
                if (i == 0) {
                    append(child.toString())
                } else if (child is LatexRenderable) {
                    append(child.toReadableStringAsSecondTermInASum())
                } else {
                    append(" + $child")
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
                    append(child.toLatexStringAsSecondTermInASum(ctx))
                }
            }
        }
    }

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        return (n == 0 || op != UnaryExpressionOperator.Plus) && super.nthChildAllowed(n, op)
    }

    override fun eval(children: List<Double>) = children.sum()
}

data class ProductOperator(val forcedSigns: List<Int>) : NaryOperator(PRODUCT_PRECEDENCE) {

    override val name = "Product"

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

    override fun matches(operator: Operator) = operator is ProductOperator
}

val DefaultProductOperator = ProductOperator(forcedSigns = emptyList())

object DerivativeOperator : ExpressionOperator {
    override fun eval(children: List<Double>) = Double.NaN

    override val name = "Derivative"
    override val precedence = DERIVATIVE_PRECEDENCE
    override val arity = ARITY_VARIABLE

    override fun nthChildAllowed(n: Int, op: Operator) = when (n) {
        0 -> true
        else -> op.precedence >= PRODUCT_PRECEDENCE
    }

    override fun <T> readableString(children: List<T>) = when (children[0]) {
        Constants.One -> "diff[${children[1]} / d${children[2]}]"
        else -> "[diff ^ ${children[0]}][${children[1]} " +
            "/ ${children.drop(2).joinToString(separator = " ") { "d$it" }}]"
    }

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>) = when (children[0]) {
        Constants.One -> "\\frac{\\mathrm{d} ${children[1].toLatexString(ctx)}}" +
            "{\\mathrm{d} ${children[2].toLatexString(ctx)}}"
        else -> "\\frac{\\mathrm{d}^{${children[0].toLatexString()}} ${children[1].toLatexString(ctx)}}" +
            "{${children.drop(2).joinToString(separator = " \\, ") { "\\mathrm{d} " + it.toLatexString(ctx) }}}"
    }
}

object IndefiniteIntegralOperator : BinaryOperator, ExpressionOperator {
    override fun eval(children: List<Double>) = Double.NaN

    override val name = "IndefiniteIntegral"
    override val precedence = INTEGRAL_PRECEDENCE
    override val arity = ARITY_TWO

    override fun nthChildAllowed(n: Int, op: Operator) = true

    override fun <T> readableString(left: T, right: T) = "prim[$left, $right]"

    override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable) =
        "\\int ${left.toLatexString(ctx)} \\, \\mathrm{d}${right.toLatexString(ctx)}"
}

object DefiniteIntegralOperator : ExpressionOperator {
    override fun eval(children: List<Double>) = Double.NaN

    override val name = "DefiniteIntegral"
    override val precedence = INTEGRAL_PRECEDENCE
    override val arity = ARITY_FOUR

    override fun nthChildAllowed(n: Int, op: Operator) = true

    override fun <T> readableString(children: List<T>) = "int[${children.joinToString()}]"

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
        val (lowerBound, upperBound, function, variable) = children.map { it.toLatexString(ctx) }
        return "\\int_{$lowerBound}^{$upperBound} $function \\, \\mathrm{d}$variable"
    }
}

object VectorOperator : ExpressionOperator {
    override fun eval(children: List<Double>) = Double.NaN

    override val name = "Vector"
    override val precedence = MAX_PRECEDENCE
    override val arity = ARITY_VARIABLE

    override fun nthChildAllowed(n: Int, op: Operator) = true

    override fun <T> readableString(children: List<T>) = children.joinToString(prefix = "vec[", postfix = "]")

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>) =
        children.joinToString(prefix = "\\begin{pmatrix}", postfix = "\\end{pmatrix}", separator = " \\\\ ") {
            it.toLatexString(ctx)
        }
}

data class MatrixOperator(val rows: Int, val columns: Int) : ExpressionOperator {
    override fun eval(children: List<Double>) = Double.NaN

    override val name = "Matrix"
    override val precedence = MAX_PRECEDENCE
    override val arity = ARITY_VARIABLE

    override fun nthChildAllowed(n: Int, op: Operator) = true

    override fun <T> readableString(children: List<T>) =
        children
            .chunked(columns)
            .joinToString(prefix = "mat[", postfix = "]", separator = "; ") { it.joinToString() }

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>) =
        children
            .map { it.toLatexString(ctx) }
            .chunked(columns)
            .joinToString(prefix = "\\begin{pmatrix}", postfix = "\\end{pmatrix}", separator = " \\\\ ") {
                it.joinToString(separator = " & ")
            }
}
