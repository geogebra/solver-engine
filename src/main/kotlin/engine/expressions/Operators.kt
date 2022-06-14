package engine.expressions

import java.math.BigInteger

const val MAX_CHILD_COUNT = 1000

private const val SUM_PRECEDENCE = 10
private const val PLUS_MINUS_PRECEDENCE = 15
private const val PRODUCT_PRECEDENCE = 20
private const val DIVIDE_PRECEDENCE = 30
private const val IMPLICIT_PRODUCT_PRECEDENCE = 40
private const val FRACTION_PRECEDENCE = 50
private const val POWER_PRECEDENCE = 60
private const val NATURAL_LOG_PRECEDENCE = 50
private const val DIVIDE_BY_PRECEDENCE = 90
private const val MAX_PRECEDENCE = 100

private const val ARITY_NULL = 0
private const val ARITY_ONE = 1
private const val ARITY_TWO = 2
private const val ARITY_THREE = 3
private const val ARITY_VARIABLE = -1

interface Operator {
    val precedence: Int
    val arity: Int

    fun equiv(other: Operator) = this == other
    fun nthChildAllowed(n: Int, op: Operator): Boolean

    fun childrenAllowed(ops: Iterable<Operator>): Boolean {
        return ops.withIndex().all { (i, op) -> nthChildAllowed(i, op) }
    }

    fun minChildCount(): Int = if (arity == ARITY_VARIABLE) 2 else arity
    fun maxChildCount(): Int = if (arity == ARITY_VARIABLE) MAX_CHILD_COUNT else arity

    fun <T> readableString(children: List<T>): String {
        return "${toString()}(${children.joinToString(", ")})"
    }
}

abstract class NullaryOperator : Operator {
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
    override val precedence = MAX_PRECEDENCE
    override val arity = ARITY_THREE

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        require(n in 0 until arity)
        return op is IntegerOperator
    }

    override fun <T> readableString(children: List<T>) = "[${children[0]} ${children[1]}/${children[2]}]"
}

enum class BracketOperator(private val opening: String, private val closing: String) : Operator {
    Bracket("(", ")"),
    SquareBracket("[.", ".]"),
    CurlyBracket("{.", ".}");

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

    override fun equiv(other: Operator): Boolean {
        return other is BracketOperator
    }
}

enum class UnaryOperator(override val precedence: Int) : Operator {
    InvisibleBracket(MAX_PRECEDENCE) {
        override fun childAllowed(op: Operator) = true
        override fun <T> readableString(child: T) = "{$child}"
    },
    DivideBy(DIVIDE_BY_PRECEDENCE) {
        override fun childAllowed(op: Operator) = op.precedence > NaryOperator.Product.precedence
        override fun <T> readableString(child: T) = ":$child"
    },
    Plus(PLUS_MINUS_PRECEDENCE) {
        override fun <T> readableString(child: T) = "+$child"
    },
    Minus(PLUS_MINUS_PRECEDENCE) {
        override fun <T> readableString(child: T) = "-$child"
    },
    SquareRoot(MAX_PRECEDENCE) {
        override fun childAllowed(op: Operator) = true
    },
    NaturalLog(NATURAL_LOG_PRECEDENCE) {
        override fun childAllowed(op: Operator) =
            op.precedence >= BinaryOperator.Fraction.precedence
    };

    override val arity = ARITY_ONE
    open fun childAllowed(op: Operator) = op.precedence > this.precedence

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        require(n == 0)
        return childAllowed(op)
    }

    open fun <T> readableString(child: T): String {
        return "$this($child)"
    }

    override fun <T> readableString(children: List<T>): String {
        require(children.size == arity)
        return readableString(children[0])
    }
}

enum class BinaryOperator(override val precedence: Int) : Operator {
    Fraction(FRACTION_PRECEDENCE) {
        override fun leftChildAllowed(op: Operator) = true
        override fun rightChildAllowed(op: Operator) = true
        override fun <T> readableString(left: T, right: T) = "[$left / $right]"
    },
    Divide(DIVIDE_PRECEDENCE) {
        override fun leftChildAllowed(op: Operator) =
            op.precedence >= NaryOperator.ImplicitProduct.precedence

        override fun rightChildAllowed(op: Operator) =
            op.precedence >= NaryOperator.ImplicitProduct.precedence

        override fun <T> readableString(left: T, right: T) = "$left:$right"
    },
    Power(POWER_PRECEDENCE) {
        override fun leftChildAllowed(op: Operator) =
            op.precedence >= BracketOperator.Bracket.precedence

        override fun rightChildAllowed(op: Operator) = true

        override fun <T> readableString(left: T, right: T) = "[$left ^ $right]"
    },
    Root(MAX_PRECEDENCE) {
        override fun leftChildAllowed(op: Operator) = true
        override fun rightChildAllowed(op: Operator) = true
    };

    override val arity = ARITY_TWO

    open fun leftChildAllowed(op: Operator) = op.precedence > this.precedence
    open fun rightChildAllowed(op: Operator) = op.precedence > this.precedence

    override fun nthChildAllowed(n: Int, op: Operator) = when (n) {
        0 -> leftChildAllowed(op)
        1 -> rightChildAllowed(op)
        else -> throw IllegalArgumentException(
            "Binary operator ${this::class.simpleName} should have exactly two children. " +
                "Child $op is invalid at position $n."
        )
    }

    open fun <T> readableString(left: T, right: T): String {
        return "$this($left, $right)"
    }

    override fun <T> readableString(children: List<T>): String {
        require(children.size == arity)
        return readableString(children[0], children[1])
    }
}

enum class NaryOperator(override val precedence: Int) : Operator {
    Sum(SUM_PRECEDENCE) {
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
    Product(PRODUCT_PRECEDENCE) {
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
    ImplicitProduct(IMPLICIT_PRODUCT_PRECEDENCE) {
        override fun <T> readableString(children: List<T>): String {
            return children.joinToString("")
        }
    };

    override val arity = ARITY_VARIABLE
    override fun nthChildAllowed(n: Int, op: Operator) = op.precedence > this.precedence
}
