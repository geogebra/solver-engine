package engine.expressions

import engine.operators.BinaryExpressionOperator
import engine.operators.DecimalOperator
import engine.operators.IntegerOperator
import engine.operators.LatexRenderable
import engine.operators.MixedNumberOperator
import engine.operators.NaryOperator
import engine.operators.Operator
import engine.operators.RecurringDecimalOperator
import engine.operators.RenderContext
import engine.operators.UnaryExpressionOperator
import engine.operators.VariableOperator
import engine.utility.RecurringDecimal
import java.math.BigDecimal
import java.math.BigInteger

enum class Decorator {
    RoundBracket {
        override fun decorateString(str: String) = "($str)"
        override fun decorateLatexString(str: String) = "{\\left( $str \\right)}"
    },
    SquareBracket {
        override fun decorateString(str: String) = "[. $str .]"
        override fun decorateLatexString(str: String) = "{\\left[ $str \\right]}"
    },
    CurlyBracket {
        override fun decorateString(str: String) = "{. $str .}"
        override fun decorateLatexString(str: String) = "{\\left\\{ $str \\right\\}}"
    },
    MissingBracket {
        override fun decorateString(str: String) = str
        override fun decorateLatexString(str: String) = str
    };

    abstract fun decorateString(str: String): String
    abstract fun decorateLatexString(str: String): String
}

class Expression(
    val operator: Operator,
    operands: List<Expression>,
    val decorators: List<Decorator> = emptyList()
) : LatexRenderable {
    val operands: List<Expression>

    init {
        this.operands = operands.mapIndexed { index, expression ->
            if (expression.hasBracket() || operator.nthChildAllowed(index, expression.operator)) expression
            else expression.decorate(Decorator.RoundBracket)
        }
    }

    override fun toString(): String {
        return decorators.fold(operator.readableString(operands)) { acc, dec -> dec.decorateString(acc) }
    }

    /**
     * Returns a LaTeX string representation of the expression.  The string should be of the form "{...}".
     * Each operand should itself be enclosed in "{...}" and there should be no other curly brackets,
     * except for the pair "{}" itself.
     *
     * This ensures that paths can be followed in the string representation by counting instances of
     * "{" and "}" and discarding "{}".
     */
    override fun toLatexString(ctx: RenderContext): String {
        return decorators.fold(operator.latexString(ctx, operands)) { acc, dec -> dec.decorateLatexString(acc) }
    }

    fun equiv(other: Expression): Boolean {
        return operator.equiv(other.operator) &&
            operands.size == other.operands.size &&
            operands.zip(other.operands).all { (op1, op2) -> op1.equiv(op2) }
    }

    fun decorate(decorator: Decorator) = Expression(operator, operands, decorators + decorator)

    fun hasBracket() = decorators.isNotEmpty()

    fun removeBrackets() = Expression(operator, operands)

    fun outerBracket() = decorators.lastOrNull()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Expression

        if (operator != other.operator) return false
        if (decorators != other.decorators) return false
        if (operands != other.operands) return false

        return true
    }

    override fun hashCode(): Int {
        var result = operator.hashCode()
        result = 31 * result + decorators.hashCode()
        result = 31 * result + operands.hashCode()
        return result
    }
}

fun xp(n: Int) = xp(n.toBigInteger())

fun xp(n: BigInteger): Expression {
    val posExpr = Expression(IntegerOperator(n.abs()), emptyList())
    return if (n.signum() >= 0) posExpr else negOf(posExpr)
}

fun xp(x: BigDecimal): Expression {
    val operator =
        if (x.scale() <= 0) IntegerOperator(x.abs().toBigInteger()) else DecimalOperator(x.abs())
    val posExpr = Expression(operator, emptyList())
    return if (x.signum() >= 0) posExpr else negOf(posExpr)
}

fun xp(x: RecurringDecimal): Expression {
    return Expression(RecurringDecimalOperator(x), emptyList())
}

fun xp(v: String) = Expression(VariableOperator(v), emptyList())

fun mixedNumber(integer: BigInteger, numerator: BigInteger, denominator: BigInteger) =
    Expression(MixedNumberOperator, listOf(xp(integer), xp(numerator), xp(denominator)))

fun bracketOf(expr: Expression, decorator: Decorator? = null) = expr.decorate(decorator ?: Decorator.RoundBracket)
fun squareBracketOf(expr: Expression) = expr.decorate(Decorator.SquareBracket)
fun curlyBracketOf(expr: Expression) = expr.decorate(Decorator.CurlyBracket)
fun missingBracketOf(expr: Expression) = expr.decorate(Decorator.MissingBracket)

fun negOf(expr: Expression) = Expression(UnaryExpressionOperator.Minus, listOf(expr))
fun plusOf(expr: Expression) = Expression(UnaryExpressionOperator.Plus, listOf(expr))

fun fractionOf(numerator: Expression, denominator: Expression) =
    Expression(BinaryExpressionOperator.Fraction, listOf(numerator, denominator))

fun powerOf(base: Expression, exponent: Expression) = Expression(BinaryExpressionOperator.Power, listOf(base, exponent))

fun squareRootOf(radicand: Expression) = Expression(UnaryExpressionOperator.SquareRoot, listOf(radicand))

fun rootOf(radicand: Expression, order: Expression) = Expression(BinaryExpressionOperator.Root, listOf(radicand, order))

fun sumOf(vararg terms: Expression) = Expression(NaryOperator.Sum, terms.asList())

fun productOf(vararg factors: Expression) = Expression(NaryOperator.Product, factors.asList())

fun implicitProductOf(vararg factors: Expression) = Expression(NaryOperator.ImplicitProduct, factors.asList())
