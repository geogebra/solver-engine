package engine.operators

private const val EQUATION_PRECEDENCE = 0
private const val EQUATION_SYSTEM_PRECEDENCE = -10

interface StatementOperator : Operator {
    override val kind get() = OperatorKind.STATEMENT
}

object EquationOperator : BinaryOperator, StatementOperator {
    override val precedence = EQUATION_PRECEDENCE

    override fun leftChildAllowed(op: Operator): Boolean {
        require(op.kind == OperatorKind.EXPRESSION)
        return true
    }

    override fun rightChildAllowed(op: Operator): Boolean {
        require(op.kind == OperatorKind.EXPRESSION)
        return true
    }

    override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable): String {
        if (ctx.align) {
            val innerCtx = ctx.copy(align = false)
            return "${left.toLatexString(innerCtx)} & = & ${right.toLatexString(innerCtx)}"
        } else {
            return "{${left.toLatexString(ctx)} = ${right.toLatexString(ctx)}}"
        }
    }

    override fun <T> readableString(left: T, right: T) = "$left = $right"
}

object EquationSystemOperator : StatementOperator {
    override val precedence = EQUATION_SYSTEM_PRECEDENCE
    override val arity = ARITY_VARIABLE

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        require(op is EquationOperator)
        return true
    }

    override fun <T> readableString(children: List<T>) = children.joinToString(", ")

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
        val alignCtx = ctx.copy(align = true)
        return buildString {
            append("\\left\\{\\begin{array}{rcl}")
            for (eq in children) {
                append(eq.toLatexString(alignCtx), " \\\\ ")
            }
            append("\\end{array}\\right.")
        }
    }
}
