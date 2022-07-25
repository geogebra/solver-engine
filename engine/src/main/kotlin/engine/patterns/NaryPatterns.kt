package engine.patterns

import engine.expressions.ChildPath
import engine.expressions.NaryOperator
import engine.expressions.Path
import engine.expressions.Subexpression

/**
 * An interface for a Pattern connected by an
 * NaryOperator, with the operands provided as
 * list of `Pattern` objects
 */
interface NaryPatternBase : Pattern {

    val operator: NaryOperator
    val operands: List<Pattern>

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

data class PartialNaryPattern(
    override val operator: NaryOperator,
    override val operands: List<Pattern>,
    val minSize: Int = 0
) : NaryPatternBase {

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        val childCount = subexpression.expr.operands.size
        if (subexpression.expr.operator != operator || childCount < operands.size || childCount < minSize) {
            return emptySequence()
        }

        fun rec(match: Match, searchIndex: Int, initialChildIndex: Int): Sequence<Match> {
            return sequence {
                if (searchIndex < operands.size) {
                    val lastChildIndex = childCount - operands.count() + searchIndex
                    for (childIndex in initialChildIndex..lastChildIndex) {
                        val childMatches = operands[searchIndex].findMatches(subexpression.nthChild(childIndex), match)
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

data class CommutativeNaryPattern(override val operator: NaryOperator, override val operands: List<Pattern>) :
    NaryPatternBase {

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
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
                        val childMatches = operands[searchIndex].findMatches(subexpression.nthChild(index), match)
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
