package expressions

import java.math.BigDecimal
import java.math.BigInteger

interface Expression {
    fun variables(): Set<VariableExpr> = emptySet()
    fun children(): Sequence<Expression> = emptySequence()
    fun copyWithChildren(children: Sequence<Expression>) = this
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
    override fun children(): Sequence<Expression> = sequenceOf(expr)

    override fun copyWithChildren(children: Sequence<Expression>): Expression {
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
    override fun children(): Sequence<Expression> = sequenceOf(left, right)

    override fun copyWithChildren(children: Sequence<Expression>): Expression {
        if (children.count() != 2) {
            throw java.lang.IllegalArgumentException()
        }

        return BinaryExpr(operator, children.elementAt(0), children.elementAt(1))
    }

    override fun toString(): String {
        return "${operator}(${left}, ${right})"
    }
}

data class NaryExpr(val operator: NaryOperator, val operands: Sequence<Expression>) : Expression {
    override fun children(): Sequence<Expression> = operands

    override fun copyWithChildren(children: Sequence<Expression>): Expression {
        return NaryExpr(operator, children)
    }

    override fun toString(): String {
        return operands.map { it.toString() }.joinToString(", ", "${operator}(", ")")
    }
}


fun fractionOf(numerator: Expression, denominator: Expression)
        = BinaryExpr(BinaryOperator.Fraction, numerator, denominator)

fun sumOf(vararg terms: Expression)
        = NaryExpr(NaryOperator.Sum, terms.asSequence())