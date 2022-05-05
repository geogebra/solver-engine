package patterns

import expressions.*

interface Pattern {

    fun children(): List<Pattern>

    fun checkExpressionKind(expr: Expression): Boolean

    fun findMatches(match: Match, subexpression: Subexpression): Sequence<Match> {
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

    fun substitute(m: Match, result: Expression): Expression = result
}

class IntegerPattern : Pattern {

    fun getIntBinding(m: Match): IntegerExpr {
        return m.getBinding(this)!!.expr as IntegerExpr
    }

    override fun children() = emptyList<Pattern>()

    override fun checkExpressionKind(expr: Expression) = expr is IntegerExpr
}

class VariablePattern : Pattern {

    override fun children() = emptyList<Pattern>()

    override fun checkExpressionKind(expr: Expression) = expr is VariableExpr
}

data class UnaryPattern(val operator: UnaryOperator, val ptn: Pattern) : Pattern {

    override fun children() = listOf(ptn)

    override fun checkExpressionKind(expr: Expression) = expr is UnaryExpr && expr.operator == operator
}

data class BinaryPattern(val operator: BinaryOperator, val left: Pattern, val right: Pattern) : Pattern {

    override fun children() = listOf(left, right)

    override fun checkExpressionKind(expr: Expression) = expr is BinaryExpr && expr.operator == operator
}

data class NaryPattern(val operator: NaryOperator, val operands: List<Pattern>) : Pattern {

    override fun children() = operands

    override fun checkExpressionKind(expr: Expression): Boolean {
        return expr is NaryExpr && expr.operator == operator && expr.children().count() == children().count()
    }
}


fun fractionOf(numerator: Pattern, denominator: Pattern) =
    BinaryPattern(BinaryOperator.Fraction, numerator, denominator)

fun sumOf(vararg terms: Pattern) = NaryPattern(NaryOperator.Sum, terms.asList())