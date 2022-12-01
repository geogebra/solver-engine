package engine.expressions

import engine.operators.BinaryExpressionOperator
import engine.operators.LatexRenderable
import engine.operators.NaryOperator
import engine.operators.Operator
import engine.operators.RenderContext
import engine.operators.VariableOperator
import engine.patterns.Match
import engine.patterns.PathProvider

/**
 * Decorators affect the visual appearance (and hence sometimes the transformations that can be applied to an expression
 * node) but do not affect the tree structure of an expression.  A typical decorator is a bracket.
 */
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
    MissingBracket;

    open fun decorateString(str: String): String = str
    open fun decorateLatexString(str: String): String = str
}

/**
 * Each expression can have a [Label] attached to it.  As labels implement the [Extractor] interface, they can be used
 * in a rule to tag some sub-expressions of the result for further processing.  It is possible for the same label to be
 * present in more than one node of an expression.
 */
enum class Label : Extractor {
    A,
    B,
    C;

    override fun extract(sub: Expression) = sub.labelledPart(this)
}

/**
 * A mathematical expression.  It is made of an [operator] which defines what type of expression it is, and of zero or
 * more [operands] which are the children of the expression.
 */
@Suppress("TooManyFunctions")
class Expression internal constructor(
    val operator: Operator,
    val operands: List<Expression>,
    val decorators: List<Decorator>,
    val origin: Origin,
    val label: Label?
) : LatexRenderable, PathProvider {

    /**
     * Create a new expression with [Build] origin and no label.  Operand brackets are not adjusted and if a required
     * bracket is missing in an operand, an exception will be thrown.
     */
    constructor(operator: Operator, operands: List<Expression>, decorators: List<Decorator> = emptyList()) :
        this(operator, operands, decorators, Build, null)

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

    private fun withDecorators(newDecorators: List<Decorator>) =
        Expression(operator, operands, newDecorators, origin, label)

    /**
     * Returns true if the expression node is labelled (not if the children are labelled).
     */
    fun hasLabel() = label != null

    /**
     * Returns a copy labelled with [newLabel] instead of the existing label.
     */
    fun withLabel(newLabel: Label?) = Expression(operator, operands, decorators, origin, newLabel)

    /**
     * Returns a node in the expression tree labelled with [findLabel] if there is one, else null.
     */
    fun labelledPart(findLabel: Label = Label.A): Expression? = when (findLabel) {
        label -> this
        else -> children().firstNotNullOfOrNull { it.labelledPart(findLabel) }
    }

    /**
     * Returns a copy of the expression with all labels cleared recursively.
     */
    fun clearLabels(): Expression =
        Expression(operator, operands.map { it.clearLabels() }, decorators, origin, null)

    fun children() = origin.computeChildrenOrigin(this)

    fun flattenedProductChildren() = when (operator) {
        NaryOperator.ImplicitProduct -> children()
        NaryOperator.Product -> children().flatMap {
            if (!it.hasBracket() && it.operator == NaryOperator.ImplicitProduct) it.children() else listOf(it)
        }
        else -> listOf(this)
    }

    fun nthChild(n: Int) = origin.computeChildOrigin(this, n)

    fun withOrigin(newOrigin: Origin) = Expression(operator, operands, decorators, newOrigin, label)

    fun pathMappings(rootPath: Path = RootPath) = origin.computePathMappings(rootPath, children())

    override fun toString(): String {
        val s = decorators.fold(operator.readableString(operands)) { acc, dec -> dec.decorateString(acc) }
        return s
        // Return this instead to show labels in debug output:
        // return if (label == null) s else "[ $label::$s ]"
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

    fun decorate(decorator: Decorator) = Expression(operator, operands, decorators + decorator, origin, label)

    fun hasBracket() = decorators.isNotEmpty()

    fun removeBrackets() =
        if (hasBracket()) Expression(operator, operands, emptyList(), origin, label) else this

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
                ).withDecorators(decorators).withLabel(label)
            } else {
                mapChildren { it.substituteKeepBrackets(subPath, newExpr) }
            }
        }
        else -> mapChildren { it.substituteKeepBrackets(subPath, newExpr) }
    }

    private fun mapChildren(f: (Expression) -> Expression) = Expression(
        operator, children().map(f), decorators, Build, label
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
