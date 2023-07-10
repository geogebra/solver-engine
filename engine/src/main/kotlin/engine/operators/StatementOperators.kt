package engine.operators

import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.closedOpenIntervalOf
import engine.expressions.openClosedIntervalOf
import engine.expressions.openIntervalOf
import engine.expressions.setUnionOf
import engine.sign.Sign
import java.math.BigDecimal

private const val PREDICATE_PRECEDENCE = 0
private const val EQUATION_SYSTEM_PRECEDENCE = -10
private const val EQUATION_UNION_PRECEDENCE = -20
private const val STATEMENT_WITH_CONSTRAINT_PRECEDENCE = -15

internal interface StatementOperator : Operator {
    override val kind get() = OperatorKind.STATEMENT
}

internal object StatementWithConstraintOperator : BinaryOperator, StatementOperator {
    override val name = "StatementWithConstraint"

    override val precedence = STATEMENT_WITH_CONSTRAINT_PRECEDENCE

    override fun leftChildAllowed(op: Operator): Boolean {
        require(op.kind == OperatorKind.STATEMENT)
        return true
    }

    override fun rightChildAllowed(op: Operator): Boolean {
        require(op.kind == OperatorKind.STATEMENT)
        return true
    }

    override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable): String {
        val innerCtx = ctx.copy(align = false)
        return buildString {
            append("\\left\\{\\begin{array}{l}")
            append(left.toLatexString(innerCtx), " \\\\")
            append(right.toLatexString(innerCtx), " \\\\")
            append("\\end{array}\\right.")
        }
    }

    override fun <T> readableString(left: T, right: T): String {
        return "$left GIVEN $right"
    }
}

internal object EquationOperator : BinaryOperator, StatementOperator {
    override val name = "Equation"

    override val precedence = PREDICATE_PRECEDENCE

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
            return "${left.toLatexString(ctx)} = ${right.toLatexString(ctx)}"
        }
    }

    override fun <T> readableString(left: T, right: T) = "$left = $right"

    fun getDual() = EquationOperator
}

internal sealed interface EquationOperation

internal object AddEquationsOperator : BinaryOperator, StatementOperator, EquationOperation {
    override fun <T> readableString(left: T, right: T): String {
        return "$left /+/ $right"
    }

    override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable): String {
        val alignCtx = ctx.copy(align = true)
        return buildString {
            append("\\begin{array}{rcl}")
            append(left.toLatexString(alignCtx), " & + \\\\ ")
            append(right.toLatexString(alignCtx), " & \\\\ ")
            append("\\end{array}")
        }
    }

    override val name = "AddEquations"
    override val precedence = EQUATION_SYSTEM_PRECEDENCE
}

internal object SubtractEquationsOperator : BinaryOperator, StatementOperator, EquationOperation {
    override fun <T> readableString(left: T, right: T): String {
        return "$left /-/ $right"
    }

    override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable): String {
        val alignCtx = ctx.copy(align = true)
        return buildString {
            append("\\begin{array}{rcl}")
            append(left.toLatexString(alignCtx), " & - \\\\ ")
            append(right.toLatexString(alignCtx), " & \\\\ ")
            append("\\end{array}")
        }
    }

    override val name = "SubtractEquations"
    override val precedence = EQUATION_SYSTEM_PRECEDENCE
}

internal enum class InequalityOperators(
    val readableString: String,
    val latexString: String,
) : BinaryOperator, StatementOperator {

    LessThan("<", "<") {
        override fun holdsFor(val1: BigDecimal, val2: BigDecimal) = val1 < val2

        override fun holdsFor(sign: Sign) = sign == Sign.NEGATIVE

        override fun toInterval(boundary: Expression) = openIntervalOf(Constants.NegativeInfinity, boundary)

        override fun getDual() = GreaterThan
    },

    LessThanEqual("<=", "\\leq") {
        override fun holdsFor(val1: BigDecimal, val2: BigDecimal) = val1 <= val2

        override fun holdsFor(sign: Sign) = sign == Sign.NEGATIVE || sign == Sign.ZERO

        override fun toInterval(boundary: Expression) = openClosedIntervalOf(Constants.NegativeInfinity, boundary)

        override fun getDual() = GreaterThanEqual
    },

    GreaterThan(">", ">") {
        override fun holdsFor(val1: BigDecimal, val2: BigDecimal) = val1 > val2

        override fun holdsFor(sign: Sign) = sign == Sign.POSITIVE

        override fun toInterval(boundary: Expression) = openIntervalOf(boundary, Constants.Infinity)

        override fun getDual() = LessThan
    },

    GreaterThanEqual(">=", "\\geq") {
        override fun holdsFor(val1: BigDecimal, val2: BigDecimal) = val1 >= val2

        override fun holdsFor(sign: Sign) = sign == Sign.POSITIVE || sign == Sign.ZERO

        override fun toInterval(boundary: Expression) = closedOpenIntervalOf(boundary, Constants.Infinity)

        override fun getDual() = LessThanEqual
    },

    NotEqual("!=", "\\neq") {
        override fun holdsFor(val1: BigDecimal, val2: BigDecimal) = val1 != val2

        override fun holdsFor(sign: Sign) = sign != Sign.ZERO

        override fun toInterval(boundary: Expression) = setUnionOf(
            openIntervalOf(Constants.NegativeInfinity, boundary),
            openIntervalOf(boundary, Constants.Infinity),
        )

        override fun getDual() = NotEqual
    }, ;

    override val precedence = PREDICATE_PRECEDENCE

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
            return "${left.toLatexString(innerCtx)} & $latexString & ${right.toLatexString(innerCtx)}"
        } else {
            return "${left.toLatexString(ctx)} $latexString ${right.toLatexString(ctx)}"
        }
    }

    override fun <T> readableString(left: T, right: T) = "$left $readableString $right"

    abstract fun holdsFor(val1: BigDecimal, val2: BigDecimal): Boolean

    abstract fun holdsFor(sign: Sign): Boolean?

    abstract fun toInterval(boundary: Expression): Expression

    abstract fun getDual(): InequalityOperators

    companion object {
        fun fromReadableString(value: String): InequalityOperators? {
            return values().find { it.readableString == value }
        }
    }
}

internal data class DoubleInequalityOperator(
    val leftOp: InequalityOperators,
    val rightOp: InequalityOperators,
) : TernaryOperator, StatementOperator {
    override val name = when {
        leftOp == InequalityOperators.LessThan && rightOp == InequalityOperators.LessThan -> "OpenRange"
        leftOp == InequalityOperators.LessThan && rightOp == InequalityOperators.LessThanEqual -> "OpenClosedRange"
        leftOp == InequalityOperators.LessThanEqual && rightOp == InequalityOperators.LessThan -> "ClosedOpenRange"
        leftOp == InequalityOperators.LessThanEqual && rightOp == InequalityOperators.LessThanEqual -> "ClosedRange"
        leftOp == InequalityOperators.GreaterThan && rightOp == InequalityOperators.GreaterThan -> "ReversedOpenRange"
        leftOp == InequalityOperators.GreaterThan &&
            rightOp == InequalityOperators.GreaterThanEqual -> "ReversedOpenClosedRange"
        leftOp == InequalityOperators.GreaterThanEqual &&
            rightOp == InequalityOperators.GreaterThan -> "ReversedClosedOpenRange"
        leftOp == InequalityOperators.GreaterThanEqual &&
            rightOp == InequalityOperators.GreaterThanEqual -> "ReversedClosedRange"
        else -> throw error("Invalid operators $leftOp and $rightOp for ${this::class.simpleName}")
    }

    override val precedence = PREDICATE_PRECEDENCE

    override fun latexString(
        ctx: RenderContext,
        first: LatexRenderable,
        second: LatexRenderable,
        third: LatexRenderable,
    ): String {
        if (ctx.align) {
            val innerCtx = ctx.copy(align = false)
            return first.toLatexString(innerCtx) +
                " & " +
                leftOp +
                " & " +
                second.toLatexString(innerCtx) +
                " & " +
                rightOp +
                " & " +
                third.toLatexString(innerCtx)
        } else {
            return first.toLatexString(ctx) + " " + leftOp +
                " " + second.toLatexString(ctx) + " " +
                rightOp + " " + third.toLatexString(ctx)
        }
    }

    override fun <T> readableString(first: T, second: T, third: T): String {
        return "$first ${leftOp.readableString} $second ${rightOp.readableString} $third"
    }
}

internal object EquationSystemOperator : StatementOperator {
    override val name = "EquationSystem"

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

internal object InequalitySystemOperator : StatementOperator {
    override val name = "InequalitySystem"

    override val precedence = EQUATION_SYSTEM_PRECEDENCE
    override val arity = ARITY_VARIABLE

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        require(op is InequalityOperators)
        return true
    }

    override fun <T> readableString(children: List<T>): String {
        return children.joinToString(", ")
    }

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

internal object StatementUnionOperator : StatementOperator {
    override val name = "EquationUnion"

    override val precedence = EQUATION_UNION_PRECEDENCE
    override val arity = ARITY_VARIABLE

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        require(op is StatementOperator)
        return true
    }

    override fun <T> readableString(children: List<T>) = children.joinToString(" OR ")

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
        return "${children[0].toLatexString(ctx)} \\text{or} ${children[1].toLatexString(ctx)}"
    }
}

internal enum class SolutionOperator : BinaryOperator, StatementOperator {

    Identity {
        override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable): String {
            return "${left.toLatexString(ctx)} \\in \\emptyset"
        }
    },
    Contradiction {
        override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable): String {
            return "${left.toLatexString(ctx)} \\in \\mathbb{R}"
        }
    },
    ImplicitSolution {
        override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable): String {
            return "${left.toLatexString(ctx)} \\in \\mathbb{R} : ${right.toLatexString(ctx)}"
        }
    },
    SetSolution {
        override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable): String {
            return "${left.toLatexString(ctx)} \\in ${right.toLatexString(ctx)}"
        }
    },
    ;

    override val precedence = PREDICATE_PRECEDENCE

    override fun leftChildAllowed(op: Operator): Boolean {
        check(op == VariableListOperator)
        return true
    }

    override fun rightChildAllowed(op: Operator) = true

    override fun <T> readableString(left: T, right: T): String {
        val leftString = left.toString()
        val leftWithSeparator = if (leftString == "") "" else "$leftString : "
        return "$name[$leftWithSeparator$right]"
    }

    override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable): String {
        return "${left.toLatexString(ctx)} \\in \\emptyset"
    }

    fun variables(children: List<Expression>): List<String> {
        return children[0].children.map { it.operator.name }
    }
}

internal object VariableListOperator : Operator {
    override val name = "VariableList"
    override val precedence = 0
    override val arity = ARITY_VARIABLE_FROM_ZERO
    override val kind = OperatorKind.INNER
    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        check(op is VariableOperator)
        return true
    }

    override fun <T> readableString(children: List<T>): String {
        if (children.size == 1) {
            return children[0].toString()
        }
        return children.joinToString(", ")
    }
    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
        if (children.size == 1) {
            return children[0].toLatexString(ctx)
        }
        return children.map { it.toLatexString(ctx) }.joinToString(", ")
    }

    fun variables(children: List<Expression>): List<String> {
        return children.map { it.operator.name }
    }
}
