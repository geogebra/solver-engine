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

    fun <T> readableString(children: List<T>): String {
        return "${toString()}(${children.joinToString(", ")})"
    }
}

abstract class NullaryOperator : Operator {
    override val precedence = 10
    override val arity = 0

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        throw IllegalArgumentException()
    }

    override fun <T> readableString(children: List<T>): String {
        require(children.isEmpty())
        return toString()
    }
}

data class IntegerOperator(val value: BigInteger) : NullaryOperator() {
    init {
        require(value.signum() >= 0)
    }

    override fun toString() = value.toString()
}

data class VariableOperator(val name: String) : NullaryOperator() {
    override fun toString() = name
}

object MixedNumberOperator : Operator {
    override val precedence = 10
    override val arity = 3

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        require(n in 0 until 3)
        return op is IntegerOperator
    }

    override fun <T> readableString(children: List<T>) = "[${children[0]} ${children[1]}/${children[2]}]"
}

enum class UnaryOperator(override val precedence: Int) : Operator {
    Bracket(10) {
        override fun childAllowed(op: Operator) = true
        override fun <T> readableString(child: T) = "($child)"
    },
    DivideBy(9) {
        override fun childAllowed(op: Operator) = op.precedence > NaryOperator.Product.precedence
        override fun <T> readableString(child: T) = ":$child"
    },
    Plus(9) {
        override fun childAllowed(op: Operator) =
            op.precedence >= NaryOperator.Product.precedence

        override fun <T> readableString(child: T) = "+$child"
    },
    Minus(9) {
        override fun childAllowed(op: Operator) =
            op.precedence >= NaryOperator.Product.precedence

        override fun <T> readableString(child: T) = "-$child"
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

    open fun <T> readableString(child: T): String {
        return "${this}($child)"
    }

    override fun <T> readableString(children: List<T>): String {
        require(children.size == 1)
        return readableString(children[0])
    }
}

enum class BinaryOperator(override val precedence: Int) : Operator {
    Fraction(5) {
        override fun leftChildAllowed(op: Operator) = true
        override fun rightChildAllowed(op: Operator) = true
        override fun <T> readableString(left: T, right: T) = "[$left / $right]"
    },
    Divide(3) {
        override fun leftChildAllowed(op: Operator) =
            op.precedence >= NaryOperator.ImplicitProduct.precedence

        override fun rightChildAllowed(op: Operator) =
            op.precedence >= NaryOperator.ImplicitProduct.precedence

        override fun <T> readableString(left: T, right: T) = "$left:$right"
    },
    Power(6) {
        override fun leftChildAllowed(op: Operator) =
            op.precedence >= UnaryOperator.Bracket.precedence

        override fun rightChildAllowed(op: Operator) = true

        override fun <T> readableString(left: T, right: T) = "[$left ^ $right]"
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

    open fun <T> readableString(left: T, right: T): String {
        return "${this}($left, $right)"
    }

    override fun <T> readableString(children: List<T>): String {
        require(children.size == 2)
        return readableString(children[0], children[1])
    }
}

enum class NaryOperator(override val precedence: Int) : Operator {
    Sum(1) {
        override fun <T> readableString(children: List<T>): String {
            return buildString {
                for ((i, child) in children.map { it.toString() }.withIndex()) {
                    if (i == 0) {
                        append(child)
                    } else if (child.startsWith("-")) {
                        append(" - ")
                        append(child.removePrefix("-"))
                    } else {
                        append(" + ")
                        append(child)
                    }
                }
            }
        }
    },
    Product(2) {
        override fun <T> readableString(children: List<T>): String {
            return buildString {
                for ((i, child) in children.map { it.toString() }.withIndex()) {
                    if (i == 0) {
                        append(child)
                    } else if (child.startsWith(":")) {
                        append(" : ")
                        append(child.removePrefix(":"))
                    } else {
                        append(" * ")
                        append(child)
                    }
                }
            }
        }
    },
    ImplicitProduct(4) {
        override fun <T> readableString(children: List<T>): String {
            return children.joinToString("")
        }
    };

    override val arity = -1
    override fun nthChildAllowed(n: Int, op: Operator) = op.precedence > this.precedence
}
