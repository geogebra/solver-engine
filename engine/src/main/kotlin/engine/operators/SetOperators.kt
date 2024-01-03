package engine.operators

import engine.expressions.Expression

private const val SET_PRECEDENCE = 0

internal enum class SetOperators : Operator {
    FiniteSet {
        override val arity = ARITY_VARIABLE
        override val kind = OperatorKind.SET
        override val precedence = SET_PRECEDENCE

        override fun minChildCount() = 0

        override fun nthChildAllowed(n: Int, op: Operator): Boolean {
            require(op.kind == OperatorKind.EXPRESSION || op.kind == OperatorKind.SET_ELEMENT)
            return true
        }

        override fun <T> readableString(children: List<T>): String {
            return children.joinToString(separator = ", ", prefix = "{", postfix = "}")
        }

        override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
            return if (children.isEmpty()) {
                "\\emptyset"
            } else {
                children.joinToString(
                    separator = ", ",
                    prefix = "\\left\\{",
                    postfix = "\\right\\}",
                ) { it.toLatexString(ctx) }
            }
        }
    },

    CartesianProduct {
        override val arity = ARITY_VARIABLE
        override val kind = OperatorKind.SET
        override val precedence = SET_PRECEDENCE

        override fun minChildCount() = 0

        override fun nthChildAllowed(n: Int, op: Operator): Boolean {
            require(op.kind == OperatorKind.SET)
            return true
        }

        override fun <T> readableString(children: List<T>): String {
            return children.joinToString(separator = "*")
        }

        override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
            return if (children.isEmpty()) {
                "\\emptyset"
            } else {
                children.joinToString(" \\times ") { it.toLatexString(ctx) }
            }
        }
    },

    SetUnion {
        override val arity = ARITY_VARIABLE
        override val kind = OperatorKind.SET
        override val precedence = SET_PRECEDENCE

        override fun minChildCount() = 0

        override fun nthChildAllowed(n: Int, op: Operator): Boolean {
            require(op.kind == OperatorKind.SET)
            return true
        }

        override fun <T> readableString(children: List<T>): String {
            return "SetUnion[" + children.joinToString(separator = ", ") + "]"
        }

        override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
            return if (children.isEmpty()) {
                "\\emptyset"
            } else {
                children.joinToString(", ") { it.toLatexString(ctx) }
            }
        }
    },

    SetDifference {
        override val arity = 2
        override val precedence = SET_PRECEDENCE
        override val kind = OperatorKind.SET

        override fun nthChildAllowed(n: Int, op: Operator): Boolean {
            require(op.kind == OperatorKind.SET)
            return true
        }

        override fun <T> readableString(children: List<T>): String {
            return "${children[0]} \\ ${children[1]}"
        }

        override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
            return "${children[0].toLatexString(ctx)} \\setminus ${children[1].toLatexString(ctx)}"
        }
    },

    Reals {
        override val arity = ARITY_NULL
        override val kind = OperatorKind.SET
        override val precedence = SET_PRECEDENCE

        override fun nthChildAllowed(n: Int, op: Operator): Boolean {
            throw IllegalArgumentException(
                "Nullary operator ${this::class.simpleName} should have no children. " +
                    "Child $op is invalid at position $n.",
            )
        }

        override fun <T> readableString(children: List<T>) = "/reals/"

        override fun latexString(ctx: RenderContext, children: List<LatexRenderable>) = "\\mathbb{R}"
    },
}

data class IntervalOperator(
    val closedLeft: Boolean,
    val closedRight: Boolean,
) : BinaryOperator {
    override val name = when {
        closedLeft && closedRight -> "ClosedInterval"
        !closedLeft && closedRight -> "OpenClosedInterval"
        closedLeft && !closedRight -> "ClosedOpenInterval"
        else -> "OpenInterval"
    }

    override val kind = OperatorKind.SET
    override val precedence = SET_PRECEDENCE

    override fun <T> readableString(left: T, right: T): String {
        return "${if (closedLeft) "[" else "("} $left, $right ${if (closedRight) "]" else ")"}"
    }

    override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable): String {
        return "${if (closedLeft) "\\left[" else "\\left("} ${left.toLatexString(ctx)}, " +
            "${right.toLatexString(ctx)} ${if (closedRight) "\\right]" else "\\right)"}"
    }
}

internal object TupleOperator : Operator {
    override val name = "Tuple"
    override val precedence = 0
    override val arity = ARITY_VARIABLE_FROM_ZERO
    override val kind = OperatorKind.SET_ELEMENT

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        check(op.kind == OperatorKind.EXPRESSION)
        return true
    }

    override fun <T> readableString(children: List<T>): String {
        if (children.size == 1) {
            return children[0].toString()
        }
        return "(${children.joinToString(", ")})"
    }

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
        if (children.size == 1) {
            return children[0].toLatexString(ctx)
        }
        return "\\left(${children.map { it.toLatexString(ctx) }.joinToString(", ")}\\right)"
    }

    fun variables(children: List<Expression>): List<String> {
        return children.map { it.operator.name }
    }
}
