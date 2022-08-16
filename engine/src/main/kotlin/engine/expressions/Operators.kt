package engine.expressions

import java.math.BigInteger

const val MAX_CHILD_COUNT = 1000

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

    fun <T : Expression> latexString(children: List<T>): String
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

    override fun <T : Expression> latexString(children: List<T>): String {
        require(children.isEmpty())
        return "{${latexString()}}"
    }

    abstract fun latexString(): String
}

data class IntegerOperator(val value: BigInteger) : NullaryOperator() {
    init {
        require(value.signum() >= 0)
    }

    override fun toString() = value.toString()

    override fun latexString() = value.toString()
}

data class VariableOperator(val name: String) : NullaryOperator() {
    override fun toString() = name

    override fun latexString() = name
}

object MixedNumberOperator : Operator {
    override val precedence = MAX_PRECEDENCE
    override val arity = ARITY_THREE

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        require(n in 0 until arity)
        return op is IntegerOperator
    }

    override fun <T> readableString(children: List<T>) = "[${children[0]} ${children[1]}/${children[2]}]"

    override fun <T : Expression> latexString(children: List<T>) =
        "{${children[0].toLatexString()}\\frac${children[1].toLatexString()}${children[2].toLatexString()}}"
}

enum class BracketOperator(
    private val opening: String,
    private val closing: String,
    private val latexOpening: String,
    private val latexClosing: String
) : Operator {
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

    override fun <T : Expression> latexString(children: List<T>): String {
        require(children.size == 1)
        return "{$latexOpening ${children[0].toLatexString()} $latexClosing}"
    }

    override fun equiv(other: Operator): Boolean {
        return other is BracketOperator
    }
}

enum class UnaryOperator(override val precedence: Int) : Operator {
    InvisibleBracket(MAX_PRECEDENCE) {
        override fun childAllowed(op: Operator) = true
        override fun <T> readableString(child: T) = "{$child}"
        override fun <T : Expression> latexString(child: T) = "{${child.toLatexString()}}"
    },
    DivideBy(DIVIDE_BY_PRECEDENCE) {
        override fun childAllowed(op: Operator) = op.precedence > NaryOperator.Product.precedence
        override fun <T> readableString(child: T) = ":$child"
        override fun <T : Expression> latexString(child: T) = "\\div ${child.toLatexString()}"
    },
    Plus(PLUS_MINUS_PRECEDENCE) {
        override fun <T> readableString(child: T) = "+$child"
        override fun <T : Expression> latexString(child: T) = "{+${child.toLatexString()}}"
    },
    Minus(PLUS_MINUS_PRECEDENCE) {
        override fun <T> readableString(child: T) = "-$child"
        override fun <T : Expression> latexString(child: T) = "{-${child.toLatexString()}}"
    },
    SquareRoot(ROOT_PRECEDENCE) {
        override fun childAllowed(op: Operator) = true
        override fun <T> readableString(child: T) = "sqrt[$child]"
        override fun <T : Expression> latexString(child: T) = "{\\sqrt${child.toLatexString()}}"
    },
    NaturalLog(NATURAL_LOG_PRECEDENCE) {
        override fun childAllowed(op: Operator) =
            op.precedence >= BinaryOperator.Fraction.precedence

        override fun <T : Expression> latexString(child: T) = "{\\ln${child.toLatexString()}}"
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

    abstract fun <T : Expression> latexString(child: T): String

    override fun <T : Expression> latexString(children: List<T>): String {
        require(children.size == arity)
        return latexString(children[0])
    }
}

enum class BinaryOperator(override val precedence: Int) : Operator {
    Fraction(FRACTION_PRECEDENCE) {
        override fun leftChildAllowed(op: Operator) = true
        override fun rightChildAllowed(op: Operator) = true
        override fun <T> readableString(left: T, right: T) = "[$left / $right]"
        override fun <T : Expression> latexString(left: T, right: T) =
            "{\\frac${left.toLatexString()}${right.toLatexString()}}"
    },
    Power(POWER_PRECEDENCE) {
        override fun leftChildAllowed(op: Operator) =
            op.precedence >= BracketOperator.Bracket.precedence

        override fun rightChildAllowed(op: Operator) = true

        override fun <T> readableString(left: T, right: T) = "[$left ^ $right]"

        override fun <T : Expression> latexString(left: T, right: T) =
            "{${left.toLatexString()} ^ ${right.toLatexString()}}"
    },
    Root(ROOT_PRECEDENCE) {
        override fun leftChildAllowed(op: Operator) = true
        override fun rightChildAllowed(op: Operator) = true
        override fun <T> readableString(left: T, right: T) = "root[$left, $right]"
        override fun <T : Expression> latexString(left: T, right: T) =
            "{\\sqrt[${right.toLatexString()}]${left.toLatexString()}}"
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

    abstract fun <T : Expression> latexString(left: T, right: T): String

    override fun <T : Expression> latexString(children: List<T>): String {
        require(children.size == arity)
        return latexString(children[0], children[1])
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

        override fun <T : Expression> latexString(children: List<T>): String {
            return buildString {
                append("{")
                for ((i, child) in children.withIndex()) {
                    if (i == 0) {
                        append(child.toLatexString())
                    } else if (child.operator == UnaryOperator.Minus) {
                        append(" {{} - ", child.operands[0].toLatexString(), "}")
                    } else {
                        append(" + ", child.toLatexString())
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

        override fun <T : Expression> latexString(children: List<T>): String {
            return buildString {
                append("{")
                for ((i, child) in children.withIndex()) {
                    if (i == 0) {
                        append(child.toLatexString())
                    } else if (child.operator == UnaryOperator.DivideBy) {
                        append(" {{} \\div ", child.operands[0].toLatexString(), "}")
                    } else {
                        append(" \\times ", child.toLatexString())
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

        override fun <T : Expression> latexString(children: List<T>): String {
            return children.joinToString(separator = " ", prefix = "{", postfix = "}") { it.toLatexString() }
        }
    };

    override val arity = ARITY_VARIABLE
    override fun nthChildAllowed(n: Int, op: Operator) = op.precedence > this.precedence
}
