package engine.expressions

import engine.operators.BinaryExpressionOperator
import engine.operators.DecimalOperator
import engine.operators.EquationOperator
import engine.operators.EquationSystemOperator
import engine.operators.ExpressionOperator
import engine.operators.InequalityOperators
import engine.operators.IntegerOperator
import engine.operators.IntervalOperator
import engine.operators.LatexRenderable
import engine.operators.MixedNumberOperator
import engine.operators.Operator
import engine.operators.ProductOperator
import engine.operators.RecurringDecimalOperator
import engine.operators.RenderContext
import engine.operators.SetOperators
import engine.operators.SolutionOperator
import engine.operators.StatementWithConstraintOperator
import engine.operators.SumOperator
import engine.operators.UnaryExpressionOperator
import engine.operators.VariableListOperator
import engine.operators.VariableOperator
import engine.patterns.ExpressionProvider
import engine.patterns.Match
import engine.utility.Rational
import engine.utility.product
import java.math.BigInteger

/**
 * Decorators affect the visual appearance (and hence sometimes the transformations that can be applied to an expression
 * node) but do not affect the tree structure of an expression.  A typical decorator is a bracket.
 */
enum class Decorator {
    RoundBracket {
        override fun decorateString(str: String) = "($str)"
        override fun decorateLatexString(str: String) = "\\left( $str \\right)"
    },
    SquareBracket {
        override fun decorateString(str: String) = "[. $str .]"
        override fun decorateLatexString(str: String) = "\\left[ $str \\right]"
    },
    CurlyBracket {
        override fun decorateString(str: String) = "{. $str .}"
        override fun decorateLatexString(str: String) = "\\left\\{ $str \\right\\}"
    },
    MissingBracket,
    PartialBracket {
        override fun decorateString(str: String) = "<. $str .>"
    }, ;

    open fun decorateString(str: String): String = str
    open fun decorateLatexString(str: String): String = str
}

/**
 * Each expression can have a [Label] attached to it.  As labels implement the [Extractor] interface, they can be used
 * in a rule to tag some sub-expressions of the result for further processing.  It is possible for the same label to be
 * present in more than one node of an expression.
 */
enum class Label : Extractor<Expression> {
    A,
    B,
    C,
    ;

    override fun extract(sub: Expression) = sub.labelledPart(this)
}

/**
 * Metadata about a node, that is managed in the same way by all types.  This interface is not strictly needed
 * now as all instances are of type [BasicMeta]
 */
interface NodeMeta {
    /**
     * The [decorators] list contains all the decorators (mainly enclosing brackets) that the node is wrapped in.
     */
    val decorators: List<Decorator>

    /**
     * The [origin] of the node is where it came from, used to build path mappings. The default value [Build] means
     * that it is a new node not taken from another expression.
     */
    val origin: Origin

    /**
     * A [label] can be attached to a node so it can be retrieved later independently of its position in a larger
     * node.
     */
    val label: Label?

    /**
     * A [name] can be attached to a node - this can be displayed next to the expression
     * to make references to it easier
     *
     * E.g. here the name is "(1)"
     *     x + 5 = 3x    (1)
     */
    val name: String?

    fun copyMeta(
        decorators: List<Decorator> = this.decorators,
        origin: Origin = this.origin,
        label: Label? = this.label,
        name: String? = this.name,
    ): NodeMeta
}

data class BasicMeta(
    override val decorators: List<Decorator> = emptyList(),
    override val origin: Origin = Build,
    override val label: Label? = null,
    override val name: String? = null,
) : NodeMeta {
    override fun copyMeta(decorators: List<Decorator>, origin: Origin, label: Label?, name: String?) =
        copy(decorators, origin, label, name)
}

/**
 * A mathematical expression.  It is made of an [operator] which defines what type of expression it is, and of zero or
 * more [operands] which are the children of the expression.
 */
@Suppress("TooManyFunctions")
open class Expression internal constructor(
    val operator: Operator,
    internal val operands: List<Expression>,
    protected val meta: NodeMeta,
) : LatexRenderable, ExpressionProvider {

    internal val decorators get() = meta.decorators
    val origin get() = meta.origin
    private val label get() = meta.label
    val name get() = meta.name

    init {
        operands.forEachIndexed { i, op -> require(op.hasBracket() || operator.nthChildAllowed(i, op.operator)) }
    }

    val parent = when (val origin = origin) {
        is Child -> origin.parent
        else -> null
    }

    val variables: Set<String> get() = when (this) {
        is Variable -> setOf(variableName)
        else -> this.operands.flatMap { it.variables }.toSet()
    }

    /**
     * Returns true if the expression node is labelled (not if the children are labelled).
     */
    fun hasLabel() = label != null

    /**
     * Returns a copy labelled with [newLabel] instead of the existing label.
     */
    fun withLabel(newLabel: Label?) = expressionOf(operator, operands, meta.copyMeta(label = newLabel))

    fun byName() = if (name != null) nameXp(name!!) else this

    fun withName(newName: String?) = expressionOf(operator, operands, meta.copyMeta(name = newName))

    fun withoutName() = withName(null)

    /**
     * Returns a node in the expression tree labelled with [findLabel] if there is one, else null.
     */
    fun labelledPart(findLabel: Label = Label.A): Expression? = when (findLabel) {
        label -> this
        else -> children.firstNotNullOfOrNull { it.labelledPart(findLabel) }
    }

    /**
     * Returns a copy of the expression with all labels cleared recursively.
     */
    fun clearLabels(): Expression =
        expressionOf(operator, operands.map { it.clearLabels() }, meta.copyMeta(label = null))

    val children by lazy { origin.computeChildrenOrigin(this) }

    /**
     * Number of children of this expression.
     */
    val childCount get() = this.operands.size

    fun factors() = when (operator) {
        is ProductOperator -> children
        else -> listOf(this)
    }

    val firstChild get() = nthChild(0)
    val secondChild get() = nthChild(1)

    fun nthChild(n: Int) = children[n]

    fun withOrigin(newOrigin: Origin) = expressionOf(operator, operands, meta.copyMeta(origin = newOrigin))

    internal fun pathMappings(rootPath: Path = RootPath()) = origin.computePathMappings(rootPath, children)

    fun mergedPathMappings(rootPath: Path = RootPath()) = mergePathMappings(pathMappings(rootPath))
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
        return operator == other.operator &&
            operands.size == other.operands.size &&
            operands.zip(other.operands).all { (op1, op2) -> op1.equiv(op2) }
    }

    fun decorate(decorator: Decorator?) = if (decorator == null) {
        this
    } else {
        expressionOf(operator, operands, meta.copyMeta(decorators = decorators + decorator))
    }

    fun hasBracket() = decorators.isNotEmpty()

    fun isPartialSum() = operator is SumOperator && decorators.getOrNull(0) === Decorator.PartialBracket
    fun isPartialProduct() = this is Product && decorators.getOrNull(0) === Decorator.PartialBracket

    fun removeBrackets() =
        if (hasBracket()) expressionOf(operator, operands, meta.copyMeta(decorators = emptyList())) else this

    fun outerBracket() = decorators.lastOrNull()

    /**
     * The expression contains no variables of any kind
     */
    fun isConstant() = variables.isEmpty()

    /**
     * The expression does not depend on the specified symbols
     */
    fun isConstantIn(symbols: List<String>) = variables.all { !symbols.contains(it) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Expression

        if (operator != other.operator) return false
        if (decorators != other.decorators) return false
        return operands == other.operands
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
     * Updates the origin of the expression so that it now descends from [ancestor]
     */
    internal fun updateOrigin(ancestor: Expression): Expression = when (val origin = origin) {
        ancestor.origin -> ancestor
        is Child -> origin.parent.updateOrigin(ancestor).nthChild(origin.index)
        else -> this
    }

    /**
     * Builds an expression that substitutes [newExpr] for [subExpr] in this expression if possible, otherwise returns
     * the expression unchanged with the exception that outer brackets are removed if it is the root expression.
     */
    fun substitute(subExpr: Expression, newExpr: Expression): Expression = when {
        subExpr.origin is Root && origin !is Root -> this
        subExpr.origin is Root -> newExpr.removeBrackets()
        else -> justSubstitute(subExpr.origin, newExpr)
    }

    /**
     * Builds and expression that substitutes [newExpr] for the expression at [subOrigin] in this expression, flattening
     * products as fit.
     */
    private fun justSubstitute(subOrigin: Origin, newExpr: Expression): Expression = when (subOrigin) {
        origin -> newExpr
        is Child -> justSubstitute(
            subOrigin.parent.origin,
            subOrigin.parent.replaceNthChild(subOrigin.index, newExpr),
        )
        else -> this
    }

    /**
     * Substitutes all occurrences of [oldValue] with [newValue]
     */
    fun substituteAllOccurrences(oldValue: Expression, newValue: Expression): Expression {
        if (this == oldValue) {
            return newValue
        }
        var newExpr = this
        for ((i, child) in children.withIndex()) {
            val newChild = child.substituteAllOccurrences(oldValue, newValue)
            if (newChild != child) {
                newExpr = newExpr.replaceNthChild(i, newChild)
            }
        }
        return newExpr
    }

    protected open fun replaceNthChild(childIndex: Int, newChild: Expression) =
        replaceChildren(
            children.mapIndexed { i, op ->
                when {
                    i != childIndex -> op
                    newChild.hasBracket() || operator.nthChildAllowed(i, newChild.operator) -> newChild
                    else -> {
                        val outerBracket = op.outerBracket()
                        val newBracket = when {
                            outerBracket == null -> Decorator.RoundBracket
                            outerBracket == Decorator.PartialBracket && newChild.operator != op.operator ->
                                Decorator.RoundBracket
                            else -> outerBracket
                        }
                        newChild.decorate(newBracket)
                    }
                }
            },
        )

    internal fun replaceChildren(newChildren: List<Expression>) = expressionOf(
        operator,
        newChildren,
        meta.copyMeta(origin = Build),
    )

    override fun getBoundExprs(m: Match) = listOf(this)

    override fun getBoundExpr(m: Match) = this

    override fun toReadableStringAsSecondTermInASum(): String {
        if (!hasBracket()) {
            if (operator === UnaryExpressionOperator.Minus) return " - ${this.firstChild}"
            if (operator === UnaryExpressionOperator.PlusMinus) return " +/- ${this.firstChild}"
        }
        return " + $this"
    }

    @Suppress("ReturnCount")
    override fun toLatexStringAsSecondTermInASum(ctx: RenderContext): String {
        if (isPartialSum()) return children.joinToString("") { it.toLatexStringAsSecondTermInASum(ctx) }
        if (!hasBracket()) {
            if (operator === UnaryExpressionOperator.Minus) return " - ${firstChild.toLatexString(ctx)}"
            if (operator === UnaryExpressionOperator.PlusMinus) return " \\pm ${firstChild.toLatexString(ctx)}"
        }
        return " + ${toLatexString(ctx)}"
    }

    override fun isInlineDivideByTerm(): Boolean {
        return operator === UnaryExpressionOperator.DivideBy
    }

    /** Returns true if [possibleAncestor] is `===` to `this` or an ancestor of `this` */
    fun isChildOfOrSelf(possibleAncestor: Expression?): Boolean {
        if (possibleAncestor === null) return false
        if (possibleAncestor === this) return true
        return parent?.isChildOfOrSelf(possibleAncestor) == true
    }

    open fun toJson(): List<Any> {
        val serializedOperands = operands.map { it.toJson() }
        return if (decorators.isEmpty() && name == null) {
            listOf(operator.name) + serializedOperands
        } else {
            listOf(
                listOf(operator.name) +
                    (if (name == null) listOf() else listOf(name)) +
                    decorators.map { it.toString() },
            ) + serializedOperands
        }
    }

    val doubleValue: Double by lazy {
        when (operator) {
            is ExpressionOperator -> operator.eval(children.map { it.doubleValue })
            else -> Double.NaN
        }
    }
}

/**
 * Returns true if the expression can only have a single value.
 * Some expression can have multiple values, such as 1 +/- 2
 */
fun Expression.hasSingleValue(): Boolean {
    if (operator == UnaryExpressionOperator.PlusMinus) {
        return false
    }
    return children.all { it.hasSingleValue() }
}

fun Expression.splitPlusMinus(): List<Expression> {
    if (operator == UnaryExpressionOperator.PlusMinus) {
        val splitChild = firstChild.splitPlusMinus()
        return splitChild.map { simplifiedNegOf(it).decorate(outerBracket()) } +
            splitChild.map { it.decorate(outerBracket()) }
    }
    if (childCount == 0) {
        return listOf(this)
    }
    val splitChildren = children.map { it.splitPlusMinus() }
    return product(splitChildren).map { replaceChildren(it) }.toList()
}

/** Returns `this` or a subexpression without any outer `-`, `±`, and unary `+` */
fun Expression.withoutNegOrPlus(): Expression = when (operator) {
    UnaryExpressionOperator.Minus, UnaryExpressionOperator.Plus, UnaryExpressionOperator.PlusMinus ->
        firstChild.withoutNegOrPlus()
    else -> this
}

fun Expression.isNeg() = operator == UnaryExpressionOperator.Minus

fun Expression.isSignedFraction() = this is Fraction || (this.isNeg() && firstChild is Fraction)

fun Expression.inverse(): Expression = when {
    this == Constants.One -> this
    isNeg() -> simplifiedNegOf(firstChild.inverse())
    this is Fraction -> simplifiedFractionOf(denominator, numerator)
    else -> fractionOf(Constants.One, this)
}

fun Expression.asRational(): Rational? = when (operator) {
    UnaryExpressionOperator.Minus -> firstChild.asPositiveRational()?.let { -it }
    else -> asPositiveRational()
}

fun Expression.asPositiveRational(): Rational? = when (this) {
    is Fraction -> numerator.asPositiveInteger()?.let { num ->
        denominator.asPositiveInteger()?.let { den -> Rational(num, den) }
    }
    is IntegerExpression -> Rational(value)
    else -> null
}

fun Expression.asInteger(): BigInteger? = when {
    this is IntegerExpression -> value
    operator == UnaryExpressionOperator.Minus -> firstChild.asPositiveInteger()?.negate()
    else -> null
}

fun Expression.asPositiveInteger(): BigInteger? = when (this) {
    is IntegerExpression -> value
    else -> null
}

fun Expression.isEquationSystem(): Boolean = operator is EquationSystemOperator

fun expressionOf(operator: Operator, operands: List<Expression>): Expression {
    return expressionOf(operator, operands, BasicMeta())
}

@Suppress("CyclomaticComplexMethod")
private fun expressionOf(
    operator: Operator,
    operands: List<Expression>,
    meta: NodeMeta,
): Expression {
    return when (operator) {
        is VariableOperator -> Variable(operator.name, meta)
        is IntegerOperator -> IntegerExpression(operator.value, meta)
        is DecimalOperator -> DecimalExpression(operator.value, meta)
        is RecurringDecimalOperator -> RecurringDecimalExpression(operator.value, meta)
        is MixedNumberOperator -> {
            @Suppress("MagicNumber")
            assert(operands.size == 3)
            MixedNumberExpression(
                operands[0] as IntegerExpression,
                operands[1] as IntegerExpression,
                operands[2] as IntegerExpression,
                meta,
            )
        }
        is ProductOperator -> Product(operands, operator.forcedSigns, meta)
        BinaryExpressionOperator.Fraction -> Fraction(operands[0], operands[1], meta)
        BinaryExpressionOperator.Power -> Power(operands[0], operands[1], meta)

        VariableListOperator -> VariableList(operands.map { it as Variable }, meta)

        SolutionOperator.Identity -> Identity(operands[0] as VariableList, operands[1], meta)
        SolutionOperator.Contradiction -> Contradiction(operands[0] as VariableList, operands[1], meta)
        SolutionOperator.ImplicitSolution -> ImplicitSolution(operands[0] as VariableList, operands[1], meta)
        SolutionOperator.SetSolution -> SetSolution(operands[0] as VariableList, operands[1], meta)

        is IntervalOperator -> Interval(operands[0], operands[1], operator.closedLeft, operator.closedRight)
        SetOperators.FiniteSet -> FiniteSet(operands)

        EquationOperator -> Equation(operands[0], operands[1], meta)
        is InequalityOperators -> Inequality(operands[0], operands[1], operator, meta)
        StatementWithConstraintOperator -> StatementWithConstraint(operands[0], operands[1], meta)

        else -> Expression(operator, operands, meta)
    }
}
