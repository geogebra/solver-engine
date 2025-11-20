/*
 * Copyright (c) 2023 GeoGebra GmbH, office@geogebra.org
 * This file is part of GeoGebra
 *
 * The GeoGebra source code is licensed to you under the terms of the
 * GNU General Public License (version 3 or later)
 * as published by the Free Software Foundation,
 * the current text of which can be found via this link:
 * https://www.gnu.org/licenses/gpl.html ("GPL")
 * Attribution (as required by the GPL) should take the form of (at least)
 * a mention of our name, an appropriate copyright notice
 * and a link to our website located at https://www.geogebra.org
 *
 * For further details, please see https://www.geogebra.org/license
 *
 */

@file:Suppress("TooManyFunctions")

package engine.expressions

import engine.operators.BinaryExpressionOperator
import engine.operators.Comparator
import engine.operators.ComparisonOperator
import engine.operators.DecimalOperator
import engine.operators.DoubleComparisonOperator
import engine.operators.EulerEOperator
import engine.operators.ExpressionOperator
import engine.operators.ExpressionWithConstraintOperator
import engine.operators.IntegerOperator
import engine.operators.IntervalOperator
import engine.operators.LatexRenderable
import engine.operators.ListOperator
import engine.operators.MixedNumberOperator
import engine.operators.NameOperator
import engine.operators.Operator
import engine.operators.PiOperator
import engine.operators.ProductOperator
import engine.operators.RecurringDecimalOperator
import engine.operators.RenderContext
import engine.operators.SetOperators
import engine.operators.SolutionOperator
import engine.operators.StatementSystemOperator
import engine.operators.StatementUnionOperator
import engine.operators.SumOperator
import engine.operators.TrigonometricFunctionOperator
import engine.operators.UnaryExpressionOperator
import engine.operators.UnitExpressionOperator
import engine.operators.UnitType
import engine.operators.VariableListOperator
import engine.operators.VariableOperator
import engine.operators.VoidOperator
import engine.patterns.ExpressionProvider
import engine.patterns.Match
import engine.sign.Sign
import engine.utility.Rational
import engine.utility.product
import java.math.BigDecimal
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
    },

    ;

    open fun decorateString(str: String): String = str

    open fun decorateLatexString(str: String): String = str
}

class LabelSpace {
    internal fun getLabelInstance(label: Label) = LabelInstance(this, label)
}

data class LabelInstance(val space: LabelSpace, val label: Label) : Extractor<Expression> {
    override fun extract(sub: Expression) = sub.labelledPart(this)
}

/**
 * Each expression can have a [Label] attached to it.  As labels implement the [Extractor] interface, they can be used
 * in a rule to tag some sub-expressions of the result for further processing.  It is possible for the same label to be
 * present in more than one node of an expression.
 */
enum class Label {
    A,
    B,
    C,
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
    val label: LabelInstance?

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
        label: LabelInstance? = this.label,
        name: String? = this.name,
    ): NodeMeta
}

data class BasicMeta(
    override val decorators: List<Decorator> = emptyList(),
    override val origin: Origin = Build,
    override val label: LabelInstance? = null,
    override val name: String? = null,
) : NodeMeta {
    override fun copyMeta(decorators: List<Decorator>, origin: Origin, label: LabelInstance?, name: String?) =
        copy(decorators, origin, label, name)
}

/**
 * A mathematical expression.  It is made of an [operator] which defines what type of expression it is, and of zero or
 * more [operands] which are the children of the expression.
 */
@Suppress("TooManyFunctions")
open class Expression internal constructor(
    internal val operator: Operator,
    internal val operands: List<Expression>,
    protected val meta: NodeMeta,
) : LatexRenderable, ExpressionProvider {
    internal val decorators get() = meta.decorators
    internal val origin get() = meta.origin
    private val label get() = meta.label
    val name get() = meta.name

    val path get() = meta.origin.path

    init {
        operands.forEachIndexed { i, op -> require(op.hasBracket() || operator.nthChildAllowed(i, op.operator)) }
    }

    val parent = when (val origin = origin) {
        is Child -> origin.parent
        else -> null
    }

    val variables: Set<String>
        get() = when (this) {
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
    fun withLabel(newLabel: LabelInstance?) = expressionOf(operator, operands, meta.copyMeta(label = newLabel))

    fun byName() = if (name != null) nameXp(name!!) else this

    fun withName(newName: String?) = expressionOf(operator, operands, meta.copyMeta(name = newName))

    fun withoutName() = withName(null)

    /**
     * Returns a node in the expression tree labelled with [findLabel] if there is one, else null.
     */
    fun labelledPart(findLabel: LabelInstance): Expression? =
        when (findLabel) {
            label -> this
            else -> children.firstNotNullOfOrNull { it.labelledPart(findLabel) }
        }

    /**
     * Returns a copy of the expression with all labels cleared recursively.
     */
    fun clearLabels(labelSpace: LabelSpace): Expression {
        return expressionOf(
            operator = operator,
            operands = operands.map { it.clearLabels(labelSpace) },
            meta = if (meta.label?.space == labelSpace) meta.copyMeta(label = null) else meta,
        )
    }

    val children by lazy { origin.computeChildrenOrigin(this) }

    /**
     * Returns the children in their natural visiting order.  Ideally this would already be the case with [children]
     * but root[x, y] means the y-th root of x and we want the visiting order to be (y, x) not (x, y).
     *
     * In the future, we can make sure [children] is in natural visiting order but that means changing the API as at
     * least root[x, y] will have a different JSON representation
     */
    internal open fun childrenInVisitingOrder() = children

    /**
     * Number of children of this expression.
     */
    val childCount get() = this.operands.size

    fun factors() =
        when (operator) {
            is ProductOperator -> children
            else -> listOf(this)
        }

    val firstChild get() = nthChild(0)
    val secondChild get() = nthChild(1)
    val thirdChild get() = nthChild(2)

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

    internal fun withDecorators(decorators: List<Decorator>) =
        if (decorators == this.decorators) {
            this
        } else {
            expressionOf(operator, operands, meta.copyMeta(decorators = decorators))
        }

    fun decorate(decorator: Decorator?) = if (decorator == null) this else withDecorators(decorators + decorator)

    fun removeBrackets() = if (hasBracket()) withDecorators(emptyList()) else this

    fun hasBracket() = decorators.isNotEmpty()

    /**
     * Any bracket other than Decorator.PartialBracket is a "visible bracket"
     */
    fun hasVisibleBracket() = decorators.any { it != Decorator.PartialBracket && it != Decorator.MissingBracket }

    fun isPartialSum() = this is Sum && decorators.getOrNull(0) === Decorator.PartialBracket

    fun isPartialProduct() = this is Product && decorators.getOrNull(0) === Decorator.PartialBracket

    fun outerBracket() = decorators.lastOrNull()

    /**
     * The expression contains no variables of any kind
     */
    fun isConstant() = variables.isEmpty()

    /**
     * The expression does not depend on the specified symbols
     */
    fun isConstantIn(symbols: Collection<String>) = variables.all { !symbols.contains(it) }

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
     * Substitute [newExpr] for [oldExpr] in the expression, returning the new expression.  This does not produce
     * any flattening of sums or products (or other expressions).
     *
     * Brackets around [newExpr] are adjusted (removed if unnecessary, added if required and in the case of sums or
     * products substituted into a sum or product, a [Decorator.PartialBracket] is used).
     */
    fun substitute(oldExpr: Expression, newExpr: Expression): Expression {
        return justSubstitute(oldExpr.origin, newExpr.adjustBracketToReplace(oldExpr))
    }

    /**
     * Builds and expression that substitutes [newExpr] for the expression at [subOrigin] in this expression without
     * changing brackets or the structure of the expression.  This is unsafe and should not be used by anything but
     * [substitute]
     */
    private fun justSubstitute(subOrigin: Origin, newExpr: Expression): Expression {
        return when (subOrigin) {
            origin -> newExpr
            is Child -> justSubstitute(
                subOrigin.parent.origin,
                subOrigin.parent.replaceNthChild(subOrigin.index, newExpr),
            )
            else -> this
        }
    }

    /**
     * Adjust the brackets around the expression which is meant to be the new version of [oldExpression] so that
     * when substituting it for [oldExpression] in a parent expression, no further bracket adjustment
     * will be needed.
     */
    private fun adjustBracketToReplace(oldExpression: Expression): Expression {
        val origin = oldExpression.origin

        return if (origin is Child) {
            val parent = origin.parent
            val index = origin.index

            val oldBracket = oldExpression.outerBracket()
            when {
                parent.operator.nthChildAllowed(index, operator) -> removeBrackets()
                oldBracket != null && canUseOldBracket(oldExpression, parent) -> withDecorators(listOf(oldBracket))
                else -> withDecorators(listOf(fallbackBracket(parent)))
            }
        } else {
            removeBrackets()
        }
    }

    /**
     * Given that a bracket is missing around the expression, returns the bracket that should be put around it
     * so it can be substituted into [parent]
     *
     * [Decorator.PartialBracket] is used for sums in sums or products in products because we do not want to draw the
     * brackets, even though we want to keep the grouping.
     */
    private fun fallbackBracket(parent: Expression): Decorator {
        return when {
            this is Sum && parent is Sum -> Decorator.PartialBracket
            this is Product && parent is Product -> Decorator.PartialBracket
            else -> Decorator.RoundBracket
        }
    }

    /**
     * Returns true if the expression can be substituted for [oldExpression] in [parent] without adding a bracket
     */
    private fun canUseOldBracket(oldExpression: Expression, parent: Expression): Boolean {
        return when {
            this is Sum && parent is Sum -> oldExpression is Sum
            this is Product && parent is Product -> oldExpression is Product
            else -> {
                val previousBracket = oldExpression.outerBracket()
                previousBracket != Decorator.MissingBracket && previousBracket != Decorator.PartialBracket
            }
        }
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
                newExpr = newExpr.replaceNthChild(i, newChild.adjustBracketFor(operator, i))
            }
        }
        return newExpr
    }

    private fun replaceNthChild(childIndex: Int, newChild: Expression) =
        replaceChildren(
            children.mapIndexed { i, op ->
                when {
                    i != childIndex -> op
                    else -> newChild
                }
            },
        )

    internal fun replaceChildren(newChildren: List<Expression>) =
        expressionOf(
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

    internal open fun fillJson(s: MutableMap<String, Any>) {
        s["type"] = operator.name
        if (operands.isNotEmpty()) {
            s["operands"] = operands.map { it.toJson() }
        }
    }

    fun toJson(): Map<String, Any> {
        val s = mutableMapOf<String, Any>()
        fillJson(s)
        if (decorators.isNotEmpty()) {
            s["decorators"] = decorators.map { it.toString() }
        }
        if (name != null) {
            s["name"] = name!!
        }
        return s
    }

    /**
     * Approximation of the expression numeric value as a Double.  Returns NaN if it is undefined or if it is
     * not a numerical value (e.g. an equation or a variable expression)
     */
    val doubleValue: Double by lazy {
        when (operator) {
            is ExpressionOperator -> operator.eval(children.map { it.doubleValue })
            else -> Double.NaN
        }
    }

    /**
     * Approximation of the expression numeric value as a Double for the given variable values. Returns NaN if it is
     * undefined or if it is not a numerical value (e.g. an equation or has other variables than the ones given)
     */
    fun evaluate(variableValues: Map<String, Double>): Double {
        return when {
            isConstantIn(variableValues.keys) -> doubleValue
            this is Variable -> variableValues[variableName] ?: Double.NaN
            operator is ExpressionOperator -> operator.eval(children.map { it.evaluate(variableValues) })
            else -> Double.NaN
        }
    }

    fun evaluate(variable: String, value: Double): Double {
        return when {
            variable !in variables -> doubleValue
            this is Variable -> if (variableName == variable) value else Double.NaN
            operator is ExpressionOperator -> operator.eval(children.map { it.evaluate(variable, value) })
            else -> Double.NaN
        }
    }

    /**
     * Returns the sign of the expression if it can definitely be ascertained.
     * [Sign.NONE] is returned if the expression could be undefined,
     * [Sign.UNKNOWN] is returned if the expression has a value but its sign could not be narrowed down.
     */
    open fun signOf(): Sign = Sign.NONE

    fun isDefinitelyNotUndefined() = signOf() != Sign.NONE

    /**
     * Returns the depth of the expression.  The depth of an expression is the depth of its tree representation.
     */
    val depth: Int by lazy { if (operands.isEmpty()) 0 else 1 + operands.maxOf { it.depth } }
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
fun Expression.withoutNegOrPlus(): Expression =
    when (operator) {
        UnaryExpressionOperator.Minus, UnaryExpressionOperator.Plus, UnaryExpressionOperator.PlusMinus ->
            firstChild.withoutNegOrPlus()
        else -> this
    }

fun Expression.isRationalExpression(): Boolean {
    return (this is Fraction && !denominator.isConstant()) ||
        (this is Minus && firstChild is Fraction && !(firstChild as Fraction).denominator.isConstant())
}

fun Expression.inverse(): Expression =
    when {
        this == Constants.One -> this
        this is Minus -> simplifiedNegOf(firstChild.inverse())
        this is Fraction -> simplifiedFractionOf(denominator, numerator)
        else -> fractionOf(Constants.One, this)
    }

fun Expression.asRational(): Rational? =
    when (operator) {
        UnaryExpressionOperator.Minus -> firstChild.asPositiveRational()?.let { -it }
        else -> asPositiveRational()
    }

fun Expression.asPositiveRational(): Rational? =
    when (this) {
        is Fraction -> numerator.asPositiveInteger()?.let { num ->
            denominator.asPositiveInteger()?.let { den -> Rational(num, den) }
        }
        is IntegerExpression -> Rational(value)
        else -> null
    }

fun Expression.asInteger(): BigInteger? =
    when {
        this is IntegerExpression -> value
        operator == UnaryExpressionOperator.Minus -> firstChild.asPositiveInteger()?.negate()
        else -> null
    }

fun Expression.asPositiveInteger(): BigInteger? =
    when (this) {
        is IntegerExpression -> value
        else -> null
    }

fun Expression.asDecimal(): BigDecimal? =
    when (this) {
        is Minus -> argument.asDecimal()?.negate()
        else -> asPositiveDecimal()
    }

fun Expression.asPositiveDecimal(): BigDecimal? =
    when (this) {
        is DecimalExpression -> value
        is IntegerExpression -> value.toBigDecimal()
        else -> null
    }

fun Expression.hasRedundantBrackets(): Boolean =
    hasBracket() && outerBracket() != Decorator.MissingBracket &&
        when (val origin = origin) {
            is Child -> origin.parent.operator.nthChildAllowed(origin.index, operator)
            else -> true
        }

fun Expression.variablePowerBase(): Variable? {
    return when {
        this is Variable -> this
        this is Power && this.base is Variable -> this.base as Variable
        else -> null
    }
}

fun Expression.isPolynomial(): Boolean =
    when (this) {
        !is ValueExpression -> false
        is Fraction -> numerator.isPolynomial() && denominator.isConstant()
        is DivideBy -> divisor.isConstant()
        is Root -> radicand.isConstant()
        is SquareRoot -> argument.isConstant()
        else -> children.all { it.isPolynomial() }
    }

fun Expression.containsRoots(): Boolean {
    return when (this) {
        is Root, is SquareRoot -> true
        else -> children.any { it.containsRoots() }
    }
}

fun Expression.containsLogs(): Boolean {
    return when (this) {
        is Logarithm -> true
        else -> children.any { it.containsLogs() }
    }
}

fun Expression.containsPowers(): Boolean {
    return when (this) {
        is Power -> true
        else -> children.any { it.containsPowers() }
    }
}

fun Expression.containsDecimals(): Boolean =
    this is DecimalExpression || this is RecurringDecimalExpression ||
        children.any { it.containsDecimals() }

fun Expression.containsFractions(): Boolean = this is Fraction || children.any { it.containsFractions() }

fun Expression.containsUnits(unitType: UnitType? = null): Boolean =
    (this is UnitExpression && (unitType == null || unit == unitType)) || children.any { it.containsUnits() }

fun Expression.containsExpression(expression: Expression): Boolean =
    this == expression || children.any { it.containsExpression(expression) }

fun Expression.containsTrigExpression(): Boolean =
    this is TrigonometricExpression || children.any { it.containsTrigExpression() }

fun Expression.allSubterms(): List<Expression> = listOf(this) + children.flatMap { it.allSubterms() }

fun Expression.complexity(): Int = 1 + children.sumOf { it.complexity() }

inline fun <reified T : Expression> Expression.isSigned(): Boolean = this is T || this is Minus && argument is T

internal fun expressionOf(operator: Operator, operands: List<Expression>) =
    expressionOf(operator, operands, BasicMeta())

@Suppress("CyclomaticComplexMethod", "LongMethod")
private fun expressionOf(operator: Operator, operands: List<Expression>, meta: NodeMeta): Expression {
    return when (operator) {
        is VariableOperator -> Variable(operator.variableName, operator.subscript, meta)
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
        PiOperator -> PiExpression(meta)
        EulerEOperator -> EulerEExpression(meta)
        UnaryExpressionOperator.Minus -> Minus(operands[0], meta)
        UnaryExpressionOperator.Plus -> Plus(operands[0], meta)
        UnaryExpressionOperator.PlusMinus -> PlusMinus(operands[0], meta)
        UnaryExpressionOperator.AbsoluteValue -> AbsoluteValue(operands[0], meta)
        UnaryExpressionOperator.DivideBy -> DivideBy(operands[0], meta)
        UnaryExpressionOperator.SquareRoot -> SquareRoot(operands[0], meta)
        UnaryExpressionOperator.Percentage -> Percentage(operands[0], meta)
        UnaryExpressionOperator.NaturalLog -> NaturalLog(operands[0], meta)
        UnaryExpressionOperator.LogBase10 -> LogBase10(operands[0], meta)

        is UnitExpressionOperator -> UnitExpression(operands[0], operator.unit, meta)

        is TrigonometricFunctionOperator ->
            TrigonometricExpression(operator.type, operands[0], operator.powerInside, operator.inverseNotation, meta)
        is ProductOperator -> Product(operands, operator.forcedSigns, meta)
        SumOperator -> Sum(operands, meta)

        BinaryExpressionOperator.Fraction -> Fraction(operands[0], operands[1], meta)
        BinaryExpressionOperator.Power -> Power(operands[0], operands[1], meta)
        BinaryExpressionOperator.Root -> Root(operands[0], operands[1], meta)
        BinaryExpressionOperator.PercentageOf -> PercentageOf(operands[0], operands[1], meta)
        BinaryExpressionOperator.Log -> Log(operands[0], operands[1], meta)

        ExpressionWithConstraintOperator -> ExpressionWithConstraint(operands[0], operands[1], meta)

        VariableListOperator -> VariableList(operands.map { it as Variable }, meta)

        SolutionOperator.Identity -> Identity(operands[0] as VariableList, operands[1], meta)
        SolutionOperator.Contradiction -> Contradiction(operands[0] as VariableList, operands[1], meta)
        SolutionOperator.ImplicitSolution -> ImplicitSolution(operands[0] as VariableList, operands[1], meta)
        SolutionOperator.SetSolution -> SetSolution(operands[0] as VariableList, operands[1], meta)

        is IntervalOperator -> Interval(operands[0], operands[1], operator.closedLeft, operator.closedRight, meta)
        SetOperators.FiniteSet -> FiniteSet(operands, meta)
        SetOperators.CartesianProduct -> CartesianProduct(operands, meta)
        SetOperators.SetUnion -> SetUnion(operands, meta)
        SetOperators.SetDifference -> SetDifference(operands[0] as SetExpression, operands[1] as SetExpression, meta)
        SetOperators.Reals -> Reals(meta)
        SetOperators.Integers -> Integers(meta)

        is ComparisonOperator -> when (operator.comparator) {
            Comparator.Equal -> Equation(operands[0], operands[1], meta)
            Comparator.NotEqual -> Inequation(operands[0], operands[1], meta)
            else -> Inequality(operands[0], operator.comparator, operands[1], meta)
        }
        is DoubleComparisonOperator -> DoubleInequality(
            operands[0],
            operator.leftComparator,
            operands[1],
            operator.rightComparator,
            operands[2],
            meta,
        )
        ExpressionWithConstraintOperator -> ExpressionWithConstraint(operands[0], operands[1], meta)
        StatementUnionOperator -> StatementUnion(operands, meta)

        StatementSystemOperator -> StatementSystem(operands, meta)

        is NameOperator -> Name(operator.value, meta)

        VoidOperator -> VoidExpression(meta)

        ListOperator -> ListExpression(operands, meta)

        else -> Expression(operator, operands, meta)
    }
}

// This class is only there to be subclassed and can grow some methods so it is correct for it to be abstract
@Suppress("UnnecessaryAbstractClass")
abstract class ValueExpression internal constructor(
    operator: Operator,
    operands: List<Expression>,
    meta: NodeMeta,
) : Expression(operator, operands, meta)
