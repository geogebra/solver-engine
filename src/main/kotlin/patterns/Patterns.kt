package patterns

import expressions.*

interface Pattern {
    fun findMatches(m: Match, p: Path?, e: Expression) : Sequence<Match>
    fun substitute(m: Match, result: Expression) : Expression
}


class IntegerPattern : Pattern

data class UnaryPattern(val operator: UnaryOperator, val ptn: Pattern) : Pattern {
     override fun findMatches(m: Match, p: Path?, e: Expression): Sequence<Match> {
        if (e !is UnaryExpr || e.operator != operator) {
            return emptySequence()
        }
        return ptn.findMatches(m, Path(p, 0), e.expr)
    }

    override fun substitute(m: Match, result: Expression): Expression {
        TODO("Not yet implemented")
    }
}

data class BinaryPattern(val operator: BinaryOperator, val left: Pattern, val right: Pattern) : Pattern {
    override fun findMatches(m: Match, p: Path?, e: Expression): Sequence<Match> {
        if (e !is BinaryExpr || e.operator != operator) {
            return emptySequence()
        }

        return left
            .findMatches(m, Path(p, 0), e.left)
            .flatMap { right.findMatches(it, Path(p, 1), e.right) }
    }

    override fun substitute(m: Match, result: Expression): Expression {
        TODO("Not yet implemented")
    }
}

data class NaryPattern(val operator: NaryOperator, val operands: Sequence<Pattern>) : Pattern {
    override fun findMatches(m: Match, p: Path?, e: Expression): Sequence<Match> {
        if (e !is NaryExpr || e.operator != operator) {
            return emptySequence()
        }

        var matches = sequenceOf(m)
        for ((index, op) in operands.withIndex()) {
            matches = matches.flatMap { op.findMatches(it, Path(p, index), e.operands.elementAt(index)) }
        }

        return matches
    }

    override fun substitute(m: Match, result: Expression): Expression {
        TODO("Not yet implemented")
    }
}

fun acSumPatternOf(vararg terms: Pattern): ACSumPattern {
    return ACSumPattern(terms.asList())
}

data class Match(val expression: Expression, val bindings: Map<Pattern, Path>)
