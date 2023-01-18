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
            return children.joinToString(prefix = "{", postfix = "}")
        }

        override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
            return if (children.isEmpty()) {
                "\\emptyset"
            } else {
                children.joinToString(prefix = "\\left\\{", postfix = "\\right\\}") { it.toLatexString(ctx) }
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
