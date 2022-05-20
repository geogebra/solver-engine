package expressions

import java.math.BigDecimal
import java.math.BigInteger

interface Expression {
    fun variables(): Set<VariableExpr> = emptySet()

    fun children(): List<Expression> = emptyList()

    fun mapChildrenIndexed(f: (i: Int, expr: Expression) -> Expression): Expression

    fun copyWithChildren(newChildren: List<Expression>): Expression

    fun getAt(path: Path): Expression {
        return when (path) {
            is ChildPath -> getAt(path.parent).children().elementAt(path.index)
            else -> this
        }
    }
}

interface Literal : Expression {
    override fun mapChildrenIndexed(f: (i: Int, expr: Expression) -> Expression) = this

    override fun copyWithChildren(newChildren: List<Expression>): Expression {
        if (newChildren.isNotEmpty()) {
            throw IllegalArgumentException()
        }
        return this
    }
}

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

    override fun mapChildrenIndexed(f: (i: Int, expr: Expression) -> Expression) = this


    override fun copyWithChildren(newChildren: List<Expression>): Expression {
        if (newChildren.isNotEmpty()) {
            throw IllegalArgumentException()
        }
        return this
    }

    override fun toString(): String {
        return name
    }
}

data class UnaryExpr(val operator: UnaryOperator, val expr: Expression) : Expression {
    override fun children(): List<Expression> = listOf(expr)

    override fun mapChildrenIndexed(f: (i: Int, expr: Expression) -> Expression) = copy(expr = f(0, expr))

    override fun copyWithChildren(newChildren: List<Expression>): Expression {
        if (newChildren.size != 1) {
            throw IllegalArgumentException()
        }
        return UnaryExpr(operator, newChildren[0])
    }


    override fun toString(): String {
        return "${operator}(${expr})"
    }
}

data class BinaryExpr(val operator: BinaryOperator, val left: Expression, val right: Expression) : Expression {
    override fun children(): List<Expression> = listOf(left, right)

    override fun mapChildrenIndexed(f: (i: Int, expr: Expression) -> Expression) =
        copy(left = f(0, left), right = f(1, right))


    override fun copyWithChildren(newChildren: List<Expression>): Expression {
        if (newChildren.size != 2) {
            throw IllegalArgumentException()
        }
        return BinaryExpr(operator, newChildren[0], newChildren[1])
    }

    override fun toString(): String {
        return "${operator}(${left}, ${right})"
    }
}

data class NaryExpr(val operator: NaryOperator, val operands: List<Expression>) : Expression {
    override fun children(): List<Expression> = operands

    override fun mapChildrenIndexed(f: (i: Int, expr: Expression) -> Expression) =
        copy(operands = operands.mapIndexed(f))


    override fun copyWithChildren(newChildren: List<Expression>): Expression {
        if (newChildren.size != 2) {
            throw IllegalArgumentException()
        }
        return NaryExpr(operator, newChildren)
    }

    override fun toString(): String {
        return operands.joinToString(", ", "${operator}(", ")") { it.toString() }
    }
}

data class MixedNumber(val integer: IntegerExpr, val numerator: IntegerExpr, val denominator: IntegerExpr) :
    Expression {

    override fun children() = listOf<Expression>(integer, numerator, denominator)


    override fun copyWithChildren(newChildren: List<Expression>): Expression {
        if (newChildren.size != 3) {
            throw IllegalArgumentException()
        }
        return MixedNumber(newChildren[0] as IntegerExpr, newChildren[1] as IntegerExpr, newChildren[2] as IntegerExpr)
    }

    override fun mapChildrenIndexed(f: (i: Int, expr: Expression) -> Expression) = MixedNumber(
        f(0, integer) as IntegerExpr,
        f(1, numerator) as IntegerExpr,
        f(2, denominator) as IntegerExpr,
    )

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
