package engine.operators

import engine.expressions.Constants
import engine.expressions.Expression
import engine.expressions.closedOpenIntervalOf
import engine.expressions.openClosedIntervalOf
import engine.expressions.openIntervalOf
import java.math.BigDecimal

private const val PREDICATE_PRECEDENCE = 0
private const val EQUATION_SYSTEM_PRECEDENCE = -10
private const val EQUATION_UNION_PRECEDENCE = -20

interface StatementOperator : Operator {
    override val kind get() = OperatorKind.STATEMENT
}

object EquationOperator : BinaryOperator, StatementOperator {
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
}

sealed interface EquationOperation

object AddEquationsOperator : BinaryOperator, StatementOperator, EquationOperation {
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

object SubtractEquationsOperator : BinaryOperator, StatementOperator, EquationOperation {
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

enum class InequalityOperators(
    private val readableString: String,
    private val latexString: String,
) : BinaryOperator, StatementOperator {

    LessThan("<", "<") {
        override fun holdsFor(val1: BigDecimal, val2: BigDecimal) = val1 < val2

        override fun toInterval(boundary: Expression) = openIntervalOf(Constants.NegativeInfinity, boundary)

        override fun getDual() = GreaterThan
    },

    LessThanEqual("<=", "\\leq") {
        override fun holdsFor(val1: BigDecimal, val2: BigDecimal) = val1 <= val2

        override fun toInterval(boundary: Expression) = openClosedIntervalOf(Constants.NegativeInfinity, boundary)

        override fun getDual() = GreaterThanEqual
    },

    GreaterThan(">", ">") {
        override fun holdsFor(val1: BigDecimal, val2: BigDecimal) = val1 > val2

        override fun toInterval(boundary: Expression) = openIntervalOf(boundary, Constants.Infinity)

        override fun getDual() = LessThan
    },

    GreaterThanEqual(">=", "\\geq") {
        override fun holdsFor(val1: BigDecimal, val2: BigDecimal) = val1 >= val2

        override fun toInterval(boundary: Expression) = closedOpenIntervalOf(boundary, Constants.Infinity)

        override fun getDual() = LessThanEqual
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

    abstract fun toInterval(boundary: Expression): Expression

    abstract fun getDual(): InequalityOperators
}

object EquationSystemOperator : StatementOperator {
    override val name = "EquationSystem"

    override val precedence = EQUATION_SYSTEM_PRECEDENCE
    override val arity = ARITY_VARIABLE

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        require(op is EquationOperator || op is SolutionOperator)
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

object EquationUnionOperator : StatementOperator {
    override val name = "EquationUnion"

    override val precedence = EQUATION_UNION_PRECEDENCE
    override val arity = ARITY_VARIABLE

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        require(op is EquationOperator)
        return true
    }

    override fun <T> readableString(children: List<T>) = children.joinToString(" OR ")

    override fun latexString(ctx: RenderContext, children: List<LatexRenderable>): String {
        return "${children[0].toLatexString(ctx)}, ${children[1].toLatexString(ctx)}"
    }
}

interface Solution {
    fun variables(children: List<Expression>): List<String>
}

object SolutionOperator : BinaryOperator, StatementOperator, Solution {
    override val name = "Solution"

    override val precedence = PREDICATE_PRECEDENCE

    override fun leftChildAllowed(op: Operator): Boolean {
        require(op is VariableOperator || op is TupleOperator)
        return true
    }

    override fun rightChildAllowed(op: Operator): Boolean {
        require(op.kind == OperatorKind.SET)
        return true
    }

    override fun <T> readableString(left: T, right: T): String {
        return "Solution[$left, $right]"
    }

    override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable): String {
        return "${left.toLatexString(ctx)} \\in ${right.toLatexString(ctx)}"
    }

    override fun variables(children: List<Expression>): List<String> {
        return listOf(children[0].operator.name)
    }
}

enum class MultiVariateSolutionOperator : BinaryOperator, StatementOperator, Solution {

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
        return "$name[$left: $right]"
    }

    override fun latexString(ctx: RenderContext, left: LatexRenderable, right: LatexRenderable): String {
        return "${left.toLatexString(ctx)} \\in \\emptyset"
    }

    override fun variables(children: List<Expression>): List<String> {
        return children[0].children.map { it.operator.name }
    }
}

object VariableListOperator : Operator {
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
