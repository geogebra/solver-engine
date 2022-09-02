package engine.expressionmakers

import engine.expressions.Expression
import engine.expressions.MappedExpression
import engine.expressions.PathMappingLeaf
import engine.expressions.PathMappingType
import engine.expressions.Subexpression
import engine.expressions.divideBy
import engine.expressions.flattenedNaryMappedExpression
import engine.expressions.negOf
import engine.expressions.xp
import engine.patterns.IntegerProvider
import engine.patterns.Maker
import engine.patterns.Match
import engine.patterns.NaryPatternBase
import engine.patterns.OptionalNegPattern
import engine.patterns.OptionalWrappingPattern
import engine.patterns.PartialNaryPattern
import engine.patterns.PathProvider
import engine.patterns.Pattern
import java.math.BigInteger

typealias ExpressionMaker = Maker<MappedExpression>

@Suppress("TooManyFunctions")
class MakerBuilder(private val match: Match) {

    fun introduce(expression: Expression): MappedExpression =
        MappedExpression(expression, PathMappingLeaf(listOf(), PathMappingType.Introduce))

    fun move(pathProvider: PathProvider) =
        UnaryPathMappingAnnotator(PathMappingType.Move, pathProvider).make(match)

    fun transform(pathProvider: PathProvider): MappedExpression =
        UnaryPathMappingAnnotator(PathMappingType.Transform, pathProvider).make(match)

    fun transform(pathProvider: PathProvider, toExpression: MappedExpression): MappedExpression =
        BinaryPathMappingAnnotator(PathMappingType.Transform, pathProvider, toExpression).make(match)

    fun factor(pathProvider: PathProvider) =
        UnaryPathMappingAnnotator(PathMappingType.Factor, pathProvider).make(match)

    fun distribute(pathProvider: PathProvider) =
        UnaryPathMappingAnnotator(PathMappingType.Distribute, pathProvider).make(match)

    fun cancel(pathProvider: PathProvider, inExpression: MappedExpression) =
        BinaryPathMappingAnnotator(PathMappingType.Cancel, pathProvider, inExpression).make(match)

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
     * Returns the integer value bound to the argument in the match.
     */
    fun getValue(integerProvider: IntegerProvider): BigInteger {
        return integerProvider.getBoundInt(match)
    }

    /**
     * Finds a match for the given pattern in the given subexpression in the context of the current match.
     */
    fun matchPattern(pattern: Pattern, subexpression: Subexpression): Match? {
        return pattern.findMatches(subexpression, match).firstOrNull()
    }

    /**
     * Creates a [MappedExpression] by applying a set of operations on an explicitly given match.
     * To be used together with [matchPattern].
     */
    fun buildWith(match: Match, init: MakerBuilder.() -> MappedExpression): MappedExpression {
        val builder = MakerBuilder(match)
        return builder.init()
    }

    /**
     * Adds a negative sign to [to] if [from] matches a negative expression.
     */
    fun copySign(from: OptionalNegPattern, to: MappedExpression) =
        if (from.isNeg(match)) negOf(to) else to

    /**
     * Adds a negative sign to [to] unless [from] matches a negative expression.
     */
    fun copyFlippedSign(from: OptionalNegPattern, to: MappedExpression) =
        if (from.isNeg(match)) to else negOf(to)

    /**
     * Transforms the integer provided by [ptn] according to the given [operation].
     */
    fun numericOp(
        ptn: IntegerProvider,
        operation: (BigInteger) -> BigInteger
    ): MappedExpression {
        val value = operation(ptn.getBoundInt(match))

        val result = when {
            value.signum() >= 0 -> xp(value)
            else -> negOf(xp(-value))
        }
        return MappedExpression(
            result,
            PathMappingLeaf(ptn.getBoundPaths(match), PathMappingType.Transform),
        )
    }

    /**
     * Combines the integer values of [ptn1] and [ptn2] according to the given [operation].
     */
    fun numericOp(
        ptn1: IntegerProvider,
        ptn2: IntegerProvider,
        operation: (BigInteger, BigInteger) -> BigInteger
    ): MappedExpression {
        val value = operation(ptn1.getBoundInt(match), ptn2.getBoundInt(match))

        val result = when {
            value.signum() >= 0 -> xp(value)
            else -> negOf(xp(-value))
        }
        return MappedExpression(
            result,
            PathMappingLeaf(ptn1.getBoundPaths(match) + ptn2.getBoundPaths(match), PathMappingType.Combine),
        )
    }

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

    fun OptionalWrappingPattern.isWrapping() = this.isWrapping(match)

    fun MappedExpression.wrapIf(pattern: OptionalWrappingPattern, wrapper: (MappedExpression) -> MappedExpression) =
        if (pattern.isWrapping()) wrapper(this) else this

    fun optionalDivideBy(pattern: OptionalWrappingPattern, mappedExpression: MappedExpression) =
        mappedExpression.wrapIf(pattern, ::divideBy)
}
