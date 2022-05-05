package patterns

import expressions.*

interface Pattern {
    fun findMatches(match: Match, subexpression: Subexpression): Sequence<Match>

    fun substitute(m: Match, result: Expression): Expression
}

interface FixedSizePattern : Pattern {

    fun children(): List<Pattern>

    fun checkExpressionKind(expr: Expression): Boolean

    override fun findMatches(match: Match, subexpression: Subexpression): Sequence<Match> {
        if (!checkExpressionKind(subexpression.expr)) {
            return emptySequence()
        }

        val previouslyMatched = match.getBinding(this)

        if (previouslyMatched != null) {
            return if (previouslyMatched.expr == subexpression.expr) {
                sequenceOf(match.childBindings(this, subexpression))
            } else {
                emptySequence()
            }
        }

        var matches = sequenceOf(match.childBindings(this, subexpression))
        for ((index, op) in children().withIndex()) {
            matches = matches.flatMap { op.findMatches(it, subexpression.nthChild(index)) }
        }
        return matches
    }

    override fun substitute(m: Match, result: Expression): Expression = result
}

class IntegerPattern : FixedSizePattern {

    fun getIntBinding(m: Match): IntegerExpr {
        return m.getBinding(this)!!.expr as IntegerExpr
    }

    override fun children() = emptyList<Pattern>()

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

data class AssocNaryPattern(val operator: NaryOperator, val operands: List<Pattern>) : Pattern {

    override fun findMatches(match: Match, subexpression: Subexpression): Sequence<Match> {
        if (subexpression.expr !is NaryExpr || subexpression.expr.operator != operator
            || subexpression.expr.children().count() < operands.count()
        ) {
            return emptySequence()
        }

        fun rec(match: Match, searchIndex: Int, initialChildIndex: Int): Sequence<Match> {
            return sequence {
                if (searchIndex < operands.size) {
                    val lastChildIndex = subexpression.expr.children().count() - operands.count() + searchIndex
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

        return rec(match, 0, 0)
    }

    override fun substitute(m: Match, result: Expression): Expression {
        TODO("Not yet implemented")
    }
}

fun fractionOf(numerator: Pattern, denominator: Pattern) =
    BinaryPattern(BinaryOperator.Fraction, numerator, denominator)

fun sumOf(vararg terms: Pattern) = NaryPattern(NaryOperator.Sum, terms.asList())