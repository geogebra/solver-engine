package patterns

import expressions.*
import java.math.BigInteger

interface Pattern {
    fun findMatches(subexpression: Subexpression, match: Match = RootMatch): Sequence<Match>
}

interface FullSizePattern : Pattern {

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


data class FixedPattern(val expr: Expression) : FullSizePattern {
    override fun children(): List<Pattern> = emptyList()

    override fun checkExpressionKind(expr: Expression) = this.expr == expr
}

class AnyPattern : FullSizePattern {
    override fun children() = emptyList<Pattern>()

    override fun checkExpressionKind(expr: Expression) = true
}

interface IntegerPattern : Pattern {
    fun getBoundInt(m: Match): BigInteger


}

class UnsignedIntegerPattern : IntegerPattern, FullSizePattern {

    override fun getBoundInt(m: Match): BigInteger {
        return (m.getBoundExpr(this)!! as IntegerExpr).value
    }

    override fun children(): List<Pattern> = emptyList()

    override fun checkExpressionKind(expr: Expression) = expr is IntegerExpr
}

class SignedIntegerPattern : IntegerPattern {
    private val integer = UnsignedIntegerPattern()
    private val neg = negOf(integer)
    private val ptn = OneOfPattern(listOf(integer, neg, bracketOf(neg)))

    override fun getBoundInt(m: Match): BigInteger {
        val value = integer.getBoundInt(m)
        return when {
            m.getLastBinding(neg) == null -> value
            else -> -value
        }
    }

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        return ptn.findMatches(subexpression, match).map { it.newChild(this, it.getLastBinding(ptn)!!) }
    }
}

class VariablePattern : FullSizePattern {

    override fun children() = emptyList<Pattern>()

    override fun checkExpressionKind(expr: Expression) = expr is VariableExpr
}

data class UnaryPattern(val operator: UnaryOperator, val ptn: Pattern) : FullSizePattern {

    override fun children() = listOf(ptn)

    override fun checkExpressionKind(expr: Expression) = expr is UnaryExpr && expr.operator == operator
}

data class BinaryPattern(val operator: BinaryOperator, val left: Pattern, val right: Pattern) : FullSizePattern {

    override fun children() = listOf(left, right)

    override fun checkExpressionKind(expr: Expression) = expr is BinaryExpr && expr.operator == operator
}

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

data class NaryPattern(override val operator: NaryOperator, override val operands: List<Pattern>) : NaryPatternBase,
    FullSizePattern {

    override fun children() = operands

    override fun checkExpressionKind(expr: Expression): Boolean {
        return expr is NaryExpr && expr.operator == operator && expr.children().count() == children().count()
    }
}

data class PartialNaryPattern(
    override val operator: NaryOperator,
    override val operands: List<Pattern>,
    val minSize: Int = 0
) : NaryPatternBase {

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

    fun getRestSubexpressions(m: Match): List<Subexpression> {
        val sub = m.getLastBinding(this)!!
        val matchIndexes = getMatchIndexes(m, sub.path)

        return sub.children().filter { subexpression -> !matchIndexes.contains(subexpression.index()) }
    }
}

data class CommutativeNaryPattern(override val operator: NaryOperator, override val operands: List<Pattern>) :
    NaryPatternBase {

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        val childCount = subexpression.expr.children().count()
        if (subexpression.expr !is NaryExpr || subexpression.expr.operator != operator
            || childCount != operands.count()
        ) {
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

data class MixedNumberPattern(
    val integer: UnsignedIntegerPattern = UnsignedIntegerPattern(),
    val numerator: UnsignedIntegerPattern = UnsignedIntegerPattern(),
    val denominator: UnsignedIntegerPattern = UnsignedIntegerPattern(),
) : FullSizePattern {

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

fun negOf(operand: Pattern) = UnaryPattern(UnaryOperator.Minus, operand)

fun sumContaining(vararg terms: Pattern) = PartialNaryPattern(NaryOperator.Sum, terms.asList())

fun commutativeSumOf(vararg terms: Pattern) = CommutativeNaryPattern(NaryOperator.Sum, terms.asList())

fun productOf(vararg factors: Pattern) = NaryPattern(NaryOperator.Product, factors.asList())

fun productContaining(vararg factors: Pattern, minSize: Int = 0) =
    PartialNaryPattern(NaryOperator.Product, factors.asList(), minSize)

data class NumericCondition2(
    val ptn1: IntegerPattern,
    val ptn2: IntegerPattern,
    val condition: (BigInteger, BigInteger) -> Boolean
) : MatchCondition {
    override fun checkMatch(match: Match): Boolean {
        val n1 = ptn1.getBoundInt(match)
        val n2 = ptn2.getBoundInt(match)

        return condition(n1, n2)
    }
}

fun numericCondition(
    ptn1: UnsignedIntegerPattern,
    ptn2: UnsignedIntegerPattern,
    condition: (BigInteger, BigInteger) -> Boolean
) =
    NumericCondition2(ptn1, ptn2, condition)

interface MatchCondition {
    fun checkMatch(match: Match): Boolean
}

data class FindPattern(val pattern: Pattern, val deepFirst: Boolean = false) : Pattern {

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        val ownMatches = pattern.findMatches(subexpression, match)
        val childMatches = subexpression.children().asSequence()
            .flatMap { findMatches(it, match) }
            .map { it.newChild(this, it.getLastBinding(pattern)!!) }
        return when {
            deepFirst -> childMatches + ownMatches
            else -> ownMatches + childMatches
        }
    }
}

data class OneOfPattern(val options: List<Pattern>) : Pattern {

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        return sequence {
            for (option in options) {
                for (match in option.findMatches(subexpression, match)) {
                    yield(match.newChild(this@OneOfPattern, match.getLastBinding(option)!!))
                }
            }
        }
    }
}
