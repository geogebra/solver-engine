package transformations

import expressionmakers.cancel
import expressionmakers.makeNumericOp
import expressionmakers.restOf
import expressionmakers.substituteIn
import expressions.IntegerExpr
import patterns.FixedPattern
import patterns.IntegerPattern
import patterns.sumContaining

object EvaluateIntegerSum : Rule {
    private val term1 = IntegerPattern()
    private val term2 = IntegerPattern()

    override val pattern = sumContaining(term1, term2)

    override val resultMaker = substituteIn(pattern, makeNumericOp(term1, term2) { n1, n2 -> n1 + n2 })
}

object EliminateZeroInSum : Rule {
    private val zero = FixedPattern(IntegerExpr(0))

    override val pattern = sumContaining(zero)

    override val resultMaker = cancel(zero, restOf(pattern))
}
