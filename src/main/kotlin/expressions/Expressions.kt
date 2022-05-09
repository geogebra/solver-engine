package expressions

import java.math.BigDecimal
import java.math.BigInteger

interface Expression {
    fun variables(): Set<VariableExpr> = emptySet()
    fun children(): List<Expression> = emptyList()
    fun copyWithChildren(children: List<Expression>) = this
}

interface Literal : Expression

data class IntegerExpr(val value: BigInteger) : Literal {
    constructor(value: Long) : this(BigInteger.valueOf(value))

    override fun toString(): String {
        return value.toString()
    }

}

data class DecimalExpr(val value: BigDecimal) : Literal {
    constructor(value: Double) : this(BigDecimal.valueOf(value))

    override fun toString(): String {
        return value.toString()
    }
}

data class VariableExpr(val name: String) : Expression {
    override fun variables(): Set<VariableExpr> = setOf(this)

    override fun toString(): String {
        return name
    }
}

data class UnaryExpr(val operator: UnaryOperator, val expr: Expression) : Expression {
    override fun children(): List<Expression> = listOf(expr)

    override fun copyWithChildren(children: List<Expression>): Expression {
        if (children.count() != 1) {
            throw java.lang.IllegalArgumentException()
        }

        return UnaryExpr(operator, children.elementAt(0))
    }

    override fun toString(): String {
        return "${operator}(${expr})"
    }
}

data class BinaryExpr(val operator: BinaryOperator, val left: Expression, val right: Expression) : Expression {
    override fun children(): List<Expression> = listOf(left, right)

    override fun copyWithChildren(children: List<Expression>): Expression {
        if (children.count() != 2) {
            throw java.lang.IllegalArgumentException()
        }

        return BinaryExpr(operator, children.elementAt(0), children.elementAt(1))
    }

    override fun toString(): String {
        return "${operator}(${left}, ${right})"
    }
}

data class NaryExpr(val operator: NaryOperator, val operands: List<Expression>) : Expression {
    override fun children(): List<Expression> = operands

    override fun copyWithChildren(children: List<Expression>): Expression {
        return NaryExpr(operator, children)
    }

    override fun toString(): String {
        return operands.joinToString(", ", "${operator}(", ")") { it.toString() }
    }
}

data class MixedNumber(val integer: IntegerExpr, val numerator: IntegerExpr, val denominator: IntegerExpr) :
    Expression {

    override fun children() = listOf<Expression>(integer, numerator, denominator)

    override fun copyWithChildren(children: List<Expression>): Expression {
        if (children.count() != 3 || children[0] !is IntegerExpr || children[1] !is IntegerExpr
            || children[2] !is IntegerExpr
        ) {
            throw java.lang.IllegalArgumentException()
        }
        return MixedNumber(children[0] as IntegerExpr, children[1] as IntegerExpr, children[2] as IntegerExpr)
    }

    override fun toString(): String {
        return "$integer $numerator/$denominator"
    }
}


fun xp(n: Int) = IntegerExpr(n.toBigInteger())
fun xp(n: BigInteger) = IntegerExpr(n)
fun xp(v: String) = VariableExpr(v)

fun bracketOf(expr: Expression) = UnaryExpr(UnaryOperator.Bracket, expr)

fun negOf(expr: Expression) = UnaryExpr(UnaryOperator.Minus, expr)

fun fractionOf(numerator: Expression, denominator: Expression) =
    BinaryExpr(BinaryOperator.Fraction, numerator, denominator)

fun powerOf(base: Expression, exponent: Expression) = BinaryExpr(BinaryOperator.Power, base, exponent)

fun sumOf(vararg terms: Expression) = NaryExpr(NaryOperator.Sum, terms.asList())

fun productOf(vararg factors: Expression) = NaryExpr(NaryOperator.Product, factors.asList())

fun implicitProductOf(vararg factors: Expression) = NaryExpr(NaryOperator.ImplicitProduct, factors.asList())
