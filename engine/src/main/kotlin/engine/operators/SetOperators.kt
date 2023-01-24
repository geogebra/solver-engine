package engine.operators

private const val SET_PRECEDENCE = 0

enum class SetOperators : Operator {

    FiniteSet {
        override val arity = ARITY_VARIABLE
        override val kind = OperatorKind.SET
        override val precedence = SET_PRECEDENCE

        override fun minChildCount() = 0

        override fun nthChildAllowed(n: Int, op: Operator): Boolean {
            require(op.kind == OperatorKind.EXPRESSION)
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
                    postfix = "\\right\\}"
                ) { it.toLatexString(ctx) }
            }
        }
    },

    Reals {
        override val arity = ARITY_NULL
        override val kind = OperatorKind.SET
        override val precedence = SET_PRECEDENCE

        override fun nthChildAllowed(n: Int, op: Operator): Boolean {
            throw IllegalArgumentException(
                "Nullary operator ${this::class.simpleName} should have no children. " +
                    "Child $op is invalid at position $n."
            )
        }

        override fun <T> readableString(children: List<T>) = "REALS"
        override fun latexString(ctx: RenderContext, children: List<LatexRenderable>) = "\\mathbb{R}"
    }
}

data class IntervalOperator(
    private val closedLeft: Boolean,
    private val closedRight: Boolean
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
