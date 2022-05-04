package patterns

import expressions.*

interface Pattern {
    fun findMatches(match: Match, subexpression: Subexpression): Sequence<Match>
    fun substitute(m: Match, result: Expression) : Expression = result

    fun getExpressionBinding(m: Match): Expression = m.getBinding(this)!!.expr
}

class IntegerPattern : Pattern {
    override fun findMatches(match: Match, subexpression: Subexpression): Sequence<Match> {
        if (subexpression.expr !is IntegerExpr) {
            return emptySequence()
        }

        val previouslyMatched = match.getBinding(this)
        if (previouslyMatched == null || previouslyMatched.expr == subexpression.expr) {
            return sequenceOf(match.childBindings(this, subexpression))
        }

        return emptySequence();
    }

    fun getIntBinding(m : Match): IntegerExpr {
        return m.getBinding(this)!!.expr as IntegerExpr
    }

    fun getPath(m : Match): Path {
        return m.getBinding(this)!!.path
    }
}

class VariablePattern : Pattern {
    override fun findMatches(match: Match, subexpression: Subexpression): Sequence<Match> {
        if (subexpression.expr !is VariableExpr) {
            return emptySequence()
        }

        val previouslyMatched = match.getBinding(this)
        if (previouslyMatched == null || previouslyMatched.expr == subexpression.expr) {
            return sequenceOf(match.childBindings(this, subexpression))
        }

        return emptySequence();
    }
}

data class UnaryPattern(val operator: UnaryOperator, val ptn: Pattern) : Pattern {
     override fun findMatches(match: Match, subexpression: Subexpression): Sequence<Match> {
        if (subexpression.expr !is UnaryExpr || subexpression.expr.operator != operator) {
            return emptySequence()
        }
        return ptn.findMatches(match.childBindings(this, subexpression), subexpression.nthChild(0))
    }
}

data class BinaryPattern(val operator: BinaryOperator, val left: Pattern, val right: Pattern) : Pattern {
    override fun findMatches(match: Match, subexpression: Subexpression): Sequence<Match> {
        if (subexpression.expr !is BinaryExpr || subexpression.expr.operator != operator) {
            return emptySequence()
        }

        return left
            .findMatches(match.childBindings(this, subexpression), subexpression.nthChild(0))
            .flatMap { right.findMatches(it, subexpression.nthChild(1)) }
    }
}

data class NaryPattern(val operator: NaryOperator, val operands: Sequence<Pattern>) : Pattern {
    override fun findMatches(match: Match, subexpression: Subexpression): Sequence<Match> {
        if (subexpression.expr !is NaryExpr || subexpression.expr.operator != operator) {
            return emptySequence()
        }

        var matches = sequenceOf(match.childBindings(this, subexpression))
        for ((index, op) in operands.withIndex()) {
            matches = matches.flatMap { op.findMatches(it, subexpression.nthChild(index)) }
        }

        return matches
    }
}



fun fractionOf(numerator: Pattern, denominator: Pattern)
        = BinaryPattern(BinaryOperator.Fraction, numerator, denominator)

fun sumOf(vararg terms: Pattern)
        = NaryPattern(NaryOperator.Sum, terms.asSequence())