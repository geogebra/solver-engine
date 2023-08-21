package engine.operators

import engine.expressions.Expression
import engine.sign.Sign
import java.math.BigDecimal

private const val PREDICATE_PRECEDENCE = 0
private const val EQUATION_SYSTEM_PRECEDENCE = -10
private const val EQUATION_UNION_PRECEDENCE = -20
private const val STATEMENT_WITH_CONSTRAINT_PRECEDENCE = -15

internal interface StatementOperator : Operator {
    override val kind get() = OperatorKind.STATEMENT
}

enum class Comparator(
    val type: Type,
    val readableString: String,
    val latexString: String,
    val dual: () -> Comparator,
    val compareSign: Sign,
    val operatorName: String,
) {
    Equal(
        type = Type.Equation,
        readableString = "=",
        latexString = "=",
        dual = { Equal },
        compareSign = Sign.ZERO,
        operatorName = "Equation",
    ),

    NotEqual(
        type = Type.Inequation,
        readableString = "!=",
        latexString = "\\neq",
        dual = { NotEqual },
        compareSign = Sign.NOT_ZERO,
        operatorName = "Inequation",
    ),

    LessThan(
        type = Type.Inequality,
        readableString = "<",
        latexString = "\\lt",
        dual = { GreaterThan },
        compareSign = Sign.NEGATIVE,
        operatorName = "LessThan",
    ),

    LessThanOrEqual(
        type = Type.Inequality,
        readableString = "<=",
        latexString = "\\leq",
        dual = { GreaterThanOrEqual },
        compareSign = Sign.NON_POSITIVE,
        operatorName = "LessThanEqual",
    ),

    GreaterThan(
        type = Type.Inequality,
        readableString = ">",
        latexString = "\\gt",
        dual = { LessThan },
        compareSign = Sign.POSITIVE,
        operatorName = "GreaterThan",
    ),

    GreaterThanOrEqual(
        type = Type.Inequality,
        readableString = ">=",
        latexString = "\\geq",
        dual = { LessThanOrEqual },
        compareSign = Sign.NON_NEGATIVE,
        operatorName = "GreaterThanEqual",
    ),
    ;

    fun holdsFor(diffSign: Sign): Boolean {
        return diffSign.implies(compareSign)
    }
    fun holdsFor(left: BigDecimal, right: BigDecimal): Boolean {
        return holdsFor(Sign.fromInt((left - right).signum()))
    }

    enum class Type {
        Equation,
        Inequation,
        Inequality,
    }

    companion object {
        fun fromReadableString(s: String) = Comparator.values().firstOrNull { it.readableString == s }
    }
}

internal data class ComparisonOperator(
    val comparator: Comparator,
) : BinaryOperator, StatementOperator, SolvableOperator {
    override fun getDual(): SolvableOperator {
        return ComparisonOperator(comparator.dual())
    }

    override val name = comparator.operatorName
    override val precedence: Int = PREDICATE_PRECEDENCE

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
            return "${left.toLatexString(innerCtx)} & ${comparator.latexString} & ${right.toLatexString(innerCtx)}"
        } else {
            return "${left.toLatexString(ctx)} ${comparator.latexString} ${right.toLatexString(ctx)}"
        }
    }

    override fun <T> readableString(left: T, right: T) = "$left ${comparator.readableString} $right"
}

internal data class DoubleComparisonOperator(
    val leftComparator: Comparator,
    val rightComparator: Comparator,
) : StatementOperator, TernaryOperator {
    override val name = when {
        leftComparator == Comparator.LessThan && rightComparator == Comparator.LessThan -> "OpenRange"
        leftComparator == Comparator.LessThan && rightComparator == Comparator.LessThanOrEqual -> "OpenClosedRange"
        leftComparator == Comparator.LessThanOrEqual && rightComparator == Comparator.LessThan -> "ClosedOpenRange"
        leftComparator == Comparator.LessThanOrEqual && rightComparator == Comparator.LessThanOrEqual -> "ClosedRange"
        leftComparator == Comparator.GreaterThan && rightComparator == Comparator.GreaterThan -> "ReversedOpenRange"
        leftComparator == Comparator.GreaterThan &&
            rightComparator == Comparator.GreaterThanOrEqual -> "ReversedOpenClosedRange"
        leftComparator == Comparator.GreaterThanOrEqual &&
            rightComparator == Comparator.GreaterThan -> "ReversedClosedOpenRange"
        leftComparator == Comparator.GreaterThanOrEqual &&
            rightComparator == Comparator.GreaterThanOrEqual -> "ReversedClosedRange"
        else -> throw error("Invalid operators $leftComparator and $rightComparator for ${this::class.simpleName}")
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
                leftComparator.latexString +
                " & " +
                second.toLatexString(innerCtx) +
                " & " +
                rightComparator.latexString +
                " & " +
                third.toLatexString(innerCtx)
        } else {
            return first.toLatexString(ctx) + " " + leftComparator.latexString +
                " " + second.toLatexString(ctx) + " " +
                rightComparator.latexString + " " + third.toLatexString(ctx)
        }
    }

    override fun <T> readableString(first: T, second: T, third: T): String {
        return "$first ${leftComparator.readableString} $second ${rightComparator.readableString} $third"
    }
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

internal interface SolvableOperator : Operator {
    fun getDual(): SolvableOperator

    fun isSelfDual() = getDual() == this
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

internal object StatementSystemOperator : StatementOperator {
    override val name = "EquationSystem"

    override val precedence = EQUATION_SYSTEM_PRECEDENCE
    override val arity = ARITY_VARIABLE

    override fun nthChildAllowed(n: Int, op: Operator): Boolean {
        require(op is StatementOperator)
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
