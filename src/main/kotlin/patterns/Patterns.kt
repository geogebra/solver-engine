package patterns

import expressions.*
import java.math.BigInteger
import java.util.*

interface Pattern {
    fun findMatches(subexpression: Subexpression, match: Match = RootMatch): Sequence<Match>
}

interface FixedSizePattern : Pattern {

    fun children(): List<Pattern>

    fun checkExpressionKind(expr: Expression): Boolean

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
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
            matches = matches.flatMap { op.findMatches(subexpression.nthChild(index), it) }
        }
        return matches
    }
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

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
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

    fun getMatchIndexes(m: Match, path: Path): SortedSet<Int> {
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

    fun getRestSubexpressions(m: Match): List<Subexpression> {
        val sub = m.getLastBinding(this)!!
        val matchIndexes = getMatchIndexes(m, sub.path)

        return sub.children().filter { subexpression -> !matchIndexes.contains(subexpression.index()) }
    }
}

data class MixedNumberPattern(
    val integer: IntegerPattern = IntegerPattern(),
    val numerator: IntegerPattern = IntegerPattern(),
    val denominator: IntegerPattern = IntegerPattern(),
) : FixedSizePattern {

    override fun children() = listOf(integer, numerator, denominator)

    override fun checkExpressionKind(expr: Expression) = expr is MixedNumber
}

data class ConditionPattern(val pattern: Pattern, val condition: MatchCondition) : Pattern {

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        return pattern.findMatches(subexpression, match).filter { condition.checkMatch(it) }
    }
}

fun fractionOf(numerator: Pattern, denominator: Pattern) =
    BinaryPattern(BinaryOperator.Fraction, numerator, denominator)

fun bracketOf(expr: Pattern) = UnaryPattern(UnaryOperator.Bracket, expr)

fun sumOf(vararg terms: Pattern) = NaryPattern(NaryOperator.Sum, terms.asList())

fun sumContaining(vararg terms: Pattern) = AssocNaryPattern(NaryOperator.Sum, terms.asList())

fun productOf(vararg factors: Pattern) = NaryPattern(NaryOperator.Product, factors.asList())

fun productContaining(vararg factors: Pattern, minSize: Int = 0) =
    AssocNaryPattern(NaryOperator.Product, factors.asList(), minSize)

data class NumericCondition2(
    val ptn1: IntegerPattern,
    val ptn2: IntegerPattern,
    val condition: (BigInteger, BigInteger) -> Boolean
) : MatchCondition {
    override fun checkMatch(match: Match): Boolean {
        val n1 = ptn1.getBoundInt(match).value
        val n2 = ptn2.getBoundInt(match).value

        return condition(n1, n2)
    }
}

fun numericCondition(ptn1: IntegerPattern, ptn2: IntegerPattern, condition: (BigInteger, BigInteger) -> Boolean) =
    NumericCondition2(ptn1, ptn2, condition)

interface MatchCondition {
    fun checkMatch(match: Match): Boolean
}

data class FindPattern(val pattern: Pattern) : Pattern {

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        return sequence {
            for (match in pattern.findMatches(subexpression, match)) {
                yield(match.newChild(this@FindPattern, match.getLastBinding(pattern)!!))
            }
            for (child in subexpression.children()) {
                for (match in findMatches(child, match)) {
                    yield(match.newChild(this@FindPattern, match.getLastBinding(pattern)!!))
                }
            }
        }
    }
}
