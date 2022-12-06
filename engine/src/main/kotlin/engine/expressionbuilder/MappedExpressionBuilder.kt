package engine.expressionbuilder

import engine.context.Context
import engine.expressions.Combine
import engine.expressions.Distribute
import engine.expressions.Expression
import engine.expressions.Factor
import engine.expressions.Label
import engine.expressions.New
import engine.expressions.divideBy
import engine.expressions.negOf
import engine.expressions.productOf
import engine.expressions.simplifiedPowerOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.patterns.CoefficientPattern
import engine.patterns.ExpressionProvider
import engine.patterns.IntegerPattern
import engine.patterns.IntegerProvider
import engine.patterns.Match
import engine.patterns.NaryPattern
import engine.patterns.NumberProvider
import engine.patterns.OptionalNegPattern
import engine.patterns.OptionalWrappingPattern
import engine.patterns.Pattern
import engine.patterns.RecurringDecimalPattern
import engine.utility.RecurringDecimal
import engine.utility.primeFactorDecomposition
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

@Suppress("TooManyFunctions")
class MappedExpressionBuilder(
    val context: Context,
    private val match: Match,
) {

    fun introduce(expression: Expression): Expression = expression.withOrigin(New)

    fun move(expressionProvider: ExpressionProvider): Expression {
        val expressions = expressionProvider.getBoundExprs(match)
        return when (expressions.size) {
            0 -> throw MoveEmptyExpressionProviderException()
            1 -> expressions[0]
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

    // TODO
    @Suppress("UnusedPrivateMember")
    fun cancel(expressionProvider: ExpressionProvider, inExpression: Expression) = inExpression

    /**
     * Returns true if the given pattern is bound to a value in the match.
     */
    fun isBound(pattern: Pattern): Boolean {
        return match.getLastBinding(pattern) != null
    }

    /**
     * Returns the last subexpression bound to pattern
     */
    fun get(pattern: ExpressionProvider): Expression? = pattern.getBoundExpr(match)

    fun get(getExpression: (Match) -> Expression?) = getExpression(match)

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

    /**
     * Finds a match for the given pattern in the given subexpression in the context of the current match.
     */
    fun matchPattern(pattern: Pattern, subexpression: Expression): Match? {
        return pattern.findMatches(context, match, subexpression).firstOrNull()
    }

    /**
     * Creates a [MappedExpression] by applying a set of operations on an explicitly given match.
     * To be used together with [matchPattern].
     */
    fun buildWith(match: Match, init: MappedExpressionBuilder.() -> Expression): Expression {
        val builder = MappedExpressionBuilder(context, match)
        return builder.init()
    }

    /**
     * Adds a negative sign to [to] if [from] matches a negative expression.
     */
    fun <T : Pattern> copySign(from: OptionalNegPattern<T>, to: Expression) =
        if (from.isNeg(match)) negOf(to) else to

    /**
     * Adds a negative sign to [to] unless [from] matches a negative expression.
     */
    fun <T : Pattern> copyFlippedSign(from: OptionalNegPattern<T>, to: Expression) =
        if (from.isNeg(match)) to else negOf(to)

    fun transformTo(ptn: ExpressionProvider, value: Expression) = value.withOrigin(
        Combine(ptn.getBoundExprs(match))
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
        operation: (BigInteger) -> BigInteger
    ) = transformTo(ptn, xp(operation(ptn.getBoundInt(match))))

    /**
     * Combines the integer values of [ptn1] and [ptn2] according to the given [operation].
     */
    fun integerOp(
        ptn1: IntegerProvider,
        ptn2: IntegerProvider,
        operation: (BigInteger, BigInteger) -> BigInteger
    ) = combineTo(ptn1, ptn2, xp(operation(ptn1.getBoundInt(match), ptn2.getBoundInt(match))))

    /**
     * Combines the numeric values of [ptn1] and [ptn2] according to the given [operation].
     */
    fun numericOp(
        ptn: NumberProvider,
        operation: (BigDecimal) -> BigDecimal
    ) = transformTo(ptn, xp(operation(ptn.getBoundNumber(match))))

    /**
     * Combines the numeric values of [ptn1] and [ptn2] according to the given [operation].
     */
    fun numericOp(
        ptn1: NumberProvider,
        ptn2: NumberProvider,
        operation: (BigDecimal, BigDecimal) -> BigDecimal
    ) = combineTo(ptn1, ptn2, xp(operation(ptn1.getBoundNumber(match), ptn2.getBoundNumber(match))))

    /**
     * Rounds [n] to the context's precision.
     */
    fun round(n: BigDecimal): BigDecimal = n.setScale(context.effectivePrecision, RoundingMode.HALF_UP)

    /**
     * Returns the "rest of" part of [pattern] which match some operands of an nary expression, i.e. the non-matched
     * operands.
     */
    fun restOf(pattern: NaryPattern) = pattern.substitute(match, arrayOf())

    fun NaryPattern.substitute(vararg newVals: Expression) = substitute(match, newVals)

    fun collectLikeTermsInSum(
        sub: Expression,
        commonTerm: CoefficientPattern,
        labelForCollectedTerms: Label? = null
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
}

class MoveEmptyExpressionProviderException : Exception("need at least one expression")
