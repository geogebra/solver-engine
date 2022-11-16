package engine.expressions

import engine.operators.BinaryExpressionOperator
import engine.operators.LatexRenderable
import engine.operators.NaryOperator
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
        operands.forEachIndexed { i, op -> require(op.hasBracket() || operator.nthChildAllowed(i, op.operator)) }
    }

    val parent = when (origin) {
        is Child -> origin.parent
        else -> null
    }

    val variables: Set<String> = when (operator) {
        is VariableOperator -> setOf(operator.name)
        else -> this.operands.flatMap { it.variables }.toSet()
    }

    private fun withDecorators(newDecorators: List<Decorator>) = Expression(operator, operands, newDecorators, origin)

    fun children() = origin.computeChildrenOrigin(this)

    fun flattenedProductChildren() = when (operator) {
        NaryOperator.ImplicitProduct -> children()
        NaryOperator.Product -> children().flatMap {
            if (!it.hasBracket() && it.operator == NaryOperator.ImplicitProduct) it.children() else listOf(it)
        }
        else -> listOf(this)
    }

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

    /**
     * Adjusts the presence or absence of brackets in the expression so that it can be the [index]th child of
     * [operator]. Unnecessary brackets are removed, required brackets are added.
     */
    internal fun adjustBracketFor(operator: Operator, index: Int): Expression {
        val bracketsRequired = !operator.nthChildAllowed(index, this.operator)
        return when {
            bracketsRequired && !hasBracket() -> decorate(Decorator.RoundBracket)
            !bracketsRequired -> removeBrackets()
            else -> this
        }
    }

    /**
     * Substitutes [newExpr] into the expression at position [subPath] if possible, otherwise returns the expression
     * unchanged with the exception that outer brackets are removed if it is the root expression.
     */
    fun substitute(subPath: Path, newExpr: Expression): Expression = when {
        subPath == RootPath && origin.path != RootPath -> this
        subPath == RootPath -> newExpr.removeBrackets()
        subPath == origin.path -> newExpr
        else -> substituteKeepBrackets(subPath as ChildPath, newExpr)
    }

    private fun substituteKeepBrackets(subPath: ChildPath, newExpr: Expression): Expression = when {
        origin.path == null || !subPath.hasAncestor(origin.path) -> this
        origin.path == subPath -> {
            newExpr.adjustBracketFor((origin as Child).parent.operator, subPath.index)
        }
        operator == NaryOperator.Product || operator == NaryOperator.ImplicitProduct -> {
            val children = flattenedProductChildren()

            val index = children.indexOfFirst { it.origin.path == subPath }
            if (index != -1) {
                productOf(
                    children.mapIndexed { i, op ->
                        if (i == index) {
                            newExpr.adjustBracketFor(NaryOperator.Product, subPath.index)
                        } else {
                            op
                        }
                    }
                ).withDecorators(decorators)
            } else {
                mapChildren { it.substituteKeepBrackets(subPath, newExpr) }
            }
        }
        else -> mapChildren { it.substituteKeepBrackets(subPath, newExpr) }
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
