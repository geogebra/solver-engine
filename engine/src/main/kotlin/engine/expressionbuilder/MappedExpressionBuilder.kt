package engine.expressionbuilder

import engine.context.Context
import engine.expressions.Cancel
import engine.expressions.Combine
import engine.expressions.Distribute
import engine.expressions.Expression
import engine.expressions.Factor
import engine.expressions.Label
import engine.expressions.Move
import engine.expressions.New
import engine.expressions.asRational
import engine.expressions.divideBy
import engine.expressions.negOf
import engine.expressions.productOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.operators.EquationOperator
import engine.operators.InequalityOperators
import engine.operators.NaryOperator
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
import engine.patterns.SolutionVariablePattern
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
    private val match: Match,
) {

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

    fun distribute(expressionProvider: ExpressionProvider) =
        expressionProvider.getBoundExpr(match)!!.withOrigin(Distribute(expressionProvider.getBoundExprs(match)))

    fun distribute(vararg expressionProviders: ExpressionProvider) =
        expressionProviders.map { distribute(it) }

    fun cancel(expressionProvider: ExpressionProvider, inExpression: Expression) =
        inExpression.withOrigin(Cancel(inExpression.origin, expressionProvider.getBoundExprs(match)))

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
        val builder = MappedExpressionBuilder(context, match)
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

    fun NaryPattern.extract(): Expression {
        val matchedChildren = getMatchedChildExpressions(match)

        return when (operator) {
            NaryOperator.Sum -> sumOf(matchedChildren)
            NaryOperator.Product, NaryOperator.ImplicitProduct -> productOf(matchedChildren)
        }
    }

    fun SolvablePattern.isEquation() = getBoundExpr(match)?.operator == EquationOperator

    fun InequalityPattern.holdsFor(val1: BigDecimal, val2: BigDecimal): Boolean {
        val operator = getBoundExpr(match)!!.operator as InequalityOperators
        return operator.holdsFor(val1, val2)
    }

    fun InequalityPattern.toInterval(boundary: Expression): Expression {
        val operator = getBoundExpr(match)!!.operator as InequalityOperators
        return operator.toInterval(boundary)
    }

    fun InequalityPattern.sameInequality(lhs: Expression, rhs: Expression): Expression {
        val operator = getBoundExpr(match)!!.operator as InequalityOperators
        return Expression(operator, listOf(lhs, rhs))
    }

    fun InequalityPattern.dualInequality(lhs: Expression, rhs: Expression): Expression {
        val operator = getBoundExpr(match)!!.operator as InequalityOperators
        return Expression(operator.getDual(), listOf(lhs, rhs))
    }

    fun SolvablePattern.sameSolvable(lhs: Expression, rhs: Expression): Expression {
        val operator = getBoundExpr(match)!!.operator
        return Expression(operator, listOf(lhs, rhs))
    }

    fun leadingCoefficientOfPolynomial(polynomialExpr: Expression): Expression? {
        val monomial = monomialPattern(SolutionVariablePattern())
        var degree = BigInteger.ZERO
        var leadingCoefficient: Expression? = null
        for (term in polynomialExpr.children()) {
            if (!term.isConstant()) {
                val monomialMatch = matchPattern(monomial, term) ?: continue
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

    fun collectLikeTermsInSum(
        sub: Expression,
        commonTerm: CoefficientPattern,
        labelForCollectedTerms: Label? = null,
    ): Expression {
        val coefficients = mutableListOf<Expression>()

        val otherTerms = mutableListOf<Expression>()
        var firstIndex: Int? = null

        for ((index, term) in sub.children().withIndex()) {
            val m = matchPattern(commonTerm, term)
            if (m != null) {
                coefficients.add(commonTerm.coefficient(m))
                if (firstIndex == null) {
                    firstIndex = index
                }
            } else {
                otherTerms.add(term)
            }
        }

        require(firstIndex != null)

        val collectedTerms = productOf(sumOf(coefficients), move(commonTerm.value)).withLabel(labelForCollectedTerms)
        otherTerms.add(firstIndex, collectedTerms)

        return sumOf(otherTerms)
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

    fun MetadataMaker.make() = make(context, match)

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
        pathModifier: GmPathModifier,
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
}

class EmptyExpressionProviderException : Exception("No expressions were bound by the expression provider")
