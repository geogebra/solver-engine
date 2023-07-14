package engine.expressionbuilder

import engine.context.Context
import engine.expressions.Cancel
import engine.expressions.Combine
import engine.expressions.Distribute
import engine.expressions.Expression
import engine.expressions.Factor
import engine.expressions.Inequality
import engine.expressions.Introduce
import engine.expressions.Move
import engine.expressions.MoveUnaryOperator
import engine.expressions.New
import engine.expressions.PathScope
import engine.expressions.asRational
import engine.expressions.buildExpression
import engine.expressions.divideBy
import engine.expressions.negOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.variableListOf
import engine.expressions.xp
import engine.operators.SolvableOperator
import engine.patterns.ArbitraryVariablePattern
import engine.patterns.CoefficientPattern
import engine.patterns.ExpressionProvider
import engine.patterns.InequalityPattern
import engine.patterns.IntegerPattern
import engine.patterns.IntegerProvider
import engine.patterns.Match
import engine.patterns.NaryPattern
import engine.patterns.NumberProvider
import engine.patterns.OptionalNegPattern
import engine.patterns.OptionalWrappingPattern
import engine.patterns.Pattern
import engine.patterns.RationalCoefficientPattern
import engine.patterns.RationalPattern
import engine.patterns.RecurringDecimalPattern
import engine.patterns.SolvablePattern
import engine.patterns.monomialPattern
import engine.steps.metadata.DragTargetPosition
import engine.steps.metadata.GmAction
import engine.steps.metadata.GmActionType
import engine.steps.metadata.GmDragToInfo
import engine.steps.metadata.GmPathModifier
import engine.steps.metadata.MetadataMaker
import engine.utility.RecurringDecimal
import engine.utility.primeFactorDecomposition
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Suppress("TooManyFunctions")
open class MappedExpressionBuilder(
    val context: Context,
    val expression: Expression,
    private val match: Match,
) {

    fun introduce(expressionProvider: ExpressionProvider, toExpression: Expression) =
        toExpression.withOrigin(Introduce(expressionProvider.getBoundExprs(match)))

    fun introduce(expression: Expression): Expression = expression.withOrigin(New)

    /**
     * Returns true if the given pattern is bound to a value in the match.
     */
    fun isBound(pattern: Pattern): Boolean {
        return match.getLastBinding(pattern) != null
    }

    /**
     * Returns the last subexpression bound to pattern
     */
    fun get(expressionProvider: ExpressionProvider): Expression {
        val expressions = expressionProvider.getBoundExprs(match)
        return when (expressions.size) {
            0 -> throw EmptyExpressionProviderException()
            1 -> expressions[0]
            else -> expressions[0].withOrigin(Factor(expressions))
        }
    }

    fun get(getExpression: (Match) -> Expression?) = getExpression(match)

    fun move(expressionProvider: ExpressionProvider): Expression {
        val expressions = expressionProvider.getBoundExprs(match)
        return when (expressions.size) {
            0 -> throw EmptyExpressionProviderException()
            1 -> expressions[0].withOrigin(Move(expressions[0]))
            else -> expressions[0].withOrigin(Factor(expressions))
        }
    }

    fun transform(expressionProvider: ExpressionProvider) =
        expressionProvider.getBoundExpr(match)!!.withOrigin(Combine(expressionProvider.getBoundExprs(match)))

    fun transform(expressionProvider: ExpressionProvider, toExpression: Expression) =
        toExpression.withOrigin(Combine(expressionProvider.getBoundExprs(match)))

    fun factor(expressionProvider: ExpressionProvider) =
        expressionProvider.getBoundExpr(match)!!.withOrigin(Factor(expressionProvider.getBoundExprs(match)))

    fun factorOp(from: List<Expression>, to: Expression): Expression {
        return to.withOrigin(Factor(from, PathScope.Operator))
    }

    fun distribute(expressionProvider: ExpressionProvider) =
        expressionProvider.getBoundExpr(match)!!.withOrigin(Distribute(expressionProvider.getBoundExprs(match)))

    fun distribute(vararg expressionProviders: ExpressionProvider) =
        expressionProviders.map { distribute(it) }

    fun cancel(expressionProvider: ExpressionProvider, inExpression: Expression): Expression {
        val boundExpressions = expressionProvider.getBoundExprs(match)
        return inExpression.withOrigin(
            Cancel(inExpression.origin, boundExpressions.map { Pair(it, PathScope.default) }),
        )
    }

    fun cancel(
        expressionProviderWithScopes: Map<ExpressionProvider, List<PathScope>>,
        inExpression: Expression,
    ): Expression {
        val expressionToScope = mutableListOf<Pair<Expression, PathScope>>()
        expressionProviderWithScopes.forEach { (expressionProvider, scopeList) ->
            val boundExpressions = expressionProvider.getBoundExprs(match)
            boundExpressions.forEach { boundExpr ->
                scopeList.forEach { scope ->
                    val pair = Pair(boundExpr, scope)
                    expressionToScope.add(pair)
                }
            }
        }
        return inExpression.withOrigin(Cancel(inExpression.origin, expressionToScope))
    }

    /**
     * [from] : the original expression containing a decorated UnaryOperator
     *
     * [to]   : output expression decorated with UnaryOperator which needs
     *          to be described as a "move" path-mapping of root of [from]
     *          to [to]
     *
     * For e.g. in 2*(negOp(x)) -> negOp(2*x)
     * here [from] = negOp(x); [to] = negOp(2*x)
     */
    fun moveUnaryOperator(from: ExpressionProvider, to: Expression): Expression {
        val fromExprList = from.getBoundExprs(match)
        return to.withOrigin(MoveUnaryOperator(fromExprList[0].origin))
    }

    /**
     * Returns the numeric value bound to the argument in the match.
     */
    fun getValue(numberProvider: NumberProvider): BigDecimal {
        return numberProvider.getBoundNumber(match)
    }

    /**
     * Returns the integer value bound to the argument in the match.
     */
    fun getValue(integerProvider: IntegerProvider): BigInteger {
        return integerProvider.getBoundInt(match)
    }

    fun getValue(pattern: RecurringDecimalPattern): RecurringDecimal {
        return pattern.getBoundRecurringDecimal(match)
    }

    fun getValue(pattern: RationalPattern) = get(pattern).asRational()

    fun getCoefficientValue(pattern: RationalCoefficientPattern) = get(pattern::coefficient)!!.asRational()

    /**
     * Finds a match for the given pattern in the given subexpression in the context of the current match.
     */
    fun matchPattern(pattern: Pattern, subexpression: Expression): Match? {
        return pattern.findMatches(context, match, subexpression).firstOrNull()
    }

    /**
     Creates a [MappedExpression] by applying a set of operations on an explicitly given
     match. To be used together with [matchPattern].
     */
    fun buildWith(match: Match, init: MappedExpressionBuilder.() -> Expression): Expression {
        val builder = MappedExpressionBuilder(context, expression, match)
        return builder.init()
    }

    /**
     Adds a negative sign to [to] if [from] matches a negative expression.
     */
    fun <T : Pattern> copySign(from: OptionalNegPattern<T>, to: Expression) =
        if (from.isNeg(match)) negOf(to) else to

    /**
     Adds a negative sign to [to] unless [from] matches a negative expression.
     */
    fun <T : Pattern> copyFlippedSign(from: OptionalNegPattern<T>, to: Expression) =
        if (from.isNeg(match)) to else negOf(to)

    fun transformTo(ptn: ExpressionProvider, value: Expression) = value.withOrigin(
        Combine(ptn.getBoundExprs(match)),
    )

    fun transformTo(ptn: ExpressionProvider, transformer: (Expression) -> Expression) =
        transformTo(ptn, transformer(ptn.getBoundExpr(match)!!))

    fun combineTo(ptn1: ExpressionProvider, ptn2: ExpressionProvider, value: Expression) =
        value.withOrigin(Combine(ptn1.getBoundExprs(match) + ptn2.getBoundExprs(match)))

    /**
     * Transforms the integer provided by [ptn] according to the given [operation].
     */
    fun integerOp(
        ptn: IntegerProvider,
        operation: (BigInteger) -> BigInteger,
    ) = transformTo(ptn, xp(operation(ptn.getBoundInt(match))))

    /**
     * Combines the integer values of [ptn1] and [ptn2] according to the given [operation].
     */
    fun integerOp(
        ptn1: IntegerProvider,
        ptn2: IntegerProvider,
        operation: (BigInteger, BigInteger) -> BigInteger,
    ) = combineTo(ptn1, ptn2, xp(operation(ptn1.getBoundInt(match), ptn2.getBoundInt(match))))

    /**
     * Combines the numeric values of [ptn1] and [ptn2] according to the given [operation].
     */
    fun numericOp(
        ptn: NumberProvider,
        operation: (BigDecimal) -> BigDecimal,
    ) = transformTo(ptn, xp(operation(ptn.getBoundNumber(match))))

    /**
     * Combines the numeric values of [ptn1] and [ptn2] according to the given [operation].
     */
    fun numericOp(
        ptn1: NumberProvider,
        ptn2: NumberProvider,
        operation: (BigDecimal, BigDecimal) -> BigDecimal,
    ) = combineTo(ptn1, ptn2, xp(operation(ptn1.getBoundNumber(match), ptn2.getBoundNumber(match))))

    /**
     * Rounds [n] to the context's precision.
     */
    fun round(n: BigDecimal): BigDecimal = n.setScale(context.effectivePrecision, RoundingMode.HALF_UP)

    /**
     * Returns the "rest of" part of [pattern] which match some operands of an n-ary expression, i.e. the non-matched
     * operands.
     */
    fun restOf(pattern: NaryPattern) = pattern.substitute(match, arrayOf())

    fun NaryPattern.substitute(vararg newVals: Expression) = substitute(match, newVals)

    fun SolvablePattern.isSelfDual(): Boolean {
        val operator = getBoundExpr(match)!!.operator as SolvableOperator
        return operator.getDual() == operator
    }

    fun SolvablePattern.deriveSolvable(lhs: Expression, rhs: Expression, useDual: Boolean = false): Expression {
        val operator = getBoundExpr(match)!!.operator as SolvableOperator
        return buildExpression(
            if (useDual) operator.getDual() else operator,
            listOf(lhs, rhs),
        )
    }

    // Probably can get rid of
    fun InequalityPattern.holdsFor(val1: BigDecimal, val2: BigDecimal): Boolean {
        val ineq = getBoundExpr(match)!! as Inequality
        return ineq.comparator.holdsFor(val1, val2)
    }

    @Suppress("ReturnCount")
    fun leadingCoefficientOfPolynomial(polynomialExpr: Expression): Expression? {
        val variables = polynomialExpr.variables
        if (variables.size != 1) return null

        val monomial = monomialPattern(ArbitraryVariablePattern())
        var degree = BigInteger.ZERO
        var leadingCoefficient: Expression? = null
        for (term in polynomialExpr.children) {
            if (!term.isConstant()) {
                // If it isn't a monomial, `polynomialExpr` isn't a polynomial or a polynomial not expanded
                val monomialMatch = matchPattern(monomial, term) ?: return null
                val monomialDegree = monomial.exponent.getBoundInt(monomialMatch)
                when {
                    monomialDegree > degree -> {
                        leadingCoefficient = monomial.coefficient(monomialMatch)
                        degree = monomialDegree
                    }
                    monomialDegree == degree -> {
                        // The polynomial is not normalised
                        return null
                    }
                }
            }
        }
        return leadingCoefficient
    }

    fun OptionalWrappingPattern.isWrapping() = this.isWrapping(match)

    fun Expression.wrapIf(pattern: OptionalWrappingPattern, wrapper: (Expression) -> Expression) =
        if (pattern.isWrapping()) wrapper(this) else this

    fun OptionalNegPattern<Pattern>.isNeg() = this.isNeg(match)

    /**
     * return a list of mapped expression of, prime factors of `integer`
     * raised to the power of its multiplicity.
     * for e.g. 63 --> listOf( xp(3^2), xp(7) )
     */
    fun productOfPrimeFactors(integer: IntegerPattern): List<Expression> {
        return getValue(integer)
            .primeFactorDecomposition()
            .map { (f, n) -> introduce(simplifiedPowerOf(xp(f), xp(n))) }
    }

    fun optionalDivideBy(pattern: OptionalWrappingPattern, mappedExpression: Expression) =
        mappedExpression.wrapIf(pattern, ::divideBy)

    fun MetadataMaker.make() = make(context, expression, match)

    /** Returns a [GmAction] that represents a tap/click user interaction on the passed expression to
     * trigger the transformation in Graspable Math (GM). */
    fun tap(expressionProvider: ExpressionProvider, pathModifier: GmPathModifier? = null): GmAction {
        return GmAction(GmActionType.Tap, listOf(get(expressionProvider)), pathModifier = pathModifier)
    }

    /** Returns a [GmAction] that represents a double tap/click user interaction on the passed expression to
     * trigger the transformation in Graspable Math (GM). */
    fun doubleTap(expressionProvider: ExpressionProvider, pathModifier: GmPathModifier? = null): GmAction {
        return GmAction(GmActionType.DoubleTap, listOf(get(expressionProvider)), pathModifier = pathModifier)
    }

    /**
     * Stands for "tap on operator". It represents the type of transformation that
     * you would do by tapping an operator in Graspable Math.
     */
    fun tapOp(expressionProvider: ExpressionProvider): GmAction {
        return GmAction(GmActionType.Tap, listOf(get(expressionProvider)), pathModifier = GmPathModifier.Operator)
    }

    /**
     * Returns a [GmAction] indicating a user interaction of applying the
     * Graspable Math (GM) formula with given id to the given expression.
     */
    fun applyFormula(expressionProvider: ExpressionProvider, formulaId: String): GmAction {
        return GmAction(GmActionType.Formula, listOf(get(expressionProvider)), formulaId = formulaId)
    }

    /**
     * Returns a [GmAction] indicating a user needs to manually edit the
     * provided expression in Graspable Math (GM). This is a good fallback
     * action when no gesture is available in GM to perform the transformation.
     */
    fun edit(expressionProvider: ExpressionProvider): GmAction {
        return GmAction(GmActionType.Edit, listOf(get(expressionProvider)))
    }

    fun editOp(expressionProvider: ExpressionProvider): GmAction {
        return GmAction(GmActionType.Edit, listOf(get(expressionProvider)), GmPathModifier.Operator)
    }

    /**
     * Returns a [GmAction] indicating a user needs to drag the specified
     * expression to the specified "dragTo" expression and relative position in
     * Graspable Math (GM) to perform the transformation.
     */
    fun drag(
        expressionProvider: ExpressionProvider,
        dragToExpressionProvider: ExpressionProvider,
        dragToPosition: DragTargetPosition = DragTargetPosition.Onto,
    ): GmAction {
        return GmAction(
            GmActionType.Drag,
            expressions = listOf(get(expressionProvider)),
            dragTo = GmDragToInfo(get(dragToExpressionProvider), dragToPosition),
        )
    }

    fun drag(
        expressionProvider: ExpressionProvider,
        pathModifier: GmPathModifier?,
        dragToExpressionProvider: ExpressionProvider,
        dragToPathModifier: GmPathModifier? = null,
        dragToPosition: DragTargetPosition = DragTargetPosition.Onto,
    ): GmAction {
        return GmAction(
            GmActionType.Drag,
            expressions = listOf(get(expressionProvider)),
            pathModifier = pathModifier,
            dragTo = GmDragToInfo(get(dragToExpressionProvider), dragToPosition, dragToPathModifier),
        )
    }

    /**
     * Returns a [GmAction] indicating a user needs to pick up several
     * expressions by dragging the first one over the others and then drag all of
     * them to the specified "dragTo" expression and relative position in
     * Graspable Math (GM) to perform the transformation.
     */
    fun dragCollect(
        expressionProviders: List<ExpressionProvider>,
        dragToExpressionProvider: ExpressionProvider,
        dragToPathModifier: GmPathModifier? = null,
        dragToPosition: DragTargetPosition = DragTargetPosition.Onto,
    ): GmAction {
        return GmAction(
            GmActionType.DragCollect,
            expressions = expressionProviders.map { get(it) },
            dragTo = GmDragToInfo(get(dragToExpressionProvider), dragToPosition, dragToPathModifier),
        )
    }

    fun noGmSupport(): GmAction {
        return GmAction(GmActionType.NotSupported)
    }

    /**
     * List all solution variables, useful in explanations
     */
    fun listOfsolutionVariables() = if (context.solutionVariables.size == 1) {
        xp(context.solutionVariables[0])
    } else {
        variableListOf(context.solutionVariables)
    }

    fun CoefficientPattern.getCoefficient() = coefficient(match)
}

class EmptyExpressionProviderException : Exception("No expressions were bound by the expression provider")
