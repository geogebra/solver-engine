package engine.expressionbuilder

import engine.context.Context
import engine.expressions.Expression
import engine.expressions.MappedExpression
import engine.expressions.PathMappingLeaf
import engine.expressions.PathMappingType
import engine.expressions.Subexpression
import engine.expressions.divideBy
import engine.expressions.flattenedNaryMappedExpression
import engine.expressions.negOf
import engine.expressions.powerOf
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.expressions.xp
import engine.patterns.CoefficientPattern
import engine.patterns.IntegerPattern
import engine.patterns.IntegerProvider
import engine.patterns.Match
import engine.patterns.NaryPatternBase
import engine.patterns.NumberProvider
import engine.patterns.OptionalNegPattern
import engine.patterns.OptionalWrappingPattern
import engine.patterns.PartialNaryPattern
import engine.patterns.PathProvider
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
    private fun unaryAnnotate(pathProvider: PathProvider, pathMappingType: PathMappingType) = MappedExpression(
        pathProvider.getBoundExpr(match)!!,
        PathMappingLeaf(pathProvider.getBoundPaths(match), pathMappingType)
    )

    @Suppress("UnusedPrivateMember")
    private fun binaryAnnotate(
        pathProvider: PathProvider,
        pathMappingType: PathMappingType,
        expression: MappedExpression
    ) =
        expression

    fun introduce(expression: Expression): MappedExpression =
        MappedExpression(expression, PathMappingLeaf(listOf(), PathMappingType.Introduce))

    fun move(pathProvider: PathProvider) = unaryAnnotate(pathProvider, PathMappingType.Move)

    fun transform(pathProvider: PathProvider) = unaryAnnotate(pathProvider, PathMappingType.Transform)

    fun transform(pathProvider: PathProvider, toExpression: MappedExpression): MappedExpression =
        binaryAnnotate(pathProvider, PathMappingType.Transform, toExpression)

    fun factor(pathProvider: PathProvider) = unaryAnnotate(pathProvider, PathMappingType.Factor)

    fun distribute(pathProvider: PathProvider) = unaryAnnotate(pathProvider, PathMappingType.Distribute)

    fun cancel(pathProvider: PathProvider, inExpression: MappedExpression) =
        binaryAnnotate(pathProvider, PathMappingType.Cancel, inExpression)

    /**
     * Returns true if the given pattern is bound to a value in the match.
     */
    fun isBound(pattern: Pattern): Boolean {
        return match.getLastBinding(pattern) != null
    }

    /**
     * Returns the last subexpression bound to pattern
     */
    fun get(pattern: Pattern): Subexpression? = match.getLastBinding(pattern)

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
    fun matchPattern(pattern: Pattern, subexpression: Subexpression): Match? {
        return pattern.findMatches(context, match, subexpression).firstOrNull()
    }

    /**
     * Creates a [MappedExpression] by applying a set of operations on an explicitly given match.
     * To be used together with [matchPattern].
     */
    fun buildWith(match: Match, init: MappedExpressionBuilder.() -> MappedExpression): MappedExpression {
        val builder = MappedExpressionBuilder(context, match)
        return builder.init()
    }

    /**
     * Adds a negative sign to [to] if [from] matches a negative expression.
     */
    fun <T : Pattern> copySign(from: OptionalNegPattern<T>, to: MappedExpression) =
        if (from.isNeg(match)) negOf(to) else to

    /**
     * Adds a negative sign to [to] unless [from] matches a negative expression.
     */
    fun <T : Pattern> copyFlippedSign(from: OptionalNegPattern<T>, to: MappedExpression) =
        if (from.isNeg(match)) to else negOf(to)

    fun transformTo(ptn: PathProvider, value: Expression): MappedExpression {
        return MappedExpression(
            value,
            PathMappingLeaf(ptn.getBoundPaths(match), PathMappingType.Transform),
        )
    }

    fun transformTo(ptn: PathProvider, transformer: (Expression) -> Expression) =
        transformTo(ptn, transformer(ptn.getBoundExpr(match)!!))

    fun combineTo(ptn1: PathProvider, ptn2: PathProvider, value: Expression): MappedExpression {
        return MappedExpression(
            value,
            PathMappingLeaf(ptn1.getBoundPaths(match) + ptn2.getBoundPaths(match), PathMappingType.Combine),
        )
    }

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
    fun restOf(pattern: PartialNaryPattern) = pattern.substitute(/* nothing */)

    /**
     * Substitutes the matched operands of [this] with [newVals]. If there are spare matched operands, they are removed
     * from the resulting expression. If [newVals] has more items than the number of matched operands, the extra items
     * are ignored.
     */
    fun NaryPatternBase.substitute(vararg newVals: MappedExpression): MappedExpression {
        val sub = match.getLastBinding(this)!!
        val matchIndexes = this.getMatchIndexes(match, sub.path)
        val restChildren = ArrayList<MappedExpression>()
        for (child in sub.children()) {
            val newValIndex = matchIndexes.indexOf(child.index())
            when {
                newValIndex == -1 -> restChildren.add(move(child))
                newValIndex < newVals.size -> restChildren.add(
                    newVals[newValIndex]
                )
            }
        }

        return when (restChildren.size) {
            1 -> restChildren[0]
            else -> flattenedNaryMappedExpression(operator, restChildren)
        }
    }

    fun collectLikeTermsInSum(sub: Subexpression, commonTerm: CoefficientPattern): MappedExpression {
        val coefficients = mutableListOf<MappedExpression>()

        val otherTerms = mutableListOf<MappedExpression>()
        var firstIndex: Int? = null

        for (term in sub.children()) {
            val m = matchPattern(commonTerm, term)
            if (m != null) {
                coefficients.add(commonTerm.coefficient(m))
                if (firstIndex == null) {
                    firstIndex = term.index()
                }
            } else {
                otherTerms.add(move(term))
            }
        }

        require(firstIndex != null)

        val collectedRoots = productOf(sumOf(coefficients), move(commonTerm.value))
        otherTerms.add(firstIndex, collectedRoots)

        return sumOf(otherTerms)
    }

    fun OptionalWrappingPattern.isWrapping() = this.isWrapping(match)

    fun MappedExpression.wrapIf(pattern: OptionalWrappingPattern, wrapper: (MappedExpression) -> MappedExpression) =
        if (pattern.isWrapping()) wrapper(this) else this

    fun OptionalNegPattern<Pattern>.isNeg() = this.isNeg(match)

    /**
     * return a list of mapped expression of, prime factors of `integer`
     * raised to the power of its multiplicity.
     * for e.g. 63 --> listOf( xp(3^2), xp(7) )
     */
    fun productOfPrimeFactors(integer: IntegerPattern): List<MappedExpression> {
        return getValue(integer).primeFactorDecomposition()
            .map { (f, n) -> introduce(if (n == BigInteger.ONE) xp(f) else powerOf(xp(f), xp(n))) }
    }

    fun optionalDivideBy(pattern: OptionalWrappingPattern, mappedExpression: MappedExpression) =
        mappedExpression.wrapIf(pattern, ::divideBy)
}
