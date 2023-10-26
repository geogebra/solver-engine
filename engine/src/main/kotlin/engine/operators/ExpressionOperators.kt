package engine.operators

import engine.expressions.Constants
import engine.expressions.TrigonometricExpression
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

private const val EXPRESSION_WITH_CONSTRAINT_PRECEDENCE = 5
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
internal const val MAX_PRECEDENCE = 100

private const val ONE_PERCENT = 0.01

internal interface ExpressionOperator : Operator {
    override val kind get() = OperatorKind.EXPRESSION

    fun eval(children: List<Double>): Double
}

internal data class NameOperator(val value: String) : NullaryOperator(), ExpressionOperator {

    override val name = toString()

    override fun toString() = "\"${value}\""

    override fun latexString(ctx: RenderContext) = "\\textrm{$value}"

    override fun eval(children: List<Double>) = Double.NaN
}

/**
 * Operator representing an unsigned integer.
 */
internal data class IntegerOperator(val value: BigInteger) : NullaryOperator(), ExpressionOperator {
    init {
        require(value.signum() >= 0)
    }

    override val name = value.toString()

    override fun toString() = value.toString()

    override fun latexString(ctx: RenderContext) = value.toString()

    override fun eval(children: List<Double>) = value.toDouble()
}

internal object InfinityOperator : NullaryOperator(), ExpressionOperator {

    override val name = "Infinity"

    override fun toString() = "/infinity/"

    override fun latexString(ctx: RenderContext) = "\\infty"

    override fun eval(children: List<Double>) = Double.POSITIVE_INFINITY
}

internal object UndefinedOperator : NullaryOperator(), ExpressionOperator {

    override val name = "Undefined"

    override fun toString() = "/undefined/"

    override fun latexString(ctx: RenderContext) = "\\text{undefined}"

    override fun eval(children: List<Double>) = Double.NaN
}

internal object PiOperator : NullaryOperator(), ExpressionOperator {

    override val name = "Pi"

    override fun toString() = "/pi/"

    override fun latexString(ctx: RenderContext) = "\\pi"

    override fun eval(children: List<Double>) = Math.PI
}

internal object EulerEOperator : NullaryOperator(), ExpressionOperator {

    override val name = "EulerE"

    override fun toString() = "/e/"

    override fun latexString(ctx: RenderContext) = "e"

    override fun eval(children: List<Double>) = Math.E
}

internal object ImaginaryUnitOperator : NullaryOperator(), ExpressionOperator {

    override val name = "ImaginaryUnit"

    override fun toString() = "/i/"

    override fun latexString(ctx: RenderContext) = "i"

    override fun eval(children: List<Double>) = Double.NaN
}

/**
 * Operator representing an unsigned terminating decimal.
 */
internal data class DecimalOperator(val value: BigDecimal) : NullaryOperator(), ExpressionOperator {
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
internal data class RecurringDecimalOperator(val value: RecurringDecimal) : NullaryOperator(), ExpressionOperator {

    override val name = value.toString()

    override fun toString() = value.toString()

    override fun latexString(ctx: RenderContext): String {
        val s = value.nonRepeatingValue.toPlainString()
        val repeatingStartIndex = s.length - value.repeatingDigits
        return "${s.substring(0, repeatingStartIndex)}\\overline{${s.substring(repeatingStartIndex)}}"
    }

    override fun eval(children: List<Double>) = value.toDouble()
}

internal data class VariableOperator(val variableName: String, val subscript: String? = null) :
    NullaryOperator(), ExpressionOperator {

    override val name: String get() = "Variable"

    override fun toString() = "${variableName}${if (subscript != null) "_$subscript" else ""}"

    override fun latexString(ctx: RenderContext) = "${variableName}${if (subscript != null) "_{$subscript}" else ""}"

    override fun eval(children: List<Double>) = Double.NaN
}

internal object MixedNumberOperator : ExpressionOperator {
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

internal enum class UnaryExpressionOperator(override val precedence: Int) : UnaryOperator, ExpressionOperator {
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

enum class TrigonometricFunctionType(
    val text: String,
    val evalFunc: (Double) -> Double,
    val getInv: () -> TrigonometricFunctionType,
) {
    Sin("sin", ::sin, { Arcsin }),
    Cos("cos", ::cos, { Arccos }),
    Tan("tan", ::tan, { Arctan }),
    Arcsin("arcsin", ::asin, { Sin }),
    Arccos("arccos", ::acos, { Cos }),
    Arctan("arctan", ::atan, { Tan }),
    Sec("sec", { 1 / cos(it) }, { Arcsec }),
    Csc("csc", { 1 / sin(it) }, { Arccsc }),
    Cot("cot", { 1 / tan(it) }, { Arccot }),
    Arcsec("arcsec", { acos(1 / it) }, { Sec }),
    Arccsc("arccsc", { asin(1 / it) }, { Csc }),
    Arccot("arccot", { atan(1 / it) }, { Cot }),
    Sinh("sinh", ::sinh, { Arsinh }),
    Cosh("cosh", ::cosh, { Arcosh }),
    Tanh("tanh", ::tanh, { Artanh }),
    Arsinh("arsinh", ::asinh, { Sinh }),
    Arcosh("arcosh", ::acosh, { Cosh }),
    Artanh("artanh", ::atanh, { Tanh }),
    Sech("sech", { 1 / cosh(it) }, { Arsech }),
    Csch("csch", { 1 / sinh(it) }, { Arcsch }),
    Coth("coth", { 1 / tanh(it) }, { Arcoth }),
    Arsech("arsech", { acosh(1 / it) }, { Sech }),
    Arcsch("arcsch", { asinh(1 / it) }, { Csch }),
    Arcoth("arcoth", { atanh(1 / it) }, { Coth }),
    ;

    val inverse: TrigonometricFunctionType get() = getInv()
    fun eval(x: Double) = evalFunc(x)
}

internal data class TrigonometricFunctionOperator(
    val type: TrigonometricFunctionType,
    val powerInside: Boolean = false,
    // possible values are "superscript" (e.g. sin^-1), "arcPrefix" (e.g. arcsin) or "aPrefix" (e.g. "asin")
    val inverseNotation: String = "arcPrefix",
) : UnaryOperator, ExpressionOperator {

    override val name = type.text

    override val precedence = TRIG_PRECEDENCE

    override fun childAllowed(op: Operator) = op.precedence >= PRODUCT_PRECEDENCE

    override fun <T> readableString(child: T): String = when (inverseNotation) {
        "superscript" -> {
            val inverse = this.type.inverse.text
            "[$inverse ^ -1] $child"
        }
        else -> "${type.text} $child"
    }

    override fun latexString(ctx: RenderContext, child: LatexRenderable) = when (inverseNotation) {
        "superscript" -> {
            val inverse = this.type.inverse.text
            "\\$inverse^{-1} ${child.toLatexString(ctx)}"
        }
        else -> "\\${type.text} ${child.toLatexString(ctx)}"
    }

    override fun eval(children: List<Double>): Double {
        return type.eval(children[0])
    }
}

internal enum class BinaryExpressionOperator(override val precedence: Int) : BinaryOperator, ExpressionOperator {
    Fraction(FRACTION_PRECEDENCE) {
        override fun leftChildAllowed(op: Operator) = true
        override fun rightChildAllowed(op: Operator) = true
        override fun <T> readableString(left: T, right: T) = "[$left / $right]"
        override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable) =
            "\\frac{${left.toLatexString(ctx)}}{${right.toLatexString(ctx)}}"

        override fun eval(first: Double, second: Double) = first / second
    },
    Power(POWER_PRECEDENCE) {
        override fun leftChildAllowed(op: Operator) = (op.precedence == MAX_PRECEDENCE) ||
            (op is TrigonometricFunctionOperator)

        override fun rightChildAllowed(op: Operator) = true

        override fun <T> readableString(left: T, right: T): String {
            return when {
                left is TrigonometricExpression && left.powerInside -> {
                    val operatorType = left.operator.name
                    "[$operatorType ^ $right] ${left.operands[0]}"
                }
                else -> "[$left ^ $right]"
            }
        }

        override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable): String {
            return when {
                left is TrigonometricExpression && left.powerInside -> {
                    val operatorType = left.operator.name
                    "\\$operatorType^{${right.toLatexString(ctx)}} ${left.operands[0].toLatexString(ctx)}"
                }
                else -> "${left.toLatexString(ctx)}^{${right.toLatexString(ctx)}}"
            }
        }

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

internal sealed class NaryOperator(override val precedence: Int) : ExpressionOperator {

    override val arity = ARITY_VARIABLE
    override fun nthChildAllowed(n: Int, op: Operator) = op.precedence > this.precedence

    open fun matches(operator: Operator) = operator == this
}

internal object SumOperator : NaryOperator(SUM_PRECEDENCE) {

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

internal data class ProductOperator(val forcedSigns: List<Int>) : NaryOperator(PRODUCT_PRECEDENCE) {

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

internal val DefaultProductOperator = ProductOperator(forcedSigns = emptyList())

internal object ExpressionWithConstraintOperator : BinaryOperator, StatementOperator {
    override val name = "ExpressionWithConstraint"

    override val precedence = EXPRESSION_WITH_CONSTRAINT_PRECEDENCE

    override fun leftChildAllowed(op: Operator) = true

    override fun rightChildAllowed(op: Operator): Boolean {
        require(op.kind == OperatorKind.STATEMENT)
        return true
    }

    override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable): String {
        return "${left.toLatexString(ctx)} \\text{ given } ${right.toLatexString(ctx)}"
    }

    override fun <T> readableString(left: T, right: T): String {
        return "$left GIVEN $right"
    }
}

internal object DerivativeOperator : ExpressionOperator {
    override fun eval(children: List<Double>) = Double.NaN

    override val name = "Derivative"
    override val precedence = DERIVATIVE_PRECEDENCE
    override val arity = ARITY_VARIABLE

    override fun nthChildAllowed(n: Int, op: Operator) = when (n) {
        0 -> true
        else -> op.precedence >= PRODUCT_PRECEDENCE
    }

    override fun <T> readableString(children: List<T>) = when (children[0]) {
        Constants.One -> "diff[${children[1]} / ${children[2]}]"
        else -> "[diff ^ ${children[0]}][${children[1]} " +
            "/ ${children.drop(2).joinToString(separator = " ")}]"
    }

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>) = when (children[0]) {
        Constants.One -> "\\frac{\\mathrm{d} ${children[1].toLatexString(ctx)}}" +
            "{\\mathrm{d} ${children[2].toLatexString(ctx)}}"
        else -> "\\frac{\\mathrm{d}^{${children[0].toLatexString()}} ${children[1].toLatexString(ctx)}}" +
            "{${children.drop(2).joinToString(separator = " \\, ") { "\\mathrm{d} " + it.toLatexString(ctx) }}}"
    }
}

internal object IndefiniteIntegralOperator : BinaryOperator, ExpressionOperator {
    override fun eval(children: List<Double>) = Double.NaN

    override val name = "IndefiniteIntegral"
    override val precedence = INTEGRAL_PRECEDENCE
    override val arity = ARITY_TWO

    override fun nthChildAllowed(n: Int, op: Operator) = true

    override fun <T> readableString(left: T, right: T) = "prim[$left, $right]"

    override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable) =
        "\\int ${left.toLatexString(ctx)} \\, \\mathrm{d}${right.toLatexString(ctx)}"
}

internal object DefiniteIntegralOperator : ExpressionOperator {
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

internal object VectorOperator : ExpressionOperator {
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

internal data class MatrixOperator(val rows: Int, val columns: Int) : ExpressionOperator {
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
