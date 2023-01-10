package engine.patterns

import engine.context.Context
import engine.expressions.Expression
import engine.expressions.productOf
import engine.expressions.sumOf
import engine.operators.NaryOperator

/**
 * Since the n-ary operators sum and product are both commutative
 * and associative, we often want to operate on terms which are not
 * right next to each other, nor in a fixed order.
 * This pattern facilitates such partial and commutative matching.
 */
class NaryPattern(
    val operator: NaryOperator,
    val childPatterns: List<Pattern>,
    val partial: Boolean,
    val commutative: Boolean
) : BasePattern() {

    override fun doFindMatches(context: Context, match: Match, subexpression: Expression): Sequence<Match> {
        if (!operator.equiv(subexpression.operator)) {
            return emptySequence()
        }

        val flattenedOperands = flattenOperands(subexpression)
        if (flattenedOperands.size < childPatterns.size || (!partial && flattenedOperands.size > childPatterns.size)) {
            return emptySequence()
        }

        return RecursiveMatcher(context, childPatterns, flattenedOperands, partial, commutative)
            .recursiveMatch(match.newChild(this, subexpression))
    }

    fun getRestSubexpressions(m: Match): List<Expression> {
        val sub = m.getLastBinding(this)!!
        val matchIndexes = getMatchedOrigins(m)

        return flattenOperands(sub).filter { subexpression -> !matchIndexes.contains(subexpression.origin) }
    }

    /**
     * Substitutes the matched operands of [this] with [newVals]. If there are spare matched operands, they are removed
     * from the resulting expression. If [newVals] has more items than the number of matched operands, the extra items
     * are ignored.
     */
    fun substitute(match: Match, newVals: Array<out Expression>): Expression {
        val sub = match.getLastBinding(this)!!
        val matchedOrigins = getMatchedOrigins(match)

        val flattenedOperands = flattenOperands(sub)

        val restChildren = mutableListOf<Expression>()
        for (child in flattenedOperands) {
            val newValIndex = matchedOrigins.indexOf(child.origin)
            when {
                newValIndex == -1 -> restChildren.add(child)
                newValIndex < newVals.size -> restChildren.add(
                    newVals[newValIndex]
                )
            }
        }

        return when (restChildren.size) {
            1 -> restChildren[0]
            else -> if (operator == NaryOperator.Product) productOf(restChildren) else sumOf(restChildren)
        }
    }

    private fun flattenOperands(sub: Expression) = when (sub.operator) {
        NaryOperator.Sum -> sub.children()
        else -> sub.flattenedProductChildren()
    }

    fun getMatchedChildExpressions(match: Match) =
        childPatterns.flatMap { match.getBoundExprs(it) }
            .filter { it.isChildOfOrSelf(match.getBoundExpr(this)) }

    private fun getMatchedOrigins(m: Match) = getMatchedChildExpressions(m).map { it.origin }
}

private data class RecursiveMatcher(
    val context: Context,
    val childPatterns: List<Pattern>,
    val children: List<Expression>,
    val partial: Boolean,
    val commutative: Boolean
) {

    fun recursiveMatch(match: Match): Sequence<Match> {
        val usedValues = MutableList(children.size) { false }
        return doRecursiveMatch(match, 0, usedValues)
    }

    private fun doRecursiveMatch(
        match: Match,
        searchIndex: Int,
        usedValues: MutableList<Boolean>
    ): Sequence<Match> {
        return sequence {
            if (searchIndex < childPatterns.size) {
                val availableIndices = getAvailableIndices(searchIndex, usedValues)

                for (index in availableIndices) {
                    val childMatches = childPatterns[searchIndex].findMatches(
                        context,
                        match,
                        children[index]
                    )

                    usedValues[index] = true
                    for (childMatch in childMatches) {
                        yieldAll(doRecursiveMatch(childMatch, searchIndex + 1, usedValues))
                    }
                    usedValues[index] = false
                }
            } else {
                yield(match)
            }
        }
    }

    private fun getAvailableIndices(searchIndex: Int, usedValues: List<Boolean>) = when {
        commutative -> usedValues.mapIndexed { index, used -> if (used) null else index }.filterNotNull()
        partial -> {
            val initialChildIndex = usedValues.lastIndexOf(true) + 1
            val lastChildIndex = children.count() - childPatterns.count() + searchIndex
            initialChildIndex..lastChildIndex
        }
        else -> listOf(usedValues.lastIndexOf(true) + 1)
    }
}

fun productOf(vararg factors: Pattern) =
    NaryPattern(NaryOperator.Product, factors.asList(), commutative = false, partial = false)

fun productContaining(vararg factors: Pattern) =
    NaryPattern(NaryOperator.Product, factors.asList(), commutative = false, partial = true)

fun commutativeProductOf(vararg factors: Pattern) =
    NaryPattern(NaryOperator.Product, factors.asList(), commutative = true, partial = false)

fun commutativeProductContaining(vararg factors: Pattern) =
    NaryPattern(NaryOperator.Product, factors.asList(), commutative = true, partial = true)

fun sumOf(vararg factors: Pattern) =
    NaryPattern(NaryOperator.Sum, factors.asList(), commutative = false, partial = false)

fun sumContaining(vararg factors: Pattern) =
    NaryPattern(NaryOperator.Sum, factors.asList(), commutative = false, partial = true)

fun commutativeSumOf(vararg factors: Pattern) =
    NaryPattern(NaryOperator.Sum, factors.asList(), commutative = true, partial = false)

fun commutativeSumContaining(vararg factors: Pattern) =
    NaryPattern(NaryOperator.Sum, factors.asList(), commutative = true, partial = true)
