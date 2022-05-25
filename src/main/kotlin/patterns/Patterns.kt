package patterns

import expressions.*
import java.math.BigInteger

interface PathProvider {

    fun getBoundPaths(m: Match): List<Path>

    fun getBoundExpr(m: Match): Expression?

    fun checkPreviousMatch(expr: Expression, match: Match): Boolean {
        val previous = getBoundExpr(match)
        return previous == null || previous == expr
    }
}

interface Pattern : PathProvider {
    fun findMatches(subexpression: Subexpression, match: Match = RootMatch): Sequence<Match>

    override fun getBoundPaths(m: Match) = m.getBoundPaths(this)

    override fun getBoundExpr(m: Match) = m.getBoundExpr(this)
}

data class OperatorPattern(val operator: Operator, val childPatterns: List<Pattern>) : Pattern {
    init {
        require(childPatterns.size >= operator.minChildCount())
        require(childPatterns.size <= operator.maxChildCount())
    }

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (subexpression.expr.operator != operator ||
            subexpression.expr.operands.size != childPatterns.size ||
            !checkPreviousMatch(subexpression.expr, match)
        ) {
            return emptySequence()
        }

        var matches = sequenceOf(match.newChild(this, subexpression))
        for ((index, op) in childPatterns.withIndex()) {
            matches = matches.flatMap { op.findMatches(subexpression.nthChild(index), it) }
        }
        return matches
    }
}

data class FixedPattern(val expr: Expression) : Pattern {

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        return when (subexpression.expr) {
            expr -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

class AnyPattern : Pattern {

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        return sequenceOf(match.newChild(this, subexpression))
    }
}

interface IntegerProvider : PathProvider {

    fun getBoundInt(m: Match): BigInteger
}

class UnsignedIntegerPattern : Pattern, IntegerProvider {

    override fun getBoundInt(m: Match): BigInteger {
        return (m.getBoundExpr(this)!!.operator as IntegerOperator).value
    }

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        return when (subexpression.expr.operator) {
            is IntegerOperator -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

class SignedIntegerPattern : Pattern, IntegerProvider {
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
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        return ptn.findMatches(subexpression, match).map { it.newChild(this, it.getLastBinding(ptn)!!) }
    }
}

class VariablePattern : Pattern {
    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }

        return when (subexpression.expr.operator) {
            is VariableOperator -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
}

class MixedNumberPattern : Pattern {

    private fun getBoundMixedNumber(match: Match) = match.getBoundExpr(this)!!.operator as MixedNumberOperator

    inner class PartIntegerProvider(val getInt: (MixedNumberOperator) -> BigInteger) : IntegerProvider {
        override fun getBoundInt(m: Match): BigInteger {
            return getInt(getBoundMixedNumber(m))
        }

        override fun getBoundPaths(m: Match): List<Path> {
            return this@MixedNumberPattern.getBoundPaths(m)
        }

        override fun getBoundExpr(m: Match): Expression? = xp(getBoundInt(m))
    }

    val integer = PartIntegerProvider { it.integer }
    val numerator = PartIntegerProvider { it.numerator }
    val denominator = PartIntegerProvider { it.denominator }

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        return when (subexpression.expr.operator) {
            is MixedNumberOperator -> sequenceOf(match.newChild(this, subexpression))
            else -> emptySequence()
        }
    }
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

data class ConditionPattern(val pattern: Pattern, val condition: MatchCondition) : Pattern {

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        return pattern.findMatches(subexpression, match).filter { condition.checkMatch(it) }
    }
}

fun fractionOf(numerator: Pattern, denominator: Pattern) =
    OperatorPattern(BinaryOperator.Fraction, listOf(numerator, denominator))

fun bracketOf(expr: Pattern) = OperatorPattern(UnaryOperator.Bracket, listOf(expr))

fun sumOf(vararg terms: Pattern) = OperatorPattern(NaryOperator.Sum, terms.asList())

fun negOf(operand: Pattern) = OperatorPattern(UnaryOperator.Minus, listOf(operand))

fun sumContaining(vararg terms: Pattern) = PartialNaryPattern(NaryOperator.Sum, terms.asList())

fun commutativeSumOf(vararg terms: Pattern) = CommutativeNaryPattern(NaryOperator.Sum, terms.asList())

fun productOf(vararg factors: Pattern) = OperatorPattern(NaryOperator.Product, factors.asList())

fun productContaining(vararg factors: Pattern, minSize: Int = 0) =
    PartialNaryPattern(NaryOperator.Product, factors.asList(), minSize)

data class NumericCondition2(
    val ptn1: IntegerProvider,
    val ptn2: IntegerProvider,
    val condition: (BigInteger, BigInteger) -> Boolean
) : MatchCondition {
    override fun checkMatch(match: Match): Boolean {
        return condition(ptn1.getBoundInt(match), ptn2.getBoundInt(match))
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
