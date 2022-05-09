package patterns

import expressions.*
import steps.PathMapping
import java.util.*

interface Pattern {
    fun findMatches(match: Match, subexpression: Subexpression): Sequence<Match>

    fun substitute(m: Match, result: ExpressionMaker): ExpressionMaker
}

interface FixedSizePattern : Pattern {

    fun children(): List<Pattern>

    fun checkExpressionKind(expr: Expression): Boolean

    override fun findMatches(match: Match, subexpression: Subexpression): Sequence<Match> {
        if (!checkExpressionKind(subexpression.expr)) {
            return emptySequence()
        }

        val previouslyMatched = match.getBoundExpr(this)

        if (previouslyMatched != null) {
            return when (previouslyMatched) {
                subexpression.expr -> sequenceOf(match.newChild(this, subexpression))
                else -> emptySequence()
            }
        }

        var matches = sequenceOf(match.newChild(this, subexpression))
        for ((index, op) in children().withIndex()) {
            matches = matches.flatMap { op.findMatches(it, subexpression.nthChild(index)) }
        }
        return matches
    }

    override fun substitute(m: Match, result: ExpressionMaker): ExpressionMaker = result
}


data class FixedPattern(val expr: Expression) : FixedSizePattern {
    override fun children(): List<Pattern> = emptyList()

    override fun checkExpressionKind(expr: Expression) = this.expr == expr
}

class AnyPattern : FixedSizePattern {
    override fun children() = emptyList<Pattern>()

    override fun checkExpressionKind(expr: Expression) = true
}

class IntegerPattern : FixedSizePattern {

    fun getBoundInt(m: Match): IntegerExpr {
        return m.getBoundExpr(this)!! as IntegerExpr
    }

    override fun children(): List<Pattern> = emptyList()

    override fun checkExpressionKind(expr: Expression) = expr is IntegerExpr
}

class VariablePattern : FixedSizePattern {

    override fun children() = emptyList<Pattern>()

    override fun checkExpressionKind(expr: Expression) = expr is VariableExpr
}

data class UnaryPattern(val operator: UnaryOperator, val ptn: Pattern) : FixedSizePattern {

    override fun children() = listOf(ptn)

    override fun checkExpressionKind(expr: Expression) = expr is UnaryExpr && expr.operator == operator
}

data class BinaryPattern(val operator: BinaryOperator, val left: Pattern, val right: Pattern) : FixedSizePattern {

    override fun children() = listOf(left, right)

    override fun checkExpressionKind(expr: Expression) = expr is BinaryExpr && expr.operator == operator
}

data class NaryPattern(val operator: NaryOperator, val operands: List<Pattern>) : FixedSizePattern {

    override fun children() = operands

    override fun checkExpressionKind(expr: Expression): Boolean {
        return expr is NaryExpr && expr.operator == operator && expr.children().count() == children().count()
    }
}

data class AssocNaryPattern(val operator: NaryOperator, val operands: List<Pattern>, val minSize: Int = 0) : Pattern {

    override fun findMatches(match: Match, subexpression: Subexpression): Sequence<Match> {
        val childCount = subexpression.expr.children().count()
        if (subexpression.expr !is NaryExpr || subexpression.expr.operator != operator
            || childCount < operands.count() || childCount < minSize
        ) {
            return emptySequence()
        }

        fun rec(match: Match, searchIndex: Int, initialChildIndex: Int): Sequence<Match> {
            return sequence {
                if (searchIndex < operands.size) {
                    val lastChildIndex = childCount - operands.count() + searchIndex
                    for (childIndex in initialChildIndex..lastChildIndex) {
                        val childMatches = operands[searchIndex].findMatches(match, subexpression.nthChild(childIndex))
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

    private fun getMatchIndexes(m: Match, path: Path): SortedSet<Int> {
        val matchIndexes = TreeSet<Int>()
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

    override fun substitute(m: Match, result: ExpressionMaker): ExpressionMaker {
        val sub = m.getLastBinding(this)!!
        val matchIndexes = getMatchIndexes(m, sub.path)
        val firstIndex = matchIndexes.first()

        val restChildren = ArrayList<ExpressionMaker>()
        for ((index, child) in sub.expr.children().withIndex()) {
            if (index == firstIndex) {
                restChildren.add(result)
            } else if (!matchIndexes.contains(index)) {
                restChildren.add(FixedExpressionMaker(child))
            }
        }

        return NaryExpressionMaker(operator, restChildren)
    }

    fun getRest(m: Match): Expression {
        val sub = m.getLastBinding(this)!!
        val matchIndexes = getMatchIndexes(m, sub.path)

        val restChildren = sub.expr.children().filterIndexed { i, _ -> !matchIndexes.contains(i) }
        return sub.expr.copyWithChildren(restChildren)
    }
}

fun fractionOf(numerator: Pattern, denominator: Pattern) =
    BinaryPattern(BinaryOperator.Fraction, numerator, denominator)

fun sumOf(vararg terms: Pattern) = NaryPattern(NaryOperator.Sum, terms.asList())

fun sumContaining(vararg terms: Pattern) = AssocNaryPattern(NaryOperator.Sum, terms.asList())

fun productOf(vararg factors: Pattern) = NaryPattern(NaryOperator.Product, factors.asList())

fun productContaining(vararg factors: Pattern, minSize: Int = 0) =
    AssocNaryPattern(NaryOperator.Product, factors.asList(), minSize)
