package engine.patterns

import engine.expressions.BinaryOperator
import engine.expressions.BracketOperator
import engine.expressions.ChildPath
import engine.expressions.Expression
import engine.expressions.IntegerOperator
import engine.expressions.MixedNumberOperator
import engine.expressions.NaryOperator
import engine.expressions.Operator
import engine.expressions.Path
import engine.expressions.Subexpression
import engine.expressions.UnaryOperator
import engine.expressions.VariableOperator
import java.math.BigInteger

interface PathProvider {

    fun getBoundPaths(m: Match): List<Path>

    fun getBoundExpr(m: Match): Expression?

    fun checkPreviousMatch(expr: Expression, match: Match): Boolean {
        val previous = getBoundExpr(match)
        return previous == null || previous.equiv(expr)
    }
}

interface Pattern : PathProvider {
    fun findMatches(subexpression: Subexpression, match: Match = RootMatch): Sequence<Match>

    override fun getBoundPaths(m: Match) = m.getBoundPaths(this)

    override fun getBoundExpr(m: Match) = m.getBoundExpr(this)

    val key get() = this
}

data class OperatorPattern(val operator: Operator, val childPatterns: List<Pattern>) : Pattern {
    init {
        require(childPatterns.size >= operator.minChildCount())
        require(childPatterns.size <= operator.maxChildCount())
    }

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!subexpression.expr.operator.equiv(operator) ||
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
        return when {
            subexpression.expr.equiv(expr) -> sequenceOf(match.newChild(this, subexpression))
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

sealed class OptionalNegPatternBase<T : Pattern>(val pattern: T) : Pattern {

    private val neg = negOf(pattern)
    private val ptn = OneOfPattern(listOf(pattern, neg, bracketOf(neg)))

    override val key = ptn

    fun isNeg(m: Match) = m.getLastBinding(neg) != null

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        return ptn.findMatches(subexpression, match)
    }
}

class OptionalNegPattern(pattern: Pattern) : OptionalNegPatternBase<Pattern>(pattern)

class SignedIntegerPattern : OptionalNegPatternBase<UnsignedIntegerPattern>(UnsignedIntegerPattern()), IntegerProvider {
    override fun getBoundInt(m: Match): BigInteger {
        val value = pattern.getBoundInt(m)
        return if (isNeg(m)) -value else value
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

    override val key = pattern.key

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        return pattern.findMatches(subexpression, match).filter { condition.checkMatch(it) }
    }
}

fun fractionOf(numerator: Pattern, denominator: Pattern) =
    OperatorPattern(BinaryOperator.Fraction, listOf(numerator, denominator))

fun divideBy(divisor: Pattern) =
    OperatorPattern(UnaryOperator.DivideBy, listOf(divisor))

fun powerOf(base: Pattern, exponent: Pattern) =
    OperatorPattern(BinaryOperator.Power, listOf(base, exponent))

fun squareRootOf(radicand: Pattern) =
    OperatorPattern(UnaryOperator.SquareRoot, listOf(radicand))

fun rootOf(radicand: Pattern, degree: Pattern) =
    OperatorPattern(BinaryOperator.Root, listOf(radicand, degree))

fun bracketOf(expr: Pattern) = OperatorPattern(BracketOperator.Bracket, listOf(expr))

fun mixedNumberOf(
    integer: UnsignedIntegerPattern = UnsignedIntegerPattern(),
    numerator: UnsignedIntegerPattern = UnsignedIntegerPattern(),
    denominator: UnsignedIntegerPattern = UnsignedIntegerPattern(),
) = OperatorPattern(MixedNumberOperator, listOf(integer, numerator, denominator))

fun sumOf(vararg terms: Pattern) = OperatorPattern(NaryOperator.Sum, terms.asList())

fun negOf(operand: Pattern) = OperatorPattern(UnaryOperator.Minus, listOf(operand))

fun optionalNegOf(operand: Pattern) = OptionalNegPattern(operand)

fun sumContaining(vararg terms: Pattern) = PartialNaryPattern(NaryOperator.Sum, terms.asList())

fun commutativeSumOf(vararg terms: Pattern) = CommutativeNaryPattern(NaryOperator.Sum, terms.asList())

fun productOf(vararg factors: Pattern) = OperatorPattern(NaryOperator.Product, factors.asList())

fun productContaining(vararg factors: Pattern, minSize: Int = 0) =
    PartialNaryPattern(NaryOperator.Product, factors.asList(), minSize)

fun commutativeProductOf(vararg factors: Pattern) = CommutativeNaryPattern(NaryOperator.Product, factors.asList())

data class NumericCondition1(
    val ptn: IntegerProvider,
    val condition: (BigInteger) -> Boolean
) : MatchCondition {
    override fun checkMatch(match: Match): Boolean {
        return condition(ptn.getBoundInt(match))
    }
}

data class NumericCondition2(
    val ptn1: IntegerProvider,
    val ptn2: IntegerProvider,
    val condition: (BigInteger, BigInteger) -> Boolean
) : MatchCondition {
    override fun checkMatch(match: Match): Boolean {
        return condition(ptn1.getBoundInt(match), ptn2.getBoundInt(match))
    }
}

fun numericCondition(ptn: IntegerProvider, condition: (BigInteger) -> Boolean) = NumericCondition1(ptn, condition)

fun numericCondition(
    ptn1: IntegerProvider,
    ptn2: IntegerProvider,
    condition: (BigInteger, BigInteger) -> Boolean
) =
    NumericCondition2(ptn1, ptn2, condition)

interface MatchCondition {
    fun checkMatch(match: Match): Boolean
}

data class FindPattern(val pattern: Pattern, val deepFirst: Boolean = false) : Pattern {

    override val key = pattern

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        val ownMatches = pattern.findMatches(subexpression, match)
        val childMatches = subexpression.children().asSequence().flatMap { findMatches(it, match) }
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
                for (m in option.findMatches(subexpression, match)) {
                    yield(m.newChild(this@OneOfPattern, m.getLastBinding(option)!!))
                }
            }
        }
    }
}

fun oneOf(vararg options: Pattern) = OneOfPattern(options.asList())

data class AllOfPattern(val patterns: List<Pattern>) : Pattern {
    init {
        require(patterns.isNotEmpty())
    }

    override val key = patterns[0]

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        fun rec(i: Int, m: Match): Sequence<Match> {
            if (i == patterns.size - 1) {
                return patterns[i].findMatches(subexpression, m)
            }
            return patterns[i].findMatches(subexpression, m).flatMap { rec(i + 1, it) }
        }

        return rec(0, match)
    }
}

fun allOf(vararg patterns: Pattern?): Pattern {
    val nonNullPatterns = patterns.filterNotNull()
    return when (nonNullPatterns.size) {
        0 -> throw java.lang.IllegalArgumentException("At least one non-null pattern should be specified in allOf")
        1 -> nonNullPatterns[0]
        else -> AllOfPattern(nonNullPatterns)
    }
}

data class OptionalDivideBy(val pattern: Pattern) : Pattern {

    private val divide = divideBy(pattern)
    private val ptn = oneOf(pattern, divide)

    override val key = ptn

    fun isDivide(m: Match) = m.getLastBinding(divide) != null

    override fun findMatches(subexpression: Subexpression, match: Match): Sequence<Match> {
        if (!checkPreviousMatch(subexpression.expr, match)) {
            return emptySequence()
        }
        return ptn.findMatches(subexpression, match)
    }
}
