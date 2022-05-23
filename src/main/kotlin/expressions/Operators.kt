package expressions

interface Operator {
    val precedence: Int

    fun nthChildAllowed(n: Int, op: Operator): Boolean
}

object NullaryOperator : Operator {
    override val precedence = 10

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        throw IllegalArgumentException()
    }
}

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

    override fun nthChildAllowed(n: Int, op: Operator) = op.precedence > this.precedence
}
