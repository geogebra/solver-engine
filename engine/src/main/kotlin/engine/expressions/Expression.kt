package engine.expressions

import engine.operators.BinaryExpressionOperator
import engine.operators.LatexRenderable
import engine.operators.Operator
import engine.operators.RenderContext
import engine.operators.VariableOperator
import engine.patterns.Match
import engine.patterns.PathProvider

enum class Decorator {
    RoundBracket {
        override fun decorateString(str: String) = "($str)"
        override fun decorateLatexString(str: String) = "{\\left( $str \\right)}"
    },
    SquareBracket {
        override fun decorateString(str: String) = "[. $str .]"
        override fun decorateLatexString(str: String) = "{\\left[ $str \\right]}"
    },
    CurlyBracket {
        override fun decorateString(str: String) = "{. $str .}"
        override fun decorateLatexString(str: String) = "{\\left\\{ $str \\right\\}}"
    },
    MissingBracket {
        override fun decorateString(str: String) = "{{ $str }}"
        override fun decorateLatexString(str: String) = str
    };

    abstract fun decorateString(str: String): String
    abstract fun decorateLatexString(str: String): String
}

@Suppress("TooManyFunctions")
class Expression internal constructor(
    val operator: Operator,
    val operands: List<Expression>,
    val decorators: List<Decorator> = emptyList(),
    val origin: Origin = Build
) : LatexRenderable, PathProvider {

    init {
        operands.forEachIndexed { i, op -> op.hasBracket() || operator.nthChildAllowed(i, op.operator) }
    }

    val parent = when (origin) {
        is Child -> origin.parent
        else -> null
    }

    val variables: Set<String> = when (operator) {
        is VariableOperator -> setOf(operator.name)
        else -> this.operands.flatMap { it.variables }.toSet()
    }

    fun children() = origin.computeChildrenOrigin(this)

    fun nthChild(n: Int) = origin.computeChildOrigin(this, n)

    fun withOrigin(newOrigin: Origin) = Expression(operator, operands, decorators, newOrigin)

    fun pathMappings(rootPath: Path = RootPath) = origin.computePathMappings(rootPath, children())

    override fun toString(): String {
        return decorators.fold(operator.readableString(operands)) { acc, dec -> dec.decorateString(acc) }
    }

    /**
     * Returns a LaTeX string representation of the expression.  The string should be of the form "{...}".
     * Each operand should itself be enclosed in "{...}" and there should be no other curly brackets,
     * except for the pair "{}" itself.
     *
     * This ensures that paths can be followed in the string representation by counting instances of
     * "{" and "}" and discarding "{}".
     */
    override fun toLatexString(ctx: RenderContext): String {
        return decorators.fold(operator.latexString(ctx, operands)) { acc, dec -> dec.decorateLatexString(acc) }
    }

    fun equiv(other: Expression): Boolean {
        return operator.equiv(other.operator) &&
            operands.size == other.operands.size &&
            operands.zip(other.operands).all { (op1, op2) -> op1.equiv(op2) }
    }

    fun decorate(decorator: Decorator) = Expression(operator, operands, decorators + decorator, origin)

    fun hasBracket() = decorators.isNotEmpty()

    fun removeBrackets() =
        if (hasBracket()) Expression(operator, operands, emptyList(), origin) else this

    fun outerBracket() = decorators.lastOrNull()

    /**
     * The expression contains no variables of any kind
     */
    fun isConstant() = variables.isEmpty()

    /**
     * The expression does not depend on the specified symbol / variable
     */
    fun isConstantIn(symbol: String) = !variables.contains(symbol)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Expression

        if (operator != other.operator) return false
        if (decorators != other.decorators) return false
        if (operands != other.operands) return false

        return true
    }

    override fun hashCode(): Int {
        var result = operator.hashCode()
        result = 31 * result + decorators.hashCode()
        result = 31 * result + operands.hashCode()
        return result
    }

    fun substitute(subPath: Path, newExpr: Expression): Expression = when (origin.path) {
        subPath -> if (subPath == RootPath) newExpr.removeBrackets() else newExpr
        else -> substituteKeepBrackets(subPath, newExpr)
    }

    private fun substituteKeepBrackets(subPath: Path, newExpr: Expression): Expression = when {
        origin.path == null || !subPath.hasAncestor(origin.path) -> this
        origin.path == subPath -> wrapInBracketsForParent(newExpr)
        else -> mapChildren { it.substituteKeepBrackets(subPath, newExpr) }
    }

    /**
     * Wrap [mappedExpr] in the correct bracket or absence of bracket so that it can correctly replace [this] in its
     * parent.
     */
    private fun wrapInBracketsForParent(mappedExpr: Expression): Expression {
        return when {
            origin !is Child -> mappedExpr.removeBrackets()
            origin.parent.operator.nthChildAllowed(origin.index, mappedExpr.operator) -> mappedExpr.removeBrackets()
            mappedExpr.hasBracket() -> mappedExpr
            else -> bracketOf(mappedExpr, outerBracket())
        }
    }

    private fun mapChildren(f: (Expression) -> Expression) = Expression(
        operator, children().map(f), decorators, Build
    )

    override fun getBoundPaths(m: Match) = if (origin.path == null) emptyList() else listOf(origin.path)

    override fun getBoundExpr(m: Match) = this
}

fun Expression.numerator(): Expression {
    require(operator == BinaryExpressionOperator.Fraction) { "Fraction expected, got: $operator" }
    return nthChild(0)
}

fun Expression.denominator(): Expression {
    require(operator == BinaryExpressionOperator.Fraction) { "Fraction expected, got: $operator" }
    return nthChild(1)
}

fun Expression.base(): Expression {
    require(operator == BinaryExpressionOperator.Power) { "Power expected, got: $operator" }
    return nthChild(0)
}

fun Expression.exponent(): Expression {
    require(operator == BinaryExpressionOperator.Power) { "Power expected, got: $operator" }
    return nthChild(1)
}
