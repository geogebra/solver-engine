package expressions

import java.math.BigInteger

interface Operator {
    val precedence: Int
    val arity: Int
    fun nthChildAllowed(n: Int, op: Operator): Boolean

    fun childrenAllowed(ops: Iterable<Operator>): Boolean {
        return ops.withIndex().all { (i, op) -> nthChildAllowed(i, op) }
    }

    fun minChildCount(): Int = if (arity >= 0) arity else 2
    fun maxChildCount(): Int = if (arity >= 0) arity else 1000
}

abstract class NullaryOperator : Operator {
    override val precedence = 10
    override val arity = 0

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        throw IllegalArgumentException()
    }

}

data class IntegerOperator(val value: BigInteger) : NullaryOperator()

data class VariableOperator(val name: String) : NullaryOperator()

data class MixedNumberOperator(
    val integer: BigInteger,
    val numerator: BigInteger,
    val denominator: BigInteger
) : NullaryOperator()

enum class UnaryOperator(override val precedence: Int) : Operator {
    Bracket(10) {
        override fun childAllowed(op: Operator) = true
    },
    Plus(9) {
        override fun childAllowed(op: Operator) =
            op.precedence >= NaryOperator.Product.precedence
    },
    Minus(9) {
        override fun childAllowed(op: Operator) =
            op.precedence >= NaryOperator.Product.precedence
    },
    SquareRoot(10) {
        override fun childAllowed(op: Operator) = true
    },
    NaturalLog(5) {
        override fun childAllowed(op: Operator) =
            op.precedence >= BinaryOperator.Fraction.precedence
    };

    override val arity = 1
    open fun childAllowed(op: Operator) = op.precedence > this.precedence

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        require(n == 0)
        return childAllowed(op)
    }
}

enum class BinaryOperator(override val precedence: Int) : Operator {
    Fraction(5) {
        override fun leftChildAllowed(op: Operator) = true
        override fun rightChildAllowed(op: Operator) = true
    },
    Divide(3) {
        override fun leftChildAllowed(op: Operator) =
            op.precedence >= NaryOperator.ImplicitProduct.precedence

        override fun rightChildAllowed(op: Operator) =
            op.precedence >= NaryOperator.ImplicitProduct.precedence

    },
    Power(6) {
        override fun leftChildAllowed(op: Operator) =
            op.precedence >= UnaryOperator.Bracket.precedence

        override fun rightChildAllowed(op: Operator) = true
    },
    Root(10) {
        override fun leftChildAllowed(op: Operator) = true
        override fun rightChildAllowed(op: Operator) = true
    };

    override val arity = 2

    open fun leftChildAllowed(op: Operator) = op.precedence > this.precedence
    open fun rightChildAllowed(op: Operator) = op.precedence > this.precedence

    override fun nthChildAllowed(n: Int, op: Operator) = when (n) {
        0 -> leftChildAllowed(op)
        1 -> rightChildAllowed(op)
        else -> throw IllegalArgumentException()
    }
}

enum class NaryOperator(override val precedence: Int) : Operator {
    Sum(1),
    Product(2),
    ImplicitProduct(4);

    override val arity = -1
    override fun nthChildAllowed(n: Int, op: Operator) = op.precedence > this.precedence
}
