package engine.patterns

import engine.context.Context
import engine.expressions.ChildPath
import engine.expressions.Path
import engine.expressions.Subexpression
import engine.operators.NaryOperator

/**
 * An interface for a Pattern connected by an
 * NaryOperator, with the operands provided as
 * list of `Pattern` objects
 */
abstract class NaryPatternBase(val operator: NaryOperator, val operands: List<Pattern>) : BasePattern() {

    fun getMatchIndexes(m: Match, path: Path): List<Int> {
        val matchIndexes = mutableListOf<Int>()
        for (op in operands) {
            for (p in m.getBoundPaths(op)) {
                val (parent, index) = p as ChildPath
                if (parent == path) {
                    matchIndexes.add(index)
                }
            }
        }

        return matchIndexes
    }
}

class PartialNaryPattern(
    operator: NaryOperator,
    operands: List<Pattern>,
    val minSize: Int = 0
) : NaryPatternBase(operator, operands) {

    override fun doFindMatches(context: Context, match: Match, subexpression: Subexpression): Sequence<Match> {
        val childCount = subexpression.expr.operands.size
        if (subexpression.expr.operator != operator || childCount < operands.size || childCount < minSize) {
            return emptySequence()
        }

        fun rec(match: Match, searchIndex: Int, initialChildIndex: Int): Sequence<Match> {
            return sequence {
                if (searchIndex < operands.size) {
                    val lastChildIndex = childCount - operands.count() + searchIndex
                    for (childIndex in initialChildIndex..lastChildIndex) {
                        val childMatches = operands[searchIndex].findMatches(
                            context,
                            match,
                            subexpression.nthChild(childIndex)
                        )
                        for (childMatch in childMatches) {
                            yieldAll(rec(childMatch, searchIndex + 1, childIndex + 1))
                        }
                    }
                } else {
                    yield(match)
                }
            }
        }

        return rec(match.newChild(this, subexpression), 0, 0)
    }

    fun getRestSubexpressions(m: Match): List<Subexpression> {
        val sub = m.getLastBinding(this)!!
        val matchIndexes = getMatchIndexes(m, sub.path)

        return sub.children().filter { subexpression -> !matchIndexes.contains(subexpression.index()) }
    }
}

class CommutativeNaryPattern(operator: NaryOperator, operands: List<Pattern>) : NaryPatternBase(operator, operands) {

    override fun doFindMatches(context: Context, match: Match, subexpression: Subexpression): Sequence<Match> {
        val childCount = subexpression.expr.operands.size
        if (subexpression.expr.operator != operator || childCount != operands.count()) {
            return emptySequence()
        }

        val usedValues = MutableList(operands.size) { false }
        fun rec(match: Match, searchIndex: Int): Sequence<Match> {
            return sequence {
                if (searchIndex < operands.size) {
                    for ((index, used) in usedValues.withIndex()) {
                        if (used) {
                            continue
                        }

                        usedValues[index] = true
                        val childMatches = operands[searchIndex].findMatches(
                            context,
                            match,
                            subexpression.nthChild(index)
                        )
                        for (childMatch in childMatches) {
                            yieldAll(rec(childMatch, searchIndex + 1))
                        }
                        usedValues[index] = false
                    }
                } else {
                    yield(match)
                }
            }
        }

        return rec(match.newChild(this, subexpression), 0)
    }
}

fun sumContaining(vararg terms: Pattern) = PartialNaryPattern(NaryOperator.Sum, terms.asList())

fun commutativeSumOf(vararg terms: Pattern) = CommutativeNaryPattern(NaryOperator.Sum, terms.asList())

fun productContaining(vararg factors: Pattern, minSize: Int = 0) =
    PartialNaryPattern(NaryOperator.Product, factors.asList(), minSize)

fun commutativeProductOf(vararg factors: Pattern) = CommutativeNaryPattern(NaryOperator.Product, factors.asList())
